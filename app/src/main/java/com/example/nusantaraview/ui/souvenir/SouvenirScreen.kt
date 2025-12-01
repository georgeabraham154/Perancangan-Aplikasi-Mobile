package com.example.nusantaraview.ui.souvenir

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nusantaraview.data.model.Souvenir
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SouvenirScreen(
    viewModel: SouvenirViewModel
) {
    val souvenirList by viewModel.souvenirList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var currentUserId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Souvenir?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchSouvenirs()
        scope.launch { currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { itemToEdit = null; showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, contentDescription = "Tambah") }
        }
    ) { paddingValues ->

        // GUNAKAN LAZY GRID SUPAYA HEADER DAN CARD MENYATU
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {

            // --- HEADER 1: MAPS (Span Full Width) ---
            item(span = { GridItemSpan(maxLineSpan) }) {
                TopSectionMap()
            }

            // --- HEADER 2: JUDUL SECTION (Span Full Width) ---
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Rekomendasi Warga Lokal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // --- ITEMS: CARD SOUVENIR ---
            if (isLoading && souvenirList.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (souvenirList.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("Belum ada data oleh-oleh.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                items(souvenirList) { item ->
                    SouvenirItemCard(
                        item = item,
                        currentUserId = currentUserId,
                        onEditClick = { itemToEdit = item; showDialog = true }
                    )
                }
            }
        }
    }

    if (showDialog) {
        SouvenirDialog(
            onDismiss = { showDialog = false },
            viewModel = viewModel,
            souvenirToEdit = itemToEdit
        )
    }
}

// Widget Tampilan Maps (Header)
@Composable
fun TopSectionMap() {
    val context = LocalContext.current

    Column {
        Text(
            text = "Tempat Oleh-Oleh Terdekat",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable {
                    // Intent ke Google Maps cari "Oleh oleh terdekat"
                    val gmmIntentUri = Uri.parse("geo:0,0?q=oleh+oleh+khas+terdekat")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://googleusercontent.com/maps.google.com/search?q=oleh+oleh"))
                        context.startActivity(browserIntent)
                    }
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Gambar statis peta
                AsyncImage(
                    model = "https://cartodb-basemaps-a.global.ssl.fastly.net/dark_all/15/26370/17150.png",
                    contentDescription = "Map Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Gradient
                Box(modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))
                ))

                // Tombol/Tulisan di tengah peta
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lihat di Google Maps",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SouvenirItemCard(
    item: Souvenir,
    currentUserId: String?,
    onEditClick: () -> Unit
) {
    val isOwner = currentUserId != null && item.userId == currentUserId

    // LOGIKA ICON KATEGORI
    val categoryIcon = when (item.category) {
        "Makanan" -> Icons.Default.Restaurant
        "Minuman" -> Icons.Default.LocalCafe
        else -> Icons.Default.ShoppingBag
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Box Gambar + Badge
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                AsyncImage(
                    model = item.imageUrl ?: "https://placehold.co/600x400?text=No+Image",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Badge Kategori (Kiri Atas)
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(item.category, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                if (isOwner) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd).padding(4.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // Nama Barang
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Nama Toko
                Text(
                    text = item.storeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Harga
                val format = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(item.price)
                Text(
                    text = format,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // DESKRIPSI
                if (!item.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp) // Garis pemisah tipis
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2, // Dibatasi 2 baris biar kartu gak kepanjangan
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.2 // Biar jarak antar baris enak dibaca
                    )
                }
            }
        }
    }
}