# Agent Weave

A Spring Boot application that demonstrates LangGraph4j agent implementation without using any LLM.

## Features

- Spring Boot 3.2.0
- **LangGraph4j** library (version 1.7.3) for building stateful agent graphs
- **MCP (Model Context Protocol) Client** integration to call MCP server tools
- RESTful API endpoint `/hello` to interact with the agent
- No LLM dependencies - pure graph-based agent execution using LangGraph4j
- State-based graph execution using LangGraph4j StateGraph API
- Proper separation of concerns: State, Nodes, and Agent classes
- MCP client connects to MCP server at `http://localhost:9091/mcp`

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Building the Project

```bash
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:9090`

### API Endpoints

#### GET /hello

Invokes the LangGraph4j agent to process a greeting.

**Query Parameters:**
- `name` (optional): Name to greet. Defaults to "World" if not provided.

**Example Request:**
```
GET http://localhost:9090/hello?name=John
```

**Example Response:**
```json
{
  "message": "hello world Agent processed your request using LangGraph4j with MCP.",
  "agent": "LangGraph4j Agent",
  "name": "John"
}
```

**Note:** The response comes from the MCP server's `hello` tool, which returns "hello world".

#### GET /mails

Reads emails from Outlook mailbox using the MCP server's `readOutlookEmails` tool.

**Query Parameters:**
- `maxResults` (optional): Maximum number of emails to retrieve. Defaults to 10 if not specified.
- `folderId` (optional): Mail folder ID. Defaults to 'inbox' if not specified.

**Example Request:**
```
GET http://localhost:9090/mails?maxResults=5&folderId=inbox
```

**Example Response:**
```json
{
  "emails": [
    {
      "id": "email-id-1",
      "subject": "Email Subject",
      "from": "sender@example.com",
      "body": "Email content..."
    }
  ],
  "count": 1,
  "status": "success",
  "maxResults": 5,
  "folderId": "inbox"
}
```

## Project Structure

```
agent-weave/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── agentweave/
│   │   │           ├── AgentWeaveApplication.java
│   │   │           ├── agent/
│   │   │           │   ├── HelloAgent.java (uses LangGraph4j StateGraph)
│   │   │           │   ├── node/
│   │   │           │   │   └── HelloNode.java (implements AsyncNodeAction, calls MCP)
│   │   │           │   └── state/
│   │   │           │       └── HelloState.java (extends AgentState)
│   │   │           ├── config/
│   │   │           │   └── AgentConfig.java
│   │   │           ├── controller/
│   │   │           │   └── HelloController.java
│   │   │           └── mcp/
│   │   │               └── McpClient.java (MCP client service)
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

## How It Works

The application uses **LangGraph4j** to build a stateful agent graph with **MCP (Model Context Protocol)** integration:

1. **HelloState**: Extends `AgentState` from LangGraph4j, defining the state schema with channels for name, message, greeting, and status.

2. **McpClient**: HTTP-based MCP client service that communicates with the MCP server at `http://localhost:9091/mcp`. It uses JSON-RPC 2.0 format to call MCP tools. All MCP tool call requests include the following headers:
   - `Accept: application/json, text/event-stream`
   - `Content-Type: application/json`

3. **HelloNode**: Implements `AsyncNodeAction<HelloState>` from LangGraph4j. This node:
   - Calls the MCP server's `hello` tool using the `McpClient`
   - Processes the response from the MCP server
   - Updates the agent state with the result

4. **HelloAgent**: Uses LangGraph4j's `StateGraph` to build a graph with:
   - A "hello" node that calls the MCP server's hello tool
   - Edges connecting START → hello → END
   - Graph compilation and execution using LangGraph4j's API

5. **HelloController**: Exposes a REST endpoint that accepts a name parameter and invokes the LangGraph4j agent.

6. **AgentConfig**: Spring configuration that creates and wires the agent bean with the MCP client.

The agent processes requests through LangGraph4j's graph structure and calls external tools via MCP, demonstrating graph-based agent execution with MCP tool integration.

## MCP Server Configuration

The application expects an MCP server running at `http://localhost:9091/mcp`. You can configure the MCP server URL in `application.properties`:

```properties
mcp.server.url=http://localhost:9091/mcp
```

### MCP Client Request Headers

When making MCP tool calls, the `McpClient` includes the following request headers:

- **Accept**: `application/json, text/event-stream`
- **Content-Type**: `application/json`

These headers are required for the MCP server's HTTP Sync Stateless protocol to properly handle requests and responses.

### MCP Tool Call Request Format

The MCP client sends JSON-RPC 2.0 requests with the following structure:

```json
{
  "method": "tools/call",
  "params": {
    "name": "tool-name",
    "arguments": {},
    "_meta": {
      "progressToken": 1234567890
    }
  },
  "jsonrpc": "2.0",
  "id": 1234567890
}
```

Ensure the MCP server is running and exposes the required tools (e.g., `hello`, `readOutlookEmails`) before starting this application.

## License

This project is provided as-is for demonstration purposes.

