package com.android.rumahsehatmannawasalwa.data.model.booking

import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.meta.Meta
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.google.gson.annotations.SerializedName

data class DetailAppointmentResponse(
    @SerializedName("data") val data: DetailAppointmentData,
    @SerializedName("meta") val meta: Meta? = null
)

data class DetailAppointmentData(
    @SerializedName("appointment") val appointment: AppointmentInfo,
    @SerializedName("service") val service: Service?,
    @SerializedName("therapist") val therapist: User?,
    @SerializedName("patient") val patient: User?,
    @SerializedName("transaction") val transaction: ApiTransaction?,
    @SerializedName("location_type") val locationType: String?
)

data class AppointmentInfo(
    val id: Int,

    @SerializedName("booking_date")
    val bookingDate: String,

    @SerializedName("booking_time")
    val bookingTime: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("total_price")
    val totalPrice: String,

    @SerializedName("cancelation_reason")
    val cancellationReason: String?,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("payment_deadline")
    val paymentDeadline: String?,

    @SerializedName("payment_remaining_seconds")
    val paymentRemainingSeconds: Long? = null,

    @SerializedName("therapy_record_id")
    val therapyRecordId: Int? = null,

    @SerializedName("is_expired_warning")
    val isExpiredWarning: Boolean = false
)