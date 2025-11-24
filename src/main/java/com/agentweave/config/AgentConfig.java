package com.agentweave.config;

import com.agentweave.agent.HelloAgent;
import com.agentweave.mcp.McpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean
    public HelloAgent helloAgent(McpClient mcpClient) {
        return new HelloAgent(mcpClient);
    }
}

