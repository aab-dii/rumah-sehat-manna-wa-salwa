package com.android.rumahsehatmannawasalwa.data.model.booking

import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.service.Layanan
import com.google.gson.annotations.SerializedName

data class ApiBooking(
    @SerializedName("id") val id: Int,
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("service_id") val serviceId: Int,
    @SerializedName("therapist_id") val therapistId: Int,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("booking_time") val bookingTime: String,
    @SerializedName("status") val status: String,
    @SerializedName("total_price") val totalPrice: Int,
    @SerializedName("service") val service: Layanan?,
    @SerializedName("therapist") val therapist: User?,
    @SerializedName("patient") val patient: User?
)
