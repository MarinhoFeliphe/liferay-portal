# AI Hub Cell

OAuth2-based authorization token service for AI Hub Cell integration. Generates access tokens (via client credentials flow) and user JWT tokens (for on-behalf-of authentication). Controlled by feature flag `LPD-62272`.

## Sub-Modules

| Module | Purpose |
|---|---|
| `ai-hub-cell-api` | Configuration interface (`AIHubCellConfiguration`: clientId, clientSecret, serviceURL) and `JWTTokenUtil` (HS256 JWT generation/validation) |
| `ai-hub-cell-impl` | Config category registration, `AIHubCellRequestAuthVerifier` (reads `Liferay-AI-Hub-Cell-On-Behalf-Of` header), SAP entry lifecycle listener (`AI_HUB_CELL_TOKEN` policy) |
| `ai-hub-cell-rest-api` | Generated REST interface and `AuthorizationToken` DTO (accessToken, scope, serviceURL, userToken) |
| `ai-hub-cell-rest-impl` | `AuthorizationTokenResourceImpl` - performs OAuth2 client_credentials POST to `{serviceURL}/o/oauth2/token`, generates 1-min JWT user token |
| `ai-hub-cell-rest-client` | Generated Java HTTP client |
| `ai-hub-cell-rest-test` | Integration test: creates OAuth2Application, configures AI Hub Cell, calls POST endpoint, asserts token fields |

## REST API

- **Base URI**: `/ai-hub-cell`
- **Endpoint**: `POST /v1.0/authorization-tokens` - returns `AuthorizationToken` (accessToken, scope, serviceURL, userToken)
- **Application**: `Liferay.AI.Hub.Cell.REST`

## Authentication Flow

1. Client calls `POST /o/ai-hub-cell/v1.0/authorization-tokens`
2. Service reads `AIHubCellConfiguration` (clientId, clientSecret, serviceURL) for the company
3. Service makes OAuth2 client_credentials request to `{serviceURL}/o/oauth2/token`
4. Service generates a 1-minute JWT user token via `JWTTokenUtil`
5. Returns both tokens in `AuthorizationToken` DTO

## Build

```bash
# From liferay-portal/modules/
../gradlew :apps:ai-hub-cell:ai-hub-cell-impl:deploy
../gradlew :apps:ai-hub-cell:ai-hub-cell-rest-impl:deploy

# Regenerate REST Builder code
../gradlew :apps:ai-hub-cell:ai-hub-cell-rest-impl:buildREST

# Run integration tests
../gradlew :apps:ai-hub-cell:ai-hub-cell-rest-test:testIntegration
```

## Key Files

- Config: `ai-hub-cell-api/.../configuration/AIHubCellConfiguration.java`
- JWT: `ai-hub-cell-api/.../security/JWTTokenUtil.java`
- Auth verifier: `ai-hub-cell-impl/.../auth/verifier/AIHubCellRequestAuthVerifier.java`
- REST impl: `ai-hub-cell-rest-impl/.../resource/v1_0/AuthorizationTokenResourceImpl.java`
- OpenAPI spec: `ai-hub-cell-rest-impl/rest-openapi.yaml`
- REST config: `ai-hub-cell-rest-impl/rest-config.yaml`
