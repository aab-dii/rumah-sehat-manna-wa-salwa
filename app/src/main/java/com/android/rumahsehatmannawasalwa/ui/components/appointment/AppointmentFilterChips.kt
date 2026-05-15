package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.ui.components.Badge
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.BodyGray
import com.android.rumahsehatmannawasalwa.ui.theme.SurfaceGrey
import com.android.rumahsehatmannawasalwa.ui.theme.DividerLight
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface

/**
 * Satu item chip (label tampilan + nilai API untuk filter status).
 * [value] bisa berupa satu status ("pending") atau lebih dari satu ("pending,confirmed").
 */
data class FilterChipOption(
    val label: String,
    val value: String
)

/**
 * Baris chip filter horizontal yang bisa di-scroll.
 * Bisa dipakai di semua role (pasien, admin, terapis).
 *
 * @param options    Daftar pilihan chip
 * @param selected   Nilai [FilterChipOption.value] yang sedang aktif
 * @param onSelected Callback saat chip dipilih, mengirim [FilterChipOption.value]
 */

/**
 * Baris chip filter horizontal yang bisa di-scroll.
 */
@Composable
fun AppointmentFilterChips(
    options: List<FilterChipOption>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .clickable { onSelected(option.value) },
                color = if (isSelected) GreenPrimary.copy(alpha = 0.08f) else SurfaceGrey,
                border = if (isSelected) BorderStroke(1.5.dp, GreenPrimary) else null,
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = option.label,
                    color = if (isSelected) GreenPrimary else BodyGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

// ─── Preset chip options per konteks ────────────────────────────────────────

/** Chip untuk tab "Akan Datang" (pasien maupun admin) */
val upcomingFilterOptions = listOf(
    FilterChipOption("Semua", "pending,confirmed,waiting_payment,waiting_verification,payment_rejected"),
    FilterChipOption("Menunggu Konfirmasi", "pending"),
    FilterChipOption("Terjadwal", "confirmed"),
    FilterChipOption("Menunggu Pembayaran", "waiting_payment"),
    FilterChipOption("Menunggu Verifikasi", "waiting_verification"),
    FilterChipOption("Pembayaran Ditolak", "payment_rejected"),
)

/** Chip untuk tab "Akan Datang" (terapis) */
val therapistUpcomingFilterOptions = listOf(
    FilterChipOption("Semua", "confirmed,in_progress"),
    FilterChipOption("Terkonfirmasi", "confirmed"),
    FilterChipOption("Berjalan", "in_progress")
)

/** Chip untuk tab "Riwayat" (pasien maupun admin) */
val historyFilterOptions = listOf(
    FilterChipOption("Semua", "completed,canceled,force_completed"),
    FilterChipOption("Selesai", "completed,force_completed"),
    FilterChipOption("Dibatalkan", "canceled"),
)

/** Chip untuk tab "Riwayat" (terapis) */
val therapistHistoryFilterOptions = listOf(
    FilterChipOption("Semua", "completed,canceled"),
    FilterChipOption("Selesai", "completed"),
    FilterChipOption("Dibatalkan", "canceled"),
    FilterChipOption("Perlu Isi", "force_completed"),
)
