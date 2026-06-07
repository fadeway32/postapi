package com.fadeway32.postadmin.controller;

import com.fadeway32.postadmin.dto.GroovyExecutionResult;
import com.fadeway32.postadmin.dto.GroovySimulateRequest;
import com.fadeway32.postadmin.service.GroovyNativeExecutionService;
import com.fadeway32.postadmin.web.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/groovy")
public class GroovySimulationController {

    private final GroovyNativeExecutionService groovyNativeExecutionService;

    public GroovySimulationController(GroovyNativeExecutionService groovyNativeExecutionService) {
        this.groovyNativeExecutionService = groovyNativeExecutionService;
    }

    @PostMapping("/simulate")
    public Result<GroovyExecutionResult> simulate(@Valid @RequestBody GroovySimulateRequest request) {
        return Result.ok(groovyNativeExecutionService.simulate(request));
    }
}
