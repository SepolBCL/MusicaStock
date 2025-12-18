package ipca.example.musicastock.data.repository

import ipca.example.musicastock.data.ResultWrapper
import ipca.example.musicastock.data.auth.TokenStore
import ipca.example.musicastock.data.auth.dto.ForgotPasswordRequest
import ipca.example.musicastock.data.auth.dto.ResetPasswordRequest
import ipca.example.musicastock.data.remote.api.AuthApi
import ipca.example.musicastock.data.remote.dto.LoginRequestDto
import ipca.example.musicastock.data.remote.dto.RegisterRequestDto
import ipca.example.musicastock.domain.repository.ILoginRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) : ILoginRepository {

    override fun login(email: String, password: String): Flow<ResultWrapper<Unit>> = flow {
        emit(ResultWrapper.Loading())

        try {
            val response = api.login(LoginRequestDto(email, password))

            if (!response.isSuccessful) {
                emit(ResultWrapper.Error("Login inválido (${response.code()})."))
                return@flow
            }

            val authBody = response.body()
            val token = authBody?.token
            val userId = authBody?.userId // Captura o userId do DTO

            if (token.isNullOrBlank()) {
                emit(ResultWrapper.Error("Login efetuado, mas a API não devolveu token."))
                return@flow
            }

            // Guarda as credenciais no TokenStore
            tokenStore.saveToken(token)
            if (!userId.isNullOrBlank()) {
                tokenStore.saveUserId(userId) // Persiste o ID do utilizador
            }

            emit(ResultWrapper.Success(Unit))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e.message ?: "Erro desconhecido no login"))
        }
    }.flowOn(Dispatchers.IO)

    override fun register(email: String, password: String): Flow<ResultWrapper<Unit>> = flow {
        emit(ResultWrapper.Loading())

        try {
            val response = api.register(RegisterRequestDto(email, password))

            if (!response.isSuccessful) {
                emit(ResultWrapper.Error("Registo inválido (${response.code()})."))
                return@flow
            }

            val authBody = response.body()
            val token = authBody?.token
            val userId = authBody?.userId // Captura o userId também no registo

            // Se a API devolver credenciais no registo, guarda-as imediatamente
            if (!token.isNullOrBlank()) {
                tokenStore.saveToken(token)
                if (!userId.isNullOrBlank()) {
                    tokenStore.saveUserId(userId)
                }
            }

            emit(ResultWrapper.Success(Unit))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e.message ?: "Erro ao registar"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun forgotPassword(email: String): ResultWrapper<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) ResultWrapper.Success(Unit)
                else ResultWrapper.Error("Erro ao pedir recuperação de password. Código: ${response.code()}")
            } catch (e: Exception) {
                ResultWrapper.Error("Falha na comunicação com o servidor: ${e.message}")
            }
        }

    override suspend fun resetPassword(
        token: String,
        newPassword: String
    ): ResultWrapper<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = ResetPasswordRequest(
                token = token,
                newPassword = newPassword
            )
            val response = api.resetPassword(request)

            if (response.isSuccessful) {
                ResultWrapper.Success(Unit)
            } else {
                ResultWrapper.Error(
                    "Erro ao atualizar a password. Código: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ResultWrapper.Error("Falha na comunicação com o servidor: ${e.message}")
        }
    }
}