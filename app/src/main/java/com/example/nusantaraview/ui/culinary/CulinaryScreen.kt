package com.example.nusantaraview.ui.culinary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun CulinaryScreen(
    viewModel: CulinaryViewModel
) {
    val culinaryList by viewModel.culinaryList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // === LIST DATA ===
        if (culinaryList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(culinaryList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            AsyncImage(
                                model = item.fotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(item.namaMakanan, style = MaterialTheme.typography.titleMedium)
                                Text(item.namaWarung)
                                Text("Rp ${item.harga}")
                                if (item.isRecommended) {
                                    Text("⭐ Wajib Coba", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // === LOADING STATE ===
        if (isLoading && culinaryList.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // === ERROR STATE ===
        if (errorMessage != null && culinaryList.isEmpty()) {
            Text(
                text = errorMessage ?: "Terjadi kesalahan",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // === FAB ADD ITEM ===
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("+")
        }

        // === DIALOG TAMBAH KULINER ===
        if (showAddDialog) {
            AddCulinaryDialog(
                onDismiss = { showAddDialog = false },
                viewModel = viewModel
            )
        }
    }
}
