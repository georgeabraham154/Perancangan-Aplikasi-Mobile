package com.example.nusantaraview.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class UserGallery(
    val id: String? = null,

    val caption: String? = null, // Bisa null di database

    val location: String, // NOT NULL di database

    @SerialName("image_url")
    val imageUrl: String, // NOT NULL di database (karena ini galeri foto)

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)