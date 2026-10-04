package com.pemmob.mbkmybacaankomik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pemmob.mbkmybacaankomik.ui.screen.DetailScreen
import com.pemmob.mbkmybacaankomik.ui.screen.HomeScreen
import com.pemmob.mbkmybacaankomik.ui.screen.ReadScreen
import com.pemmob.mbkmybacaankomik.ui.theme.MBKMyBacaanKomikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MBKMyBacaanKomikTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController  = navController,
                        startDestination = "home"
                    ) {
                        // ─── Home Screen ───────────────────────────────────
                        composable(route = "home") {
                            HomeScreen(navController = navController)
                        }

                        // ─── Detail Screen (dengan slug parameter) ─────────
                        composable(
                            route     = "detail/{komikSlug}",
                            arguments = listOf(
                                navArgument("komikSlug") {
                                    type         = NavType.StringType
                                    defaultValue = "legenda-garuda-putih"
                                }
                            )
                        ) { backStackEntry ->
                            val slug = backStackEntry.arguments?.getString("komikSlug")
                                ?: "legenda-garuda-putih"
                            DetailScreen(
                                navController = navController,
                                komikSlug     = slug
                            )
                        }

                        // ─── Fallback route tanpa slug ─────────────────────
                        composable(route = "detail") {
                            DetailScreen(navController = navController)
                        }

                        // ─── Read Screen (dengan parameter komikSlug & chapterSlug) ─────────
                        composable(
                            route     = "read/{komikSlug}/{chapterSlug}",
                            arguments = listOf(
                                navArgument("komikSlug") {
                                    type         = NavType.StringType
                                    defaultValue = "legenda-garuda-putih"
                                },
                                navArgument("chapterSlug") {
                                    type         = NavType.StringType
                                    defaultValue = "bab-1"
                                }
                            )
                        ) { backStackEntry ->
                            val komikSlug = backStackEntry.arguments?.getString("komikSlug")
                                ?: "legenda-garuda-putih"
                            val chapterSlug = backStackEntry.arguments?.getString("chapterSlug")
                                ?: "bab-1"
                            ReadScreen(
                                navController = navController,
                                komikSlug     = komikSlug,
                                chapterSlug   = chapterSlug
                            )
                        }

                        // ─── Read Screen (dengan parameter komikSlug saja) ─
                        composable(
                            route     = "read/{komikSlug}",
                            arguments = listOf(
                                navArgument("komikSlug") {
                                    type         = NavType.StringType
                                    defaultValue = "legenda-garuda-putih"
                                }
                            )
                        ) { backStackEntry ->
                            val komikSlug = backStackEntry.arguments?.getString("komikSlug")
                                ?: "legenda-garuda-putih"
                            ReadScreen(
                                navController = navController,
                                komikSlug     = komikSlug,
                                chapterSlug   = "bab-1"
                            )
                        }

                        // ─── Fallback route read tanpa slug ────────────────
                        composable(route = "read") {
                            ReadScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}