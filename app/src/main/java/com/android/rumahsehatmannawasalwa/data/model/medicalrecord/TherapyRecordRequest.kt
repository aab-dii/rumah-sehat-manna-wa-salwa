package com.android.rumahsehatmannawasalwa.data.model.medicalrecord

import com.google.gson.annotations.SerializedName

data class TherapyRecordRequest(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("patient_complaint") val patientComplaint: String,
    @SerializedName("therapist_action") val therapistAction: String,
    @SerializedName("additional_notes") val additionalNotes: String
)
