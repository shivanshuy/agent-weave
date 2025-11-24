package com.agentweave.agent.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * State definition for the Hello agent using LangGraph4j
 */
public class HelloState extends AgentState {
    
    public static final String NAME_KEY = "name";
    public static final String MESSAGE_KEY = "message";
    public static final String GREETING_KEY = "greeting";
    public static final String STATUS_KEY = "status";

    public static final Map<String, Channel<?>> SCHEMA;
    
    static {
        Map<String, Channel<?>> schema = new HashMap<>();
        // Create channels using LangGraph4j Channels API
        // For simple value channels, we can use appender with a single-value list
        // or try alternative approaches based on the actual API
        try {
            // Try using appender with ArrayList supplier for each channel
            schema.put(NAME_KEY, Channels.appender(() -> new java.util.ArrayList<String>()));
            schema.put(MESSAGE_KEY, Channels.appender(() -> new java.util.ArrayList<String>()));
            schema.put(GREETING_KEY, Channels.appender(() -> new java.util.ArrayList<String>()));
            schema.put(STATUS_KEY, Channels.appender(() -> new java.util.ArrayList<String>()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create state schema with Channels.appender", e);
        }
        SCHEMA = Map.copyOf(schema);
    }

    public HelloState(Map<String, Object> initData) {
        super(initData);
    }

    public String getName() {
        // Appender channels store lists, get the last value (most recent)
        List<String> nameList = this.<List<String>>value(NAME_KEY).orElse(new ArrayList<>());
        return nameList.isEmpty() ? "World" : nameList.get(nameList.size() - 1);
    }

    public void setName(String name) {
        // State is updated through node actions, not directly
    }

    public String getMessage() {
        // Appender channels store lists, get the last value (most recent)
        List<String> msgList = this.<List<String>>value(MESSAGE_KEY).orElse(new ArrayList<>());
        return msgList.isEmpty() ? "" : msgList.get(msgList.size() - 1);
    }

    public void setMessage(String message) {
        // State is updated through node actions, not directly
    }

    public String getGreeting() {
        // Appender channels store lists, get the last value (most recent)
        List<String> greetingList = this.<List<String>>value(GREETING_KEY).orElse(new ArrayList<>());
        return greetingList.isEmpty() ? "" : greetingList.get(greetingList.size() - 1);
    }

    public void setGreeting(String greeting) {
        // State is updated through node actions, not directly
    }

    public String getStatus() {
        // Appender channels store lists, get the last value (most recent)
        List<String> statusList = this.<List<String>>value(STATUS_KEY).orElse(new ArrayList<>());
        return statusList.isEmpty() ? "" : statusList.get(statusList.size() - 1);
    }

    public void setStatus(String status) {
        // State is updated through node actions, not directly
    }
}

