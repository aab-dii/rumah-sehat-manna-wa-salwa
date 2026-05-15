package com.android.rumahsehatmannawasalwa.data.model.notification

import com.google.gson.annotations.SerializedName

// Single wrapper from ResponseFormatter::success($notifications)
data class NotificationListResponse(
    @SerializedName("meta") val meta: Meta?,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: NotificationPaginatedData
)

data class NotificationPaginatedData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("data") val data: List<Notification>,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("total") val total: Int
)

data class Meta(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)
