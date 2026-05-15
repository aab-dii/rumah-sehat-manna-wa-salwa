package com.android.rumahsehatmannawasalwa.data.model.booking

import androidx.compose.ui.graphics.Color
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils

data class BookingUiModel(
    val appointment: AppointmentInfo?,
    val displayId : String,
    val statusLabel: String,
    val statusColor: Color,
    val paymentMethod: String,
    val paymentStatus: String,
    val patient: User?,
    val therapist: User?,
    val service: Service?,
    val transaction: ApiTransaction?
)