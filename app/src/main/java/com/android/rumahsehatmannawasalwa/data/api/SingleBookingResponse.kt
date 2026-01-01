package com.android.rumahsehatmannawasalwa.data.api

import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking
import com.google.gson.annotations.SerializedName

data class SingleBookingResponse(
    @SerializedName("data") val data: ApiBooking,
    @SerializedName("message") val message: String? = null
)
