package com.example.nusantaraview.ui.souvenir

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nusantaraview.data.model.Souvenir

@Composable
fun EditSouvenirDialog(
    souvenir: Souvenir, // Data yang mau diedit
    onDismiss: () -> Unit,
    viewModel: SouvenirViewModel
) {
    // Isi state awal dengan data dari parameter 'souvenir'
    var itemName by remember { mutableStateOf(souvenir.itemName) }
    var storeName by remember { mutableStateOf(souvenir.storeName) }
    var price by remember { mutableStateOf(souvenir.price.toString()) }
    var description by remember { mutableStateOf(souvenir.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Edit Oleh-Oleh") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Nama Barang") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Nama Toko") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter(Char::isDigit) },
                    label = { Text("Harga") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUri != null) "Ganti Foto (Terpilih)" else "Ganti Foto (Opsional)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isLoading) {
                        viewModel.updateSouvenir(
                            originalItem = souvenir,
                            newName = itemName,
                            newStore = storeName,
                            newPrice = price,
                            newDesc = description,
                            newImageUri = imageUri,
                            context = context
                        )
                        onDismiss()
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Loading..." else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}