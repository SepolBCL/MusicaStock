package ipca.example.musicastock.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ipca.example.musicastock.domain.models.Collection
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    navController: NavHostController,
    environmentId: String, // o ambiente “ligado” à app (ex.: Sala de Estudo)
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    // Carregar dados do ambiente
    LaunchedEffect(environmentId) {
        viewModel.load(environmentId)
    }

    // Ouvir eventos one-shot do ViewModel (ex.: navegação)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.NavigateToAllCollections -> {
                    // Ajusta esta route para a tua navegação real
                    navController.navigate("collections")
                }
            }
        }
    }

    // Dialog de erro simples
    uiState.error?.let { errorMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            },
            title = { Text("Erro") },
            text = { Text(errorMsg) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Jukebox – Ambiente",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF181818),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabeçalho: Ambiente + cidade
                Text(
                    text = uiState.environmentName.ifBlank { "Ambiente não definido" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (uiState.city.isNotBlank()) {
                    Text(
                        text = "Cidade: ${uiState.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Cartões com estado interno/externo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeatherCard(
                        modifier = Modifier.weight(1f),
                        tempRange = uiState.externalTempRange,
                        description = uiState.externalWeatherDescription,
                        precipitation = uiState.externalPrecipitation
                    )

                    SensorsCard(
                        modifier = Modifier.weight(1f),
                        temperature = uiState.internalTemperature,
                        humidity = uiState.internalHumidity,
                        light = uiState.internalLight
                    )
                }

                // Botões de modo de recomendação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SuggestionModeButton(
                        text = "Sugestões por Meteorologia",
                        selected = uiState.selectedMode == RecommendationMode.WEATHER,
                        onClick = { viewModel.selectWeatherMode() }
                    )
                    SuggestionModeButton(
                        text = "Sugestões por Sensores",
                        selected = uiState.selectedMode == RecommendationMode.SENSORS,
                        onClick = { viewModel.selectSensorsMode() }
                    )
                }

                // Botão para ver todas as coletâneas
                Button(
                    onClick = { viewModel.onViewAllCollectionsClicked() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAF512E),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Ver todas as coletâneas",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Lista de coletâneas sugeridas
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val collectionsToShow: List<Collection> = when (uiState.selectedMode) {
                        RecommendationMode.WEATHER -> uiState.weatherBasedCollections
                        RecommendationMode.SENSORS -> uiState.sensorBasedCollections
                        RecommendationMode.NONE -> emptyList()
                    }

                    if (uiState.selectedMode == RecommendationMode.NONE) {
                        Text(
                            text = "Escolha um tipo de sugestão para ver as coletâneas recomendadas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    } else if (collectionsToShow.isEmpty()) {
                        Text(
                            text = "Não foram encontradas coletâneas compatíveis com este contexto.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    } else {
                        Text(
                            text = "Coletâneas sugeridas:",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(collectionsToShow, key = { it.id }) { collection ->
                                CollectionSuggestionRow(
                                    collection = collection,
                                    onClick = {
                                        // Ajusta esta rota para a tua navegação real
                                        navController.navigate("collectionDetail/${collection.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherCard(
    modifier: Modifier = Modifier,
    tempRange: String,
    description: String,
    precipitation: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Exterior (IPMA)",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (tempRange.isNotBlank()) {
                Text(
                    text = "Temperatura: $tempRange",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            if (precipitation.isNotBlank()) {
                Text(
                    text = "Prob. precipitação: $precipitation",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SensorsCard(
    modifier: Modifier = Modifier,
    temperature: String?,
    humidity: String?,
    light: String?
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Interior (Sensores)",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            temperature?.let {
                Text(
                    text = "Temperatura: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            humidity?.let {
                Text(
                    text = "Humidade: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            light?.let {
                Text(
                    text = "Luz: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }

            if (temperature == null && humidity == null && light == null) {
                Text(
                    text = "Ainda não existem leituras de sensores para este ambiente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SuggestionModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Color(0xFFAF512E) else Color(0xFF303030)
    val fg = if (selected) Color.White else Color.White.copy(alpha = 0.9f)

    Surface(
        modifier = modifier.clickable { onClick() },
        color = bg,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CollectionSuggestionRow(
    collection: Collection,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = collection.title ?: "(Sem título)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            collection.style?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFAF512E)
                )
            }
        }
    }
}
