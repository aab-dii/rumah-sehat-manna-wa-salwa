package com.android.rumahsehatmannawasalwa.data.model.meta

import com.google.gson.annotations.SerializedName

data class Meta(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?
)
