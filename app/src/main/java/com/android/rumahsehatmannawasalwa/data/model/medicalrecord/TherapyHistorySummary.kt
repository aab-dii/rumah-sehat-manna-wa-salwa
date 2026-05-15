package com.android.rumahsehatmannawasalwa.data.model.medicalrecord

import com.google.gson.annotations.SerializedName

data class TherapyHistorySummary(
    @SerializedName("id") val id: Int,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("therapist_name") val therapistName: String,
    @SerializedName("booking_time") val bookingTime: String,
    @SerializedName("examination_date") val examinationDate: String,
    @SerializedName("day") val day: String,
    @SerializedName("day_number") val dayNumber: String,
    @SerializedName("month") val month: String
)
