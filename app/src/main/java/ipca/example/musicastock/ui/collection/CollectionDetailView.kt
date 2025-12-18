package ipca.example.musicastock.ui.collection

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ipca.example.musicastock.domain.models.Music
import ipca.example.musicastock.ui.musics.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailView(
    navController: NavHostController,
    collectionId: String,
    collectionViewModel: CollectionViewModel = hiltViewModel(),
    musicViewModel: MusicViewModel = hiltViewModel()
) {
    val collectionUi = collectionViewModel.uiState
    val musicUi = musicViewModel.uiState
    val collection = collectionUi.collections.find { it.colletionId == collectionId }

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(collectionId) {
        if (collectionUi.collections.isEmpty()) {
            collectionViewModel.fetchCollections()
        }
        musicViewModel.fetchMusicsByCollection(collectionId)
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
                        painter = painterResource(id = R.drawable.img_51),
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
                            Column {
                                Text(
                                    text = collection?.title ?: "Coletânea",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!collection?.style.isNullOrBlank()) {
                                    Text(
                                        text = collection?.style ?: "",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
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
                        actions = {
                            if (collection != null) {
                                IconButton(
                                    onClick = {
                                        navController.navigate("collectionEdit/$collectionId")
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { showDeleteDialog = true }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Apagar",
                                        tint = Color.Red
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = { navController.navigate("musicDetail/$collectionId") },
                        containerColor = Color(0xFFAF512E)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar Música",
                            tint = Color.White
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        musicUi.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }

                        collection == null -> EmptyCollectionScreen()

                        musicUi.musics.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sem músicas nesta coletânea.", color = Color.White)
                            }
                        }

                        else -> {
                            CollectionMusicList(
                                navController = navController,
                                collectionId = collectionId,
                                musics = musicUi.musics,
                                musicViewModel = musicViewModel
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteDialog && collection != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Apagar Coletânea") },
                text = {
                    Text(
                        "Tens a certeza que queres apagar  coletânea \"" +
                                (collection.title ?: "sem título") +
                                "\"?\nEsta ação é irreversível."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        collection.colletionId?.let { id ->
                            collectionViewModel.deleteCollection(id)
                            navController.popBackStack()
                        }
                        showDeleteDialog = false
                    }) {
                        Text("Apagar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyCollectionScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Nenhuma coletânea selecionada.", color = Color.White)
    }
}

@Composable
fun CollectionMusicList(
    navController: NavHostController,
    collectionId: String,
    musics: List<Music>,
    musicViewModel: MusicViewModel
) {
    var musicToDelete by remember { mutableStateOf<Music?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(musics) { music ->
                MusicCard(
                    music = music,
                    onEdit = {
                        val id = music.musId ?: return@MusicCard
                        navController.navigate("musicDetail/$collectionId/$id")
                    },
                    onDelete = {
                        musicToDelete = music
                    }
                )
            }
        }

        if (musicToDelete != null) {
            AlertDialog(
                onDismissRequest = { musicToDelete = null },
                title = { Text("Remover Música da Coletânea") },
                text = {
                    Text(
                        "Tens a certeza que queres remover a música \"" +
                                (musicToDelete?.musTitle ?: "sem título") +
                                "\" desta coletânea?"
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val id = musicToDelete?.musId
                        if (id != null) {
                            musicViewModel.removeMusicFromCollection(collectionId, id)
                        }
                        musicToDelete = null
                    }) {
                        Text("Remover", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { musicToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

    }
}

@Composable
fun MusicCard(
    music: Music,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    fun openAudio() {
        val url = music.audioUrl
        if (!url.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {


            if (!music.audioUrl.isNullOrBlank()) {
                IconButton(
                    onClick = { openAudio() }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.spotify),
                        contentDescription = "Ouvir música",
                        tint = Color(0xFFAF512E)
                    )
                }
            }

            val titleColumnModifier =
                if (!music.audioUrl.isNullOrBlank()) {
                    Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .clickable { openAudio() }
                } else {
                    Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                }

            Column(
                modifier = titleColumnModifier
            ) {
                Text(
                    text = music.musTitle ?: "Sem título",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Text(
                    text = music.artist ?: "Desconhecido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFAF512E)
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Apagar",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
