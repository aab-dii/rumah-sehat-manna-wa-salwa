package com.android.rumahsehatmannawasalwa.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminRoute(val route: String, val title: String, val icon: ImageVector) {
    object Home : AdminRoute("admin_home", "Beranda", Icons.Default.Home)
    object Users : AdminRoute("admin_users", "Kelola Pengguna", Icons.Default.Group) // Replaces Patients/Therapists
    object Appointments : AdminRoute("admin_appointments", "Kelola Janji Temu", Icons.Default.DateRange)
    
    object Services : AdminRoute("admin_services", "Kelola Layanan", Icons.Default.Spa)
    object Schedules : AdminRoute("admin_schedules", "Jadwal Terapis", Icons.Default.Schedule)
    object Settings : AdminRoute("admin_settings", "Pengaturan", Icons.Default.Settings)
    
    // Detail Routes
    object AppointmentDetail : AdminRoute("admin_appointment_detail/{bookingId}", "Detail Booking", Icons.Default.Info) {
        fun createRoute(bookingId: Int) = "admin_appointment_detail/$bookingId"
    }
}

// Helper list for Drawer
val DrawerItems = listOf(
    AdminRoute.Home,
    AdminRoute.Users,
    AdminRoute.Appointments,
    AdminRoute.Services,
    AdminRoute.Schedules,
    AdminRoute.Settings
)
