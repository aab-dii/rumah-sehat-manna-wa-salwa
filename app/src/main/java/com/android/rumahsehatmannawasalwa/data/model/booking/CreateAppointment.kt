package com.android.rumahsehatmannawasalwa.data.model.booking

import android.net.Uri

data class CreateAppointment(
    val patientId: Int? = null,
    val serviceId: Int,
    val therapistId: Int,
    val date: String,
    val time: String,
    val price: Int,
    val paymentOption: String,
    val proofUri: Uri?
)