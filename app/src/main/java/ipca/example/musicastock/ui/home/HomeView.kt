package ipca.example.musicastock.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ipca.example.musicastock.R
import ipca.example.musicastock.domain.models.Collection
import kotlinx.coroutines.flow.collectLatest

private val AccentOrange = Color(0xFFAF512E)
private val GlassBg = Color.Black.copy(alpha = 0.28f)
private val CardBg = Color(0xFF141414)
private val MenuBg = Color(0xFF1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    navController: NavHostController,
    environmentId: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    var envMenuExpanded by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newCity by remember { mutableStateOf("") }
    var newLinkedCollectionId by remember { mutableStateOf("") }


    LaunchedEffect(Unit) { viewModel.loadEnvironments(preselectId = environmentId) }

    LaunchedEffect(uiState.isLoading, uiState.selectedMode) {
        if (!uiState.isLoading && uiState.selectedMode == RecommendationMode.NONE) {
            viewModel.selectWeatherMode()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                HomeEvent.NavigateToAllCollections -> navController.navigate("collections")
            }
        }
    }

    uiState.error?.let { errorMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } },
            title = { Text("Erro") },
            text = { Text(errorMsg) }
        )
    }

    if (showCreateDialog) {
        CreateEnvironmentDialog(
            name = newName,
            description = newDescription,
            city = newCity,
            linkedCollectionId = newLinkedCollectionId,
            onNameChange = { newName = it },
            onDescriptionChange = { newDescription = it },
            onCityChange = { newCity = it },
            onLinkedCollectionIdChange = { newLinkedCollectionId = it },
            onDismiss = { showCreateDialog = false },
            onCreate = {
                viewModel.createEnvironment(
                    name = newName,
                    description = newDescription,
                    city = newCity,
                    linkedCollectionId = newLinkedCollectionId.ifBlank { null }
                )
                showCreateDialog = false
                newName = ""
                newDescription = ""
                newCity = ""
                newLinkedCollectionId = ""
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.img_3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Scaffold(containerColor = Color.Transparent) { innerPadding ->

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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
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
                            .background(Color.Black.copy(alpha = 0.30f))
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Sair", color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }

                    // ===== Header: Cidade (em cima e maior) + Dropdown estilizado =====
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val selected = uiState.environments.firstOrNull {
                            it.environmentId == uiState.selectedEnvironmentId
                        }
                        val cityToShow = (selected?.city ?: uiState.city).orEmpty()

                        Text(
                            text = "Jukebox Smart Environments",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFAF512E),
                            fontWeight = FontWeight.Bold
                        )

                        if (cityToShow.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cityToShow,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }


                        // “Glass” container do dropdown
                        Surface(
                            color = GlassBg,
                            shape = RoundedCornerShape(22.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                ExposedDropdownMenuBox(
                                    expanded = envMenuExpanded,
                                    onExpandedChange = { envMenuExpanded = !envMenuExpanded }
                                ) {
                                    val envTitle =
                                        selected?.name
                                            ?: uiState.environmentName
                                                .ifBlank { "Selecionar ambiente" }

                                    OutlinedTextField(
                                        value = envTitle,
                                        onValueChange = {},
                                        readOnly = true,
                                        singleLine = true,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        trailingIcon = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = AccentOrange
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                // ✅ botão "+" dentro do campo
                                                Surface(
                                                    color = Color.Transparent,
                                                    modifier = Modifier

                                                        .size(30.dp)
                                                        .clickable { showCreateDialog = true }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = "Adicionar ambiente",
                                                            tint = Color(0xFFAF512E),

                                                        modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = AccentOrange,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                            focusedContainerColor = CardBg.copy(alpha = 0.55f),
                                            unfocusedContainerColor = CardBg.copy(alpha = 0.55f),
                                            cursorColor = AccentOrange
                                        )
                                    )

                                    ExposedDropdownMenu(
                                        expanded = envMenuExpanded,
                                        onDismissRequest = { envMenuExpanded = false },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(MenuBg)
                                    ) {
                                        uiState.environments.forEach { env ->
                                            val isSelected = env.environmentId == uiState.selectedEnvironmentId

                                            DropdownMenuItem(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (isSelected) AccentOrange.copy(alpha = 0.14f)
                                                        else Color.Transparent
                                                    ),
                                                text = {
                                                    Column {
                                                        Text(
                                                            text = env.name,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        if (!env.city.isNullOrBlank()) {
                                                            Text(
                                                                text = env.city!!,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = AccentOrange
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    envMenuExpanded = false
                                                    viewModel.selectEnvironment(env.environmentId)
                                                }
                                            )
                                        }
                                    }
                                }

                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WeatherCard(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            tempRange = uiState.externalTempRange,
                            description = uiState.externalWeatherDescription,
                            precipitation = uiState.externalPrecipitation,
                            selected = uiState.selectedMode == RecommendationMode.WEATHER,
                            onSelect = { viewModel.selectWeatherMode() }
                        )

                        SensorsCard(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            temperature = uiState.internalTemperature,
                            humidity = uiState.internalHumidity,
                            light = uiState.internalLight,
                            selected = uiState.selectedMode == RecommendationMode.SENSORS,
                            onSelect = { viewModel.selectSensorsMode() }
                        )
                    }

                    val collectionsToShow: List<Collection> = when (uiState.selectedMode) {
                        RecommendationMode.WEATHER -> uiState.weatherBasedCollections
                        RecommendationMode.SENSORS -> uiState.sensorBasedCollections
                        RecommendationMode.NONE -> emptyList()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Coletâneas sugeridas",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )

                            Divider(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            )

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    uiState.isLoading -> CircularProgressIndicator(color = Color.White)

                                    collectionsToShow.isEmpty() -> Text(
                                        text = "Não foram encontradas coletâneas compatíveis com este contexto.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )

                                    else -> LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(items = collectionsToShow, key = { it.colletionId }) { collection ->
                                            CollectionSuggestionRow(
                                                collection = collection,
                                                onClick = { navController.navigate("collectionDetail/${collection.colletionId}") }
                                            )
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
private fun CreateEnvironmentDialog(
    name: String,
    description: String,
    city: String,
    linkedCollectionId: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onLinkedCollectionIdChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Novo Ambiente",
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Preencher os dados do ambiente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f)
                )

                StyledField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Nome",
                    singleLine = true
                )

                StyledField(
                    value = city,
                    onValueChange = onCityChange,
                    label = "Cidade",
                    singleLine = true
                )

                StyledField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = "Descrição (opcional)",
                    singleLine = false
                )

                StyledField(
                    value = linkedCollectionId,
                    onValueChange = onLinkedCollectionIdChange,
                    label = "LinkedCollectionId (opcional)",
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.White.copy(alpha = 0.85f))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onCreate,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Criar")
                    }
                }
            }
        }
    }
}

@Composable
private fun StyledField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = AccentOrange,
            unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
            focusedContainerColor = Color.Black.copy(alpha = 0.22f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.22f),
            focusedLabelColor = AccentOrange,
            unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
            cursorColor = AccentOrange
        )
    )
}

