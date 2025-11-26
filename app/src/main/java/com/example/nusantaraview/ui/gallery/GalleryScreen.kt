package com.example.nusantaraview.ui.gallery

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nusantaraview.data.model.UserGallery

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = viewModel() // Inject ViewModel
) {
    val uiState = viewModel.galleryUiState
    var showUploadDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload Foto", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (uiState) {
                is GalleryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is GalleryUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is GalleryUiState.Success -> {
                    if (uiState.photos.isEmpty()) {
                        Text("Belum ada foto. Jadilah yang pertama!", Modifier.align(Alignment.Center))
                    } else {
                        GalleryGrid(photos = uiState.photos)
                    }
                }
            }
        }
    }

    // Tampilkan Dialog jika state true
    if (showUploadDialog) {
        UploadGalleryDialog(
            onDismiss = { showUploadDialog = false },
            onUpload = { uri, caption, location ->
                viewModel.uploadGallery(uri, caption, location)
                showUploadDialog = false
                Toast.makeText(context, "Mengupload...", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun GalleryGrid(photos: List<UserGallery>) {
    // Grid Pinterest (Staggered)
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2), // 2 Kolom
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        items(photos) { photo ->
            GalleryItemCard(photo)
        }
    }
}

@Composable
fun GalleryItemCard(photo: UserGallery) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Menampilkan Gambar dari URL Supabase
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photo.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = photo.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // Tinggi menyesuaikan aspek rasio gambar asli
            )

            // Bagian Teks (Caption & Lokasi)
            Column(modifier = Modifier.padding(8.dp)) {
                if (!photo.caption.isNullOrEmpty()) {
                    Text(
                        text = photo.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = photo.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun UploadGalleryDialog(
    onDismiss: () -> Unit,
    onUpload: (Uri, String, String) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Photo Picker Android Modern
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bagikan Momenmu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Area Preview / Tombol Pilih Foto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Button(onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) {
                            Text("Pilih Foto")
                        }
                    }
                }
                // Tombol ganti foto jika sudah ada yg dipilih
                if (selectedImageUri != null) {
                    TextButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Ganti Foto")
                    }
                }

                // Input Text
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedImageUri != null && location.isNotEmpty()) {
                        onUpload(selectedImageUri!!, caption, location)
                    }
                },
                // Tombol disable kalau belum pilih foto atau isi lokasi
                enabled = selectedImageUri != null && location.isNotEmpty()
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}