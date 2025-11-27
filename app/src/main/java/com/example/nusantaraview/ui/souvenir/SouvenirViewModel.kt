package com.example.nusantaraview.ui.souvenir

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.model.Souvenir
// 👇 Import Client yang kamu buat
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

    fun fetchSouvenirs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Menggunakan SupabaseClient.client
                val data = SupabaseClient.client
                    .from("souvenirs")
                    .select()
                    .decodeList<Souvenir>()
                    .sortedByDescending { it.createdAt }
                _souvenirList.value = data
            } catch (e: Exception) {
                _errorMessage.value = "Gagal ambil data: ${e.message}"
                Log.e("SouvenirVM", "Fetch Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSouvenir(nama: String, toko: String, hargaStr: String, uri: Uri?, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Cek User Login (PENTING UNTUK RLS)
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser == null) {
                    throw Exception("User belum login. Tidak bisa menambah data.")
                }

                // 2. Upload Gambar
                val imgUrl = uploadImage(uri, context)
                val hargaInt = hargaStr.toIntOrNull() ?: 0

                // 3. Buat Object (Masukkan User ID)
                val newItem = Souvenir(
                    itemName = nama,
                    storeName = toko,
                    price = hargaInt,
                    imageUrl = imgUrl,
                    userId = currentUser.id // 👈 Kunci agar Policy SQL lolos!
                )

                // 4. Kirim ke Database
                SupabaseClient.client.from("souvenirs").insert(newItem)

                fetchSouvenirs()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal tambah: ${e.message}"
                Log.e("SouvenirVM", "Add Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun editSouvenir(originalItem: Souvenir, nama: String, toko: String, hargaStr: String, newUri: Uri?, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cek Login
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser == null) throw Exception("Sesi habis. Silakan login ulang.")

                val finalUrl = if (newUri != null) uploadImage(newUri, context) else originalItem.imageUrl
                val hargaInt = hargaStr.toIntOrNull() ?: 0

                SupabaseClient.client.from("souvenirs").update(
                    {
                        set("item_name", nama)
                        set("store_name", toko)
                        set("price", hargaInt)
                        set("image_url", finalUrl)
                        // User ID tidak perlu diupdate, tetap pakai yang lama
                    }
                ) {
                    filter {
                        eq("id", originalItem.id!!)
                    }
                }
                fetchSouvenirs()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal edit: ${e.message}"
                Log.e("SouvenirVM", "Edit Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSouvenir(item: Souvenir) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cek Login
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser == null) throw Exception("Sesi habis.")

                SupabaseClient.client.from("souvenirs").delete {
                    filter {
                        eq("id", item.id!!)
                    }
                }
                fetchSouvenirs()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal hapus: ${e.message}"
                Log.e("SouvenirVM", "Delete Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun uploadImage(uri: Uri?, context: Context): String? {
        if (uri == null) return null
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val fileName = "souvenirs/${UUID.randomUUID()}.jpg"
        // Menggunakan bucket 'souvenir-images' dari SupabaseClient
        val bucket = SupabaseClient.client.storage.from("souvenir-images")
        bucket.upload(fileName, bytes)
        return bucket.publicUrl(fileName)
    }
}