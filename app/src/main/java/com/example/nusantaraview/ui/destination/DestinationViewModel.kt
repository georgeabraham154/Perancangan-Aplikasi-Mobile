package com.example.nusantaraview.ui.destination

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.model.Destination
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import java.util.UUID

@OptIn(InternalSerializationApi::class)
class DestinationViewModel : ViewModel() {

    private val _destinations = MutableStateFlow<List<Destination>>(emptyList())
    val destinations: StateFlow<List<Destination>> = _destinations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 🔧 FUNGSI HELPER: Upload Gambar ke Bucket destination-images
    private suspend fun uploadImage(imageUri: Uri, context: Context): String? {
        return try {
            Log.d("DestinationVM", "📤 Mulai upload gambar...")

            // 1. Baca file gambar dari URI
            val byteArray = context.contentResolver.openInputStream(imageUri)?.use {
                it.readBytes()
            }

            if (byteArray == null) {
                Log.e("DestinationVM", "❌ Gagal membaca file gambar")
                return null
            }

            Log.d("DestinationVM", "✅ File dibaca, ukuran: ${byteArray.size} bytes")

            // 2. Generate nama file unik
            val fileName = "destinations/${UUID.randomUUID()}.jpg"
            Log.d("DestinationVM", "📝 Nama file: $fileName")

            // 3. 🎯 PENTING: Gunakan bucket 'destination-images' (BUKAN 'images')
            val bucket = SupabaseClient.client.storage.from("destination-images")

            // 4. Upload file ke Supabase Storage
            Log.d("DestinationVM", "⬆️ Mengupload ke Supabase...")
            bucket.upload(fileName, byteArray, upsert = false)

            // 5. Ambil URL publik
            val imageUrl = bucket.publicUrl(fileName)
            Log.d("DestinationVM", "✅ Upload berhasil! URL: $imageUrl")

            return imageUrl

        } catch (e: Exception) {
            Log.e("DestinationVM", "❌ Upload gagal: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // 📥 FUNGSI: Ambil semua data destinasi dari database
    fun fetchDestinations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("DestinationVM", "🔍 Fetching destinations...")

                val data = SupabaseClient.client.from("destinations")
                    .select {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Destination>()

                _destinations.value = data
                Log.d("DestinationVM", "✅ Berhasil fetch ${data.size} destinasi")

            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil data: ${e.message}"
                Log.e("DestinationVM", "❌ Fetch Error: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ➕ FUNGSI: Tambah destinasi baru
    fun addDestination(
        name: String,
        location: String,
        price: String,
        description: String,
        imageUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("DestinationVM", "➕ Menambahkan destinasi baru...")

                // 1. Cek user login
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser == null) {
                    _errorMessage.value = "Anda harus login terlebih dahulu"
                    Log.e("DestinationVM", "❌ User tidak login!")
                    _isLoading.value = false
                    return@launch
                }

                Log.d("DestinationVM", "✅ User login: ${currentUser.id}")

                // 2. Upload gambar (jika ada)
                var finalImageUrl: String? = null
                if (imageUri != null) {
                    Log.d("DestinationVM", "📸 Ada gambar untuk diupload")
                    finalImageUrl = uploadImage(imageUri, context)

                    if (finalImageUrl == null) {
                        _errorMessage.value = "Gagal upload gambar. Coba lagi."
                        Log.e("DestinationVM", "❌ Upload gambar gagal")
                        _isLoading.value = false
                        return@launch
                    }
                } else {
                    Log.d("DestinationVM", "ℹ️ Tidak ada gambar")
                }

                // 3. Konversi harga
                val ticketPriceValue = price.toIntOrNull() ?: 0
                Log.d("DestinationVM", "💰 Harga: $ticketPriceValue")

                // 4. Buat object Destination
                val newDestination = Destination(
                    id = null,
                    name = name,
                    location = location,
                    ticketPrice = ticketPriceValue,
                    description = description.ifBlank { null },
                    imageUrl = finalImageUrl,
                    userId = currentUser.id,
                    createdAt = null
                )

                // 5. Insert ke database
                Log.d("DestinationVM", "💾 Menyimpan ke database...")
                SupabaseClient.client.from("destinations").insert(newDestination)
                Log.d("DestinationVM", "✅ Simpan berhasil!")

                // 6. Refresh list
                kotlinx.coroutines.delay(500) // Delay sedikit biar database kelar nyimpen
                fetchDestinations()

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambah: ${e.message}"
                Log.e("DestinationVM", "❌ Error adding: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✏️ FUNGSI: Update destinasi
    fun updateDestination(
        destination: Destination,
        newName: String,
        newLocation: String,
        newPrice: String,
        newDescription: String,
        newImageUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("DestinationVM", "✏️ Mengupdate destinasi: ${destination.id}")

                // 1. Cek apakah ada gambar baru
                var finalImageUrl = destination.imageUrl // Default: pakai gambar lama

                if (newImageUri != null) {
                    Log.d("DestinationVM", "📸 Ada gambar baru untuk diupload")
                    val uploadedUrl = uploadImage(newImageUri, context)

                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                        Log.d("DestinationVM", "✅ Gambar baru berhasil diupload")
                    } else {
                        Log.e("DestinationVM", "⚠️ Upload gambar baru gagal, pakai gambar lama")
                    }
                } else {
                    Log.d("DestinationVM", "ℹ️ Tidak ada gambar baru, pakai gambar lama")
                }

                // 2. Konversi harga
                val ticketPriceValue = newPrice.toIntOrNull() ?: 0

                // 3. Update object
                val updatedDestination = destination.copy(
                    name = newName,
                    location = newLocation,
                    ticketPrice = ticketPriceValue,
                    description = newDescription.ifBlank { null },
                    imageUrl = finalImageUrl
                )

                // 4. Update ke database
                Log.d("DestinationVM", "💾 Menyimpan perubahan...")
                SupabaseClient.client.from("destinations").update(updatedDestination) {
                    filter { eq("id", destination.id ?: "") }
                }
                Log.d("DestinationVM", "✅ Update berhasil!")

                // 5. Refresh list
                kotlinx.coroutines.delay(500)
                fetchDestinations()

            } catch (e: Exception) {
                _errorMessage.value = "Gagal update: ${e.message}"
                Log.e("DestinationVM", "❌ Update error: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🗑️ FUNGSI: Hapus destinasi
    fun deleteDestination(destinationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("DestinationVM", "🗑️ Menghapus destinasi: $destinationId")

                SupabaseClient.client.from("destinations").delete {
                    filter { eq("id", destinationId) }
                }

                Log.d("DestinationVM", "✅ Hapus berhasil!")

                kotlinx.coroutines.delay(500)
                fetchDestinations()

            } catch (e: Exception) {
                _errorMessage.value = "Gagal hapus: ${e.message}"
                Log.e("DestinationVM", "❌ Delete error: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}