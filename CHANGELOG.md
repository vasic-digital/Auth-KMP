<!-- SPDX-FileCopyrightText: 2025 Milos Vasic -->
<!-- SPDX-License-Identifier: Apache-2.0 -->

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-03-06

### Added
- Initial release extracted from Yole monolith
- `OAuth2Flow` - Authorization URL generation, authorization code exchange, token refresh, and token revocation
- `AuthTokenManager` - Thread-safe token storage with expiration tracking and lifecycle management
- `SecureStorage` - Platform-agnostic secure storage interface with credential, token, and key helpers
- `DropboxOAuth2Flow` - Pre-configured OAuth2 flow for Dropbox API
- `GoogleDriveOAuth2Flow` - Pre-configured OAuth2 flow for Google Drive API
- `OneDriveOAuth2Flow` - Pre-configured OAuth2 flow for OneDrive/Microsoft Graph API
- Kotlin Multiplatform support (Android, Desktop/JVM, iOS, Wasm/JS)
- Comprehensive test suite
- CI/CD via GitHub Actions

### Infrastructure
- Gradle build with version catalog
- Kover code coverage
- SPDX license headers (Apache-2.0)
