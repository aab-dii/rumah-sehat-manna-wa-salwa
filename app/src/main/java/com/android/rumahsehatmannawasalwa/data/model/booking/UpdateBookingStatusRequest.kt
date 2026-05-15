package com.android.rumahsehatmannawasalwa.data.model.booking

import com.google.gson.annotations.SerializedName

data class UpdateBookingStatusRequest(
    @SerializedName("status") val status: String
)
