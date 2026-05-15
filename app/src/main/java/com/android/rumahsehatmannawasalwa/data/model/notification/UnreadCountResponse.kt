package com.android.rumahsehatmannawasalwa.data.model.notification

import com.google.gson.annotations.SerializedName

data class UnreadCountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UnreadCountData
)

data class UnreadCountData(
    @SerializedName("count") val count: Int
)
