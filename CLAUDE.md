# Auth-KMP

## Project Overview

Kotlin Multiplatform OAuth2 authentication library. Package: `digital.vasic.auth`.

## Build Commands

```bash
./gradlew desktopTest    # Run tests
./gradlew build          # Build all targets
```

## Architecture

- `SecureStorage.kt` - Platform-agnostic secure storage interface
- `OAuth2Flow.kt` - OAuth2 flow + provider subclasses (Dropbox, Google Drive, OneDrive)
- `AuthTokenManager.kt` - Thread-safe token lifecycle management

## Key Patterns

- `AuthTokenManager` uses internal non-locking methods + public locking methods to prevent mutex deadlocks
- `SecureStorage` is an interface — implementations are provided by consumers (e.g., Security-KMP)
- `TokenResponse` is `@Serializable` for Ktor JSON deserialization

## Dependencies

- Ktor Client (HTTP), kotlinx-serialization (JSON), kotlinx-datetime (expiration), kotlinx-coroutines (Mutex)
