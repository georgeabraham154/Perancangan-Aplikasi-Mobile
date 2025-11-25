// data/model/Destination.kt
package com.example.nusantaraview.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Destination(
    val id: String? = null,
    val name: String,
    val location: String,
    @SerialName("ticket_price")
    val ticketPrice: Int,
    val description: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class DestinationInsert(
    val name: String,
    val location: String,
    @SerialName("ticket_price")
    val ticketPrice: Int,
    val description: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("user_id")
    val userId: String
)