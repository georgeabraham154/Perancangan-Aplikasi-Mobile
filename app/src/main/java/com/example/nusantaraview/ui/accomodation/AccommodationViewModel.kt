package com.example.nusantaraview.ui.accommodation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.model.Accommodation
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
class AccommodationViewModel : ViewModel() {

    private val _accommodations = MutableStateFlow<List<Accommodation>>(emptyList())
    val accommodations: StateFlow<List<Accommodation>> = _accommodations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    //Fungsi untuk mengambil semua data penginapan dari Supabase//
    fun fetchAccommodations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Query ke tabel accommodations dengan sorting
                val data = SupabaseClient.client.from("accommodations")
                    .select {
                        // Urutkan berdasarkan created_at, data terbaru di atas
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Accommodation>()

                // Update StateFlow dengan data baru
                _accommodations.value = data
            } catch (e: Exception) {
                // Tangani error dan simpan pesan error
                _errorMessage.value = "Gagal mengambil data: ${e.message}"
                Log.e("AccommodationVM", "Fetch error: ${e.message}")
            } finally {
                // Set loading ke false setelah selesai
                _isLoading.value = false
            }
        }
    }
    //Fungsi private untuk upload gambar ke Supabase Storage //
    private suspend fun uploadImage(imageUri: Uri, context: Context): String? {
        return try {
            // Baca file gambar menjadi ByteArray
            val byteArray = context.contentResolver.openInputStream(imageUri)?.use {
                it.readBytes()
            } ?: return null

            // Nama bucket di Supabase Storage
            val bucketName = "accomodation-images"
            // Generate nama file unik dengan UUID
            val fileName = "accommodations/${UUID.randomUUID()}.jpg"

            // Akses bucket storage
            val bucket = SupabaseClient.client.storage.from(bucketName)

            // Upload file ke storage dan ambil URL publik
            bucket.upload(fileName, byteArray, upsert = false)
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            // Log error jika upload gagal
            Log.e("AccommodationVM", "Upload error: ${e.message}")
            null
        }
    }

    // Fungsi untuk menambahkan penginapan baru//
    fun addAccommodation(
        name: String,
        facilities: String,
        price: String,
        description: String,
        imageUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalImageUrl: String? = null

                // 1. Upload gambar jika ada
                if (imageUri != null) {
                    finalImageUrl = uploadImage(imageUri, context)
                }

                // 2. Ambil user yang sedang login
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                    ?: throw Exception("Anda harus login terlebih dahulu")

                val currentUserId = currentUser.id

                // 3. Konversi harga dari String ke Int (default 0 jika gagal)
                val priceValue = price.toIntOrNull() ?: 0

                // 4. Buat objek Accommodation baru
                val newAccommodation = Accommodation(
                    id = null, // ID akan di-generate otomatis oleh database
                    name = name,
                    facilities = facilities,
                    pricePerNight = priceValue,
                    description = description.ifBlank { null }, // Null jika kosong
                    imageUrl = finalImageUrl,
                    userId = currentUserId, // ID user yang login
                    createdAt = null // Timestamp akan di-generate otomatis
                )

                // 5. Insert data ke Supabase
                SupabaseClient.client.from("accommodations")
                    .insert(newAccommodation)

                // 6. Refresh list untuk mendapatkan data terbaru
                fetchAccommodations()

            } catch (e: Exception) {
                // Tangani error dan simpan pesan error
                _errorMessage.value = "Gagal menambahkan penginapan: ${e.message}"
                Log.e("AccommodationVM", "Add error: ${e.message}")
            } finally {
                // Set loading ke false setelah selesai
                _isLoading.value = false
            }
        }
    }

    //Fungsi untuk mengupdate penginapan yang sudah ada//
    fun updateAccommodation(
        accommodationId: String,
        name: String,
        facilities: String,
        price: String,
        description: String,
        imageUri: Uri?,
        currentImageUrl: String?,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Default gunakan gambar lama
                var finalImageUrl: String? = currentImageUrl

                // 1. Upload gambar baru jika ada
                if (imageUri != null) {
                    val uploadedUrl = uploadImage(imageUri, context)

                    // Jika upload berhasil, gunakan URL baru
                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    }
                }

                // 2. Konversi harga dari String ke Int
                val priceValue = price.toIntOrNull() ?: 0

                // 3. Buat objek Accommodation untuk update
                val updatedAccommodation = Accommodation(
                    id = accommodationId,
                    name = name,
                    facilities = facilities,
                    pricePerNight = priceValue,
                    description = description.ifBlank { null },
                    imageUrl = finalImageUrl,
                    userId = null, // userId tidak perlu diupdate
                    createdAt = null // createdAt tidak perlu diupdate
                )

                // 4. Update data ke Supabase dengan filter by ID
                SupabaseClient.client.from("accommodations")
                    .update(updatedAccommodation) {
                        filter {
                            eq("id", accommodationId) // Filter: hanya update data dengan ID ini
                        }
                    }

                // 5. Refresh list untuk mendapatkan data terbaru
                fetchAccommodations()

            } catch (e: Exception) {
                // Tangani error dan simpan pesan error
                _errorMessage.value = "Gagal mengupdate penginapan: ${e.message}"
                Log.e("AccommodationVM", "Update error: ${e.message}")
            } finally {
                // Set loading ke false setelah selesai
                _isLoading.value = false
            }
        }
    }
}