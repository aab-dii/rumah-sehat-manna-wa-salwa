package com.android.rumahsehatmannawasalwa.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary

@Composable
fun TimeSlotGrid(
    slots: List<String>,
    selectedSlot: String?,
    isLoading: Boolean, // ✅ Tambahkan parameter baru ini
    onSlotSelected: (String) -> Unit
) {
    when {
        // --- 1. STATE LOADING ---
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // Beri tinggi tetap agar layout tidak melompat
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = GreenPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Mencari jam tersedia...",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // --- 2. STATE KOSONG ---
        slots.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada jadwal tersedia untuk tanggal ini",
                    color = Color.Red.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- 3. STATE DATA ADA (GRID) ---
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp) // Sesuaikan max height
            ) {
                items(slots) { slot ->
                    val isSelected = slot == selectedSlot

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) GreenPrimary else Color.White,
                        label = "bgColor"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color.DarkGray,
                        label = "textColor"
                    )
                    val elevation by animateDpAsState(
                        targetValue = if (isSelected) 4.dp else 0.dp,
                        label = "elevation"
                    )

                    Box(
                        modifier = Modifier
                            .shadow(elevation, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundColor)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GreenPrimary else Color.LightGray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSlotSelected(slot) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = slot,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}