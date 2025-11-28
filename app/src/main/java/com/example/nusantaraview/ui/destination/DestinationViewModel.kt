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

    fun fetchDestinations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = SupabaseClient.client.from("destinations")
                    .select()
                    .decodeList<Destination>()

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
                var finalImageUrl: String? = null

                // 1. Upload Gambar ke Storage
                if (imageUri != null) {
                    val byteArray = context.contentResolver.openInputStream(imageUri)?.use {
                        it.readBytes()
                    }
                    if (byteArray != null) {
                        val fileName = "destinations/${UUID.randomUUID()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("images")
                        bucket.upload(fileName, byteArray)
                        finalImageUrl = bucket.publicUrl(fileName)
                        Log.d("DestinationVM", "Gambar berhasil diupload: $finalImageUrl")
                    }
                }

                // 2. Ambil User ID yang sedang login
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()

                if (currentUser == null) {
                    Log.e("DestinationVM", "ERROR: User tidak login!")
                    throw Exception("Anda harus login terlebih dahulu")
                }

                val currentUserId = currentUser.id
                Log.d("DestinationVM", "✅ Current User ID: $currentUserId")
                Log.d("DestinationVM", "✅ User Email: ${currentUser.email}")

                // 3. Konversi Harga ke Int
                val ticketPriceValue = price.toIntOrNull() ?: 0

                // 4. Buat objek Destination
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

                Log.d("DestinationVM", "Data yang akan disimpan: $newDestination")

                // 5. Insert ke Supabase
                SupabaseClient.client.from("destinations")
                    .insert(newDestination)

                Log.d("DestinationVM", "✅ Destinasi berhasil ditambahkan!")

                // 6. PENTING: Kasih delay 500ms biar Supabase sempat proses (SEPERTI ACCOMMODATION)
                kotlinx.coroutines.delay(500)

                // 7. Refresh data
                fetchDestinations()

            } catch (e: Exception) {
                Log.e("DestinationVM", "Error adding destination: ${e.message}", e)
                _errorMessage.value = "Gagal menambahkan destinasi: ${e.message}"
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
                var finalImageUrl = destination.imageUrl

                // Upload gambar baru jika ada
                if (newImageUri != null) {
                    val byteArray = context.contentResolver.openInputStream(newImageUri)?.use {
                        it.readBytes()
                    }
                    if (byteArray != null) {
                        val fileName = "destinations/${UUID.randomUUID()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("images")
                        bucket.upload(fileName, byteArray)
                        finalImageUrl = bucket.publicUrl(fileName)
                        Log.d("DestinationVM", "Gambar baru berhasil diupload: $finalImageUrl")
                    }
                }

                val ticketPriceValue = newPrice.toIntOrNull() ?: 0

                val finalUserId = destination.userId
                    ?: SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("User ID tidak ditemukan")

                val updatedDestination = Destination(
                    id = destination.id,
                    name = newName,
                    location = newLocation,
                    ticketPrice = ticketPriceValue,
                    description = newDescription.ifBlank { null },
                    imageUrl = finalImageUrl,
                    userId = finalUserId,
                    createdAt = destination.createdAt
                )

                Log.d("DestinationVM", "Updating destination ID: ${destination.id}")
                Log.d("DestinationVM", "Updated data: $updatedDestination")

                SupabaseClient.client.from("destinations")
                    .update(updatedDestination) {
                        filter {
                            eq("id", destination.id ?: "")
                        }
                    }

                Log.d("DestinationVM", "✅ Destinasi berhasil diupdate!")

                // Kasih delay 500ms (SEPERTI ACCOMMODATION)
                kotlinx.coroutines.delay(500)

                fetchDestinations()

            } catch (e: Exception) {
                Log.e("DestinationVM", "❌ Error updating destination: ${e.message}", e)
                _errorMessage.value = "Gagal mengupdate destinasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDestination(destinationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("DestinationVM", "Deleting destination with ID: $destinationId")

                SupabaseClient.client.from("destinations")
                    .delete {
                        filter {
                            eq("id", destinationId)
                        }
                    }

                Log.d("DestinationVM", "✅ Destinasi berhasil dihapus!")

                // Kasih delay 500ms (SEPERTI ACCOMMODATION)
                kotlinx.coroutines.delay(500)

                fetchDestinations()

            } catch (e: Exception) {
                Log.e("DestinationVM", "Error deleting destination: ${e.message}", e)
                _errorMessage.value = "Gagal menghapus destinasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}