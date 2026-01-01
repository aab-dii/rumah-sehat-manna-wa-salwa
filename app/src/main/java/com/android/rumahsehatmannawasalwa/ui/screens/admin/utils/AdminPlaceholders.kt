package com.android.rumahsehatmannawasalwa.ui.screens.admin.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AdminBookingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Admin Booking (Coming Soon)")
    }
}

@Composable
fun AdminMedicalRecordScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Admin Riwayat Pasien (Coming Soon)")
    }
}

@Composable
fun AdminScheduleScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Admin Jadwal (Coming Soon)")
    }
}

@Composable
fun AdminServiceScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Nanti bisa pakai LayananViewModel yang sudah ada tapi mode Admin
        Text("Halaman Admin Layanan (Coming Soon)")
    }
}
