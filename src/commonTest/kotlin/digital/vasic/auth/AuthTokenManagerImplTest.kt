/*#######################################################
 *
 * SPDX-FileCopyrightText: 2025 Milos Vasic
 * SPDX-License-Identifier: Apache-2.0
 *
 * Implementation tests for AuthTokenManager
 *
 *########################################################*/
package digital.vasic.auth

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.hours

/**
 * Implementation tests for AuthTokenManager covering:
 * - Token storage and retrieval operations
 * - Token expiration handling
 * - Error scenarios and edge cases
 * - Cross-platform compatibility
 */
class AuthTokenManagerImplTest {

    private lateinit var secureStorage: TestSecureStorage
    private lateinit var authTokenManager: AuthTokenManager
    private val testService = "test_service"
    private val testToken = "test_access_token_123"
    private val testRefreshToken = "test_refresh_token_456"

    @BeforeTest
    fun setUp() {
        secureStorage = TestSecureStorage()
        authTokenManager = AuthTokenManager(testService, secureStorage)
    }

    @AfterTest
    fun tearDown() = runTest {
        secureStorage.clear()
    }

    // ==================== Token Storage Tests ====================

    @Test
    fun testStoreAccessToken() = runTest {
        val result = authTokenManager.storeAccessToken(testToken)
        assertTrue(result.isSuccess, "Should successfully store access token")
    }

