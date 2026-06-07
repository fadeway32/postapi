package com.fadeway32.postadmin.service;

import com.fadeway32.postadmin.entity.ApiCallLog;
import com.fadeway32.postadmin.entity.ApiDefinition;
import com.fadeway32.postadmin.event.ApiExecutionCompletedEvent;
import com.fadeway32.postadmin.mapper.ApiCallLogMapper;
import com.fadeway32.postadmin.util.Jsons;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ApiExecutionEventListener {

    private final ApiDefinitionService apiDefinitionService;
    private final ApiCallLogMapper apiCallLogMapper;
    private final Jsons jsons;

    public ApiExecutionEventListener(ApiDefinitionService apiDefinitionService,
                                     ApiCallLogMapper apiCallLogMapper,
                                     Jsons jsons) {
        this.apiDefinitionService = apiDefinitionService;
        this.apiCallLogMapper = apiCallLogMapper;
        this.jsons = jsons;
    }

    @Async
    @EventListener
    public void onApiExecutionCompleted(ApiExecutionCompletedEvent event) {
        if (event.isIncreaseStats()) {
            apiDefinitionService.increaseStats(event.getApi().getId(), event.isSuccess());
        }
        saveLog(event);
    }

    private void saveLog(ApiExecutionCompletedEvent event) {
        ApiDefinition api = event.getApi();
        ApiCallLog log = new ApiCallLog();
        log.setTenantId(api.getTenantId());
        log.setApiId(api.getId());
        log.setApiCode(api.getApiCode());
        log.setApiName(api.getApiName());
        log.setBatchId(event.getBatchId());
        log.setRequestPayload(jsons.write(event.getPayload()));
        log.setResponseStatus(event.getStatusCode());
        log.setResponseBody(event.getResponseBody());
        log.setSuccess(event.isSuccess());
        log.setErrorMessage(event.getErrorMessage());
        log.setElapsedMillis(event.getElapsedMillis());
        log.setTraceId(event.getTraceId());
        Map<String, Object> detail = new LinkedHashMap<String, Object>();
        detail.put("simulate", event.isSimulate());
        detail.put("request", event.getRequestDetail());
        log.setDetailJson(jsons.write(detail));
        log.setCalledAt(LocalDateTime.now());
        apiCallLogMapper.insert(log);
    }
}
