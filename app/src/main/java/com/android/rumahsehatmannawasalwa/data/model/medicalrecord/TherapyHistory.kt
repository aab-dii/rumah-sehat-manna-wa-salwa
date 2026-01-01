package com.android.rumahsehatmannawasalwa.data.model.medicalrecord

import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.google.gson.annotations.SerializedName

data class TherapyHistory(
    @SerializedName("id") val id: Int,
    @SerializedName("diagnosis") val diagnosis: String,
    @SerializedName("patient_complaint") val patientComplaint: String,
    @SerializedName("therapist_action") val therapistAction: String,
    @SerializedName("examination_date") val examinationDate: String,
    @SerializedName("therapist") val therapist: User?
)
