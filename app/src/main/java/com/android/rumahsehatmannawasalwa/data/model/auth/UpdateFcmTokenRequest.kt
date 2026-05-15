package com.android.rumahsehatmannawasalwa.data.model.auth

import com.google.gson.annotations.SerializedName

data class UpdateFcmTokenRequest(
    @SerializedName("fcm_token")
    val fcmToken: String
)
