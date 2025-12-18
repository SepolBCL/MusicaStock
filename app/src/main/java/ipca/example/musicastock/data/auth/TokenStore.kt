package ipca.example.musicastock.data.auth

import kotlinx.coroutines.flow.Flow

interface TokenStore {
    val tokenFlow: Flow<String?>
    val userIdFlow: Flow<String?> // Flow para observar o ID do utilizador

    suspend fun getToken(): String?
    suspend fun saveToken(token: String)

    suspend fun getUserId(): String?       // Recuperar ID síncrono
    suspend fun saveUserId(userId: String) // Guardar ID após login

    suspend fun clearToken() // Deve limpar token e userId
}