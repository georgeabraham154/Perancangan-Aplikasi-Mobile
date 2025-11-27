package com.example.nusantaraview.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class Souvenir(
    val id: String? = null,

    @SerialName("item_name")
    val itemName: String,

    @SerialName("store_name")
    val storeName: String,

    val price: Int,

    val description: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
