package ipca.example.musicastock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ipca.example.musicastock.data.auth.TokenStore
import ipca.example.musicastock.ui.collection.CollectionCreateView
import ipca.example.musicastock.ui.collection.CollectionDetailView
import ipca.example.musicastock.ui.collection.CollectionEditView
import ipca.example.musicastock.ui.collection.CollectionView
import ipca.example.musicastock.ui.home.HomeView
import ipca.example.musicastock.ui.login.LoginView
import ipca.example.musicastock.ui.musics.AllMusicsView
import ipca.example.musicastock.ui.musics.MusicDetailView
import ipca.example.musicastock.ui.theme.MusicastockTheme
import javax.inject.Inject


// Ambiente "Crossfit Box" (Porto)
private const val DEFAULT_ENVIRONMENT_ID = "d32082ce-f5d5-4ec6-aa5a-5801e52e0204"


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            // Estado para a rota inicial (login ou home)
            var startDestination by remember { mutableStateOf<String?>(null) }

            // Ler o token de forma assíncrona (DataStore)
            LaunchedEffect(Unit) {
                val token = tokenStore.getToken()
                startDestination = if (token.isNullOrBlank()) "login" else "home"
            }

            MusicastockTheme {
                // Enquanto ainda não sabemos a rota inicial, mostra loading
                if (startDestination == null) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Surface(
                            modifier = Modifier.padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = startDestination!!
                            ) {
                                // ------------- LOGIN -------------
                                composable("login") {
                                    LoginView(
                                        onLoginSuccess = {
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                // ------------- HOME -------------
                                composable("home") {
                                    HomeView(
                                        navController = navController,
                                        environmentId = DEFAULT_ENVIRONMENT_ID
                                    )
                                }

                                // ------------- COLEÇÕES -------------
                                composable("collections") {
                                    CollectionView(navController)
                                }

                                composable(
                                    route = "collectionDetail/{collectionId}",
                                    arguments = listOf(
                                        navArgument("collectionId") { type = NavType.StringType }
                                    )
                                ) { entry ->
                                    val id = entry.arguments?.getString("collectionId")
                                        ?: return@composable

                                    CollectionDetailView(
                                        navController = navController,
                                        collectionId = id
                                    )
                                }

                                composable("collectionCreate") {
                                    CollectionCreateView(navController)
                                }

                                composable(
                                    route = "collectionEdit/{collectionId}",
                                    arguments = listOf(
                                        navArgument("collectionId") { type = NavType.StringType }
                                    )
                                ) { entry ->
                                    val id = entry.arguments?.getString("collectionId")
                                        ?: return@composable

                                    CollectionEditView(
                                        navController = navController,
                                        collectionId = id
                                    )
                                }

                                // ------------- MÚSICAS -------------
                                composable("allMusics") {
                                    AllMusicsView(navController = navController)
                                }

                                composable(
                                    route = "musicDetail/{collectionId}",
                                    arguments = listOf(
                                        navArgument("collectionId") { type = NavType.StringType }
                                    )
                                ) { entry ->
                                    val colId = entry.arguments?.getString("collectionId")
                                        ?: return@composable

                                    MusicDetailView(
                                        navController = navController,
                                        collectionId = colId,
                                        musicId = null
                                    )
                                }

                                composable(
                                    route = "musicDetail/{collectionId}/{musicId}",
                                    arguments = listOf(
                                        navArgument("collectionId") { type = NavType.StringType },
                                        navArgument("musicId") { type = NavType.StringType }
                                    )
                                ) { entry ->
                                    val colId = entry.arguments?.getString("collectionId")
                                        ?: return@composable

                                    val musId = entry.arguments?.getString("musicId")
                                        ?: return@composable

                                    MusicDetailView(
                                        navController = navController,
                                        collectionId = colId,
                                        musicId = musId
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
