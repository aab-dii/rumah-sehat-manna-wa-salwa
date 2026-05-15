package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary
import com.android.rumahsehatmannawasalwa.ui.theme.DividerLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.SlateTextDark
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils

@Composable
fun TherapyHistoryItemCard(
    record: TherapyHistorySummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, GreenLight),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Baris atas: nama layanan + badge "Selesai" ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.serviceName,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextDark,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Baris info: tanggal, terapis, pasien (opsional) ─────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(
                    icon = Icons.Default.Event,
                    text = buildString {
                        append(FormatterUtils.formatDateHuman(record.examinationDate))
                        val time = FormatterUtils.formatTimeRange(record.bookingTime)
                        if (time != "-") append(", $time")
                    }
                )
                InfoRow(
                    icon = Icons.Default.PersonOutline,
                    text = record.therapistName
                )
            }
        }
    }
}
