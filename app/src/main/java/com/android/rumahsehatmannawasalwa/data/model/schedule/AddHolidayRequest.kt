package com.android.rumahsehatmannawasalwa.data.model.schedule

import com.google.gson.annotations.SerializedName

data class AddHolidayRequest(
    @SerializedName("therapist_id") val therapistId: Int,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    val reason: String
)

data class AddHolidayResponse(
    @SerializedName("cancelled_bookings") val cancelledBookings: Int
)