@Composable
private fun WeatherCard(
    modifier: Modifier = Modifier,
    tempRange: String,
    description: String,
    precipitation: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Exterior",
                style = MaterialTheme.typography.titleSmall,
                color = AccentOrange,
                fontWeight = FontWeight.Bold
            )

            if (tempRange.isNotBlank()) {
                Text("Temperatura: $tempRange", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            }
            if (description.isNotBlank()) {
                Text(description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
            }
            if (precipitation.isNotBlank()) {
                Text("Prob. precipitação: $precipitation", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }

            Spacer(modifier = Modifier.weight(1f))

            SuggestionModeButton(
                text = "Sugestões",
                selected = selected,
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SensorsCard(
    modifier: Modifier = Modifier,
    temperature: String?,
    humidity: String?,
    light: String?,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Interior",
                style = MaterialTheme.typography.titleSmall,
                color = AccentOrange,
                fontWeight = FontWeight.Bold
            )

            temperature?.let { Text("Temperatura: $it", style = MaterialTheme.typography.bodyMedium, color = Color.White) }
            humidity?.let { Text("Humidade: $it", style = MaterialTheme.typography.bodyMedium, color = Color.White) }
            light?.let { Text("Luz: $it", style = MaterialTheme.typography.bodyMedium, color = Color.White) }

            if (temperature == null && humidity == null && light == null) {
                Text(
                    text = "Ainda não existem leituras de sensores para este ambiente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            SuggestionModeButton(
                text = "Sugestões",
                selected = selected,
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth()
            )
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
    val bg = if (selected) AccentOrange else Color(0xFF303030)
    val fg = if (selected) Color.White else Color.White.copy(alpha = 0.9f)

    Surface(
        modifier = modifier.clickable { onClick() },
        color = bg,
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = collection.title ?: "(Sem título)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            collection.style?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = AccentOrange)
            }
        }
    }
}
