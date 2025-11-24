package com.agentweave.agent.node;

import com.agentweave.agent.state.HelloState;
import com.agentweave.mcp.McpClient;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Hello node implementation using LangGraph4j AsyncNodeAction
 * This node calls the MCP server's hello tool to process the greeting message
 */
public class HelloNode implements AsyncNodeAction<HelloState> {

    private final McpClient mcpClient;

    public HelloNode(McpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(HelloState state) {
        String name = state.getName();
        
        // Call MCP server's hello tool
        String mcpResponse = mcpClient.callHelloTool(name);
        
        // Process the response from MCP server
        String greeting = mcpResponse != null && !mcpResponse.isEmpty() 
            ? mcpResponse 
            : "Hello, " + name + "!";
        String message = greeting + " Agent processed your request using LangGraph4j with MCP.";
        
        // Update state - appender channels expect lists, so we append single values as lists
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put(HelloState.GREETING_KEY, java.util.List.of(greeting));
        updates.put(HelloState.MESSAGE_KEY, java.util.List.of(message));
        updates.put(HelloState.STATUS_KEY, java.util.List.of("completed"));
        
        return CompletableFuture.completedFuture(updates);
    }
}

