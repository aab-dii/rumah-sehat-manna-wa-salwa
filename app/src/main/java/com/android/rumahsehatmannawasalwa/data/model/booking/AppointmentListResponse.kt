package com.android.rumahsehatmannawasalwa.data.model.booking

import com.android.rumahsehatmannawasalwa.data.model.meta.Meta
import com.android.rumahsehatmannawasalwa.data.model.common.Pagination
import com.google.gson.annotations.SerializedName


data class BookingListResponse(
    @SerializedName("data") val data: Pagination<BookingListItem>,
    @SerializedName("meta") val meta: Meta? = null
)

data class BookingListItem(
    val id: Int,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("booking_time") val bookingTime: String,
    @SerializedName("status") val status: String,
    @SerializedName("patient_id") val patientId: Int = 0,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("therapist_id") val therapistId: Int = 0,
    @SerializedName("therapist_name") val therapistName: String,
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("total_price") val totalPrice: Int,
)