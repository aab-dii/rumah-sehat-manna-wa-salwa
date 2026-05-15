package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.ui.components.DetailRowSejajar
import com.android.rumahsehatmannawasalwa.ui.theme.DividerColor
import com.android.rumahsehatmannawasalwa.ui.theme.DividerLight
import com.android.rumahsehatmannawasalwa.ui.theme.GrayText
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.SlateTextDark

@Composable
fun AppointmentListCard(
    serviceName: String,
    statusLabel: String,
    statusColor: Color,
    dateInfo: String,
    personLabel1: String,
    personLabel2: String?,
    onClick: () -> Unit,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, statusColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    text = serviceName,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextDark,
                    fontSize = 16.sp
                )

                Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(50.dp), border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.5f))) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(Icons.Default.Event, dateInfo)
                InfoRow(Icons.Default.Person, personLabel1)
                if (personLabel2 != null) {
                    InfoRow(Icons.Default.Person, personLabel2)
                }
            }

            if (actions != null) {
//                Spacer(modifier = Modifier.height(2.dp))
//                HorizontalDivider(color = DividerColor, thickness = 2.dp)
//                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    content = actions
                )
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = GreenPrimary)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = GrayText, fontWeight = FontWeight.Medium)
    }
}