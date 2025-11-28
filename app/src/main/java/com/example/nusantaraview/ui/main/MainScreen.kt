package com.example.nusantaraview.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nusantaraview.ui.culinary.CulinaryScreen
import com.example.nusantaraview.ui.destination.DestinationScreen
import com.example.nusantaraview.ui.gallery.GalleryScreen
import com.example.nusantaraview.ui.souvenir.SouvenirScreen
import com.example.nusantaraview.ui.accommodation.AccommodationScreen // Pastikan import ini ada
import com.example.nusantaraview.ui.navigation.NavigationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()

    // Daftar menu navigasi bawah
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Culinary,
        NavigationItem.Accommodation,
        NavigationItem.Souvenir,
        NavigationItem.Gallery
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NusantaraView") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
        // HAPUS floatingActionButton DARI SINI. Pindahkan ke masing-masing Screen.
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = NavigationItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavigationItem.Home.route) {
                DestinationScreen(viewModel = viewModel())
            }
            composable(NavigationItem.Culinary.route) {
                CulinaryScreen(viewModel = viewModel())
            }
            composable(NavigationItem.Accommodation.route) {
                AccommodationScreen(viewModel = viewModel())
            }
            composable(NavigationItem.Souvenir.route) {
                SouvenirScreen(viewModel = viewModel())
            }
            composable(NavigationItem.Gallery.route) {
                GalleryScreen()
            }
        }
    }
}