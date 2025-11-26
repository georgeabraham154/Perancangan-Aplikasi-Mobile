package com.example.nusantaraview.data.remote

import android.content.Context
import android.net.Uri
import com.example.nusantaraview.data.model.UserGallery
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import io.github.jan.supabase.postgrest.query.Order

class SupabaseGalleryRepository(private val context: Context) {

    private val supabase = SupabaseClient.client

    suspend fun getGalleryItems(): List<UserGallery> {
        return withContext(Dispatchers.IO) {
            // Mengambil data terbaru (created_at descending)
            supabase.from("user_gallery")
                .select {
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<UserGallery>()
        }
    }

    suspend fun uploadGalleryItem(uri: Uri, caption: String, location: String) {
        withContext(Dispatchers.IO) {
            // 1. Cek apakah user sudah login (Wajib karena RLS Policy Anda)
            val currentUser = supabase.auth.currentUserOrNull()
                ?: throw Exception("Anda harus login untuk upload foto!")

            // 2. Upload ke Storage 'gallery-images'
            val byteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("Gagal membaca file gambar")

            // Nama file unik menggunakan UUID
            val fileName = "${currentUser.id}/${UUID.randomUUID()}.jpg"
            val bucket = supabase.storage.from("gallery-images")
            bucket.upload(fileName, byteArray)

            // 3. Ambil Public URL
            val imageUrl = bucket.publicUrl(fileName)

            // 4. Simpan ke Database (user_id akan otomatis terisi/dicek oleh Supabase Auth)
            val galleryItem = UserGallery(
                caption = caption,
                location = location,
                imageUrl = imageUrl,
                userId = currentUser.id // Penting agar lolos RLS policy
            )
            supabase.from("user_gallery").insert(galleryItem)
        }
    }
}