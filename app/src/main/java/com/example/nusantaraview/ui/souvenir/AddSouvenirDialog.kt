package com.example.nusantaraview.ui.souvenir

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
import com.example.nusantaraview.data.model.Souvenir

@Composable
fun SouvenirDialog(
    onDismiss: () -> Unit,
    viewModel: SouvenirViewModel,
    souvenirToEdit: Souvenir? = null
) {
    var itemName by remember { mutableStateOf(souvenirToEdit?.itemName ?: "") }
    var storeName by remember { mutableStateOf(souvenirToEdit?.storeName ?: "") }
    var price by remember { mutableStateOf(souvenirToEdit?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(souvenirToEdit?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 👇 Default kosong kalau baru, biar user milih sendiri
    var category by remember { mutableStateOf(souvenirToEdit?.category ?: "") }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    val dialogTitle = if (souvenirToEdit == null) "Tambah Oleh-Oleh" else "Edit Oleh-Oleh"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = dialogTitle, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                // --- UPLOAD FOTO ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (souvenirToEdit?.imageUrl != null) {
                        AsyncImage(model = souvenirToEdit.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(40.dp))
                            Text("Pilih Foto")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = storeName, onValueChange = { storeName = it }, label = { Text("Nama Toko") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { if (it.all { c -> c.isDigit() }) price = it }, label = { Text("Harga (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                // --- PILIH KATEGORI (3 OPSI) ---
                Text("Jenis Oleh-Oleh:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { category = "Makanan" }) {
                    RadioButton(selected = category == "Makanan", onClick = { category = "Makanan" })
                    Text("Makanan")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { category = "Minuman" }) {
                    RadioButton(selected = category == "Minuman", onClick = { category = "Minuman" })
                    Text("Minuman")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { category = "Barang" }) {
                    RadioButton(selected = category == "Barang", onClick = { category = "Barang" })
                    Text("Barang/Kerajinan")
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Button(onClick = {
                        // VALIDASI
                        if (itemName.isBlank() || price.isBlank()) {
                            showError = true
                            errorMessage = "Nama dan Harga wajib diisi!"
                        } else if (category.isBlank()) {
                            // 👇 Validasi: User wajib pilih
                            showError = true
                            errorMessage = "Pilih jenis oleh-oleh dulu!"
                        } else {
                            if (souvenirToEdit == null) {
                                viewModel.addSouvenir(itemName, storeName, price, description, imageUri, category, context)
                            } else {
                                viewModel.updateSouvenir(souvenirToEdit, itemName, storeName, price, description, imageUri, category, context)
                            }
                            onDismiss()
                        }
                    }, enabled = !isLoading) {
                        Text(if (isLoading) "Loading..." else "Simpan")
                    }
                }
            }
        }
    }
}