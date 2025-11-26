package com.example.nusantaraview.ui.culinary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nusantaraview.data.model.Culinary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CulinaryScreen(
    viewModel: CulinaryViewModel
) {
    val culinaryList by viewModel.culinaryList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading && culinaryList.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage != null && culinaryList.isEmpty() -> {
                Text(
                    text = errorMessage ?: "Terjadi kesalahan",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            culinaryList.isEmpty() -> {
                Text(
                    text = "Belum ada data kuliner.\nTap tombol + untuk menambah.",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(culinaryList) { item ->
                        CulinaryItemCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun CulinaryItemCard(item: Culinary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (item.fotoUrl != null) {
                AsyncImage(
                    model = item.fotoUrl,
                    contentDescription = item.namaMakanan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = item.namaMakanan,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = item.namaWarung,
                style = MaterialTheme.typography.bodyMedium
            )

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val formattedPrice = formatter.format(item.harga)

            Text(
                text = formattedPrice,
                style = MaterialTheme.typography.bodyMedium
            )

            if (item.isRecommended) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Wajib coba",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
