package com.example.nusantaraview.ui.culinary

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.model.Culinary
import com.example.nusantaraview.data.remote.SupabaseCulinaryClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CulinaryViewModel : ViewModel() {

    private val _culinaryList = MutableStateFlow<List<Culinary>>(emptyList())
    val culinaryList: StateFlow<List<Culinary>> = _culinaryList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchCulinary()
    }

    fun fetchCulinary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = SupabaseCulinaryClient.client
                    .from("culinary")
                    .select()
                    .decodeList<Culinary>()

                _culinaryList.value = data
                Log.d("CulinaryVM", "Fetched ${data.size} kuliner")
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil data kuliner: ${e.message}"
                Log.e("CulinaryVM", "Fetch error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCulinary(
        namaMakanan: String,
        namaWarung: String,
        harga: String,
        isRecommended: Boolean,
        imageUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalImageUrl: String? = null

                // Upload gambar ke bucket culinary-images
                if (imageUri != null) {
                    val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val fileName = "culinary/${UUID.randomUUID()}.jpg"
                        val bucket = SupabaseCulinaryClient.client.storage.from("culinary-images")
                        bucket.upload(fileName, bytes)
                        finalImageUrl = bucket.publicUrl(fileName)
                        Log.d("CulinaryVM", "Gambar diupload: $finalImageUrl")
                    }
                }

                val hargaInt = harga.toIntOrNull() ?: 0

                val newItem = Culinary(
                    namaMakanan = namaMakanan,
                    namaWarung = namaWarung,
                    harga = hargaInt,
                    fotoUrl = finalImageUrl,
                    isRecommended = isRecommended
                )

                SupabaseCulinaryClient.client
                    .from("culinary")
                    .insert(newItem)

                Log.d("CulinaryVM", "Kuliner ditambahkan: $newItem")

                fetchCulinary()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambahkan kuliner: ${e.message}"
                Log.e("CulinaryVM", "Add error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
