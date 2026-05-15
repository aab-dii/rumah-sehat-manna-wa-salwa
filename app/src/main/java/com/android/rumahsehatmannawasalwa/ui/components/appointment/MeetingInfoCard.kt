package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.ui.components.DetailRowSejajar
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.components.StatusBadge
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils.formatDateHuman

@Composable
fun MeetingInfoCard(data: BookingUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DividerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Status", fontSize = 14.sp, color = GrayText)
                StatusBadge(text = data.statusLabel, color = data.statusColor)
            }
            DetailRowSejajar("Nomor", "#${data.displayId}", isBold = true)
            
            val formattedDate = data.appointment?.bookingDate?.let {
                formatDateHuman(it)
            } ?: "-"
            DetailRowSejajar("Waktu Reservasi", formattedDate)
        }
    }
}