package digital.vasic.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

/**
 * Manages authentication tokens for cloud storage services.
 * Handles token storage, refresh, and expiration checking.
 */
class AuthTokenManager(
    private val serviceName: String,
    private val secureStorage: SecureStorage
) {
    private val mutex = Mutex()

    // ==================== Internal (non-locking) methods ====================
    // These methods do NOT acquire the mutex and must only be called from within
    // a mutex.withLock block. This prevents deadlocks from nested lock acquisition.

    private suspend fun storeAccessTokenInternal(storage: SecureStorage, token: String): Result<Unit> {
        return storage.storeToken("${serviceName}_access", token)
    }

    private suspend fun storeRefreshTokenInternal(storage: SecureStorage, token: String): Result<Unit> {
        return storage.storeToken("${serviceName}_refresh", token)
    }

    private suspend fun storeTokenExpirationInternal(storage: SecureStorage, expiresAt: Instant): Result<Unit> {
        return storage.store("${serviceName}_expires", expiresAt.toEpochMilliseconds().toString())
    }

    private suspend fun getAccessTokenInternal(storage: SecureStorage): Result<String?> {
        return storage.retrieveToken("${serviceName}_access")
    }

    private suspend fun getRefreshTokenInternal(storage: SecureStorage): Result<String?> {
        return storage.retrieveToken("${serviceName}_refresh")
    }

    private suspend fun isTokenExpiredInternal(storage: SecureStorage): Result<Boolean> {
        val expiresAtStr = storage.retrieve("${serviceName}_expires").getOrNull()

        return if (expiresAtStr == null) {
            Result.success(true) // No expiration info, assume expired
        } else {
            val expiresAt = Instant.fromEpochMilliseconds(expiresAtStr.toLong())
            val now = Clock.System.now()
            Result.success(now >= expiresAt)
        }
    }

    // ==================== Public (locking) methods ====================

    /**
     * Store access token securely
     */
    suspend fun storeAccessToken(token: String): Result<Unit> {
        return mutex.withLock {
            try {
                storeAccessTokenInternal(secureStorage, token)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Store refresh token securely
     */
    suspend fun storeRefreshToken(token: String): Result<Unit> {
        return mutex.withLock {
            try {
                storeRefreshTokenInternal(secureStorage, token)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Store token expiration time
     */
    suspend fun storeTokenExpiration(expiresAt: Instant): Result<Unit> {
        return mutex.withLock {
            try {
                storeTokenExpirationInternal(secureStorage, expiresAt)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Retrieve access token
     */
    suspend fun getAccessToken(): Result<String?> {
        return mutex.withLock {
            try {
                getAccessTokenInternal(secureStorage)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Retrieve refresh token
     */
    suspend fun getRefreshToken(): Result<String?> {
        return mutex.withLock {
            try {
                getRefreshTokenInternal(secureStorage)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Check if token is expired
     */
    suspend fun isTokenExpired(): Result<Boolean> {
        return mutex.withLock {
            try {
                isTokenExpiredInternal(secureStorage)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Check if we have a valid access token
     */
    suspend fun hasValidToken(): Result<Boolean> {
        return mutex.withLock {
            try {
                val accessTokenResult = getAccessTokenInternal(secureStorage)
                if (accessTokenResult.isFailure) return@withLock Result.success(false)

                val accessToken = accessTokenResult.getOrNull()
                if (accessToken.isNullOrBlank()) return@withLock Result.success(false)

                val isExpired = isTokenExpiredInternal(secureStorage).getOrNull() ?: true
                Result.success(!isExpired)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Clear all tokens for this service
     */
    suspend fun clearTokens(): Result<Unit> {
        return mutex.withLock {
            try {
                secureStorage.deleteToken("${serviceName}_access")
                secureStorage.deleteToken("${serviceName}_refresh")
                secureStorage.delete("${serviceName}_expires")
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Store complete token information
     */
    suspend fun storeTokenInfo(
        accessToken: String,
        refreshToken: String? = null,
        expiresIn: Long? = null
    ): Result<Unit> {
        return mutex.withLock {
            try {
                val results = mutableListOf<Result<Unit>>()

                // Store access token (using internal method to avoid nested lock)
                results.add(storeAccessTokenInternal(secureStorage, accessToken))

                // Store refresh token if provided
                refreshToken?.let {
                    results.add(storeRefreshTokenInternal(secureStorage, it))
                }

                // Store expiration if provided
                expiresIn?.let {
                    val expiresAt = Clock.System.now().plus(it.seconds)
                    results.add(storeTokenExpirationInternal(secureStorage, expiresAt))
                }

                // Return first failure or success
                results.firstOrNull { it.isFailure } ?: Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get token info for debugging (without sensitive data)
     */
    suspend fun getTokenInfo(): Result<TokenInfo> {
        return mutex.withLock {
            try {
                val hasAccessToken = getAccessTokenInternal(secureStorage).getOrNull()?.isNotBlank() ?: false
                val hasRefreshToken = getRefreshTokenInternal(secureStorage).getOrNull()?.isNotBlank() ?: false
                val isExpired = isTokenExpiredInternal(secureStorage).getOrNull() ?: true

                Result.success(
                    TokenInfo(
                        hasAccessToken = hasAccessToken,
                        hasRefreshToken = hasRefreshToken,
                        isExpired = isExpired,
                        serviceName = serviceName
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

/**
 * Token information for debugging and monitoring
 */
data class TokenInfo(
    val hasAccessToken: Boolean,
    val hasRefreshToken: Boolean,
    val isExpired: Boolean,
    val serviceName: String,
    val timestamp: Instant = Clock.System.now()
)
