package com.example.nusantaraview.ui.souvenir

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nusantaraview.data.model.Souvenir
// Sesuaikan import ini dengan lokasi file Client kamu yang sebenarnya
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SouvenirScreen(
    viewModel: SouvenirViewModel
) {
    val souvenirList by viewModel.souvenirList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // State untuk Dialog Tambah
    var showAddDialog by remember { mutableStateOf(false) }

    // State untuk Dialog Edit (Menyimpan item yang sedang diedit)
    var itemToEdit by remember { mutableStateOf<Souvenir?>(null) }

    // Logic memunculkan Dialog Tambah
    if (showAddDialog) {
        AddSouvenirDialog(
            onDismiss = { showAddDialog = false },
            viewModel = viewModel
        )
    }

    // Logic memunculkan Dialog Edit
    itemToEdit?.let { item ->
        EditSouvenirDialog(
            souvenir = item,
            onDismiss = { itemToEdit = null }, // Tutup dialog dengan mengosongkan state
            viewModel = viewModel
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Oleh-oleh")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && souvenirList.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null && souvenirList.isEmpty() -> {
                    Text(
                        text = errorMessage ?: "Error",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                souvenirList.isEmpty() -> {
                    Text(
                        text = "Belum ada data oleh-oleh.",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(souvenirList) { item ->
                            SouvenirItemCard(
                                item = item,
                                onEditClick = { selectedItem ->
                                    // Set item ini ke state agar dialog edit muncul
                                    itemToEdit = selectedItem
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SouvenirItemCard(
    item: Souvenir,
    onEditClick: (Souvenir) -> Unit
) {
    // Ambil ID user yang login
    val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id
    val isOwner = currentUserId == item.userId

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 1. Gambar Barang
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.itemName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // 2. Judul Barang
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Nama Toko
                Text(
                    text = "Toko: ${item.storeName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                // 4. Harga (Format Rupiah)
                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                val formattedPrice = formatter.format(item.price)

                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // 5. Deskripsi
                if (!item.description.isNullOrEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 6. Tombol Edit (Hanya muncul jika isOwner = true)
                if (isOwner) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { onEditClick(item) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}