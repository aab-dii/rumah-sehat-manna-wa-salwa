package com.android.rumahsehatmannawasalwa.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.ui.theme.DividerLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HorizontalCalendar(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    activeDays: List<String>? = null,
    holidayInfo: Map<LocalDate, String> = emptyMap(),
    availabilityMap: Map<String, String> = emptyMap(),
    enabled: Boolean = true
) {
    // --- 1. SETTING WAKTU & BATAS 14 HARI ---
    val effectiveStartDate = remember {
        // Jika sudah lewat jam 8 malam (20:00), mulai dari besok
        if (java.time.LocalTime.now().isAfter(java.time.LocalTime.of(20, 0)))
            LocalDate.now().plusDays(1)
        else
            LocalDate.now()
    }

    val maxSelectableDate = remember { effectiveStartDate.plusDays(13) }
    var currentWeekStart by remember { mutableStateOf(effectiveStartDate) }

    val days = (0..6).map { currentWeekStart.plusDays(it.toLong()) }

    // --- 2. FORMATTERS ---
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID")) }
    val dayLabelFormatter = remember { DateTimeFormatter.ofPattern("EEE", Locale("id", "ID")) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd") }

    // --- 3. HELPER FUNCTION ---
    fun getIndonesianDayName(date: LocalDate): String {
        return when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Senin"
            java.time.DayOfWeek.TUESDAY -> "Selasa"
            java.time.DayOfWeek.WEDNESDAY -> "Rabu"
            java.time.DayOfWeek.THURSDAY -> "Kamis"
            java.time.DayOfWeek.FRIDAY -> "Jumat"
            java.time.DayOfWeek.SATURDAY -> "Sabtu"
            java.time.DayOfWeek.SUNDAY -> "Minggu"
            else -> ""
        }
    }

    Column(modifier = modifier.alpha(if (enabled) 1f else 0.5f)) {
        // Header Navigasi (Minggu Sebelumnya / Sesudahnya)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) },
                enabled = enabled && currentWeekStart.isAfter(effectiveStartDate)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Minggu Sebelumnya",
                    tint = if (enabled && currentWeekStart.isAfter(effectiveStartDate)) GreenPrimary else Color.LightGray
                )
            }

            Text(
                text = currentWeekStart.format(monthFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val canGoNext = currentWeekStart.plusWeeks(1).isBefore(maxSelectableDate.plusDays(1))
            IconButton(
                onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) },
                enabled = enabled && canGoNext
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Minggu Selanjutnya",
                    tint = if (enabled && canGoNext) GreenPrimary else Color.LightGray
                )
            }
        }

        // Baris Tanggal (LazyRow bisa, tapi Row lebih rapi untuk 7 hari fix)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = remember(currentWeekStart) {
                (0..6).map { currentWeekStart.plusDays(it.toLong()) }
            }

            days.forEach { date ->
                val dateStr = date.toString() // Ini harus "2026-02-20"
                val isSelected = date == selectedDate
                val isHoliday = holidayInfo.containsKey(date)
                val isOutOfRange = date.isAfter(maxSelectableDate) || date.isBefore(effectiveStartDate)

                // --- BAGIAN DEBUG ---
                val status = remember(availabilityMap) { availabilityMap[dateStr] ?: "none" }
                val hasData = remember(availabilityMap) { availabilityMap.isNotEmpty() }

                val isAvailable = if (!hasData) true else status == "available"

                // Syarat Disabled: Holiday OR OutOfRange OR (Data Ada tapi Gak Available)
                val isDateDisabled = isOutOfRange || isHoliday || !enabled || !isAvailable

                Log.d("CalendarFinal", "Tgl: $dateStr | MapStatus: $status | hasData: $hasData | Disabled: $isDateDisabled")

                // D. Penentuan Warna UI
                val backgroundColor = when {
                    isDateDisabled -> DividerLight // Abu-abu Terang
                    isSelected -> GreenPrimary          // Hijau Utama
                    else -> Color.White
                }

                val textColor = when {
                    isDateDisabled -> Color.LightGray
                    isSelected -> Color.White
                    isHoliday -> Color.Red
                    else -> Color.DarkGray
                }

                val borderColor = when {
                    isSelected -> GreenPrimary
                    !isDateDisabled -> GreenPrimary.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(backgroundColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable(enabled = !isDateDisabled) {
                            Log.d("CalendarClick", "KLIK: $dateStr | Status di Map: $status | isDisabled: $isDateDisabled")

                            if (!isDateDisabled) {
                                onDateSelected(date)
                            } else {
                                Log.d("CalendarClick", "KLIK DITOLAK karena disabled!")
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.format(dayLabelFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = date.format(dateFormatter),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    // Label kecil jika statusnya "Full" di data API
                    if (status == "full" && !isOutOfRange) {
                        Text(
                            text = "Penuh",
                            color = Color.Red,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}