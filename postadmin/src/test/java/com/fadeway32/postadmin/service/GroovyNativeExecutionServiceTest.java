package com.fadeway32.postadmin.service;

import com.fadeway32.postadmin.dto.GroovyExecutionResult;
import com.fadeway32.postadmin.dto.GroovySimulateRequest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroovyNativeExecutionServiceTest {

    private final GroovyNativeExecutionService service = new GroovyNativeExecutionService();

    @Test
    void simulateReturnsScriptValue() {
        GroovySimulateRequest request = new GroovySimulateRequest();
        Map<String, Object> bindings = new LinkedHashMap<String, Object>();
        bindings.put("name", "postadmin");
        request.setBindings(bindings);
        request.setInstallSecurityManager(false);
        request.setScript("return [message: \"hello ${bindings.name}\"]");

        GroovyExecutionResult result = service.simulate(request);

        assertTrue(result.isSuccess(), result.getErrorMessage());
        assertEquals("hello postadmin", String.valueOf(((Map<?, ?>) result.getReturnValue()).get("message")));
    }

    @Test
    void simulateRejectsBlockedClass() {
        GroovySimulateRequest request = new GroovySimulateRequest();
        request.setInstallSecurityManager(false);
        request.setScript("return java.lang.System.currentTimeMillis()");

        GroovyExecutionResult result = service.simulate(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorType().contains("SECURITY") || result.getErrorType().contains("COMPILE"));
    }

    @Test
    void simulateTimesOutLongRunningScript() {
        GroovySimulateRequest request = new GroovySimulateRequest();
        request.setInstallSecurityManager(false);
        request.setTimeoutMillis(200);
        request.setScript("while (true) { }\nreturn 1");

        GroovyExecutionResult result = service.simulate(request);

        assertFalse(result.isSuccess());
        assertTrue(result.isTimeout() || "INTERRUPTED".equals(result.getErrorType()));
    }
}
