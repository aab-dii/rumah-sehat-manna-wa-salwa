package com.android.rumahsehatmannawasalwa.data.model.notification

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("type") val type: String?,
    @SerializedName("data") val data: NotificationPayload?,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String?
)

data class NotificationPayload(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("type") val type: String
)