package com.android.rumahsehatmannawasalwa.data.model.auth

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: User
)
