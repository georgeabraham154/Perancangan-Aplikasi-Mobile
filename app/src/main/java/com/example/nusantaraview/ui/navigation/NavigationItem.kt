package com.example.nusantaraview.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavigationItem("destination", "Wisata", Icons.Default.Home)
    object Culinary : NavigationItem("culinary", "Kuliner", Icons.Default.Restaurant)
    object Gallery : NavigationItem("gallery", "Galeri", Icons.Default.PhotoLibrary)
    object Souvenir : NavigationItem("souvenir", "Oleh-Oleh", Icons.Default.ShoppingBag)
}