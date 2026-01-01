package com.android.rumahsehatmannawasalwa.data.model.booking

import com.android.rumahsehatmannawasalwa.data.model.auth.Meta
import com.android.rumahsehatmannawasalwa.data.model.common.Pagination
import com.google.gson.annotations.SerializedName

data class BookingResponse(
    @SerializedName("data") val data: Pagination<ApiBooking>,
    @SerializedName("meta") val meta: Meta? = null
)

data class SingleBookingResponse(
    @SerializedName("data") val data: ApiBooking,
    @SerializedName("meta") val meta: Meta
)
