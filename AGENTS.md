# Auth-KMP Agent Guidelines

## Testing

All tests in `src/commonTest/`. Run with `./gradlew desktopTest`.

Test files:
- `OAuth2FlowTest.kt` - OAuth2 URL generation, TokenResponse, provider subclasses
- `OAuth2FlowUrlTest.kt` - Focused URL parameter validation
- `AuthTokenManagerTest.kt` - Token storage, expiration, validation, error recovery
- `AuthTokenManagerImplTest.kt` - Implementation-level token operations
- `AuthTokenManagerStressTest.kt` - Concurrent access, lifecycle, edge cases

## Rules

- Never remove or disable tests
- All changes must pass existing tests
- SecureStorage is an interface only — no platform implementations here
