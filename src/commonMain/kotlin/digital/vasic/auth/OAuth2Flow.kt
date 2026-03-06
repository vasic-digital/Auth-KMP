package digital.vasic.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * OAuth2 flow implementation for cloud storage services
 */
open class OAuth2Flow(
    private val httpClient: HttpClient,
    private val clientId: String,
    private val clientSecret: String,
    private val authorizationUrl: String,
    private val tokenUrl: String,
    private val redirectUri: String,
    private val scopes: List<String> = emptyList()
) {

    /**
     * Generate authorization URL for OAuth2 flow
     */
    fun getAuthorizationUrl(state: String? = null): String {
        val urlBuilder = URLBuilder(authorizationUrl)
        urlBuilder.parameters.apply {
            append("client_id", clientId)
            append("redirect_uri", redirectUri)
            append("response_type", "code")
            if (scopes.isNotEmpty()) {
                append("scope", scopes.joinToString(" "))
            }
            state?.let { append("state", it) }
            append("access_type", "offline") // Request refresh token
            append("prompt", "consent") // Force consent to ensure refresh token
        }
        return urlBuilder.buildString()
    }

    /**
     * Exchange authorization code for access token
     */
    suspend fun exchangeCodeForToken(authorizationCode: String): Result<TokenResponse> {
        return try {
            val response = httpClient.post {
                url(tokenUrl)
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    Parameters.build {
                        append("grant_type", "authorization_code")
                        append("code", authorizationCode)
                        append("redirect_uri", redirectUri)
                        append("client_id", clientId)
                        append("client_secret", clientSecret)
                    }.formUrlEncode()
                )
            }

            if (response.status.isSuccess()) {
                val tokenResponse = response.body<TokenResponse>()
                Result.success(tokenResponse)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(
                    Exception("Token exchange failed: ${response.status} - $errorBody")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refresh access token using refresh token
     */
    suspend fun refreshAccessToken(refreshToken: String): Result<TokenResponse> {
        return try {
            val response = httpClient.post {
                url(tokenUrl)
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    Parameters.build {
                        append("grant_type", "refresh_token")
                        append("refresh_token", refreshToken)
                        append("client_id", clientId)
                        append("client_secret", clientSecret)
                    }.formUrlEncode()
                )
            }

            if (response.status.isSuccess()) {
                val tokenResponse = response.body<TokenResponse>()
                Result.success(tokenResponse)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(
                    Exception("Token refresh failed: ${response.status} - $errorBody")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Revoke access token
     */
    suspend fun revokeToken(token: String, revokeUrl: String? = null): Result<Unit> {
        return try {
            val url = revokeUrl ?: "$tokenUrl/revoke"
            val response = httpClient.post {
                url(url)
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    Parameters.build {
                        append("token", token)
                        append("client_id", clientId)
                        append("client_secret", clientSecret)
                    }.formUrlEncode()
                )
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(
                    Exception("Token revocation failed: ${response.status} - $errorBody")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * OAuth2 token response
 */
@Serializable
data class TokenResponse(
    val access_token: String,
    val token_type: String = "Bearer",
    val expires_in: Long? = null,
    val refresh_token: String? = null,
    val scope: String? = null
) {
    /**
     * Check if response contains a refresh token
     */
    fun hasRefreshToken(): Boolean = !refresh_token.isNullOrBlank()

    /**
     * Get token expiration time in seconds from now
     */
    fun getExpiresInSeconds(): Long = expires_in ?: 3600 // Default to 1 hour
}

/**
 * Dropbox-specific OAuth2 implementation
 */
class DropboxOAuth2Flow(
    httpClient: HttpClient,
    clientId: String,
    clientSecret: String,
    redirectUri: String
) : OAuth2Flow(
    httpClient = httpClient,
    clientId = clientId,
    clientSecret = clientSecret,
    authorizationUrl = "https://www.dropbox.com/oauth2/authorize",
    tokenUrl = "https://api.dropboxapi.com/oauth2/token",
    redirectUri = redirectUri,
    scopes = listOf("files.content.write", "files.content.read", "files.metadata.read")
)

/**
 * Google Drive-specific OAuth2 implementation
 */
class GoogleDriveOAuth2Flow(
    httpClient: HttpClient,
    clientId: String,
    clientSecret: String,
    redirectUri: String
) : OAuth2Flow(
    httpClient = httpClient,
    clientId = clientId,
    clientSecret = clientSecret,
    authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth",
    tokenUrl = "https://oauth2.googleapis.com/token",
    redirectUri = redirectUri,
    scopes = listOf(
        "https://www.googleapis.com/auth/drive.file",
        "https://www.googleapis.com/auth/drive.readonly"
    )
)

/**
 * OneDrive-specific OAuth2 implementation
 */
class OneDriveOAuth2Flow(
    httpClient: HttpClient,
    clientId: String,
    clientSecret: String,
    redirectUri: String
) : OAuth2Flow(
    httpClient = httpClient,
    clientId = clientId,
    clientSecret = clientSecret,
    authorizationUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
    tokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token",
    redirectUri = redirectUri,
    scopes = listOf("Files.ReadWrite", "Files.ReadWrite.All", "offline_access")
)
