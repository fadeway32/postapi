package com.fadeway32.postadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fadeway32.postadmin.dto.ApiExecutionResult;
import com.fadeway32.postadmin.dto.BatchExecuteRequest;
import com.fadeway32.postadmin.dto.BatchExecutionResult;
import com.fadeway32.postadmin.entity.ApiCallLog;
import com.fadeway32.postadmin.entity.ApiDefinition;
import com.fadeway32.postadmin.event.ApiExecutionCompletedEvent;
import com.fadeway32.postadmin.mapper.ApiCallLogMapper;
import com.fadeway32.postadmin.util.Jsons;
import com.fadeway32.postadmin.util.TemplateRenderer;
import com.fadeway32.postapi.client.PostApiClient;
import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.model.HttpMethod;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApiExecutionService {
    private final ApiDefinitionService apiDefinitionService;
    private final SensitiveCryptoService sensitiveCryptoService;
    private final GroovyApiScriptEngine scriptEngine;
    private final PostApiClient postApiClient;
    private final Jsons jsons;
    private final TemplateRenderer templateRenderer;
    private final ApiCallLogMapper apiCallLogMapper;
    private final ApplicationEventPublisher eventPublisher;

    public ApiExecutionService(ApiDefinitionService apiDefinitionService,
                               SensitiveCryptoService sensitiveCryptoService,
                               GroovyApiScriptEngine scriptEngine,
                               PostApiClient postApiClient,
                               Jsons jsons,
                               TemplateRenderer templateRenderer,
                               ApiCallLogMapper apiCallLogMapper,
                               ApplicationEventPublisher eventPublisher) {
        this.apiDefinitionService = apiDefinitionService;
        this.sensitiveCryptoService = sensitiveCryptoService;
        this.scriptEngine = scriptEngine;
        this.postApiClient = postApiClient;
        this.jsons = jsons;
        this.templateRenderer = templateRenderer;
        this.apiCallLogMapper = apiCallLogMapper;
        this.eventPublisher = eventPublisher;
    }

    public ApiExecutionResult simulate(Long tenantId, String apiCode, Map<String, Object> payload) {
        return executeOne(tenantId, apiCode, payload, null, false, true);
    }

    public ApiExecutionResult execute(Long tenantId, String apiCode, Map<String, Object> payload) {
        return executeOne(tenantId, apiCode, payload, null, true, false);
    }

    public BatchExecutionResult batch(Long tenantId, BatchExecuteRequest request) {
        String batchId = UUID.randomUUID().toString().replace("-", "");
        BatchExecutionResult batch = new BatchExecutionResult();
        batch.setBatchId(batchId);
        List<ApiExecutionResult> results = new ArrayList<ApiExecutionResult>();
        int success = 0;
        int failure = 0;
        for (BatchExecuteRequest.Item item : request.getItems()) {
            ApiExecutionResult result = executeOne(tenantId, item.getApiCode(), item.getPayload(), batchId, true, false);
            results.add(result);
            if (result.isSuccess()) {
                success++;
            } else {
                failure++;
                if (Boolean.TRUE.equals(request.getStopOnFailure())) {
                    break;
                }
            }
        }
        batch.setResults(results);
        batch.setTotal(results.size());
        batch.setSuccess(success);
        batch.setFailure(failure);
        return batch;
    }

    public Page<ApiCallLog> logs(Long tenantId, int page, int size, String apiCode) {
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<ApiCallLog>()
                .eq(ApiCallLog::getTenantId, tenantId)
                .orderByDesc(ApiCallLog::getCalledAt);
        if (StringUtils.hasText(apiCode)) {
            wrapper.eq(ApiCallLog::getApiCode, apiCode);
        }
        return apiCallLogMapper.selectPage(new Page<ApiCallLog>(page, size), wrapper);
    }

    private ApiExecutionResult executeOne(Long tenantId,
                                          String apiCode,
                                          Map<String, Object> payload,
                                          String batchId,
                                          boolean increaseStats,
                                          boolean simulate) {
        ApiDefinition api = apiDefinitionService.findByCode(tenantId, apiCode);
        if (api == null || !Boolean.TRUE.equals(api.getEnabled())) {
            throw new IllegalArgumentException("api definition not found or disabled");
        }
        long start = System.currentTimeMillis();
        ApiExecutionResult result = new ApiExecutionResult();
        result.setApiCode(apiCode);
        Map<String, Object> safePayload = payload == null ? new LinkedHashMap<String, Object>() : new LinkedHashMap<String, Object>(payload);
        Map<String, Object> requestDetail = new LinkedHashMap<String, Object>();
        Integer statusCode = null;
        String responseBody = null;
        String traceId = null;
        String errorMessage = null;
        boolean success = false;
        try {
            Map<String, Object> secret = secret(api);
            if (hasScript(api)) {
                ScriptExecution execution = executeScript(api, safePayload, secret);
                requestDetail = execution.requestDetail;
                statusCode = execution.statusCode;
                responseBody = execution.responseBody;
                traceId = execution.traceId;
                success = execution.success;
            } else {
                PreparedRequest prepared = prepare(api, safePayload, secret);
                requestDetail = prepared.detail;
                ApiResponse response = postApiClient.execute(prepared.request);
                statusCode = response.statusCode();
                responseBody = response.body();
                traceId = response.traceId();
                success = response.is2xxSuccessful();
            }
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
        }
        long elapsed = System.currentTimeMillis() - start;
        result.setSuccess(success);
        result.setElapsedMillis(elapsed);
        result.setRequestDetail(requestDetail);
        result.setStatusCode(statusCode);
        result.setResponseBody(responseBody);
        result.setTraceId(traceId);
        result.setErrorMessage(errorMessage);
        publishCompleted(api, batchId, safePayload, requestDetail, statusCode, responseBody, traceId,
                errorMessage, elapsed, success, simulate, increaseStats);
        return result;
    }

    private ScriptExecution executeScript(ApiDefinition api, Map<String, Object> payload, Map<String, Object> secret) {
        Map<String, Object> headers = jsons.readMap(api.getHeadersJson());
        Map<String, Object> query = jsons.readMap(api.getQueryJson());
        Object scriptReturn = scriptEngine.execute(api, payload, secret, headers, query);
        ScriptExecution execution = normalizeScriptReturn(scriptReturn);
        execution.requestDetail.put("mode", "groovy");
        execution.requestDetail.put("apiCode", api.getApiCode());
        execution.requestDetail.put("method", api.getMethod());
        execution.requestDetail.put("url", api.getUrl());
        execution.requestDetail.put("headers", headers);
        execution.requestDetail.put("query", query);
        if (!execution.requestDetail.containsKey("payload")) {
            execution.requestDetail.put("payload", payload);
        }
        return execution;
    }

    private PreparedRequest prepare(ApiDefinition api, Map<String, Object> payload, Map<String, Object> secret) {
        String methodText = text(api.getMethod()).toUpperCase();
        String url = templateRenderer.render(text(api.getUrl()), payload, secret);
        String bodyType = text(api.getBodyType()).toUpperCase();
        Object body = api.getBodyTemplate();
        Long timeoutMillis = api.getTimeoutMillis();

        Map<String, Object> headers = jsons.readMap(api.getHeadersJson());
        Map<String, Object> query = jsons.readMap(api.getQueryJson());

        ApiRequest.Builder builder = ApiRequest.builder(HttpMethod.valueOf(methodText), url);
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            addMultiValue(entry.getValue(), value -> builder.header(entry.getKey(), templateRenderer.render(String.valueOf(value), payload, secret)));
        }
        for (Map.Entry<String, Object> entry : query.entrySet()) {
            addMultiValue(entry.getValue(), value -> builder.queryParam(entry.getKey(), templateRenderer.render(String.valueOf(value), payload, secret)));
        }
        if (timeoutMillis != null && timeoutMillis > 0) {
            builder.responseTimeout(Duration.ofMillis(timeoutMillis));
        }

        Object renderedBody = renderBody(body, payload, secret);
        applyBody(builder, bodyType, renderedBody, payload);

        Map<String, Object> detail = new LinkedHashMap<String, Object>();
        detail.put("method", methodText);
        detail.put("url", url);
        detail.put("headers", headers);
        detail.put("query", query);
        detail.put("bodyType", bodyType);
        detail.put("body", renderedBody);
        detail.put("timeoutMillis", timeoutMillis);

        PreparedRequest prepared = new PreparedRequest();
        prepared.request = builder.build();
        prepared.detail = detail;
        return prepared;
    }

    @SuppressWarnings("unchecked")
    private ScriptExecution normalizeScriptReturn(Object scriptReturn) {
        ScriptExecution execution = new ScriptExecution();
        execution.requestDetail = new LinkedHashMap<String, Object>();
        if (scriptReturn instanceof ApiExecutionResult) {
            ApiExecutionResult result = (ApiExecutionResult) scriptReturn;
            execution.statusCode = result.getStatusCode();
            execution.responseBody = result.getResponseBody();
            execution.traceId = result.getTraceId();
            execution.success = result.isSuccess();
            execution.requestDetail.putAll(result.getRequestDetail() == null
                    ? Collections.<String, Object>emptyMap()
                    : result.getRequestDetail());
            return execution;
        }
        if (scriptReturn instanceof ApiResponse) {
            applyResponse(execution, (ApiResponse) scriptReturn);
            return execution;
        }
        if (scriptReturn instanceof Map) {
            Map<String, Object> value = new LinkedHashMap<String, Object>((Map<String, Object>) scriptReturn);
            Object response = value.get("response");
            if (response instanceof ApiResponse) {
                applyResponse(execution, (ApiResponse) response);
            } else {
                execution.statusCode = intValue(firstNonNull(value.get("statusCode"), value.get("status")));
                execution.responseBody = value.containsKey("responseBody")
                        ? text(value.get("responseBody"))
                        : jsons.write(scriptReturn);
                execution.traceId = textOrNull(value.get("traceId"));
                execution.success = boolValue(value.get("success"), execution.statusCode == null
                        || (execution.statusCode >= 200 && execution.statusCode < 300));
            }
            Object detail = firstNonNull(value.get("requestDetail"), value.get("request"));
            if (detail instanceof Map) {
                execution.requestDetail.putAll(mapValue(detail));
            }
            execution.requestDetail.put("scriptReturn", value);
            return execution;
        }
        execution.statusCode = 200;
        execution.responseBody = scriptReturn == null ? null : String.valueOf(scriptReturn);
        execution.success = true;
        execution.requestDetail.put("scriptReturn", scriptReturn);
        return execution;
    }

    private void applyResponse(ScriptExecution execution, ApiResponse response) {
        execution.statusCode = response.statusCode();
        execution.responseBody = response.body();
        execution.traceId = response.traceId();
        execution.success = response.is2xxSuccessful();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            return new LinkedHashMap<String, Object>((Map<String, Object>) value);
        }
        return new LinkedHashMap<String, Object>();
    }

    private void applyBody(ApiRequest.Builder builder, String bodyType, Object renderedBody, Map<String, Object> payload) {
        if ("NONE".equals(bodyType)) {
            return;
        }
        if ("JSON".equals(bodyType)) {
            builder.json(renderedBody == null ? "" : renderedBody);
            return;
        }
        if ("XML".equals(bodyType)) {
            builder.xml(renderedBody == null ? "" : String.valueOf(renderedBody));
            return;
        }
        if ("FORM".equals(bodyType) || "FORM_URLENCODED".equals(bodyType)) {
            Map<String, Object> fields = renderedBody instanceof Map ? mapValue(renderedBody) : jsons.readMap(String.valueOf(renderedBody));
            if (fields.isEmpty()) {
                fields.putAll(payload);
            }
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                builder.form(entry.getKey(), entry.getValue());
            }
            return;
        }
        if ("MULTIPART".equals(bodyType)) {
            builder.multipart();
            Map<String, Object> fields = renderedBody instanceof Map ? mapValue(renderedBody) : jsons.readMap(String.valueOf(renderedBody));
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                builder.part(entry.getKey(), entry.getValue());
            }
            return;
        }
        throw new IllegalArgumentException("unsupported bodyType: " + bodyType);
    }

    private Object renderBody(Object body, Map<String, Object> payload, Map<String, Object> secret) {
        if (body == null) {
            return null;
        }
        if (body instanceof String) {
            return templateRenderer.render((String) body, payload, secret);
        }
        if (body instanceof Map) {
            Map<String, Object> rendered = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, Object> entry : mapValue(body).entrySet()) {
                rendered.put(entry.getKey(), renderBody(entry.getValue(), payload, secret));
            }
            return rendered;
        }
        return body;
    }

    private interface ValueConsumer {
        void accept(Object value);
    }

    private void addMultiValue(Object value, ValueConsumer consumer) {
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                consumer.accept(item);
            }
        } else {
            consumer.accept(value);
        }
    }

    private Object firstNonNull(Object first, Object second) {
        return first == null ? second : first;
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Integer intValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private boolean boolValue(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.valueOf(String.valueOf(value));
    }

    private String textOrNull(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean hasScript(ApiDefinition api) {
        return StringUtils.hasText(api.getScriptText());
    }

    private Map<String, Object> secret(ApiDefinition api) {
        return jsons.readMap(sensitiveCryptoService.decrypt(api.getEncryptedSecretJson()));
    }

    private void publishCompleted(ApiDefinition api,
                                  String batchId,
                                  Map<String, Object> payload,
                                  Map<String, Object> requestDetail,
                                  Integer statusCode,
                                  String responseBody,
                                  String traceId,
                                  String errorMessage,
                                  long elapsed,
                                  boolean success,
                                  boolean simulate,
                                  boolean increaseStats) {
        eventPublisher.publishEvent(new ApiExecutionCompletedEvent(api, batchId, payload, requestDetail,
                statusCode, responseBody, traceId, errorMessage, elapsed, success, simulate, increaseStats));
    }

    private static class PreparedRequest {
        private ApiRequest request;
        private Map<String, Object> detail;
    }

    private static class ScriptExecution {
        private Integer statusCode;
        private String responseBody;
        private String traceId;
        private boolean success;
        private Map<String, Object> requestDetail;
    }
}
