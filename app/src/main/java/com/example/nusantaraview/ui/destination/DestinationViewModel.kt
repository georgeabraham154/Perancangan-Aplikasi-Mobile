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

    // Fungsi helper untuk upload gambar (Meniru logika AccommodationViewModel)
    private suspend fun uploadImage(imageUri: Uri, context: Context): String? {
        return try {
            val byteArray = context.contentResolver.openInputStream(imageUri)?.use {
                it.readBytes()
            } ?: return null

            // Pastikan bucket 'images' atau 'destination-images' ada di Supabase Storage kamu
            // Jika fitur hotel pakai 'accomodation-images', di sini kita pakai 'images' (default)
            val fileName = "destinations/${UUID.randomUUID()}.jpg"
            val bucket = SupabaseClient.client.storage.from("images")

            bucket.upload(fileName, byteArray, upsert = false)
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            Log.e("DestinationVM", "Gagal upload gambar: ${e.message}")
            null
        }
    }

    fun fetchDestinations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = SupabaseClient.client.from("destinations")
                    .select()
                    .decodeList<Destination>()
                // Urutkan dari yang terbaru (opsional)
                // .sortedByDescending { it.createdAt }

                _destinations.value = data
                Log.d("DestinationVM", "Berhasil fetch ${data.size} destinasi")
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil data: ${e.message}"
                Log.e("DestinationVM", "Fetch Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                // 1. Upload Gambar (menggunakan fungsi helper)
                var finalImageUrl: String? = null
                if (imageUri != null) {
                    finalImageUrl = uploadImage(imageUri, context)
                }

                // 2. Ambil User Login
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                    ?: throw Exception("Anda harus login terlebih dahulu")
                val currentUserId = currentUser.id

                // 3. Konversi Harga
                val ticketPriceValue = price.toIntOrNull() ?: 0

                // 4. Buat Objek
                val newDestination = Destination(
                    id = null,
                    name = name,
                    location = location,
                    ticketPrice = ticketPriceValue,
                    description = description.ifBlank { null },
                    imageUrl = finalImageUrl,
                    userId = currentUserId,
                    createdAt = null
                )

                // 5. Insert ke Supabase
                SupabaseClient.client.from("destinations").insert(newDestination)
                Log.d("DestinationVM", "Simpan Berhasil!")

                // 6. Delay sedikit & Refresh (Kunci agar data muncul!)
                kotlinx.coroutines.delay(500)
                fetchDestinations()

            } catch (e: Exception) {
                Log.e("DestinationVM", "Error adding: ${e.message}")
                _errorMessage.value = "Gagal menambah: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                // 1. Cek Gambar Baru
                var finalImageUrl = destination.imageUrl
                if (newImageUri != null) {
                    val uploadedUrl = uploadImage(newImageUri, context)
                    if (uploadedUrl != null) finalImageUrl = uploadedUrl
                }

                val ticketPriceValue = newPrice.toIntOrNull() ?: 0

                // Update objek
                val updatedDestination = destination.copy(
                    name = newName,
                    location = newLocation,
                    ticketPrice = ticketPriceValue,
                    description = newDescription.ifBlank { null },
                    imageUrl = finalImageUrl
                )

                // 2. Update ke Supabase
                SupabaseClient.client.from("destinations").update(updatedDestination) {
                    filter { eq("id", destination.id ?: "") }
                }

                // 3. Refresh
                kotlinx.coroutines.delay(500)
                fetchDestinations()

            } catch (e: Exception) {
                _errorMessage.value = "Gagal update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDestination(destinationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                SupabaseClient.client.from("destinations").delete {
                    filter { eq("id", destinationId) }
                }

                kotlinx.coroutines.delay(500)
                fetchDestinations()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal hapus: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}