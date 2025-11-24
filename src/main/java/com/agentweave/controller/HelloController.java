package com.agentweave.controller;

import com.agentweave.agent.HelloAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    private final HelloAgent helloAgent;

    public HelloController(HelloAgent helloAgent) {
        this.helloAgent = helloAgent;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        Map<String, Object> response = new HashMap<>();
        
        // Execute the LangGraph4j agent
        String message = helloAgent.greet(name);
        
        response.put("message", message);
        response.put("agent", "LangGraph4j Agent");
        response.put("name", name);
        return response;
    }
}

