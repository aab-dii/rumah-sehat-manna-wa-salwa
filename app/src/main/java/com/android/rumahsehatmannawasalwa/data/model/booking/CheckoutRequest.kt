package com.android.rumahsehatmannawasalwa.data.model.booking

import com.google.gson.annotations.SerializedName

data class CheckoutRequest(
    @SerializedName("service_id") val serviceId: Int,
    @SerializedName("therapist_id") val therapistId: Int,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("booking_time") val bookingTime: String,
    @SerializedName("location_type") val locationType: String,
    @SerializedName("address") val address: String,
    @SerializedName("total_price") val totalPrice: Int
)
