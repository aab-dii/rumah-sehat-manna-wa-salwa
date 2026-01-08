package com.android.rumahsehatmannawasalwa.data.model.schedule

import com.google.gson.annotations.SerializedName

data class Schedule(
    val id: Int,
    @SerializedName("therapist_id") val therapistId: Int,
    val day: String, // Senin, Selasa, ...
    @SerializedName("start_time") val startTime: String, // H:i
    @SerializedName("end_time") val endTime: String, // H:i
    @SerializedName("is_active") val isActive: Boolean,
    val type: String? = "regular",
    @SerializedName("specific_date") val specificDate: String?,
    @SerializedName("end_date") val endDate: String?,
    val note: String?
)

data class UpdateScheduleRequest(
    @SerializedName("therapist_id") val therapistId: Int,
    val day: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("is_active") val isActive: Boolean
)
