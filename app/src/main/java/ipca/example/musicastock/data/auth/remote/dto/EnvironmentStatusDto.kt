package ipca.example.musicastock.data.remote.dto

data class WeatherInfoDto(
    val provider: String? = null,
    val city: String? = null,
    val minTemperatureC: Double? = null,
    val maxTemperatureC: Double? = null,
    val precipitationProbability: Double? = null,
    val weatherTypeId: Int? = null,
    val weatherDescription: String? = null
)

data class SmartEnvironmentDto(
    val environmentId: String,
    val name: String,
    val description: String? = null,
    val city: String? = null,
    val linkedCollectionId: String? = null
)

data class SensorReadingDto(
    val readingId: Long,
    val environmentId: String,
    val timestamp: String,
    val temperature: Double? = null,
    val humidity: Double? = null,
    val light: Double? = null
)

data class EnvironmentStatusDto(
    val environment: SmartEnvironmentDto,
    val lastReading: SensorReadingDto? = null,
    val externalWeather: WeatherInfoDto? = null,
    val externalWeatherMessage: String? = null
)
