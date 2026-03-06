# Auth-KMP

Kotlin Multiplatform OAuth2 authentication library with token management and secure storage interface.

## Features

- **OAuth2Flow** - Authorization URL generation, code exchange, token refresh, revocation
- **AuthTokenManager** - Thread-safe token storage, expiration tracking, lifecycle management
- **SecureStorage** - Platform-agnostic secure storage interface with credential/token/key helpers
- **Provider Subclasses** - Dropbox, Google Drive, OneDrive pre-configured OAuth2 flows

## Platforms

- Android
- Desktop (JVM)
- iOS (x64, arm64, simulator)
- Web (Wasm/JS)

## Installation

Add as a git submodule:

```bash
git submodule add git@github.com:vasic-digital/Auth-KMP.git
```

Then in your `settings.gradle.kts`:

```kotlin
includeBuild("Auth-KMP")
```

## Usage

```kotlin
// Create OAuth2 flow
val flow = GoogleDriveOAuth2Flow(
    httpClient = httpClient,
    clientId = "your-client-id",
    clientSecret = "your-client-secret",
    redirectUri = "https://your-app/callback"
)

// Generate authorization URL
val authUrl = flow.getAuthorizationUrl(state = "csrf-token")

// Exchange code for token
val tokenResult = flow.exchangeCodeForToken(authorizationCode)

// Manage tokens
val tokenManager = AuthTokenManager("google-drive", secureStorage)
tokenManager.storeTokenInfo(
    accessToken = token.access_token,
    refreshToken = token.refresh_token,
    expiresIn = token.expires_in
)
```

## License

Apache-2.0
