package com.example.nusantaraview.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    // Inisialisasi ViewModel untuk Destinasi
    val destinationViewModel: DestinationViewModel = viewModel()

    // State untuk mengontrol apakah dialog tambah muncul atau tidak
    var showAddDialog by remember { mutableStateOf(false) }

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
                }
            )
        },
        floatingActionButton = {
            // Tombol FAB untuk memunculkan dialog
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Destinasi")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Panggil Screen Destinasi (Grid) di sini
            DestinationScreen(viewModel = destinationViewModel)
        }

        // Jika state true, munculkan dialog
        if (showAddDialog) {
            AddDestinationDialog(
                onDismiss = { showAddDialog = false },
                viewModel = destinationViewModel
            )
        }
    }
}