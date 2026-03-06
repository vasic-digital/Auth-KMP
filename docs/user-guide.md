# Auth-KMP User Guide

## Getting Started

Auth-KMP provides OAuth2 authentication flows and token management for Kotlin Multiplatform projects.

## OAuth2 Flows

### Generic Flow

```kotlin
val flow = OAuth2Flow(
    httpClient = httpClient,
    clientId = "your-client-id",
    clientSecret = "your-client-secret",
    authorizationUrl = "https://provider.com/oauth/authorize",
    tokenUrl = "https://provider.com/oauth/token",
    redirectUri = "https://your-app/callback",
    scopes = listOf("read", "write")
)
```

### Pre-configured Providers

- `DropboxOAuth2Flow` - Dropbox OAuth2 with file scopes
- `GoogleDriveOAuth2Flow` - Google Drive with drive.file scope
- `OneDriveOAuth2Flow` - Microsoft OneDrive with Files.ReadWrite

## Token Management

```kotlin
val manager = AuthTokenManager("my-service", secureStorage)

// Store tokens
manager.storeTokenInfo(accessToken, refreshToken, expiresIn)

// Check validity
val isValid = manager.hasValidToken().getOrNull() ?: false

// Get debug info
val info = manager.getTokenInfo().getOrNull()
```

## SecureStorage Interface

Implement `SecureStorage` for your platform:

```kotlin
class MySecureStorage : SecureStorage {
    override suspend fun store(key: String, value: String): Result<Unit> { ... }
    override suspend fun retrieve(key: String): Result<String?> { ... }
    override suspend fun delete(key: String): Result<Unit> { ... }
    override suspend fun contains(key: String): Result<Boolean> { ... }
    override suspend fun listKeys(): Result<List<String>> { ... }
    override suspend fun clear(): Result<Unit> { ... }
    override suspend fun isSecure(): Result<Boolean> { ... }
}
```

Built-in helpers: `storeCredentials`, `storeToken`, `storePrivateKey` (and retrieve/delete variants).
