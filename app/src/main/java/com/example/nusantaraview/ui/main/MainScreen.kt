package com.example.nusantaraview.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nusantaraview.ui.destination.AddDestinationDialog
import com.example.nusantaraview.ui.destination.DestinationScreen
import com.example.nusantaraview.ui.destination.DestinationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val destinationViewModel: DestinationViewModel = viewModel()
    var showAddDialog by remember { mutableStateOf(false) }

    // State untuk mengontrol tab mana yang aktif (0-4)
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            0 -> "Destinasi Wisata"
                            1 -> "Kuliner Khas"
                            2 -> "Penginapan"
                            3 -> "Oleh-Oleh"
                            4 -> "Galeri Pengunjung"
                            else -> "NusantaraView"
                        }
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                // 1. Destinasi Wisata
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Destinasi"
                        )
                    },
                    label = { Text("Destinasi") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )

                // 2. Kuliner Khas
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Kuliner"
                        )
                    },
                    label = { Text("Kuliner") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )

                // 3. Penginapan
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Hotel,
                            contentDescription = "Penginapan"
                        )
                    },
                    label = { Text("Penginapan") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )

                // 4. Oleh-Oleh
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Oleh-Oleh"
                        )
                    },
                    label = { Text("Oleh-Oleh") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )

                // 5. Galeri Pengunjung
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = "Galeri"
                        )
                    },
                    label = { Text("Galeri") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
            }
        },
        floatingActionButton = {
            // FAB hanya muncul di tab yang relevan
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Tampilkan screen sesuai tab yang dipilih
            when (selectedTab) {
                0 -> DestinationScreen(viewModel = destinationViewModel)
                1 -> PlaceholderScreen("Kuliner Khas", "Fitur ini akan dikerjakan oleh Anggota 2")
                2 -> PlaceholderScreen("Penginapan", "Fitur ini akan dikerjakan oleh Anggota 3")
                3 -> PlaceholderScreen("Oleh-Oleh", "Fitur ini akan dikerjakan oleh Anggota 4")
                4 -> PlaceholderScreen("Galeri Pengunjung", "Fitur ini akan dikerjakan oleh Anggota 5")
            }
        }

        // Dialog tambah destinasi (hanya untuk tab Destinasi)
        if (showAddDialog && selectedTab == 0) {
            AddDestinationDialog(
                onDismiss = { showAddDialog = false },
                viewModel = destinationViewModel
            )
        }
    }
}

// Placeholder Screen untuk fitur yang belum dikerjakan
@Composable
fun PlaceholderScreen(title: String, message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}