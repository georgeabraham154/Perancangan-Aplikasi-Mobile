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

    fun fetchAccommodations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = SupabaseClient.client.from("accommodations")
                    .select()
                    .decodeList<Accommodation>()

                _accommodations.value = data
                Log.d("AccommodationVM", "Berhasil fetch ${data.size} penginapan")
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil data: ${e.message}"
                Log.e("AccommodationVM", "Fetch Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri, context: Context): String? {
        return try {
            Log.d("AccommodationVM", "Mulai upload gambar: $imageUri")

            val byteArray = context.contentResolver.openInputStream(imageUri)?.use {
                it.readBytes()
            }

            if (byteArray == null) {
                Log.e("AccommodationVM", "Gagal membaca file gambar")
                return null
            }

            Log.d("AccommodationVM", "Ukuran file: ${byteArray.size} bytes")

            // PENTING: Ganti "accomodation-images" dengan nama bucket yang benar di Supabase
            val bucketName = "accomodation-images"
            val fileName = "accommodations/${UUID.randomUUID()}.jpg"

            val bucket = SupabaseClient.client.storage.from(bucketName)

            Log.d("AccommodationVM", "Upload ke bucket: $bucketName, path: $fileName")

            bucket.upload(fileName, byteArray, upsert = false)

            val publicUrl = bucket.publicUrl(fileName)
            Log.d("AccommodationVM", "Upload berhasil! URL: $publicUrl")

            publicUrl
        } catch (e: Exception) {
            Log.e("AccommodationVM", "Error upload gambar: ${e.message}", e)
            Log.e("AccommodationVM", "Stack trace: ${e.stackTraceToString()}")
            null
        }
    }

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

                // 1. Upload gambar ke Storage
                if (imageUri != null) {
                    Log.d("AccommodationVM", "Mencoba upload gambar...")
                    finalImageUrl = uploadImage(imageUri, context)

                    if (finalImageUrl == null) {
                        Log.w("AccommodationVM", "Gambar gagal diupload, melanjutkan tanpa gambar")
                    }
                } else {
                    Log.d("AccommodationVM", "Tidak ada gambar yang dipilih")
                }

                // 2. Ambil user yang sedang login
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                    ?: throw Exception("Anda harus login terlebih dahulu")

                val currentUserId = currentUser.id

                // 3. Konversi harga ke Int
                val priceValue = price.toIntOrNull() ?: 0

                // 4. Buat objek Accommodation
                val newAccommodation = Accommodation(
                    id = null,
                    name = name,
                    facilities = facilities,
                    pricePerNight = priceValue,
                    description = description.ifBlank { null },
                    imageUrl = finalImageUrl,
                    userId = currentUserId,
                    createdAt = null
                )

                Log.d("AccommodationVM", "Data yang akan disimpan: $newAccommodation")

                // 5. Insert ke Supabase
                SupabaseClient.client.from("accommodations")
                    .insert(newAccommodation)

                Log.d("AccommodationVM", "Penginapan berhasil ditambahkan!")

                // 6. Refresh list
                fetchAccommodations()

            } catch (e: Exception) {
                Log.e("AccommodationVM", "Error adding accommodation: ${e.message}", e)
                Log.e("AccommodationVM", "Stack trace: ${e.stackTraceToString()}")
                _errorMessage.value = "Gagal menambahkan penginapan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                var finalImageUrl: String? = currentImageUrl

                // 1. Upload gambar baru jika ada
                if (imageUri != null) {
                    Log.d("AccommodationVM", "Mencoba upload gambar baru...")
                    val uploadedUrl = uploadImage(imageUri, context)

                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    } else {
                        Log.w("AccommodationVM", "Upload gambar gagal, tetap menggunakan gambar lama")
                    }
                }

                // 2. Konversi harga ke Int
                val priceValue = price.toIntOrNull() ?: 0

                // 3. Buat objek Accommodation untuk update
                val updatedAccommodation = Accommodation(
                    id = accommodationId,
                    name = name,
                    facilities = facilities,
                    pricePerNight = priceValue,
                    description = description.ifBlank { null },
                    imageUrl = finalImageUrl,
                    userId = null,
                    createdAt = null
                )

                Log.d("AccommodationVM", "ID yang diupdate: $accommodationId")
                Log.d("AccommodationVM", "Data yang akan diupdate: $updatedAccommodation")

                // 4. Update ke Supabase
                SupabaseClient.client.from("accommodations")
                    .update(updatedAccommodation) {
                        filter {
                            eq("id", accommodationId)
                        }
                    }

                Log.d("AccommodationVM", "Penginapan berhasil diupdate!")

                // 5. Refresh list
                kotlinx.coroutines.delay(500)
                fetchAccommodations()

            } catch (e: Exception) {
                Log.e("AccommodationVM", "Error updating accommodation: ${e.message}", e)
                Log.e("AccommodationVM", "Stack trace: ${e.stackTraceToString()}")
                _errorMessage.value = "Gagal mengupdate penginapan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}