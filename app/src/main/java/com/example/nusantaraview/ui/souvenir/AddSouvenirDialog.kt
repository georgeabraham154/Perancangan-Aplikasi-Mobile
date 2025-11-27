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
fun AddSouvenirDialog(
    onDismiss: () -> Unit,
    viewModel: SouvenirViewModel,
    souvenirToEdit: Souvenir? = null
) {
    // 👇 Perhatikan: souvenirToEdit?.itemName, storeName, price
    var namaBarang by remember { mutableStateOf(souvenirToEdit?.itemName ?: "") }
    var namaToko by remember { mutableStateOf(souvenirToEdit?.storeName ?: "") }
    var harga by remember { mutableStateOf(souvenirToEdit?.price?.toString() ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    val dialogTitle = if (souvenirToEdit != null) "Edit Oleh-Oleh" else "Tambah Oleh-Oleh"

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(dialogTitle) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = namaBarang, onValueChange = { namaBarang = it },
                    label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = namaToko, onValueChange = { namaToko = it },
                    label = { Text("Nama Toko") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = harga, onValueChange = { harga = it.filter(Char::isDigit) },
                    label = { Text("Harga") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable {
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }.padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    val photoText = if (imageUri != null) "Foto baru dipilih"
                    else if (souvenirToEdit != null) "Gunakan foto lama"
                    else "Pilih Foto"
                    Text(photoText)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isLoading) {
                        if (souvenirToEdit == null) {
                            viewModel.addSouvenir(namaBarang, namaToko, harga, imageUri, context)
                        } else {
                            viewModel.editSouvenir(souvenirToEdit, namaBarang, namaToko, harga, imageUri, context)
                        }
                        onDismiss()
                    }
                },
                enabled = !isLoading
            ) { Text(if (isLoading) "Loading..." else "Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}