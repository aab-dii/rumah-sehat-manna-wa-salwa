package com.android.rumahsehatmannawasalwa.data.model.schedule

import com.google.gson.annotations.SerializedName

data class EmergencyCloseRequest(
    @SerializedName("therapist_id") val therapistId: Int,
    val date: String, // YYYY-MM-DD
    val reason: String
)

data class EmergencyCloseResponse(
    @SerializedName("cancelled_count") val cancelledCount: Int
)
