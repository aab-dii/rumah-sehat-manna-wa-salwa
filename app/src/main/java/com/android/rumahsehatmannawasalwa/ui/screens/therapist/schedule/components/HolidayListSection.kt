package com.android.rumahsehatmannawasalwa.ui.screens.therapist.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.data.model.schedule.Schedule
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HolidayListSection(holidays: List<Schedule>) {
    Column {
        Text(
            text = "Jadwal Libur Ekstra",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp, start = 4.dp)
        )

        if (holidays.isEmpty()) {
            // Gaya mirip EmptyAgendaCard di Dashboard Terapis
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GreenSoft)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Jadwal Libur Kosong",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GreenLight
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color.LightGray, spotColor = Color.LightGray)
            ) {
                holidays.forEachIndexed { index, holiday ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dateText = if (holiday.endDate != null && holiday.endDate != holiday.specificDate) {
                                "${FormatterUtils.formatShortDateIndo(holiday.specificDate)} - ${FormatterUtils.formatShortDateIndo(holiday.endDate)}"
                            } else {
                                FormatterUtils.formatShortDateIndo(holiday.specificDate)
                            }

                            Text(dateText, fontWeight = FontWeight.Bold, color = TextPrimary)
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Red.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Libur", color = Color.Red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        
                        if (!holiday.note.isNullOrEmpty()) {
                            Text(
                                text = holiday.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    if (index < holidays.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}
