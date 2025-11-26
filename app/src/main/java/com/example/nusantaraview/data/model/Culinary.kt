package com.example.nusantaraview.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@InternalSerializationApi
@Serializable
data class Culinary(
    val id: String? = null,

    @SerialName("nama_makanan")
    val namaMakanan: String,

    @SerialName("nama_warung")
    val namaWarung: String,

    val harga: Int,

    @SerialName("foto_url")
    val fotoUrl: String? = null,

    @SerialName("is_recommended")
    val isRecommended: Boolean = false,

    @SerialName("created_at")
    val createdAt: String? = null
)
