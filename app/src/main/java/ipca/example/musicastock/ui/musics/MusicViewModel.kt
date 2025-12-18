package ipca.example.musicastock.ui.musics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipca.example.musicastock.data.ResultWrapper
import ipca.example.musicastock.domain.models.Music
import ipca.example.musicastock.domain.repository.IMusicRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MusicState(
    val musics: List<Music> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMusic: Music? = null
)

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicRepository: IMusicRepository
) : ViewModel() {

    var uiState by mutableStateOf(MusicState())
        private set

    fun fetchAllMusics() {
        viewModelScope.launch {
            musicRepository.fetchAllMusics().collect { result ->
                when (result) {
                    is ResultWrapper.Loading -> {
                        uiState = uiState.copy(isLoading = true, error = null)
                    }

                    is ResultWrapper.Success -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            musics = result.data ?: emptyList(),
                            error = null
                        )
                    }

                    is ResultWrapper.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao carregar músicas."
                        )
                    }
                }
            }
        }
    }

    fun fetchMusicsByCollection(collectionId: String) {
        viewModelScope.launch {
            musicRepository.fetchMusicsByCollection(collectionId).collect { result ->
                when (result) {
                    is ResultWrapper.Loading -> {
                        uiState = uiState.copy(isLoading = true, error = null)
                    }

                    is ResultWrapper.Success -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            musics = result.data ?: emptyList(),
                            error = null
                        )
                    }

                    is ResultWrapper.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao carregar músicas da coletânea."
                        )
                    }
                }
            }
        }
    }

    fun loadMusicById(musicId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, selectedMusic = null)

            when (val result = musicRepository.getMusicById(musicId)) {
                is ResultWrapper.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        selectedMusic = result.data,
                        error = null
                    )
                }

                is ResultWrapper.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        selectedMusic = null,
                        error = result.message ?: "Erro ao carregar música."
                    )
                }

                is ResultWrapper.Loading -> {
                    uiState = uiState.copy(isLoading = true, error = null)
                }
            }
        }
    }

    fun saveMusic(music: Music, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            musicRepository.saveMusic(music).collect { result ->
                when (result) {
                    is ResultWrapper.Loading -> {
                        uiState = uiState.copy(isLoading = true, error = null)
                    }

                    is ResultWrapper.Success -> {
                        uiState = uiState.copy(isLoading = false, error = null)

                        val colId = music.collectionId
                        if (!colId.isNullOrBlank()) fetchMusicsByCollection(colId) else fetchAllMusics()

                        onSuccess()
                    }

                    is ResultWrapper.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao guardar música."
                        )
                    }
                }
            }
        }
    }

    fun removeMusicFromCollection(collectionId: String, musicId: String) {
        viewModelScope.launch {
            musicRepository.removeMusicFromCollection(collectionId, musicId).collect { result ->
                when (result) {
                    is ResultWrapper.Loading -> {
                        uiState = uiState.copy(isLoading = true, error = null)
                    }

                    is ResultWrapper.Success -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = null,
                            musics = uiState.musics.filterNot { it.musId == musicId }
                        )

                        fetchMusicsByCollection(collectionId)
                    }

                    is ResultWrapper.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao remover música da coletânea."
                        )
                    }
                }
            }
        }
    }
}