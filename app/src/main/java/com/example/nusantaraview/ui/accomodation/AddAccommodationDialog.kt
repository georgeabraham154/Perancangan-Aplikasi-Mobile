package com.example.nusantaraview.ui.accommodation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.nusantaraview.data.model.Accommodation

// Dialog untuk tambah/edit penginapan
// Kalau accommodation null = mode tambah, kalau ada isi = mode edit
@Composable
fun AccommodationDialog(
    accommodation: Accommodation? = null,
    onDismiss: () -> Unit,
    viewModel: AccommodationViewModel
) {
    // Deteksi mode: edit atau add
    val isEditMode = accommodation != null

    // State untuk form fields
    // Kalau mode edit, isi dengan data lama. Kalau add, kosong
    var name by remember { mutableStateOf(accommodation?.name ?: "") }
    var facilities by remember { mutableStateOf(accommodation?.facilities ?: "") }
    var price by remember { mutableStateOf(accommodation?.pricePerNight?.toString() ?: "") }
    var description by remember { mutableStateOf(accommodation?.description ?: "") }

    // State untuk nyimpen foto baru yang dipilih user
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Observe loading state dari ViewModel
    val isLoading by viewModel.isLoading.collectAsState()

    // Setup photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title berubah sesuai mode
                Text(
                    text = if (isEditMode) "Edit Penginapan" else "Tambah Penginapan Baru",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Area upload/preview foto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable {
                            // Buka photo picker saat diklik
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Logic preview: prioritas foto baru, kalau ga ada pakai foto lama
                    val displayImage = imageUri ?: accommodation?.imageUrl

                    if (displayImage != null) {
                        // Tampilkan preview foto
                        AsyncImage(
                            model = displayImage,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Tampilkan placeholder icon upload
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Text("Pilih Foto")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form input nama
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Penginapan") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Form input fasilitas
                OutlinedTextField(
                    value = facilities,
                    onValueChange = { facilities = it },
                    label = { Text("Fasilitas") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Form input harga (numeric keyboard)
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Harga per malam (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Form input deskripsi (multiline)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Tombol batal
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tombol simpan
                    Button(
                        onClick = {
                            // Validasi: cek field wajib diisi
                            if (name.isNotEmpty() && facilities.isNotEmpty() && price.isNotEmpty()) {

                                if (isEditMode) {
                                    // Mode edit: update data existing
                                    viewModel.updateAccommodation(
                                        accommodationId = accommodation?.id ?: "",
                                        name = name,
                                        facilities = facilities,
                                        price = price,
                                        description = description,
                                        imageUri = imageUri,  // Foto baru (bisa null)
                                        currentImageUrl = accommodation?.imageUrl,  // Foto lama
                                        context = context
                                    )
                                } else {
                                    // Mode add: insert data baru
                                    viewModel.addAccommodation(
                                        name = name,
                                        facilities = facilities,
                                        price = price,
                                        description = description,
                                        imageUri = imageUri,
                                        context = context
                                    )
                                }

                                // Tutup dialog setelah simpan
                                onDismiss()
                            }
                        },
                        enabled = !isLoading  // Disable button saat loading
                    ) {
                        if (isLoading) {
                            // Tampilkan loading indicator
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}

// Wrapper function untuk mode Add
@Composable
fun AddAccommodationDialog(
    onDismiss: () -> Unit,
    viewModel: AccommodationViewModel
) {
    AccommodationDialog(
        accommodation = null,  // Null = mode add
        onDismiss = onDismiss,
        viewModel = viewModel
    )
}

// Wrapper function untuk mode Edit
@Composable
fun EditAccommodationDialog(
    accommodation: Accommodation,
    onDismiss: () -> Unit,
    viewModel: AccommodationViewModel
) {
    AccommodationDialog(
        accommodation = accommodation,  // Pass data yang mau diedit
        onDismiss = onDismiss,
        viewModel = viewModel
    )
}