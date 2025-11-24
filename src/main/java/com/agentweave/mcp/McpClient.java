package com.agentweave.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP Client service to communicate with MCP server
 */
@Service
public class McpClient {

    private static final Logger logger = LoggerFactory.getLogger(McpClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String mcpServerUrl;

    public McpClient(@Value("${mcp.server.url:http://localhost:9091/mcp}") String mcpServerUrl) {
        this.mcpServerUrl = mcpServerUrl;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
            .baseUrl(mcpServerUrl)
            .build();
    }

    /**
     * Execute a tool on the MCP server
     * Spring AI MCP uses JSON-RPC 2.0 format with tools/call method
     * For sync HTTP streamable MCP server, the request structure includes _meta with progressToken
     * 
     * @param toolName Name of the tool to execute
     * @param arguments Arguments for the tool (as a map)
     * @return Result from the tool execution
     */
    public String executeTool(String toolName, Map<String, Object> arguments) {
        try {
            // Create MCP JSON-RPC 2.0 tool execution request with correct structure
            // for sync HTTP streamable MCP server
            long requestId = System.currentTimeMillis();
            
            Map<String, Object> request = new HashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("id", requestId);
            request.put("method", "tools/call");
            
            // Build params with _meta for sync HTTP streamable MCP
            Map<String, Object> params = new HashMap<>();
            params.put("name", toolName);
            params.put("arguments", arguments);
            
            // Add _meta with progressToken for sync HTTP streamable MCP
            Map<String, Object> meta = new HashMap<>();
            meta.put("progressToken", requestId);
            params.put("_meta", meta);
            
            request.put("params", params);

            // Log MCP request payload
            String requestPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            logger.info("MCP Request Payload:\n{}", requestPayload);

            // Log MCP request headers
            logger.info("MCP Request Headers:");
            logger.info("  Content-Type: application/json");
            logger.info("  Accept: application/json, text/event-stream");
            logger.info("  URL: {}", mcpServerUrl);

            // Make HTTP POST request to MCP server endpoint
            // Include required headers for sync HTTP streamable MCP server
            String response = webClient.post()
                .uri("")  // Base URL already includes /mcp
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            // Log MCP tool response
            logger.info("MCP Tool Response:\n{}", response);

            // Parse the JSON-RPC response
            JsonNode jsonResponse = objectMapper.readTree(response);
            
            // Check for errors first
            if (jsonResponse.has("error")) {
                JsonNode error = jsonResponse.get("error");
                String errorMessage = error.has("message") 
                    ? error.get("message").asText() 
                    : error.toString();
                throw new RuntimeException("MCP server error: " + errorMessage);
            }
            
            // Extract result
            if (jsonResponse.has("result")) {
                JsonNode result = jsonResponse.get("result");
                
                // Spring AI MCP returns content in result.content array
                if (result.has("content")) {
                    JsonNode content = result.get("content");
                    if (content.isArray() && content.size() > 0) {
                        JsonNode firstContent = content.get(0);
                        if (firstContent.has("text")) {
                            return firstContent.get("text").asText();
                        }
                        // If content is a string directly
                        if (firstContent.isTextual()) {
                            return firstContent.asText();
                        }
                    }
                }
                
                // Fallback: check if result itself is a string
                if (result.isTextual()) {
                    return result.asText();
                }
                
                // Return result as JSON string
                return result.toString();
            }

            // If no result, return the full response
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error calling MCP tool: " + toolName + " - " + e.getMessage(), e);
        }
    }

    /**
     * Execute the hello tool
     * Note: The hello tool in the MCP server doesn't take parameters
     * 
     * @param name Name to greet (currently not used by the tool, but kept for future use)
     * @return Greeting message from MCP server
     */
    public String callHelloTool(String name) {
        // The hello tool doesn't take parameters based on the MCP server implementation
        // But we'll pass an empty arguments map for consistency
        Map<String, Object> arguments = new HashMap<>();
        return executeTool("hello", arguments);
    }

    /**
     * Execute the readOutlookEmails tool
     * 
     * @param maxResults Maximum number of emails to retrieve (optional, default: 10)
     * @param folderId Mail folder ID (optional, default: 'inbox')
     * @return JSON string response containing list of emails
     */
    public String callReadOutlookEmailsTool(Integer maxResults, String folderId) {
        Map<String, Object> arguments = new HashMap<>();
        if (maxResults != null) {
            arguments.put("maxResults", maxResults);
        }
        if (folderId != null && !folderId.isEmpty()) {
            arguments.put("folderId", folderId);
        }
        return executeTool("readOutlookEmails", arguments);
    }

    /**
     * Execute the readOutlookEmails tool and parse response to return list of emails
     * 
     * @param maxResults Maximum number of emails to retrieve (optional, default: 10)
     * @param folderId Mail folder ID (optional, default: 'inbox')
     * @return List of email maps parsed from MCP response
     */
    public java.util.List<Map<String, Object>> readOutlookEmails(Integer maxResults, String folderId) {
        try {
            // Call the tool using executeTool method
            String response = callReadOutlookEmailsTool(maxResults, folderId);
            
            // Parse the JSON-RPC response to extract list of emails
            JsonNode jsonResponse = objectMapper.readTree(response);
            
            // Check for errors first
            if (jsonResponse.has("error")) {
                JsonNode error = jsonResponse.get("error");
                String errorMessage = error.has("message") 
                    ? error.get("message").asText() 
                    : error.toString();
                throw new RuntimeException("MCP server error: " + errorMessage);
            }
            
            // Extract result
            if (jsonResponse.has("result")) {
                JsonNode result = jsonResponse.get("result");
                
                // Spring AI MCP returns content in result.content array
                if (result.has("content")) {
                    JsonNode content = result.get("content");
                    if (content.isArray() && content.size() > 0) {
                        JsonNode firstContent = content.get(0);
                        if (firstContent.has("text")) {
                            // Parse the text content as JSON (list of emails)
                            String textContent = firstContent.get("text").asText();
                            JsonNode emailsJson = objectMapper.readTree(textContent);
                            if (emailsJson.isArray()) {
                                return objectMapper.convertValue(emailsJson, 
                                    objectMapper.getTypeFactory().constructCollectionType(
                                        java.util.List.class, Map.class));
                            }
                        }
                        // If content is directly an array
                        if (firstContent.isArray()) {
                            return objectMapper.convertValue(firstContent, 
                                objectMapper.getTypeFactory().constructCollectionType(
                                    java.util.List.class, Map.class));
                        }
                    }
                }
                
                // If result is directly an array
                if (result.isArray()) {
                    return objectMapper.convertValue(result, 
                        objectMapper.getTypeFactory().constructCollectionType(
                            java.util.List.class, Map.class));
                }
            }
            
            // Return empty list if parsing fails
            logger.warn("Could not parse emails from MCP response, returning empty list");
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            logger.error("Error parsing readOutlookEmails response", e);
            throw new RuntimeException("Error parsing emails from MCP response: " + e.getMessage(), e);
        }
    }
}

