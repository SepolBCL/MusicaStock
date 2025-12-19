package ipca.example.musicastock.domain.repository

import ipca.example.musicastock.data.ResultWrapper
import ipca.example.musicastock.domain.models.Music
import kotlinx.coroutines.flow.Flow

interface IMusicRepository {
    fun fetchAllMusics(): Flow<ResultWrapper<List<Music>>>
    fun fetchMusicsByCollection(collectionId: String): Flow<ResultWrapper<List<Music>>>
    fun fetchMusics(collectionId: String? = null): Flow<ResultWrapper<List<Music>>>
    suspend fun getMusicById(id: String): ResultWrapper<Music?>
    fun saveMusic(music: Music): Flow<ResultWrapper<Unit>>

    fun removeMusicFromCollection(collectionId: String, musicId: String): Flow<ResultWrapper<Unit>>
    fun deleteMusic(musicId: String): Flow<ResultWrapper<Unit>>
}

