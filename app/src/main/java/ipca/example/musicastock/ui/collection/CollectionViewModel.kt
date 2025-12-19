package ipca.example.musicastock.ui.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipca.example.musicastock.data.ResultWrapper
import ipca.example.musicastock.domain.models.Collection
import ipca.example.musicastock.domain.repository.ICollectionRepository
import kotlinx.coroutines.channels.Channel //
import kotlinx.coroutines.flow.receiveAsFlow //
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionState(
    val collections: List<Collection> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false,
    val userEmail: String = "Jukebox API"
)

// Definição de eventos de navegação para o ecrã
sealed class CollectionNavEvent {
    object NavigateBack : CollectionNavEvent()
}

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    var uiState by mutableStateOf(CollectionState())
        private set

    // Canal para eventos de navegação (disparados uma única vez)
    private val _navigationEvent = Channel<CollectionNavEvent>() //
    val navigationEvent = _navigationEvent.receiveAsFlow() //

    fun fetchCollections() {
        viewModelScope.launch {
            collectionRepository.fetchCollections().collect { result ->
                when (result) {
                    is ResultWrapper.Loading -> {
                        uiState = uiState.copy(isLoading = true, error = null)
                    }

                    is ResultWrapper.Success -> {
                        uiState = uiState.copy(
                            collections = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }

                    is ResultWrapper.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao carregar coleções."
                        )
                    }
                }
            }
        }
    }

    fun addCollection(title: String, style: String, onSuccess: (String) -> Unit) {
        val titleTrim = title.trim()
        if (titleTrim.isBlank()) {
            uiState = uiState.copy(error = "O título é obrigatório.")
            return
        }
        val currentUserId = collectionRepository.getCurrentUserId()
        val baseCollection = Collection(
            title = titleTrim,
            style = style.trim().ifBlank { null },
            ownerId = currentUserId
        )

        viewModelScope.launch {
            collectionRepository.addCollection(baseCollection).collect { result ->
                when (result) {
                    is ResultWrapper.Loading -> {
                        uiState = uiState.copy(isLoading = true, error = null)
                    }

                    is ResultWrapper.Success -> {
                        val newId = result.data
                        if (!newId.isNullOrBlank()) {
                            uiState = uiState.copy(
                                collections = uiState.collections + baseCollection.copy(colletionId = newId),
                                isLoading = false,
                                error = null
                            )
                            onSuccess(newId)
                        }
                    }

                    is ResultWrapper.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao guardar coletânea."
                        )
                    }
                }
            }
        }
    }

    fun deleteCollection(id: String) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(id).collect { result ->
                when (result) {
                    is ResultWrapper.Loading -> {
                        uiState = uiState.copy(isLoading = true, error = null)
                    }

                    is ResultWrapper.Success -> {
                        uiState = uiState.copy(
                            collections = uiState.collections.filterNot { it.colletionId == id },
                            isLoading = false,
                            error = null
                        )
                        // Dispara o evento de navegação apenas em caso de sucesso
                        _navigationEvent.send(CollectionNavEvent.NavigateBack) //
                    }

                    is ResultWrapper.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao apagar coletânea."
                        )
                    }
                }
            }
        }
    }

    fun updateCollection(id: String, title: String, style: String) {
        val titleTrim = title.trim()
        if (titleTrim.isBlank()) {
            uiState = uiState.copy(error = "O título é obrigatório.")
            return
        }

        viewModelScope.launch {
            collectionRepository.updateCollection(id, titleTrim, style).collect { result ->
                when (result) {
                    is ResultWrapper.Loading -> {
                        uiState = uiState.copy(isLoading = true, error = null)
                    }

                    is ResultWrapper.Success -> {
                        val updatedList = uiState.collections.map { c ->
                            if (c.colletionId == id) c.copy(title = titleTrim, style = style.ifBlank { null })
                            else c
                        }
                        uiState = uiState.copy(
                            collections = updatedList,
                            isLoading = false,
                            error = null
                        )
                        // Dispara o evento de navegação apenas em caso de sucesso
                        _navigationEvent.send(CollectionNavEvent.NavigateBack) //
                    }

                    is ResultWrapper.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao atualizar coletânea."
                        )
                    }
                }
            }
        }
    }
}