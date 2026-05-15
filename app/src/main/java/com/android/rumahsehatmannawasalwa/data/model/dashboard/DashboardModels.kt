package com.android.rumahsehatmannawasalwa.data.model.dashboard

import com.google.gson.annotations.SerializedName

data class DashboardAction(
    @SerializedName("id") val id: Int,
    @SerializedName("amount") val amount: Long,
    @SerializedName("status") val status: String,
    @SerializedName("patient_name") val patientName: String, // Sesuaikan dengan Larave
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("date") val date: String
)

data class DashboardSchedule(
    @SerializedName("id") val id: Int,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("therapist_name") val therapistName: String,
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("time") val time: String
)