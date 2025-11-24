package com.agentweave.agent;

import com.agentweave.agent.node.HelloNode;
import com.agentweave.agent.state.HelloState;
import com.agentweave.mcp.McpClient;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.GraphStateException;

import java.util.Map;

/**
 * Hello agent implementation using LangGraph4j StateGraph
 * This agent uses MCP client to call MCP server's hello tool
 */
public class HelloAgent {

    private final StateGraph<HelloState> stateGraph;

    public HelloAgent(McpClient mcpClient) {
        try {
            // Build the state graph using LangGraph4j with MCP client
            this.stateGraph = new StateGraph<>(HelloState.SCHEMA, HelloState::new)
                .addNode("hello", new HelloNode(mcpClient))
                .addEdge(StateGraph.START, "hello")
                .addEdge("hello", StateGraph.END);
        } catch (GraphStateException e) {
            throw new RuntimeException("Error building agent graph", e);
        }
    }

    /**
     * Execute the agent graph with the given input
     * 
     * @param name Name to greet
     * @return Greeting message
     */
    public String greet(String name) {
        try {
            // Compile the graph
            var compiledGraph = stateGraph.compile();
            
            // Create initial state as Map - appender channels expect lists
            var initialState = Map.<String, Object>of(
                HelloState.NAME_KEY, java.util.List.of(name)
            );
            
            // Execute the graph
            var resultStateOpt = compiledGraph.invoke(initialState);
            
            // Return the message from the final state
            return resultStateOpt.map(HelloState::getMessage)
                .orElse("Error: No result from agent");
        } catch (GraphStateException e) {
            throw new RuntimeException("Error executing agent graph", e);
        }
    }

    /**
     * Execute the agent with full state and return the complete state
     * 
     * @param input Input parameters for the agent
     * @return Result state containing all processed data
     */
    public HelloState execute(Map<String, Object> input) {
        try {
            var compiledGraph = stateGraph.compile();
            var resultStateOpt = compiledGraph.invoke(input);
            return resultStateOpt.orElseThrow(() -> 
                new RuntimeException("Agent execution returned no result"));
        } catch (GraphStateException e) {
            throw new RuntimeException("Error executing agent graph", e);
        }
    }
}
