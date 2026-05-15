package com.android.rumahsehatmannawasalwa.data.model.booking

import com.android.rumahsehatmannawasalwa.data.model.meta.Meta
import com.google.gson.annotations.SerializedName

data class BookingResponse(
    @SerializedName("data") val data: DetailAppointmentResponse,
    @SerializedName("meta") val meta: Meta? = null
)

data class SingleBookingResponse(
    @SerializedName("data") val data: DetailAppointmentData,
    @SerializedName("meta") val meta: Meta
)

data class BookingCreateResponse(
    @SerializedName("data") val data: BookingData,
    @SerializedName("meta") val meta: Meta
)

data class BookingData(
    val id : Int
)
