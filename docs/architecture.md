# Auth-KMP Architecture

## Overview

OAuth2 authentication library for Kotlin Multiplatform, providing token management with secure storage abstraction.

## Dependencies

- Ktor Client (HTTP requests for token exchange/refresh)
- kotlinx-serialization (JSON parsing of token responses)
- kotlinx-datetime (token expiration tracking)
- kotlinx-coroutines (thread-safe mutex locking)

## Design Decisions

1. SecureStorage is an interface — implementations provided by consumers
2. AuthTokenManager uses internal non-locking + public locking method pattern to prevent mutex deadlocks
3. Provider-specific OAuth2 flows extend base OAuth2Flow with pre-configured URLs and scopes
4. All operations return Result<T> for explicit error handling
