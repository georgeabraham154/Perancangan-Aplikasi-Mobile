package com.example.nusantaraview.ui.souvenir

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.model.Souvenir
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SouvenirViewModel : ViewModel() {

    private val _souvenirList = MutableStateFlow<List<Souvenir>>(emptyList())
    val souvenirList: StateFlow<List<Souvenir>> = _souvenirList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchSouvenirs()
    }

    private suspend fun uploadImage(uri: Uri, context: Context): String? {
        return try {
            // Membaca file gambar dari galeri HP menjadi kumpulan byte (010101).
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val fileName = "souvenirs/${UUID.randomUUID()}.jpg" // Memberi nama unik pada file gambar (biar gak bentrok kalau ada yang upload gambar sama).
            // UUID.randomUUID() menghasilkan string acak, misal: "a1b2-c3d4-e5f6.jpg"

            // Menunjuk ke Bucket "souvenir-images" di Supabase Storage.
            val bucket = SupabaseClient.client.storage.from("souvenir-images")

            // Proses Upload ke Cloud (Internet).
            bucket.upload(fileName, bytes, upsert = false)

            // meminta Link Publiknya (https://...) buat disimpan di database.
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            Log.e("SouvenirVM", "Upload gagal: ${e.message}")
            null
        }
    }

    fun fetchSouvenirs() {
        viewModelScope.launch {
            _isLoading.value = true //menyalakan load
            try {
                val data = SupabaseClient.client //meminta data ke tabel "souvenirs" di Supabase
                    .from("souvenirs")
                    .select()
                    .decodeList<Souvenir>()
                    .sortedByDescending { it.createdAt }
                _souvenirList.value = data //mengupdate data ke UI
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil data: ${e.message}"
            } finally {
                _isLoading.value = false //mematikan load (entah itu sukses atau gagal)
            }
        }
    }

    fun addSouvenir(
        itemName: String,
        storeName: String,
        price: String,
        description: String,
        imageUri: Uri?,
        category: String,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // cek user login
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                    ?: throw Exception("Anda harus login!")

                // upload gambar dulu
                var finalImageUrl: String? = null
                if (imageUri != null) {
                    finalImageUrl = uploadImage(imageUri, context)
                }

                val priceInt = price.toIntOrNull() ?: 0

                val newItem = Souvenir(
                    itemName = itemName,
                    storeName = storeName,
                    price = priceInt,
                    description = description.ifBlank { null },
                    imageUrl = finalImageUrl,
                    userId = currentUser.id,
                    category = category
                )

                //mengirim data ke supabase
                SupabaseClient.client.from("souvenirs").insert(newItem)

                //refresh data agar langsung muncul
                fetchSouvenirs()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal tambah: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSouvenir(
        originalItem: Souvenir,
        newName: String,
        newStore: String,
        newPrice: String,
        newDesc: String,
        newImageUri: Uri?,
        newCategory: String,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser?.id != originalItem.userId) throw Exception("Bukan milik Anda!")

                var finalImageUrl = originalItem.imageUrl
                if (newImageUri != null) {
                    val uploadedUrl = uploadImage(newImageUri, context)
                    if (uploadedUrl != null) finalImageUrl = uploadedUrl
                }

                val priceInt = newPrice.toIntOrNull() ?: 0

                val updatedItem = originalItem.copy(
                    itemName = newName,
                    storeName = newStore,
                    price = priceInt,
                    description = newDesc.ifBlank { null },
                    imageUrl = finalImageUrl,
                    category = newCategory
                )

                SupabaseClient.client.from("souvenirs").update(updatedItem) {
                    filter { eq("id", originalItem.id!!) }
                }
                fetchSouvenirs()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}