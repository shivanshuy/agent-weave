package com.agentweave.controller;

import com.agentweave.mcp.McpClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mails")
public class MailController {

    private final McpClient mcpClient;

    public MailController(McpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    @GetMapping
    public Map<String, Object> getMails(
            @RequestParam(value = "maxResults", required = false) Integer maxResults,
            @RequestParam(value = "folderId", required = false) String folderId) {
        Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            // Call MCP server's readOutlookEmails tool
            List<Map<String, Object>> emails = mcpClient.readOutlookEmails(maxResults, folderId);
            
            response.put("emails", emails);
            response.put("count", emails.size());
            response.put("status", "success");
            if (maxResults != null) {
                response.put("maxResults", maxResults);
            }
            if (folderId != null) {
                response.put("folderId", folderId);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("emails", List.of());
            response.put("count", 0);
        }
        
        return response;
    }
}

