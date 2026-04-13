# AI Hub

Enterprise AI application suite for creating, managing, and executing AI-powered agents, workflows, and chatbots. Integrates with Google Vertex AI (Gemini models) via LangChain4j. Controlled by feature flag `LPD-62272`.

## Sub-Modules

| Module | Purpose |
|---|---|
| `ai-hub-api` | `SupervisorAgent` interface, `AgentContext` builder (userId, companyId, accessToken, input variables, SSE event sink), `AccountEntryUtil` |
| `ai-hub-impl` | `SupervisorAgentImpl` (Vertex AI Gemini 2.5 Flash Lite via LangChain4j), `LLMNodeExecutor` and `AIDecisionNodeExecutor` (Kaleo workflow), RAG utilities, MCP tool provider |
| `ai-hub-rest-api` | Generated DTOs (`AgentDefinition`, `AgentInstance`, `Chat`, `Message`, `ContentRetriever`, `Variable`, `Status`, `Site`), resource interfaces, manager interfaces, `SseUtil` |
| `ai-hub-rest-impl` | REST resource implementations, `AgentDefinitionManagerImpl` and `ContentRetrieverManagerImpl` (use Object API for persistence), OData entity model |
| `ai-hub-rest-client` | Generated Java HTTP client |
| `ai-hub-rest-test` | Integration tests for all REST resources |
| `ai-hub-web` | Admin UI: Fragment renderers + Display contexts (Java), React/TypeScript forms (AgentDefinitionForm, ContentRetrieverForm, ChatbotForm, InstructionDefinitionForm), Clay UI, JSP templates |
| `ai-hub-site-initializer` | Site initializer: pages, layouts, master pages, fragments, pre-built workflow definitions (change-tone, fix-spelling, improve-writing, liferay-search, make-longer, make-shorter), object definitions |
| `ai-hub-site-initializer-test` | Integration tests for site initialization |

## REST API

**Base URI**: `/ai-hub`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/v1.0/agent-definitions` | List agent definitions |
| POST | `/v1.0/agent-definitions/by-external-reference-code/{erc}/copy` | Copy agent definition |
| PATCH | `/v1.0/agent-definitions/by-external-reference-code/{erc}/update-active` | Toggle active status |
| DELETE | `/v1.0/agent-definitions/by-external-reference-code/{erc}` | Delete agent definition |
| POST | `/v1.0/agent-instances` | Execute an agent |
| GET | `/v1.0/agent-instances/subscribe` | SSE stream for agent execution |
| POST | `/v1.0/chats/by-external-reference-code/{erc}/messages` | Post chat message |
| GET | `/v1.0/chats/subscribe` | SSE stream for chat |
| POST | `/v1.0/content-retrievers` | Create content retriever |
| DELETE | `/v1.0/content-retrievers/by-external-reference-code/{erc}` | Delete content retriever |
| PUT | `/v1.0/sites/by-external-reference-code/{erc}/site-initializer` | Initialize site |

## Domain Concepts

- **AgentDefinition**: Blueprint with title, workflowDefinitionName, inputVariables, outputVariable, active status. Persisted via Object API.
- **AgentInstance**: Runtime execution of an agent. Carries context (user, company, group, input). Supports SSE for real-time feedback. Scopes: `clickToChat`, `cms`, `everywhere`.
- **ContentRetriever**: Data source for RAG. Types: web crawl, search index. Has URL, indexName, multilingual title/description, crawl date.
- **Chat/Message**: Conversation entities for chatbot interactions.
- **Workflow Definitions**: Kaleo workflows with custom `LLM` and `AIDecision` node types executed by Vertex AI Gemini.

## Tech Stack

- **AI/ML**: LangChain4j, Google Vertex AI (Gemini 2.5 Flash Lite)
- **Workflow**: Kaleo (custom LLM/AIDecision node executors)
- **Data**: Object API (custom object definitions for persistence)
- **Real-time**: Server-Sent Events (SSE)
- **Frontend**: React, TypeScript, Clay UI (@clayui/*), Liferay frontend components
- **Search**: Portal Search API / Elasticsearch (for RAG)

## Build

```bash
# From liferay-portal/modules/
../gradlew :dxp:apps:ai-hub:ai-hub-impl:deploy
../gradlew :dxp:apps:ai-hub:ai-hub-rest-impl:deploy
../gradlew :dxp:apps:ai-hub:ai-hub-web:deploy

# Regenerate REST Builder code
../gradlew :dxp:apps:ai-hub:ai-hub-rest-impl:buildREST

# Run integration tests
../gradlew :dxp:apps:ai-hub:ai-hub-rest-test:testIntegration

# Build frontend (from ai-hub-web/)
../../../gradlew npmRunBuild
```

## Key Files

- Agent interface: `ai-hub-api/.../agent/SupervisorAgent.java`
- Agent impl: `ai-hub-impl/.../agent/SupervisorAgentImpl.java`
- LLM node executor: `ai-hub-impl/.../workflow/kaleo/runtime/node/LLMNodeExecutor.java`
- Agent definition manager: `ai-hub-rest-impl/.../manager/v1_0/AgentDefinitionManagerImpl.java`
- Content retriever manager: `ai-hub-rest-impl/.../manager/v1_0/ContentRetrieverManagerImpl.java`
- OpenAPI spec: `ai-hub-rest-impl/rest-openapi.yaml`
- REST config: `ai-hub-rest-impl/rest-config.yaml`
- Site initializer resources: `ai-hub-site-initializer/src/main/resources/site-initializer/`
- Frontend forms: `ai-hub-web/src/main/resources/META-INF/resources/js/`
