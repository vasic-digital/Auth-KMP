# Auth-KMP API Reference

## SecureStorage

- `store(key, value)` - Store key-value pair
- `retrieve(key)` - Get value by key
- `delete(key)` - Remove key
- `contains(key)` - Check key existence
- `listKeys()` - List all keys
- `clear()` - Remove all data
- `isSecure()` - Check storage security
- `storeCredentials(service, username, password)` - Store credentials
- `retrieveCredentials(service)` - Retrieve credentials as Pair
- `storeToken(service, token)` / `retrieveToken(service)` / `deleteToken(service)`
- `storePrivateKey(service, key)` / `retrievePrivateKey(service)` / `deletePrivateKey(service)`

## OAuth2Flow

- `OAuth2Flow(httpClient, clientId, clientSecret, authorizationUrl, tokenUrl, redirectUri, scopes)`
- `getAuthorizationUrl(state?)` - Generate OAuth2 authorization URL
- `exchangeCodeForToken(authorizationCode)` - Exchange code for token
- `refreshAccessToken(refreshToken)` - Refresh expired token
- `revokeToken(token, revokeUrl?)` - Revoke token

## TokenResponse

- `access_token` - The access token string
- `token_type` - Token type (default "Bearer")
- `expires_in` - Seconds until expiration
- `refresh_token` - Optional refresh token
- `scope` - Granted scope
- `hasRefreshToken()` - Check if refresh token exists
- `getExpiresInSeconds()` - Get expiration (default 3600)

## AuthTokenManager

- `AuthTokenManager(serviceName, secureStorage)`
- `storeAccessToken(token)` / `getAccessToken()`
- `storeRefreshToken(token)` / `getRefreshToken()`
- `storeTokenExpiration(expiresAt)` / `isTokenExpired()`
- `hasValidToken()` - Check if token exists and is not expired
- `clearTokens()` - Remove all tokens for service
- `storeTokenInfo(accessToken, refreshToken?, expiresIn?)` - Store complete token info
- `getTokenInfo()` - Get debug info (TokenInfo)

## TokenInfo

- `hasAccessToken`, `hasRefreshToken`, `isExpired`, `serviceName`, `timestamp`

## Provider Subclasses

- `DropboxOAuth2Flow(httpClient, clientId, clientSecret, redirectUri)`
- `GoogleDriveOAuth2Flow(httpClient, clientId, clientSecret, redirectUri)`
- `OneDriveOAuth2Flow(httpClient, clientId, clientSecret, redirectUri)`
