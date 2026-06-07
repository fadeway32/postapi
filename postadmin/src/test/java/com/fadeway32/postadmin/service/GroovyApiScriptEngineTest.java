package com.fadeway32.postadmin.service;

import com.fadeway32.postadmin.entity.ApiDefinition;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroovyApiScriptEngineTest {
    private final GroovyApiScriptEngine engine = new GroovyApiScriptEngine();

    @Test
    void executeProvidesBindingsMapForRuntimeScripts() {
        ApiDefinition api = new ApiDefinition();
        api.setApiCode("9529");
        api.setMethod("POST");
        api.setUrl("https://example.test/runtime");
        api.setBodyType("JSON");
        api.setScriptText("return [success: true, responseBody: bindings.payload.name, request: [url: bindings.url]]");

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("name", "postadmin");

        Object result = engine.execute(api, payload, new LinkedHashMap<String, Object>(),
                new LinkedHashMap<String, Object>(), new LinkedHashMap<String, Object>());

        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals(Boolean.TRUE, resultMap.get("success"));
        assertEquals("postadmin", resultMap.get("responseBody"));
        assertEquals("https://example.test/runtime", ((Map<?, ?>) resultMap.get("request")).get("url"));
    }
}
