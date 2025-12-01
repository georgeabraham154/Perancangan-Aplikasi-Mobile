package com.example.nusantaraview.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class Culinary(
    @SerialName("id")
    val id: String? = null,

    @SerialName("food_name")
    val foodName: String,

    @SerialName("restaurant_name")
    val restaurantName: String,

    @SerialName("price")
    val price: Int,

    @SerialName("description")
    val description: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
