package com.example.nusantaraview.ui.gallery

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.model.UserGallery
import com.example.nusantaraview.data.remote.SupabaseGalleryRepository
import kotlinx.coroutines.launch

sealed interface GalleryUiState {
    object Loading : GalleryUiState
    data class Success(val photos: List<UserGallery>) : GalleryUiState
    data class Error(val message: String) : GalleryUiState
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SupabaseGalleryRepository(application.applicationContext)

    var galleryUiState: GalleryUiState by mutableStateOf(GalleryUiState.Loading)
        private set

    init {
        getGallery()
    }

    fun getGallery() {
        viewModelScope.launch {
            galleryUiState = GalleryUiState.Loading
            try {
                val result = repository.getGalleryItems()
                galleryUiState = GalleryUiState.Success(result)
            } catch (e: Exception) {
                e.printStackTrace()
                galleryUiState = GalleryUiState.Error("Gagal memuat galeri: ${e.message}")
            }
        }
    }

    fun uploadGallery(uri: Uri, caption: String, location: String) {
        viewModelScope.launch {
            galleryUiState = GalleryUiState.Loading
            try {
                repository.uploadGalleryItem(uri, caption, location)
                getGallery() // Refresh data setelah upload berhasil
            } catch (e: Exception) {
                e.printStackTrace()
                galleryUiState = GalleryUiState.Error("Gagal upload: ${e.message}")
            }
        }
    }
}