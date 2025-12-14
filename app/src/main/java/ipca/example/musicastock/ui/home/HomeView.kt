package ipca.example.musicastock.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ipca.example.musicastock.R
import ipca.example.musicastock.domain.models.Collection
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    navController: NavHostController,
    environmentId: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    // Carregar dados do ambiente
    LaunchedEffect(environmentId) {
        viewModel.load(environmentId)
    }

    // Eventos one-shot (ex.: navegar para todas as coleções)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.NavigateToAllCollections -> {
                    navController.navigate("collections")
                }
            }
        }
    }

    // Dialog de erro
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

    Box(modifier = Modifier.fillMaxSize()) {

        // Fundo com imagem
        Image(
            painter = painterResource(id = R.drawable.img_3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay escuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->

            val layoutDirection = LocalLayoutDirection.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {

                // HEADER tipo "hero", igual ao estilo de CollectionView
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_51),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )


                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botão Sair
                        TextButton(
                            onClick = {
                                navController.navigate("login") {
                                    popUpTo("collections") { inclusive = true }
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "logout",
                                    tint = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .width(4.dp)
                                )
                                Text(
                                    text = "Sair",
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }


                    // Centro: nome do ambiente + cidade
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.environmentName.ifBlank { "Ambiente não definido" },
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.city.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.city,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Linha de cartões: Exterior / Interior
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WeatherCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            tempRange = uiState.externalTempRange,
                            description = uiState.externalWeatherDescription,
                            precipitation = uiState.externalPrecipitation
                        )



                        SensorsCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            temperature = uiState.internalTemperature,
                            humidity = uiState.internalHumidity,
                            light = uiState.internalLight
                        )
                    }

                    // Secção de modos de recomendação
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SuggestionModeButton(
                                text = "Sugestões por Meteorologia",
                                selected = uiState.selectedMode == RecommendationMode.WEATHER,
                                onClick = { viewModel.selectWeatherMode() },
                                modifier = Modifier.weight(1f)
                            )
                            SuggestionModeButton(
                                text = "Sugestões por Sensores",
                                selected = uiState.selectedMode == RecommendationMode.SENSORS,
                                onClick = { viewModel.selectSensorsMode() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Bloco de sugestões de coletâneas
                    val collectionsToShow: List<Collection> = when (uiState.selectedMode) {
                        RecommendationMode.WEATHER -> uiState.weatherBasedCollections
                        RecommendationMode.SENSORS -> uiState.sensorBasedCollections
                        RecommendationMode.NONE -> emptyList()
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(24.dp) // garante que tens este import
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Coletâneas sugeridas",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            )

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    uiState.isLoading -> {
                                        CircularProgressIndicator(color = Color.White)
                                    }

                                    uiState.selectedMode == RecommendationMode.NONE -> {
                                        Text(
                                            text = "Escolha um tipo de sugestão para ver as coletâneas recomendadas.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }

                                    collectionsToShow.isEmpty() -> {
                                        Text(
                                            text = "Não foram encontradas coletâneas compatíveis com este contexto.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }

                                    else -> {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(
                                                collectionsToShow,
                                                key = { it.id }) { collection ->
                                                CollectionSuggestionRow(
                                                    collection = collection,
                                                    onClick = {
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



                    Button(
                        onClick = { viewModel.onViewAllCollectionsClicked() },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            topEnd = 28.dp,
                            bottomEnd = 28.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAF512E),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        )
                    ) {
                        Text("Todas as coletâneas")
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
                color=Color.White,
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
        shape = MaterialTheme.shapes.large
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
