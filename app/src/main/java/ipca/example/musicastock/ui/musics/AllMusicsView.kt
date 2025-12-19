package ipca.example.musicastock.ui.musics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ipca.example.musicastock.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMusicsView(
    navController: NavHostController,
    viewModel: MusicViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    var searchText by rememberSaveable { mutableStateOf("") }

    // Efeito para mostrar Toast sempre que houver uma nova mensagem de erro
    LaunchedEffect(uiState.error) { //
        uiState.error?.let { errorMsg ->
            android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show() //
        }
    }

    val filteredMusics = remember(uiState.musics, searchText) {
        val q = searchText.trim()
        if (q.isBlank()) uiState.musics
        else uiState.musics.filter { music ->
            val title = music.musTitle.orEmpty()
            val artist = music.artist.orEmpty()
            title.contains(q, ignoreCase = true) || artist.contains(q, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAllMusics()
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

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_5),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )

                    CenterAlignedTopAppBar(
                        title = {
                            val total = uiState.musics.size
                            val filtered = filteredMusics.size

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "As Minhas Músicas",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Filtradas: $filtered / Total: $total",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color(0xFFAF512E),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // Navega para o ecrã de detalhes com ID vazio para criar nova música
                        navController.navigate("musicDetail/")
                    },
                    containerColor = Color(0xFFAF512E),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar Música"
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    uiState.error != null && uiState.musics.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Erro ao carregar músicas",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    uiState.musics.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhuma música encontrada.", color = Color.White)
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.error != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Black.copy(alpha = 0.35f)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                }
                            }

                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Procurar",
                                        tint = Color.White.copy(alpha = 0.8f)
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = "Procurar por título ou artista...",
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                    focusedIndicatorColor = Color(0xFFAF512E),
                                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                                )
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = filteredMusics,
                                    key = { it.musId ?: "${it.musTitle}-${it.artist}" }
                                ) { music ->
                                    MusicViewCell(
                                        music = music,
                                        onDelete = { viewModel.deleteMusic(music.musId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}