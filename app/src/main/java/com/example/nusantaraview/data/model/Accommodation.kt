package com.example.nusantaraview.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@InternalSerializationApi
@Serializable
data class Accommodation(
    val id: String? = null,
    val name: String,
    val facilities: String,
    @SerialName("price_per_night")
    val pricePerNight: Int,
    val description: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
