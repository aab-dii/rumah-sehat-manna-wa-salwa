//package com.android.rumahsehatmannawasalwa.ui.components
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.filled.EditCalendar
//import androidx.compose.material.icons.filled.Event
//import androidx.compose.material.icons.filled.History
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.graphics.Color
//import androidx.navigation.NavController
//import androidx.navigation.compose.currentBackStackEntryAsState
//import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
//import com.android.rumahsehatmannawasalwa.ui.screens.therapist.home.TherapistHomeScreen
//import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
//
//@Composable
//fun TherapistBottomNavigationBar(navController: NavController) {
//    val items = listOf(
//        BottomNavItem("Beranda", "therapist_home", Icons.Default.Home),
//        BottomNavItem("Janji Temu", "therapist_appointment", Icons.Default.DateRange),
//        BottomNavItem("Jadwal", "therapist_schedule", Icons.Default.EditCalendar),
//        BottomNavItem("Profil", "profile", Icons.Default.Person)
//    )
//
//    NavigationBar(
//        containerColor = Color.White,
//        contentColor = GreenPrimary
//    ) {
//        val navBackStackEntry by navController.currentBackStackEntryAsState()
//        val currentRoute = navBackStackEntry?.destination?.route
//
//        items.forEach { item ->
//            NavigationBarItem(
//                icon = { Icon(item.icon, contentDescription = item.label) },
//                label = { Text(item.label) },
//                selected = currentRoute == item.route,
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = GreenPrimary,
//                    selectedTextColor = GreenPrimary,
//                    indicatorColor = GreenPrimary.copy(alpha = 0.1f),
//                    unselectedIconColor = Color.Gray,
//                    unselectedTextColor = Color.Gray
//                ),
//                onClick = {
//                    navController.navigate(item.route) {
//                        popUpTo(Screen.TherapistHome.route) {
//                            saveState = true
//                        }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                }
//            )
//        }
//    }
//}
