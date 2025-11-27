package com.example.nusantaraview.ui.souvenir

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.model.Souvenir
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import io.github.jan.supabase.gotrue.auth

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

    fun fetchSouvenirs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Pastikan menggunakan client yang benar
                val data = SupabaseClient.client
                    .from("souvenirs") // Nama tabel di database
                    .select()
                    .decodeList<Souvenir>()

                _souvenirList.value = data
                Log.d("SouvenirVM", "Fetched ${data.size} items")
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil data: ${e.message}"
                Log.e("SouvenirVM", "Fetch error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSouvenir(
        itemName: String,
        storeName: String,
        price: String,
        description: String,
        imageUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // PERBAIKAN 1: Ambil User ID dari session login saat ini
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                val currentUserId = currentUser?.id

                // Cek jika user belum login, lempar error
                if (currentUserId == null) {
                    _errorMessage.value = "User belum login!"
                    _isLoading.value = false
                    return@launch
                }

                var finalImageUrl: String? = null

                // Upload gambar (Pastikan bucket policy sudah diatur juga)
                if (imageUri != null) {
                    val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val fileName = "souvenirs/${UUID.randomUUID()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("souvenir-images")
                        bucket.upload(fileName, bytes)
                        finalImageUrl = bucket.publicUrl(fileName)
                    }
                }

                val priceInt = price.toIntOrNull() ?: 0

                val newItem = Souvenir(
                    itemName = itemName,
                    storeName = storeName,
                    price = priceInt,
                    description = description,
                    imageUrl = finalImageUrl,
                    userId = currentUserId // PERBAIKAN 2: Masukkan User ID ke sini
                )

                SupabaseClient.client
                    .from("souvenirs")
                    .insert(newItem)

                Log.d("SouvenirVM", "Item ditambahkan: $newItem")
                fetchSouvenirs()

            } catch (e: Exception) {
                _errorMessage.value = "Gagal: ${e.message}"
                Log.e("SouvenirVM", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun updateSouvenir(
        originalItem: Souvenir, // Data lama
        newName: String,
        newStore: String,
        newPrice: String,
        newDesc: String,
        newImageUri: Uri?, // Foto baru (jika ada)
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Cek User Login
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser?.id != originalItem.userId) {
                    _errorMessage.value = "Anda tidak memiliki izin mengubah data ini"
                    _isLoading.value = false
                    return@launch
                }

                var finalImageUrl = originalItem.imageUrl

                // 2. Jika ada foto baru, upload dulu
                if (newImageUri != null) {
                    val bytes = context.contentResolver.openInputStream(newImageUri)?.use { it.readBytes() }
                    if (bytes != null) {
                        // Tips: Sebaiknya hapus foto lama dari storage biar hemat, tapi skip dulu gpp
                        val fileName = "souvenirs/${UUID.randomUUID()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("souvenir-images")
                        bucket.upload(fileName, bytes)
                        finalImageUrl = bucket.publicUrl(fileName)
                    }
                }

                val priceInt = newPrice.toIntOrNull() ?: 0

                // 3. Buat object baru dengan data update
                val updatedItem = originalItem.copy(
                    itemName = newName,
                    storeName = newStore,
                    price = priceInt,
                    description = newDesc,
                    imageUrl = finalImageUrl
                )

                // 4. Kirim update ke Supabase berdasarkan ID
                SupabaseClient.client
                    .from("souvenirs")
                    .update(updatedItem) {
                        filter {
                            eq("id", originalItem.id!!) // Cari berdasarkan ID item
                        }
                    }

                Log.d("SouvenirVM", "Update sukses")
                fetchSouvenirs() // Refresh list

            } catch (e: Exception) {
                _errorMessage.value = "Gagal update: ${e.message}"
                Log.e("SouvenirVM", "Update error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}