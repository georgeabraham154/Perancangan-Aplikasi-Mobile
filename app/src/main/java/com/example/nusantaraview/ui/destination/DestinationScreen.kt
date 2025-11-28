package com.example.nusantaraview.ui.destination

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map // Icon Peta
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import java.text.NumberFormat
import java.util.Locale

@OptIn(InternalSerializationApi::class)
@Composable
fun DestinationScreen(
    viewModel: DestinationViewModel
) {
    val destinations by viewModel.destinations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scope = rememberCoroutineScope()
    var currentUserId by remember { mutableStateOf<String?>(null) }

    // State untuk kontrol dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var destinationToEdit by remember { mutableStateOf<com.example.nusantaraview.data.model.Destination?>(null) }

    // Ambil data saat pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchDestinations()
        scope.launch {
            currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id
        }
    }

    // PENTING: Scaffold ini yang memunculkan tombol (+)
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Wisata")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading && destinations.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (destinations.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Belum ada destinasi wisata.")
                    Text("Jadilah yang pertama posting!", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                // TAMPILAN BARU: Menggunakan LazyColumn (List ke bawah), bukan Grid
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(destinations) { destination ->
                        DestinationItemTravelStyle(
                            destination = destination,
                            currentUserId = currentUserId,
                            onEditClick = { destinationToEdit = destination }
                        )
                    }
                }
            }
        }
    }

    // Dialog Tambah
    if (showAddDialog) {
        AddDestinationDialog(
            onDismiss = { showAddDialog = false },
            viewModel = viewModel
        )
    }

    // Dialog Edit
    destinationToEdit?.let { destination ->
        EditDestinationDialog(
            destination = destination,
            onDismiss = { destinationToEdit = null },
            viewModel = viewModel
        )
    }
}

// Desain Card Baru: Gaya Travel Blog dengan Tombol Maps
@Composable
fun DestinationItemTravelStyle(
    destination: com.example.nusantaraview.data.model.Destination,
    currentUserId: String?,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val isOwner = currentUserId != null && destination.userId == currentUserId

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            // Biarkan tinggi otomatis menyesuaikan konten, tapi gambar punya tinggi tetap
            .wrapContentHeight()
    ) {
        Column {
            // 1. Gambar Besar (Header)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Gambar lebih besar
            ) {
                AsyncImage(
                    model = destination.imageUrl ?: "https://placehold.co/600x400?text=No+Image",
                    contentDescription = destination.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.BrokenImage)
                )

                // Tombol Edit (Melayang di kanan atas gambar)
                if (isOwner) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Harga Tiket (Melayang di kiri bawah gambar)
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                    .format(destination.ticketPrice)

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = formattedPrice,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 2. Informasi Detail
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nama Tempat
                    Text(
                        text = destination.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // TOMBOL MAPS (Fitur Baru)
                    IconButton(
                        onClick = {
                            // Intent untuk membuka Google Maps
                            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(destination.location)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")

                            // Cek apakah ada aplikasi maps, kalau tidak ada buka browser
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                // Fallback jika tidak ada app maps
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(destination.location)}"))
                                context.startActivity(browserIntent)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Buka Peta",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Lokasi dengan Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = destination.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Deskripsi (jika ada)
                if (!destination.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = destination.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3, // Batasi 3 baris biar rapi
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun rememberVectorPainter(image: androidx.compose.ui.graphics.vector.ImageVector) =
    androidx.compose.ui.graphics.vector.rememberVectorPainter(image)