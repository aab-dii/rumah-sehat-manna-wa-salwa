package com.android.rumahsehatmannawasalwa.data.model.service

import com.google.gson.annotations.SerializedName

data class Layanan(
    val id: Int = 0, // Laravel ID is usually Int
    @SerializedName("name") val nama: String = "",
    @SerializedName("price") val harga: Int = 0,
    @SerializedName("duration_minutes") val durasi: Int = 0,
    @SerializedName("description") val deskripsi: String = "",
    @SerializedName("full_image_url") val imageUrl: String? = null
)
