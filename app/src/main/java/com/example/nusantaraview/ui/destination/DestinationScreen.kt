package com.example.nusantaraview.ui.destination

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun DestinationScreen(
    viewModel: DestinationViewModel
) {
    // Ambil data dari ViewModel
    val destinations by viewModel.destinations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Load data saat pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchDestinations()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && destinations.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (destinations.isEmpty()) {
            Text(
                text = "Belum ada destinasi wisata.\nTap tombol + untuk menambah.",
                modifier = Modifier.align(Alignment.Center),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp), // Responsif grid
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(destinations) { destination ->
                    DestinationItem(destination)
                }
            }
        }
    }
}

@Composable
fun DestinationItem(destination: com.example.nusantaraview.data.model.Destination) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Gambar Wisata
            AsyncImage(
                model = destination.imageUrl,
                contentDescription = destination.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(Icons.Default.BrokenImage) // Icon jika gagal load gambar
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = destination.location,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rp ${destination.price}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
// Helper function untuk painter resource vector (opsional, bisa dihapus jika error dan pakai painterResource biasa)
@Composable
fun rememberVectorPainter(image: androidx.compose.ui.graphics.vector.ImageVector) =
    androidx.compose.ui.graphics.vector.rememberVectorPainter(image)