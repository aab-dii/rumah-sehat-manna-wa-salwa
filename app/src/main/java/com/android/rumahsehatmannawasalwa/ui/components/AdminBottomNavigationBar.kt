package com.android.rumahsehatmannawasalwa.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AdminBottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Booking", "admin_bookings", Icons.Default.DateRange),
        BottomNavItem("Med Rec", "admin_med_records", Icons.Default.FavoriteBorder), // History
        BottomNavItem("Jadwal", "admin_schedule", Icons.Default.Edit), // Schedule
        BottomNavItem("Layanan", "admin_services", Icons.Default.Info), // Services
        BottomNavItem("Pengguna", "admin_users", Icons.Default.Person) // Users
    )

    NavigationBar(
        containerColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                selected = currentRoute == item.route,
                onClick = {
                     if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("admin_bookings") { saveState = true } // Base destination
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray
                )
            )
        }
    }
}
