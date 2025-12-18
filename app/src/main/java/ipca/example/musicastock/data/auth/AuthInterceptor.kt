package ipca.example.musicastock.data.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // 1. Lista de rotas que não precisam de token
        val publicEndpoints = listOf(
            "api/auth/login",
            "api/auth/register",
            "api/Auth/forgot-password",
            "api/Auth/reset-password"
        )

        // 2. Se a rota for pública, prossegue sem adicionar o cabeçalho
        if (publicEndpoints.any { path.contains(it, ignoreCase = true) }) {
            return chain.proceed(request)
        }

        // 3. Obtém o token e cria o pedido autenticado
        val token = runBlocking { tokenStore.getToken() }
        val authenticatedRequest = request.newBuilder().apply {
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
        }.build()

        // 4. Executa o pedido
        val response = chain.proceed(authenticatedRequest)

        // 5. Se a API responder 401, limpa o token armazenado
        if (response.code == 401) {
            runBlocking {
                tokenStore.clearToken()
            }
            // Opcional: Aqui pode disparar um evento para a UI redirecionar para o Login
        }

        return response
    }
}