package com.android.rumahsehatmannawasalwa.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingRequest(
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("service_id") val serviceId: Int,
    @SerializedName("therapist_id") val therapistId: Int,
    @SerializedName("booking_date") val bookingDate: String, // YYYY-MM-DD
    @SerializedName("start_time") val bookingTime: String, // HH:MM
    @SerializedName("total_price") val totalPrice: Int,
    @SerializedName("status") val status: String? = "pending"
)
