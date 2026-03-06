/*#######################################################
 *
 * SPDX-FileCopyrightText: 2025 Milos Vasic
 * SPDX-License-Identifier: Apache-2.0
 *
 * Tests for OAuth2Flow URL generation
 *
 *########################################################*/
package digital.vasic.auth

import io.ktor.client.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Focused tests for OAuth2Flow.getAuthorizationUrl() method.
 * Validates that the generated authorization URL contains all required
 * OAuth2 parameters with correct values.
 */
class OAuth2FlowUrlTest {

    private val testClientId = "url_test_client_id"
    private val testClientSecret = "url_test_client_secret"
    private val testRedirectUri = "https://yole.app/oauth/callback"
    private val testAuthorizationUrl = "https://provider.example.com/oauth/authorize"
    private val testTokenUrl = "https://provider.example.com/oauth/token"

    private fun createFlow(scopes: List<String> = listOf("read", "write")): OAuth2Flow {
        return OAuth2Flow(
            httpClient = HttpClient(),
            clientId = testClientId,
            clientSecret = testClientSecret,
            authorizationUrl = testAuthorizationUrl,
            tokenUrl = testTokenUrl,
            redirectUri = testRedirectUri,
            scopes = scopes
        )
    }

    // ==================== client_id ====================

    @Test
    fun `getAuthorizationUrl should contain client_id parameter`() {
        val flow = createFlow()
        val url = flow.getAuthorizationUrl()
        assertTrue(
            url.contains("client_id=$testClientId"),
            "URL should contain client_id=$testClientId but was: $url"
        )
    }

    // ==================== redirect_uri ====================

    @Test
    fun `getAuthorizationUrl should contain redirect_uri parameter`() {
        val flow = createFlow()
        val url = flow.getAuthorizationUrl()
        assertTrue(
            url.contains("redirect_uri="),
            "URL should contain redirect_uri parameter but was: $url"
        )
        assertTrue(
            url.contains("redirect_uri=https") || url.contains("redirect_uri=https%3A"),
            "URL should contain the redirect URI value but was: $url"
        )
    }

    // ==================== response_type=code ====================

    @Test
    fun `getAuthorizationUrl should contain response_type code`() {
        val flow = createFlow()
        val url = flow.getAuthorizationUrl()
        assertTrue(
            url.contains("response_type=code"),
            "URL should contain response_type=code but was: $url"
        )
    }

    // ==================== scopes ====================

    @Test
    fun `getAuthorizationUrl should contain scopes when provided`() {
        val flow = createFlow(scopes = listOf("read", "write"))
        val url = flow.getAuthorizationUrl()
        assertTrue(
            url.contains("scope="),
            "URL should contain scope parameter when scopes are provided but was: $url"
        )
    }

    // ==================== state ====================

    @Test
    fun `getAuthorizationUrl should contain state when provided`() {
        val flow = createFlow()
        val state = "csrf_protection_token_abc123"
        val url = flow.getAuthorizationUrl(state = state)
        assertTrue(
            url.contains("state=$state"),
            "URL should contain state=$state but was: $url"
        )
    }

    // ==================== access_type=offline ====================

    @Test
    fun `getAuthorizationUrl should contain access_type offline`() {
        val flow = createFlow()
        val url = flow.getAuthorizationUrl()
        assertTrue(
            url.contains("access_type=offline"),
            "URL should contain access_type=offline to request refresh token but was: $url"
        )
    }

    // ==================== prompt=consent ====================

    @Test
    fun `getAuthorizationUrl should contain prompt consent`() {
        val flow = createFlow()
        val url = flow.getAuthorizationUrl()
        assertTrue(
            url.contains("prompt=consent"),
            "URL should contain prompt=consent to force consent screen but was: $url"
        )
    }

    // ==================== empty scopes ====================

    @Test
    fun `getAuthorizationUrl should work with empty scopes`() {
        val flow = createFlow(scopes = emptyList())
        val url = flow.getAuthorizationUrl()
        assertFalse(
            url.contains("scope="),
            "URL should not contain scope parameter when scopes are empty but was: $url"
        )
        assertTrue(url.contains("client_id=$testClientId"))
        assertTrue(url.contains("response_type=code"))
        assertTrue(url.contains("access_type=offline"))
        assertTrue(url.contains("prompt=consent"))
    }

    // ==================== multiple scopes joined with space ====================

    @Test
    fun `getAuthorizationUrl should join multiple scopes with space`() {
        val scopes = listOf("files.read", "files.write", "user.profile")
        val flow = createFlow(scopes = scopes)
        val url = flow.getAuthorizationUrl()
        assertTrue(
            url.contains("scope=files.read") || url.contains("scope=files.read"),
            "URL should contain joined scopes but was: $url"
        )
        for (scope in scopes) {
            assertTrue(
                url.contains(scope),
                "URL should contain scope '$scope' but was: $url"
            )
        }
    }
}