    @Test
    fun testGetAccessToken() = runTest {
        authTokenManager.storeAccessToken(testToken)
        val result = authTokenManager.getAccessToken()
        assertTrue(result.isSuccess, "Should successfully retrieve access token")
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testGetAccessTokenNotFound() = runTest {
        val result = authTokenManager.getAccessToken()
        assertTrue(result.isSuccess, "Should succeed with null when token not found")
        assertNull(result.getOrNull())
    }

    @Test
    fun testStoreRefreshToken() = runTest {
        val result = authTokenManager.storeRefreshToken(testRefreshToken)
        assertTrue(result.isSuccess, "Should successfully store refresh token")
    }

    @Test
    fun testGetRefreshToken() = runTest {
        authTokenManager.storeRefreshToken(testRefreshToken)
        val result = authTokenManager.getRefreshToken()
        assertTrue(result.isSuccess, "Should successfully retrieve refresh token")
        assertNotNull(result.getOrNull())
    }

    // ==================== Token Expiration Tests ====================

    @Test
    fun testStoreTokenExpiration() = runTest {
        val expiresAt = Clock.System.now().plus(1.hours)
        val result = authTokenManager.storeTokenExpiration(expiresAt)
        assertTrue(result.isSuccess, "Should successfully store token expiration")
    }

    @Test
    fun testIsTokenExpired() = runTest {
        val expiredTime = Clock.System.now().minus(1.hours)
        authTokenManager.storeTokenExpiration(expiredTime)
        val result = authTokenManager.isTokenExpired()
        assertTrue(result.isSuccess, "Should successfully check token expiration")
        assertTrue(result.getOrNull() ?: false, "Should detect expired token")
    }

    @Test
    fun testIsTokenNotExpired() = runTest {
        val validTime = Clock.System.now().plus(1.hours)
        authTokenManager.storeTokenExpiration(validTime)
        val result = authTokenManager.isTokenExpired()
        assertTrue(result.isSuccess, "Should successfully check token expiration")
        assertFalse(result.getOrNull() ?: true, "Should detect valid token")
    }

    @Test
    fun testIsTokenExpiredNoExpirationStored() = runTest {
        val result = authTokenManager.isTokenExpired()
        assertTrue(result.isSuccess, "Should successfully check token expiration")
        assertTrue(result.getOrNull() ?: false, "Should consider token expired when no expiration stored")
    }

    // ==================== Token Validation Tests ====================

    @Test
    fun testHasValidToken() = runTest {
        authTokenManager.storeAccessToken(testToken)
        val validTime = Clock.System.now().plus(1.hours)
        authTokenManager.storeTokenExpiration(validTime)
        val result = authTokenManager.hasValidToken()
        assertTrue(result.isSuccess, "Should successfully check token validity")
        assertTrue(result.getOrNull() ?: false, "Should detect valid token")
    }

    @Test
    fun testHasValidTokenExpired() = runTest {
        authTokenManager.storeAccessToken(testToken)
        val expiredTime = Clock.System.now().minus(1.hours)
        authTokenManager.storeTokenExpiration(expiredTime)
        val result = authTokenManager.hasValidToken()
        assertTrue(result.isSuccess, "Should successfully check token validity")
        assertFalse(result.getOrNull() ?: true, "Should detect expired token")
    }

    @Test
    fun testHasValidTokenNoToken() = runTest {
        val result = authTokenManager.hasValidToken()
        assertTrue(result.isSuccess, "Should successfully check token validity")
        assertFalse(result.getOrNull() ?: true, "Should detect missing token")
    }

    // ==================== Token Deletion Tests ====================

    @Test
    fun testClearTokens() = runTest {
        authTokenManager.storeAccessToken(testToken)
        authTokenManager.storeRefreshToken(testRefreshToken)
        val expiresAt = Clock.System.now().plus(1.hours)
        authTokenManager.storeTokenExpiration(expiresAt)
        val result = authTokenManager.clearTokens()
        assertTrue(result.isSuccess, "Should successfully clear tokens")

        val hasValid = authTokenManager.hasValidToken()
        assertFalse(hasValid.getOrNull() ?: true, "Should not have valid token after clear")
    }

    @Test
    fun testClearTokensForMultipleServices() = runTest {
        val services = listOf("service1", "service2", "service3")
        val managers = services.map { service ->
            AuthTokenManager(service, secureStorage)
        }

        managers.forEach { manager ->
            manager.storeAccessToken("token_${manager.getTokenInfo().getOrNull()?.serviceName}")
        }

        val results = managers.map { it.clearTokens() }
        assertTrue(results.all { it.isSuccess }, "Should successfully clear all service tokens")
        managers.forEach { manager ->
            val hasValid = manager.hasValidToken()
            assertFalse(hasValid.getOrNull() ?: true, "Should not have valid token after clear")
        }
    }

    // ==================== Edge Cases ====================

    @Test
    fun testEmptyServiceName() = runTest {
        val emptyServiceManager = AuthTokenManager("", secureStorage)
        val result = emptyServiceManager.storeAccessToken(testToken)
        assertTrue(result.isSuccess, "Should handle empty service name")
    }

    @Test
    fun testSpecialCharactersInTokens() = runTest {
        val specialToken = "token_with_special_chars_!@#\$%^&*()_+-=[]{}|;':\",./<>?"
        val result = authTokenManager.storeAccessToken(specialToken)
        assertTrue(result.isSuccess, "Should handle special characters in tokens")
    }

    @Test
    fun testVeryLongServiceNames() = runTest {
        val longService = "a".repeat(1000)
        val longServiceManager = AuthTokenManager(longService, secureStorage)
        val result = longServiceManager.storeAccessToken(testToken)
        assertTrue(result.isSuccess, "Should handle very long service names")
    }

    @Test
    fun testConcurrentTokenOperations() = runTest {
        val operations = 100
        val results = (1..operations).map { i ->
            val manager = AuthTokenManager("${testService}$i", secureStorage)
            manager.storeAccessToken("token$i")
        }
        assertTrue(results.all { it.isSuccess }, "All concurrent operations should succeed")
    }

    @Test
    fun testTokenOverwrite() = runTest {
        authTokenManager.storeAccessToken("original_token")
        authTokenManager.storeAccessToken("new_token")
        val result = authTokenManager.getAccessToken()
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    // ==================== Token Info Tests ====================

    @Test
    fun testStoreTokenInfo() = runTest {
        val result = authTokenManager.storeTokenInfo(
            accessToken = testToken,
            refreshToken = testRefreshToken,
            expiresIn = 3600L
        )
        assertTrue(result.isSuccess, "Should successfully store token info")

        val tokenInfo = authTokenManager.getTokenInfo()
        assertTrue(tokenInfo.isSuccess)
        assertTrue(tokenInfo.getOrNull()?.hasAccessToken ?: false)
        assertTrue(tokenInfo.getOrNull()?.hasRefreshToken ?: false)
        assertFalse(tokenInfo.getOrNull()?.isExpired ?: true)
        assertEquals(testService, tokenInfo.getOrNull()?.serviceName)
    }

    @Test
    fun testGetTokenInfo() = runTest {
        authTokenManager.storeTokenInfo(
            accessToken = testToken,
            refreshToken = testRefreshToken,
            expiresIn = 3600L
        )
        val result = authTokenManager.getTokenInfo()
        assertTrue(result.isSuccess)
        val tokenInfo = result.getOrNull()
        assertNotNull(tokenInfo)
        assertTrue(tokenInfo.hasAccessToken)
        assertTrue(tokenInfo.hasRefreshToken)
        assertEquals(testService, tokenInfo.serviceName)
    }

    @Test
    fun testGetTokenInfoNoTokens() = runTest {
        val result = authTokenManager.getTokenInfo()
        assertTrue(result.isSuccess)
        val tokenInfo = result.getOrNull()
        assertNotNull(tokenInfo)
        assertFalse(tokenInfo.hasAccessToken)
        assertFalse(tokenInfo.hasRefreshToken)
        assertTrue(tokenInfo.isExpired)
    }

    // ==================== Storage Failure Tests ====================

    @Test
    fun testStorageFailure() = runTest {
        secureStorage.shouldFail = true
        val result = authTokenManager.storeAccessToken(testToken)
        assertTrue(result.isFailure, "Should fail when storage fails")
    }

    @Test
    fun testRecoverFromStorageFailure() = runTest {
        secureStorage.shouldFail = true
        val firstResult = authTokenManager.storeAccessToken(testToken)
        assertTrue(firstResult.isFailure)

        secureStorage.shouldFail = false
        val secondResult = authTokenManager.storeAccessToken(testToken)
        assertTrue(secondResult.isSuccess)
    }

    // ==================== Test Helper Classes ====================

    private class TestSecureStorage : SecureStorage {
        private val storage = mutableMapOf<String, String>()
        var shouldFail = false

        override suspend fun store(key: String, value: String): Result<Unit> {
            if (shouldFail) return Result.failure(Exception("Storage failed"))
            storage[key] = value
            return Result.success(Unit)
        }

        override suspend fun retrieve(key: String): Result<String?> {
            if (shouldFail) return Result.failure(Exception("Storage failed"))
            return Result.success(storage[key])
        }

        override suspend fun delete(key: String): Result<Unit> {
            if (shouldFail) return Result.failure(Exception("Storage failed"))
            storage.remove(key)
            return Result.success(Unit)
        }

        override suspend fun contains(key: String): Result<Boolean> {
            if (shouldFail) return Result.failure(Exception("Storage failed"))
            return Result.success(storage.containsKey(key))
        }

        override suspend fun listKeys(): Result<List<String>> {
            if (shouldFail) return Result.failure(Exception("Storage failed"))
            return Result.success(storage.keys.toList())
        }

        override suspend fun clear(): Result<Unit> {
            storage.clear()
            return Result.success(Unit)
        }

        override suspend fun isSecure(): Result<Boolean> {
            return Result.success(true)
        }
    }
}
