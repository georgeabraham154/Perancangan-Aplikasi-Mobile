package com.example.nusantaraview.ui.souvenir

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nusantaraview.data.model.Souvenir
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SouvenirScreen(
    viewModel: SouvenirViewModel
) {
    val souvenirList by viewModel.souvenirList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Souvenir?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Souvenir?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading && souvenirList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (souvenirList.isEmpty()) {
                Text("Belum ada data.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(souvenirList) { item ->
                        SouvenirItemCard(
                            item = item,
                            onEditClick = {
                                itemToEdit = item
                                showDialog = true
                            },
                            onDeleteClick = {
                                itemToDelete = item
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            if (showDialog) {
                AddSouvenirDialog(
                    onDismiss = { showDialog = false },
                    viewModel = viewModel,
                    souvenirToEdit = itemToEdit
                )
            }

            if (showDeleteDialog && itemToDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Hapus Barang?") },
                    // 👇 Perhatikan: itemToDelete?.itemName
                    text = { Text("Yakin ingin menghapus ${itemToDelete?.itemName}?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                itemToDelete?.let { viewModel.deleteSouvenir(it) }
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Hapus") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
                    }
                )
            }
        }
    }
}

@Composable
fun SouvenirItemCard(
    item: Souvenir,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().height(260.dp)
    ) {
        Box {
            Column {
                AsyncImage(
                    // 👇 Perhatikan: item.imageUrl
                    model = item.imageUrl ?: "https://placehold.co/600x400?text=No+Image",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(130.dp)
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    // 👇 Perhatikan: item.itemName
                    Text(item.itemName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    // 👇 Perhatikan: item.storeName
                    Text(item.storeName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.weight(1f))
                    // 👇 Perhatikan: item.price
                    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(item.price)
                    Text(format, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opsi", tint = MaterialTheme.colorScheme.surface)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { expanded = false; onEditClick() })
                    DropdownMenuItem(text = { Text("Hapus", color = MaterialTheme.colorScheme.error) }, onClick = { expanded = false; onDeleteClick() })
                }
            }
        }
    }
}