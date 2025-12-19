package ipca.example.musicastock.ui.musics

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import ipca.example.musicastock.R
import ipca.example.musicastock.domain.models.Music

@Composable
fun MusicViewCell(
    music: Music,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showInfo by remember { mutableStateOf(false) }

    fun openUrl(url: String?) {
        val safe = url?.trim()
        if (!safe.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safe))
            runCatching { context.startActivity(intent) }
        }
    }

    val hasAudio = !music.audioUrl.isNullOrBlank()
    val hasTab = !music.tabUrl.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
                if (hasAudio) openUrl(music.audioUrl) else showInfo = true
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (hasAudio) {
                IconButton(onClick = { openUrl(music.audioUrl) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.spotify),
                        contentDescription = "Ouvir música",
                        tint = Color(0xFFAF512E)
                    )
                }
            } else {
                // placeholder para manter alinhamento quando não há áudio
                Spacer(modifier = Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = music.musTitle?.takeIf { it.isNotBlank() } ?: "Sem título",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = music.artist?.takeIf { it.isNotBlank() } ?: "Artista desconhecido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            if (hasTab) {
                IconButton(onClick = { openUrl(music.tabUrl) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_guitar),
                        contentDescription = "Abrir tablatura",
                        tint = Color(0xFFAF512E)
                    )
                }
            }

            IconButton(onClick = { showInfo = true }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Detalhes da música",
                    tint = Color(0xFFAF512E)
                )
            }
        }
    }

    if (showInfo) {
        MusicInfoDialog(
            music = music,
            onDismiss = { showInfo = false },
            onDelete = onDelete
        )
    }
}

@Composable
fun MusicInfoDialog(
    music: Music,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
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

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Info",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoRow(label = "Título", value = music.musTitle)
                InfoRow(label = "Artista", value = music.artist)
                InfoRow(label = "Álbum", value = music.album)
                InfoRow(label = "Data de lançamento", value = music.releaseDate)

                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(label = "Estilo", value = music.musStyle)

                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(label = "Link áudio", value = music.audioUrl)
                InfoRow(label = "Link tablatura", value = music.tabUrl)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Fechar",
                    color = Color(0xFFAF512E),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDelete()
                onDismiss()
            }) {
                Text(
                    text = "Eliminar",
                    color = Color.Red,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String?
) {
    val safeValue = value?.trim().takeIf { !it.isNullOrBlank() } ?: "-"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = safeValue,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
