package com.example.nusantaraview.ui.culinary

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddCulinaryDialog(
    onDismiss: () -> Unit,
    viewModel: CulinaryViewModel
) {
    var namaMakanan by remember { mutableStateOf("") }
    var namaWarung by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var isRecommended by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Tambah Kuliner") },

        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = namaMakanan,
                    onValueChange = { namaMakanan = it },
                    label = { Text("Nama Makanan") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = namaWarung,
                    onValueChange = { namaWarung = it },
                    label = { Text("Nama Warung") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = harga,
                    onValueChange = { harga = it.filter(Char::isDigit) },
                    label = { Text("Harga") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isRecommended,
                        onCheckedChange = { isRecommended = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Tandai sebagai makanan wajib coba")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Pilih Foto"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUri != null) "Foto dipilih" else "Pilih foto makanan")
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    if (!isLoading) {
                        viewModel.addCulinary(
                            namaMakanan = namaMakanan,
                            namaWarung = namaWarung,
                            harga = harga,
                            isRecommended = isRecommended,
                            imageUri = imageUri,
                            context = context
                        )
                        onDismiss()
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Simpan")
                }
            }
        },

        dismissButton = {
            TextButton(
                onClick = { if (!isLoading) onDismiss() },
                enabled = !isLoading
            ) {
                Text("Batal")
            }
        }
    )
}
