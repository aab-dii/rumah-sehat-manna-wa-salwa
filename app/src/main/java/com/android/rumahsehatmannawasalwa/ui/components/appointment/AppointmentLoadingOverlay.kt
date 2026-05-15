package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.android.rumahsehatmannawasalwa.ui.theme.*

// ui/components/LoadingOverlay.kt

@Composable
fun AppointmentLoadingOverlay() {
    // Gunakan Box untuk menumpuk di atas konten utama
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)) // Efek gelap transparan
            .zIndex(99f) // Pastikan paling atas
            .clickable(enabled = false) { }, // Lock layar agar tidak bisa diklik
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = GreenPrimary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(44.dp)
                )
                Text(
                    "Memproses Booking...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SlateTextDark
                )
                Text(
                    "Mohon tunggu sebentar, janji temu\nAnda sedang kami siapkan.",
                    fontSize = 12.sp,
                    color = GrayText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}