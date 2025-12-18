package ipca.example.musicastock.data.repository

import ipca.example.musicastock.data.ResultWrapper
import ipca.example.musicastock.data.remote.api.CollectionsApi
import ipca.example.musicastock.data.remote.api.MusicApi
import ipca.example.musicastock.data.remote.dto.MusicDto
import ipca.example.musicastock.domain.models.Music
import ipca.example.musicastock.domain.repository.IMusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val musicApi: MusicApi,
    private val collectionsApi: CollectionsApi
) : IMusicRepository {

    override fun fetchAllMusics(): Flow<ResultWrapper<List<Music>>> = flow {
        emit(ResultWrapper.Loading())
        try {
            val remote = musicApi.getAll().map { it.toDomain() }
            emit(ResultWrapper.Success(remote))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e.message ?: "Erro ao carregar músicas."))
        }
    }.flowOn(Dispatchers.IO)

    override fun fetchMusicsByCollection(collectionId: String): Flow<ResultWrapper<List<Music>>> = flow {
        emit(ResultWrapper.Loading())
        try {
            val remote = collectionsApi.getMusicsForCollection(collectionId)
                .map { it.toDomain().copy(collectionId = collectionId) }
            emit(ResultWrapper.Success(remote))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e.message ?: "Erro ao carregar músicas da coletânea."))
        }
    }.flowOn(Dispatchers.IO)

    override fun fetchMusics(collectionId: String?): Flow<ResultWrapper<List<Music>>> =
        if (collectionId.isNullOrBlank()) fetchAllMusics() else fetchMusicsByCollection(collectionId)

    override suspend fun getMusicById(id: String): ResultWrapper<Music?> {
        return try {
            val dto = musicApi.getById(id)
            ResultWrapper.Success(dto.toDomain())
        } catch (e: Exception) {
            ResultWrapper.Error(e.message ?: "Erro ao carregar música.")
        }
    }

    override fun saveMusic(music: Music): Flow<ResultWrapper<Unit>> = flow {
        emit(ResultWrapper.Loading())

        val title = music.musTitle?.trim()
        if (title.isNullOrBlank()) {
            emit(ResultWrapper.Error("O título é obrigatório."))
            return@flow
        }

        try {
            if (music.musId.isBlank()) {
                val created = musicApi.create(music.toDtoForCreate())
                val newId = created.musicId

                if (newId.isNullOrBlank()) {
                    emit(ResultWrapper.Error("A API não devolveu o ID da música criada."))
                    return@flow
                }

                val colId = music.collectionId
                if (!colId.isNullOrBlank()) {
                    collectionsApi.addMusicToCollection(colId!!, newId!!)
                }

                emit(ResultWrapper.Success(Unit))
            } else {
                val res = musicApi.update(music.musId, music.toDtoForUpdate())
                if (!res.isSuccessful) {
                    emit(ResultWrapper.Error("Erro ao atualizar música (${res.code()})."))
                    return@flow
                }
                emit(ResultWrapper.Success(Unit))
            }
        } catch (e: Exception) {
            emit(ResultWrapper.Error("Falha na operação: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun removeMusicFromCollection(
        collectionId: String,
        musicId: String
    ): Flow<ResultWrapper<Unit>> = flow {
        emit(ResultWrapper.Loading())
        try {
            val res = collectionsApi.removeMusicFromCollection(collectionId, musicId)
            if (!res.isSuccessful) {
                val errorJson = res.errorBody()?.string()
                emit(ResultWrapper.Error("Erro (${res.code()}): $errorJson"))
                return@flow
            }
            emit(ResultWrapper.Success(Unit))
        } catch (e: Exception) {
            emit(ResultWrapper.Error("Erro de ligação: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    private fun MusicDto.toDomain(): Music = Music(
        musId = this.musicId ?: "",
        musTitle = this.title,
        artist = this.artist,
        album = this.album,
        audioUrl = this.audioUrl,
        releaseDate = this.releaseDate
    )

    private fun Music.toDtoForCreate(): MusicDto = MusicDto(
        musicId = null,
        title = this.musTitle!!.trim(),
        artist = this.artist,
        album = this.album,
        audioUrl = this.audioUrl,
        releaseDate = this.releaseDate
    )

    private fun Music.toDtoForUpdate(): MusicDto = MusicDto(
        musicId = this.musId,
        title = this.musTitle!!.trim(),
        artist = this.artist,
        album = this.album,
        audioUrl = this.audioUrl,
        releaseDate = this.releaseDate
    )
}