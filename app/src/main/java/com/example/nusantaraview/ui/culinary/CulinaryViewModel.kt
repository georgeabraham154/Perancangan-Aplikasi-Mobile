package com.example.nusantaraview.ui.culinary

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.model.Culinary
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
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

    private suspend fun uploadImage(uri: Uri, context: Context): String? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null

            val fileName = "culinary/${UUID.randomUUID()}.jpg"
            val bucket = SupabaseClient.client.storage.from("culinary-images")

            bucket.upload(fileName, bytes, upsert = false)

            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            Log.e("CulinaryVM", "Upload error: ${e.message}")
            null
        }
    }

    fun fetchCulinary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = SupabaseClient.client
                    .from("culinary")
                    .select()
                    .decodeList<Culinary>()
                    .sortedByDescending { it.createdAt }

                _culinaryList.value = data

            } catch (e: Exception) {
                _errorMessage.value = "Fetch failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCulinary(
        foodName: String,
        restaurantName: String,
        price: String,
        description: String,
        imageUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                    ?: throw Exception("Harus login dulu!")

                var finalImage: String? = null
                if (imageUri != null) {
                    finalImage = uploadImage(imageUri, context)
                }

                val priceInt = price.toIntOrNull() ?: 0

                val newItem = Culinary(
                    foodName = foodName,
                    restaurantName = restaurantName,
                    price = priceInt,
                    description = description.ifBlank { null },
                    imageUrl = finalImage,
                    userId = currentUser.id
                )

                SupabaseClient.client.from("culinary").insert(newItem)

                fetchCulinary()

            } catch (e: Exception) {
                _errorMessage.value = "Insert failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
