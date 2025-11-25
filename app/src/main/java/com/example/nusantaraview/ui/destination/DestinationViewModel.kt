package com.example.nusantaraview.ui.destination

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.model.Destination
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DestinationViewModel : ViewModel() {

    private val _destinations = MutableStateFlow<List<Destination>>(emptyList())
    val destinations: StateFlow<List<Destination>> = _destinations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchDestinations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Select data dari tabel 'destinations'
                val data = SupabaseClient.client.from("destinations")
                    .select().decodeList<Destination>()
                _destinations.value = data
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil data: ${e.message}"
                Log.e("DestinationVM", "Fetch Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addDestination(name: String, location: String, price: String, description: String, imageUri: Uri?, context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalImageUrl: String? = null

                // 1. Upload Gambar
                if (imageUri != null) {
                    val byteArray = context.contentResolver.openInputStream(imageUri)?.use {
                        it.readBytes()
                    }
                    if (byteArray != null) {
                        val fileName = "destinations/${UUID.randomUUID()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("images")
                        bucket.upload(fileName, byteArray)
                        finalImageUrl = bucket.publicUrl(fileName)
                    }
                }

                // 2. Konversi Harga (String -> Long)
                // Jika input kosong atau bukan angka, anggap 0
                val ticketPriceVal = price.toLongOrNull() ?: 0L

                // 3. Buat Objek Data
                val newDestination = Destination(
                    name = name,
                    location = location,
                    price = ticketPriceVal, // Kirim sebagai angka
                    description = description,
                    imageUrl = finalImageUrl
                )

                // Debug: Cek di Logcat apa yang dikirim
                Log.d("DestinationVM", "Mencoba simpan: $newDestination")

                // 4. Kirim ke Supabase
                SupabaseClient.client.from("destinations").insert(newDestination)

                // 5. Refresh data
                fetchDestinations()

            } catch (e: Exception) {
                Log.e("DestinationVM", "Error adding: ${e.message}")
                _errorMessage.value = "Gagal menambah: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}