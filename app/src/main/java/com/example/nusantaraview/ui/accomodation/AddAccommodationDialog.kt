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

/**
 * Dialog untuk Tambah atau Edit Penginapan
 * @param accommodation jika null = mode tambah, jika ada data = mode edit
 */
@Composable
fun AccommodationDialog(
    accommodation: Accommodation? = null,
    onDismiss: () -> Unit,
    viewModel: AccommodationViewModel
) {
    // Mode: true = Edit, false = Add
    val isEditMode = accommodation != null

    // State untuk form
    var name by remember { mutableStateOf(accommodation?.name ?: "") }
    var facilities by remember { mutableStateOf(accommodation?.facilities ?: "") }
    var price by remember { mutableStateOf(accommodation?.pricePerNight?.toString() ?: "") }
    var description by remember { mutableStateOf(accommodation?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

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
                // Title berubah sesuai mode
                Text(
                    text = if (isEditMode) "Edit Penginapan" else "Tambah Penginapan Baru",
                    style = MaterialTheme.typography.headlineSmall
                )

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
                    // Prioritas tampilan: gambar baru > gambar lama > placeholder
                    val displayImage = imageUri ?: accommodation?.imageUrl

                    if (displayImage != null) {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
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

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Penginapan") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = facilities,
                    onValueChange = { facilities = it },
                    label = { Text("Fasilitas") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Harga per malam (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                            if (name.isNotEmpty() && facilities.isNotEmpty() && price.isNotEmpty()) {
                                if (isEditMode) {
                                    // Mode Edit
                                    viewModel.updateAccommodation(
                                        accommodationId = accommodation?.id ?: "",
                                        name = name,
                                        facilities = facilities,
                                        price = price,
                                        description = description,
                                        imageUri = imageUri,
                                        currentImageUrl = accommodation?.imageUrl,
                                        context = context
                                    )
                                } else {
                                    // Mode Add
                                    viewModel.addAccommodation(
                                        name = name,
                                        facilities = facilities,
                                        price = price,
                                        description = description,
                                        imageUri = imageUri,
                                        context = context
                                    )
                                }
                                onDismiss()
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

// Composable terpisah untuk backward compatibility
@Composable
fun AddAccommodationDialog(
    onDismiss: () -> Unit,
    viewModel: AccommodationViewModel
) {
    AccommodationDialog(
        accommodation = null,
        onDismiss = onDismiss,
        viewModel = viewModel
    )
}

@Composable
fun EditAccommodationDialog(
    accommodation: Accommodation,
    onDismiss: () -> Unit,
    viewModel: AccommodationViewModel
) {
    AccommodationDialog(
        accommodation = accommodation,
        onDismiss = onDismiss,
        viewModel = viewModel
    )
}