package ipca.example.musicastock.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import ipca.example.musicastock.data.ResultWrapper
import ipca.example.musicastock.data.auth.remote.EnvironmentsApi
import ipca.example.musicastock.data.remote.dto.CreateEnvironmentRequestDto
import ipca.example.musicastock.data.remote.dto.EnvironmentDto
import ipca.example.musicastock.data.remote.dto.EnvironmentStatusDto
import ipca.example.musicastock.domain.models.Collection
import ipca.example.musicastock.domain.repository.ICollectionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Collections.list
import javax.inject.Inject
import kotlin.math.roundToInt

enum class RecommendationMode { NONE, WEATHER, SENSORS }

data class EnvironmentOption(
    val environmentId: String,
    val name: String,
    val city: String?
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // ✅ dropdown
    val environments: List<EnvironmentOption> = emptyList(),
    val selectedEnvironmentId: String = "",

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

sealed class HomeEvent {
    object NavigateToAllCollections : HomeEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val environmentsApi: EnvironmentsApi,
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    private val gson = Gson()

    var uiState by mutableStateOf(HomeUiState())
        private set

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    fun loadEnvironments(preselectId: String? = null) {
        viewModelScope.launch {
            try {
                val res = environmentsApi.getEnvironmentsRaw()
                if (!res.isSuccessful) {
                    uiState = uiState.copy(error = "Erro ao obter ambientes (${res.code()}).")
                    return@launch
                }

                val raw = res.body()?.string().orEmpty()

                val type = object : TypeToken<List<EnvironmentDto>>() {}.type

                val list: List<EnvironmentDto> = runCatching {
                    gson.fromJson<List<EnvironmentDto>>(raw, type)
                }.getOrElse { emptyList() }

                val opts: List<EnvironmentOption> = list.map { env ->
                    EnvironmentOption(
                        environmentId = env.environmentId,
                        name = env.name,
                        city = env.city
                    )
                }


                val selected = preselectId
                    ?: uiState.selectedEnvironmentId.takeIf { it.isNotBlank() }
                    ?: opts.firstOrNull()?.environmentId
                    ?: ""

                uiState = uiState.copy(
                    environments = opts,
                    selectedEnvironmentId = selected
                )
            } catch (_: Exception) {
                uiState = uiState.copy(error = "Erro ao carregar ambientes. Verifique a ligação.")
            }
        }
    }

    fun selectEnvironment(environmentId: String) {
        if (environmentId.isBlank()) return
        if (environmentId == uiState.selectedEnvironmentId) return
        load(environmentId)
    }

    fun createEnvironment(
        name: String,
        description: String,
        city: String,
        linkedCollectionId: String?
    ) {
        val nameTrim = name.trim()
        val cityTrim = city.trim()

        if (nameTrim.isBlank()) {
            uiState = uiState.copy(error = "O nome do ambiente é obrigatório.")
            return
        }
        if (cityTrim.isBlank()) {
            uiState = uiState.copy(error = "A cidade é obrigatória.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val body = CreateEnvironmentRequestDto(
                    name = nameTrim,
                    description = description.trim().ifBlank { null },
                    city = cityTrim,
                    linkedCollectionId = linkedCollectionId?.trim()?.ifBlank { null }
                )

                val res = environmentsApi.createEnvironmentRaw(body)
                if (!res.isSuccessful) {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = "Erro ao criar ambiente (${res.code()})."
                    )
                    return@launch
                }

                val raw = res.body()?.string().orEmpty()
                val created: EnvironmentDto? = runCatching {
                    gson.fromJson(raw, EnvironmentDto::class.java)
                }.getOrNull()


                val newId = created?.environmentId
                if (newId.isNullOrBlank()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = "Ambiente criado, mas sem ID válido devolvido pelo servidor."
                    )
                    return@launch
                }

                loadEnvironments(preselectId = newId)
                load(newId)

            } catch (_: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Erro ao criar o ambiente. Verifique a ligação e tente novamente."
                )
            }
        }
    }

    fun load(environmentId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                error = null,
                selectedEnvironmentId = environmentId
            )

            loadEnvironments(preselectId = environmentId)

            try {
                val status: EnvironmentStatusDto =
                    environmentsApi.getEnvironmentStatus(environmentId)

                val collectionsResult = collectionRepository
                    .fetchCollections()
                    .first { it !is ResultWrapper.Loading }

                val collections: List<Collection> = when (collectionsResult) {
                    is ResultWrapper.Success -> collectionsResult.data ?: emptyList()
                    is ResultWrapper.Error -> emptyList()
                    is ResultWrapper.Loading -> emptyList()
                }

                val weatherBased = suggestByWeather(status, collections)
                val sensorBased = suggestBySensors(status, collections)

                uiState = uiState.copy(
                    isLoading = false,
                    error = null,

                    environmentName = status.environment.name,
                    city = status.environment.city.orEmpty(),

                    externalWeatherDescription = status.externalWeather?.weatherDescription.orEmpty(),
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
                    externalPrecipitation = status.externalWeather?.precipitationProbability
                        ?.let { "${it.roundToInt()}%" } ?: "",

                    internalTemperature = status.lastReading?.temperature?.let { "$it ºC" },
                    internalHumidity = status.lastReading?.humidity?.let { "$it %" },
                    internalLight = status.lastReading?.light?.let { "$it" },

                    weatherBasedCollections = weatherBased,
                    sensorBasedCollections = sensorBased
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Erro ao carregar o estado do ambiente. Verifique a ligação e tente novamente."
                )
            }
        }
    }

    fun selectWeatherMode() { uiState = uiState.copy(selectedMode = RecommendationMode.WEATHER) }
    fun selectSensorsMode() { uiState = uiState.copy(selectedMode = RecommendationMode.SENSORS) }
    fun clearError() { uiState = uiState.copy(error = null) }

    fun onViewAllCollectionsClicked() {
        viewModelScope.launch { _events.emit(HomeEvent.NavigateToAllCollections) }
    }

    // --------- Lógica de sugestões (mantida) ---------

    private enum class Mood { CALM, FOCUS, PARTY, TRAINING, SUMMER, OUTDOOR }

    private fun moodsForCollection(c: Collection): Set<Mood> {
        val style = c.style.orEmpty().trim().lowercase()
        val title = c.title.orEmpty().trim().lowercase()
        val moods = mutableSetOf<Mood>()

        when {
            style == "acoustic" -> moods += setOf(Mood.CALM, Mood.OUTDOOR)
            style.contains("lo-fi") || style.contains("lofi") -> moods += setOf(Mood.FOCUS, Mood.CALM)
            style == "soundtrack" -> moods += setOf(Mood.FOCUS, Mood.CALM)
            style == "classical" -> moods += setOf(Mood.FOCUS, Mood.CALM)
            style.contains("rock") -> moods += setOf(Mood.PARTY, Mood.TRAINING)
            style == "reggae" -> moods += setOf(Mood.SUMMER, Mood.OUTDOOR, Mood.CALM)
            style == "pop" -> moods += setOf(Mood.PARTY, Mood.SUMMER)
            style == "latino" -> moods += setOf(Mood.PARTY, Mood.SUMMER)
            style == "pimba" -> moods += setOf(Mood.PARTY)
            style == "electronic" -> moods += setOf(Mood.TRAINING, Mood.PARTY)
            style == "hip-hop" || style == "hip hop" -> moods += setOf(Mood.TRAINING, Mood.PARTY)
            style == "metal" -> moods += setOf(Mood.TRAINING, Mood.PARTY)
        }

        if (title.contains("study") || title.contains("focus") || title.contains("coding")) moods += Mood.FOCUS
        if (title.contains("chill") || title.contains("relax") || title.contains("sunset")) moods += Mood.CALM
        if (title.contains("running") || title.contains("lift") || title.contains("treino")) moods += Mood.TRAINING
        if (title.contains("summer") || title.contains("sunny") || title.contains("heat")) moods += Mood.SUMMER
        if (title.contains("festa") || title.contains("aldeia")) moods += Mood.PARTY

        return moods
    }

    private fun moodsForEnvironmentName(name: String): Set<Mood> {
        val n = name.lowercase()
        val moods = mutableSetOf<Mood>()
        if (n.contains("crossfit") || n.contains("ginásio") || n.contains("ginasio") || n.contains("pista") || n.contains("atlet")) moods += Mood.TRAINING
        if (n.contains("biblioteca") || n.contains("office") || n.contains("escrit")) moods += Mood.FOCUS
        if (n.contains("praia") || n.contains("parque") || n.contains("passeio") || n.contains("miradouro")) moods += setOf(Mood.OUTDOOR, Mood.SUMMER)
        if (n.contains("churrasco") || n.contains("jantar") || n.contains("bar")) moods += Mood.PARTY
        return moods
    }

    private fun pickRecommendations(
        collections: List<Collection>,
        linkedCollectionId: String?,
        preferred: List<Mood>,
        envMoods: Set<Mood>,
        limit: Int = 5
    ): List<Collection> {
        val linked = linkedCollectionId?.let { id -> collections.firstOrNull { it.colletionId == id } }

        val scored = collections.map { c ->
            val moods = moodsForCollection(c)
            var score = 0
            preferred.forEachIndexed { idx, m -> if (moods.contains(m)) score += (12 - idx * 2).coerceAtLeast(2) }
            envMoods.forEach { m -> if (moods.contains(m)) score += 5 }
            if (linked != null && c.colletionId == linked.colletionId) score += 1000
            score to c
        }.sortedByDescending { it.first }.map { it.second }

        val result = mutableListOf<Collection>()
        if (linked != null) result.add(linked)
        for (c in scored) {
            if (result.none { it.colletionId == c.colletionId }) result.add(c)
            if (result.size >= limit) break
        }
        return result.ifEmpty { collections.take(limit) }
    }

    private fun suggestByWeather(status: EnvironmentStatusDto, collections: List<Collection>): List<Collection> {
        val envMoods = moodsForEnvironmentName(status.environment.name)
        val linkedId = status.environment.linkedCollectionId
        val max = status.externalWeather?.maxTemperatureC
        val precip = status.externalWeather?.precipitationProbability
        val desc = status.externalWeather?.weatherDescription?.lowercase().orEmpty()

        val rainy = (precip != null && precip >= 60) || desc.contains("chuva") || desc.contains("rain") || desc.contains("aguace")

        val preferred = when {
            rainy -> listOf(Mood.FOCUS, Mood.CALM)
            max != null && max <= 14.0 -> listOf(Mood.CALM, Mood.FOCUS)
            max != null && max >= 26.0 -> listOf(Mood.SUMMER, Mood.PARTY, Mood.OUTDOOR, Mood.TRAINING)
            else -> listOf(Mood.OUTDOOR, Mood.CALM, Mood.FOCUS, Mood.PARTY)
        }

        return pickRecommendations(collections, linkedId, preferred, envMoods)
    }

    private fun suggestBySensors(status: EnvironmentStatusDto, collections: List<Collection>): List<Collection> {
        val envMoods = moodsForEnvironmentName(status.environment.name)
        val linkedId = status.environment.linkedCollectionId
        val r = status.lastReading
        val temp = r?.temperature
        val hum = r?.humidity
        val light = r?.light

        val dark = light != null && light < 200
        val bright = light != null && light > 600
        val warm = temp != null && temp >= 25
        val comfortable = temp != null && temp in 19.0..24.0
        val veryHumid = hum != null && hum >= 70

        val preferred = when {
            dark -> listOf(Mood.CALM, Mood.FOCUS)
            comfortable && light != null && light in 200.0..600.0 -> listOf(Mood.FOCUS, Mood.CALM)
            warm || bright || veryHumid -> listOf(Mood.TRAINING, Mood.PARTY, Mood.SUMMER)
            else -> listOf(Mood.CALM, Mood.FOCUS, Mood.PARTY)
        }

        return pickRecommendations(collections, linkedId, preferred, envMoods)
    }
}
