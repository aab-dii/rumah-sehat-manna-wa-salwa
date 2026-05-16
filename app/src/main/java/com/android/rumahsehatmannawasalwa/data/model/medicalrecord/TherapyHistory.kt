package com.android.rumahsehatmannawasalwa.data.model.medicalrecord

import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.google.gson.annotations.SerializedName

data class TherapyHistory(
    @SerializedName("id") val id: Int,
    @SerializedName("patient") val patient: User?,
    @SerializedName("therapist") val therapist: User?,
    @SerializedName("patient_complaint") val patientComplaint: String,
    @SerializedName("therapist_action") val therapistAction: String,
    @SerializedName("additional_notes") val additionalNotes: String?,
    @SerializedName("examination_date") val examinationDate: String,
    @SerializedName("booking") val booking: TherapyBookingRef?
)

/** Referensi booking dari response detail */
data class TherapyBookingRef(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("booking_date") val bookingDate: String? = null,
    @SerializedName("booking_time") val bookingTime: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("service") val service: TherapyServiceRef?
)

data class TherapyServiceRef(
    @SerializedName("name") val name: String?,
    @SerializedName("full_image_url") val imageUrl: String? = null
)

