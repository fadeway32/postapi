package com.fadeway32.postadmin.service;

import com.fadeway32.postadmin.entity.ApiDefinition;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GroovyApiScriptEngine {
    private final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

    @SuppressWarnings("unchecked")
    public Map<String, Object> evaluate(ApiDefinition api, Map<String, Object> payload, Map<String, Object> secret) {
        if (api.getScriptText() == null || api.getScriptText().trim().isEmpty()) {
            return new LinkedHashMap<String, Object>();
        }
        Binding binding = new Binding();
        binding.setVariable("payload", payload);
        binding.setVariable("secret", secret);
        binding.setVariable("api", api);
        binding.setVariable("now", LocalDateTime.now());
        binding.setVariable("bindings", new LinkedHashMap<String, Object>(binding.getVariables()));
        Object result = new GroovyShell(getClass().getClassLoader(), binding, compilerConfiguration).evaluate(api.getScriptText());
        if (result == null) {
            return new LinkedHashMap<String, Object>();
        }
        if (!(result instanceof Map)) {
            throw new IllegalArgumentException("groovy script must return a Map");
        }
        return new LinkedHashMap<String, Object>((Map<String, Object>) result);
    }

    public Object execute(ApiDefinition api,
                          Map<String, Object> payload,
                          Map<String, Object> secret,
                          Map<String, Object> headers,
                          Map<String, Object> query) {
        if (api.getScriptText() == null || api.getScriptText().trim().isEmpty()) {
            return null;
        }
        Binding binding = new Binding();
        binding.setVariable("api", api);
        binding.setVariable("apiDefinition", api);
        binding.setVariable("payload", payload);
        binding.setVariable("input", payload);
        binding.setVariable("params", payload);
        binding.setVariable("secret", secret);
        binding.setVariable("headers", headers);
        binding.setVariable("query", query);
        binding.setVariable("method", api.getMethod());
        binding.setVariable("url", api.getUrl());
        binding.setVariable("bodyType", api.getBodyType());
        binding.setVariable("bodyTemplate", api.getBodyTemplate());
        binding.setVariable("now", LocalDateTime.now());
        binding.setVariable("bindings", new LinkedHashMap<String, Object>(binding.getVariables()));
        return new GroovyShell(getClass().getClassLoader(), binding, compilerConfiguration).evaluate(api.getScriptText());
    }
}
