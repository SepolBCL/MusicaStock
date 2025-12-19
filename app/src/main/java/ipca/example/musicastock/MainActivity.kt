package ipca.example.musicastock

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
import ipca.example.musicastock.ui.login.ResetPasswordView
import ipca.example.musicastock.ui.musics.AllMusicsView
import ipca.example.musicastock.ui.musics.MusicDetailView
import ipca.example.musicastock.ui.theme.MusicastockTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            // 1. Definimos o destino inicial fixo como "login"
            // Removemos a verificação do tokenStore aqui para forçar a paragem no login
            val startDestination = "login"

            MusicastockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {

                            // ------------- LOGIN -------------
                            composable("login") {
                                LoginView(
                                    onLoginSuccess = {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onForgotPasswordNavigate = {
                                        navController.navigate("resetPassword")
                                    }
                                )
                            }

                            // ------------- HOME -------------
                            composable("home") {
                                HomeView(
                                    navController = navController
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

                            // ------------- RESET PASSWORD -------------
                            composable(
                                route = "resetPassword?token={token}",
                                arguments = listOf(
                                    navArgument("token") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    }
                                )
                            ) { entry ->
                                val encodedToken = entry.arguments?.getString("token") ?: ""
                                val token = if (encodedToken.isBlank()) "" else Uri.decode(encodedToken)

                                ResetPasswordView(
                                    token = token,
                                    onPasswordResetSuccess = {
                                        navController.navigate("login") {
                                            popUpTo("login") { inclusive = true }
                                        }
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