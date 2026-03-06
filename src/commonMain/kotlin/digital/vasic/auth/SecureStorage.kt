package digital.vasic.auth

/**
 * Common interface for secure storage implementations across platforms.
 * Provides secure storage of sensitive data like passwords, tokens, and keys.
 */
interface SecureStorage {

    /**
     * Store a value with the given key securely.
     * @param key The key to store the value under
     * @param value The value to store
     * @return Result indicating success or failure
     */
    suspend fun store(key: String, value: String): Result<Unit>

    /**
     * Retrieve a value stored with the given key.
     * @param key The key to retrieve the value for
     * @return Result containing the value or null if not found
     */
    suspend fun retrieve(key: String): Result<String?>

    /**
     * Delete a value stored with the given key.
     * @param key The key to delete
     * @return Result indicating success or failure
     */
    suspend fun delete(key: String): Result<Unit>

    /**
     * Check if a key exists in storage.
     * @param key The key to check
     * @return Result containing true if the key exists, false otherwise
     */
    suspend fun contains(key: String): Result<Boolean>

    /**
     * List all keys in storage.
     * @return Result containing list of all keys
     */
    suspend fun listKeys(): Result<List<String>>

    /**
     * Clear all stored values.
     * @return Result indicating success or failure
     */
    suspend fun clear(): Result<Unit>

    /**
     * Check if the storage is properly secured and functional.
     * @return Result containing true if secure, false otherwise
     */
    suspend fun isSecure(): Result<Boolean>

    /**
     * Store sensitive credentials securely.
     * @param service The service name (e.g., "webdav", "sftp")
     * @param username The username
     * @param password The password or token
     * @return Result indicating success or failure
     */
    suspend fun storeCredentials(service: String, username: String, password: String): Result<Unit> {
        val credentialData = "${username.length}:$username$password"
        return store("${service}_credentials", credentialData)
    }

    /**
     * Retrieve stored credentials.
     * @param service The service name
     * @return Result containing username and password pair, or null if not found
     */
    suspend fun retrieveCredentials(service: String): Result<Pair<String, String>?> {
        return retrieve("${service}_credentials").map { credentialData ->
            credentialData?.let {
                val colonIndex = it.indexOf(':')
                if (colonIndex >= 0) {
                    try {
                        val usernameLength = it.substring(0, colonIndex).toInt()
                        val username = it.substring(colonIndex + 1, colonIndex + 1 + usernameLength)
                        val password = it.substring(colonIndex + 1 + usernameLength)
                        Pair(username, password)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }
        }
    }

    /**
     * Delete stored credentials.
     * @param service The service name
     * @return Result indicating success or failure
     */
    suspend fun deleteCredentials(service: String): Result<Unit> {
        return delete("${service}_credentials")
    }

    /**
     * Store an authentication token securely.
     * @param service The service name
     * @param token The authentication token
     * @return Result indicating success or failure
     */
    suspend fun storeToken(service: String, token: String): Result<Unit> {
        return store("${service}_token", token)
    }

    /**
     * Retrieve a stored authentication token.
     * @param service The service name
     * @return Result containing the token or null if not found
     */
    suspend fun retrieveToken(service: String): Result<String?> {
        return retrieve("${service}_token")
    }

    /**
     * Delete a stored authentication token.
     * @param service The service name
     * @return Result indicating success or failure
     */
    suspend fun deleteToken(service: String): Result<Unit> {
        return delete("${service}_token")
    }

    /**
     * Store a private key securely.
     * @param service The service name
     * @param privateKey The private key content
     * @return Result indicating success or failure
     */
    suspend fun storePrivateKey(service: String, privateKey: String): Result<Unit> {
        return store("${service}_private_key", privateKey)
    }

    /**
     * Retrieve a stored private key.
     * @param service The service name
     * @return Result containing the private key or null if not found
     */
    suspend fun retrievePrivateKey(service: String): Result<String?> {
        return retrieve("${service}_private_key")
    }

    /**
     * Delete a stored private key.
     * @param service The service name
     * @return Result indicating success or failure
     */
    suspend fun deletePrivateKey(service: String): Result<Unit> {
        return delete("${service}_private_key")
    }
}
