package com.example.nusantaraview.ui.accommodation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.nusantaraview.data.model.Accommodation
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import java.text.NumberFormat
import java.util.Locale

@OptIn(InternalSerializationApi::class)
@Composable
fun AccommodationScreen(
    viewModel: AccommodationViewModel = viewModel()
) {
    // State dari ViewModel
    val accommodations by viewModel.accommodations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State untuk dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedAccommodation by remember { mutableStateOf<Accommodation?>(null) }

    // State untuk current user ID
    val scope = rememberCoroutineScope()
    var currentUserId by remember { mutableStateOf<String?>(null) }

    // Fetch data saat pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchAccommodations()
        scope.launch {
            currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id
        }
    }

    // Main Layout dengan Scaffold
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Penginapan")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // State 1: Loading dan list kosong
            if (isLoading && accommodations.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // State 2: List kosong setelah loading
            else if (accommodations.isEmpty()) {
                Text(
                    text = "Belum ada penginapan.\nTap tombol + untuk menambah.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            // State 3: Tampilkan list
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = accommodations,
                        key = { it.id ?: it.name } // Key untuk optimasi recomposition
                    ) { accommodation ->
                        AccommodationItem(
                            accommodation = accommodation,
                            currentUserId = currentUserId,
                            onEditClick = {
                                selectedAccommodation = accommodation
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog Add
    if (showAddDialog) {
        AddAccommodationDialog(
            onDismiss = { showAddDialog = false },
            viewModel = viewModel
        )
    }

    // Dialog Edit
    if (showEditDialog && selectedAccommodation != null) {
        EditAccommodationDialog(
            accommodation = selectedAccommodation!!,
            onDismiss = {
                showEditDialog = false
                selectedAccommodation = null
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun AccommodationItem(
    accommodation: Accommodation,
    currentUserId: String?,
    onEditClick: () -> Unit
) {
    // Cek apakah user adalah pemilik
    val isOwner = currentUserId != null && accommodation.userId == currentUserId

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Image Section dengan Edit Button
            Box {
                AsyncImage(
                    model = accommodation.imageUrl,
                    contentDescription = accommodation.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.BrokenImage)
                )

                // Edit Button - hanya tampil untuk owner
                if (isOwner) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Penginapan",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Content Section
            Column(modifier = Modifier.padding(12.dp)) {
                // Nama Penginapan
                Text(
                    text = accommodation.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Fasilitas
                Text(
                    text = accommodation.facilities,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Harga
                val formattedPrice = NumberFormat
                    .getNumberInstance(Locale("id", "ID"))
                    .format(accommodation.pricePerNight)

                Text(
                    text = "Rp $formattedPrice / malam",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Label Owner
                if (isOwner) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Penginapan milik Anda",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Helper function untuk error image
@Composable
fun rememberVectorPainter(image: androidx.compose.ui.graphics.vector.ImageVector) =
    androidx.compose.ui.graphics.vector.rememberVectorPainter(image)