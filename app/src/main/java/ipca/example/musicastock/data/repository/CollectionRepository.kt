package ipca.example.musicastock.data.repository

import ipca.example.musicastock.data.ResultWrapper
import ipca.example.musicastock.data.auth.TokenStore
import ipca.example.musicastock.data.remote.api.CollectionsApi
import ipca.example.musicastock.data.remote.dto.MusicCollectionDto
import ipca.example.musicastock.domain.models.Collection
import ipca.example.musicastock.domain.repository.ICollectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val api: CollectionsApi,
    private val tokenStore: TokenStore
) : ICollectionRepository {

    override fun getCurrentUserId(): String? = runBlocking {
        tokenStore.getUserId()
        }
    override fun getCurrentUserEmail(): String? = null

    override fun fetchCollections(): Flow<ResultWrapper<List<Collection>>> = flow {
        emit(ResultWrapper.Loading())
        val ownerId = getCurrentUserId()

        if (ownerId.isNullOrBlank()) {
            emit(ResultWrapper.Error("Utilizador não autenticado."))
            return@flow
        }

        try {
            val remote = api.getByOwner(ownerId).map { it.toDomain() }
            emit(ResultWrapper.Success(remote))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e.message ?: "Erro ao carregar coleções do servidor."))
        }
    }.flowOn(Dispatchers.IO)

    override fun addCollection(collection: Collection): Flow<ResultWrapper<String>> = flow {
        emit(ResultWrapper.Loading())
        val title = collection.title?.trim()
        if (title.isNullOrBlank()) {
            emit(ResultWrapper.Error("O título é obrigatório."))
            return@flow
        }
        val userId = getCurrentUserId()
        try {

            val created = api.create(
                MusicCollectionDto(
                    title = title,
                    style = collection.style,
                    ownerId = userId
                )
            )
            val saved = created.toDomain()
            emit(ResultWrapper.Success(saved.colletionId))
        } catch (e: Exception) {
            emit(ResultWrapper.Error("Erro ao criar coletânea: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun deleteCollection(collectionId: String): Flow<ResultWrapper<Unit>> = flow {
        emit(ResultWrapper.Loading())
        try {
            val res = api.delete(collectionId)
            if (res.isSuccessful) {
                emit(ResultWrapper.Success(Unit))
            } else {
                emit(ResultWrapper.Error("Erro ao apagar: ${res.code()}"))
            }
        } catch (e: Exception) {
            emit(ResultWrapper.Error("Erro de ligação: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateCollection(
        collectionId: String,
        title: String,
        style: String
    ): Flow<ResultWrapper<Unit>> = flow {
        emit(ResultWrapper.Loading())
        val titleTrim = title.trim()
        if (titleTrim.isBlank()) {
            emit(ResultWrapper.Error("O título é obrigatório."))
            return@flow
        }
        try {
            val current = api.getById(collectionId)
            val body = current.copy(title = titleTrim, style = style)
            val res = api.update(collectionId, body)
            if (res.isSuccessful) {
                emit(ResultWrapper.Success(Unit))
            } else {
                emit(ResultWrapper.Error("Erro ao atualizar: ${res.code()}"))
            }
        } catch (e: Exception) {
            emit(ResultWrapper.Error("Erro de ligação: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    private fun MusicCollectionDto.toDomain(): Collection =
        Collection(
            colletionId = this.collectionId ?: this.collectionId ?: "",
            title = this.title,
            style = this.style
        )
}