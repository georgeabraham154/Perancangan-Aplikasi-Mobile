package com.example.nusantaraview.ui.destination

import android.content.Intent
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
import androidx.compose.material.icons.filled.Map
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

@Composable
fun AddDestinationDialog(
    onDismiss: () -> Unit,
    viewModel: DestinationViewModel
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // State untuk error validation
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

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
                Text(text = "Tambah Wisata Baru", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(16.dp))

                // Area Upload Foto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(40.dp))
                            Text("Pilih Foto")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Nama Tempat
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (showError) showError = false
                    },
                    label = { Text("Nama Tempat") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && name.isBlank()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input Lokasi
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        if (showError) showError = false
                    },
                    label = { Text("Lokasi") },
                    placeholder = { Text("Contoh: Kota Batu") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && location.isBlank(),
                    trailingIcon = {
                        IconButton(onClick = {
                            val query = if (location.isNotEmpty()) location else "Wisata Indonesia"
                            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")

                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
                                )
                                context.startActivity(browserIntent)
                            }
                        }) {
                            Icon(Icons.Default.Map, contentDescription = "Cari di Maps")
                        }
                    }
                )
                Text(
                    text = "Tips: Klik ikon peta untuk cek lokasi di Google Maps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input Harga
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            price = it
                            if (showError) showError = false
                        }
                    },
                    label = { Text("Harga Tiket (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError && price.isBlank()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input Deskripsi
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Error Message
                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Aksi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // VALIDASI FORM
                            when {
                                name.isBlank() -> {
                                    showError = true
                                    errorMessage = "Nama tempat wajib diisi!"
                                }
                                location.isBlank() -> {
                                    showError = true
                                    errorMessage = "Lokasi wajib diisi!"
                                }
                                price.isBlank() -> {
                                    showError = true
                                    errorMessage = "Harga tiket wajib diisi!"
                                }
                                price.toIntOrNull() == null -> {
                                    showError = true
                                    errorMessage = "Harga harus berupa angka!"
                                }
                                else -> {
                                    // Validasi OK, submit data
                                    viewModel.addDestination(
                                        name = name,
                                        location = location,
                                        price = price,
                                        description = description,
                                        imageUri = imageUri,
                                        context = context
                                    )
                                    onDismiss()
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
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