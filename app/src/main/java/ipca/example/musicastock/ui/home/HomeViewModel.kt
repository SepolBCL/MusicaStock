package ipca.example.musicastock.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipca.example.musicastock.data.ResultWrapper
import ipca.example.musicastock.data.remote.api.EnvironmentsApi
import ipca.example.musicastock.data.remote.dto.EnvironmentStatusDto
import ipca.example.musicastock.domain.models.Collection
import ipca.example.musicastock.domain.repository.ICollectionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

enum class RecommendationMode {
    NONE, WEATHER, SENSORS
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val environmentName: String = "",
    val city: String = "",
    val externalWeatherDescription: String = "",
    val externalTempRange: String = "",
    val externalPrecipitation: String = "",

    val internalTemperature: String? = null,
    val internalHumidity: String? = null,
    val internalLight: String? = null,

    val selectedMode: RecommendationMode = RecommendationMode.NONE,
    val weatherBasedCollections: List<Collection> = emptyList(),
    val sensorBasedCollections: List<Collection> = emptyList()
)

/**
 * Eventos one-shot da Home (para navegação, etc.).
 */
sealed class HomeEvent {
    object NavigateToAllCollections : HomeEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val environmentsApi: EnvironmentsApi,
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    /**
     * Carrega o estado inicial da Home:
     * - estado do ambiente (meteo + sensores)
     * - lista de coletâneas disponíveis
     */
    fun load(environmentId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            try {
                // 1) Obter estado do ambiente (IPMA + sensores)
                val status: EnvironmentStatusDto =
                    environmentsApi.getEnvironmentStatus(environmentId)

                // 2) Obter coleções (podes trocar para get by owner, se quiseres)
                val collectionsResult = collectionRepository.fetchCollections().first()

                // Forçar tipo não-nulo para evitar problemas de inferência
                val collections: List<Collection> = when (collectionsResult) {
                    is ResultWrapper.Success -> collectionsResult.data ?: emptyList()
                    is ResultWrapper.Error -> emptyList()
                    is ResultWrapper.Loading -> emptyList()
                }

                // 3) Calcular sugestões
                val weatherBased = suggestByWeather(status, collections)
                val sensorBased = suggestBySensors(status, collections)

                uiState = uiState.copy(
                    isLoading = false,
                    error = null,

                    environmentName = status.environment.name,
                    city = status.environment.city.orEmpty(),

                    externalWeatherDescription =
                        status.externalWeather?.weatherDescription.orEmpty(),

                    externalTempRange = status.externalWeather?.let { weather ->
                        val min = weather.minTemperatureC
                        val max = weather.maxTemperatureC
                        when {
                            min != null && max != null -> "${min.roundToInt()}ºC - ${max.roundToInt()}ºC"
                            min != null -> "${min.roundToInt()}ºC"
                            max != null -> "${max.roundToInt()}ºC"
                            else -> ""
                        }
                    } ?: "",

                    externalPrecipitation = status.externalWeather
                        ?.precipitationProbability
                        ?.let { "${it.roundToInt()}%" }
                        ?: "",

                    internalTemperature = status.lastReading?.temperature?.let { "$it ºC" },
                    internalHumidity = status.lastReading?.humidity?.let { "$it %" },
                    internalLight = status.lastReading?.light?.let { "$it" },

                    weatherBasedCollections = weatherBased,
                    sensorBasedCollections = sensorBased
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Erro ao carregar o estado do ambiente. Verifique a ligação e tente novamente."
                )
            }
        }
    }

    fun selectWeatherMode() {
        uiState = uiState.copy(selectedMode = RecommendationMode.WEATHER)
    }

    fun selectSensorsMode() {
        uiState = uiState.copy(selectedMode = RecommendationMode.SENSORS)
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    /**
     * Chamado quando o utilizador carrega no botão
     * "Ver todas as coletâneas" na Home.
     * A composable observa [events] e faz a navegação.
     */
    fun onViewAllCollectionsClicked() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToAllCollections)
        }
    }

    // --------- Lógica de sugestões ---------

    private fun suggestByWeather(
        status: EnvironmentStatusDto,
        collections: List<Collection>
    ): List<Collection> {
        val temp = status.externalWeather?.maxTemperatureC
        val description = status.externalWeather?.weatherDescription?.lowercase() ?: ""

        return when {
            // sem meteo → devolve tudo
            temp == null -> collections

            // frio → playlists calmas / estudo
            temp < 15 ->
                collections.filter {
                    it.style?.contains("chill", true) == true ||
                            it.style?.contains("study", true) == true
                }

            // temperatura média → relax / focus
            temp in 15.0..24.9 ->
                collections.filter {
                    it.style?.contains("relax", true) == true ||
                            it.style?.contains("focus", true) == true
                }

            // quente → party / dance
            else ->
                collections.filter {
                    it.style?.contains("party", true) == true ||
                            it.style?.contains("dance", true) == true
                }
        }.ifEmpty { collections } // fallback se nenhum estilo corresponde
    }

    private fun suggestBySensors(
        status: EnvironmentStatusDto,
        collections: List<Collection>
    ): List<Collection> {
        val reading = status.lastReading

        val temp = reading?.temperature
        val light = reading?.light

        return when {
            temp == null && light == null -> collections

            // sala mais escura → playlists calmas
            light != null && light < 200 ->
                collections.filter {
                    it.style?.contains("chill", true) == true ||
                            it.style?.contains("acoustic", true) == true
                }

            // luz média e temperatura confortável → foco / estudo
            temp != null && temp in 20.0..24.0 &&
                    light != null && light in 200.0..600.0 ->
                collections.filter {
                    it.style?.contains("focus", true) == true ||
                            it.style?.contains("study", true) == true
                }

            // luz alta ou temperatura alta → party / energia
            temp != null && temp > 25 || (light != null && light > 600) ->
                collections.filter {
                    it.style?.contains("party", true) == true ||
                            it.style?.contains("rock", true) == true
                }

            else -> collections
        }.ifEmpty { collections }
    }
}
