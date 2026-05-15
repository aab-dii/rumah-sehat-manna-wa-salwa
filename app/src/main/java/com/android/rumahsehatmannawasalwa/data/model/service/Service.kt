package com.android.rumahsehatmannawasalwa.data.model.service

import com.google.gson.annotations.SerializedName

data class Service(
    val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("price") val price: Int = 0,
    @SerializedName("duration_minutes") val duration: Int = 0,
    @SerializedName("description") val description: String = "",
    @SerializedName("full_image_url") val imageUrl: String? = null
)
