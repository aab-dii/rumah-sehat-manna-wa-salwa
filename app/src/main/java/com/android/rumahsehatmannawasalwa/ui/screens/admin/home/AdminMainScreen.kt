package com.android.rumahsehatmannawasalwa.ui.screens.admin.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.rumahsehatmannawasalwa.ui.navigation.AdminRoute
import com.android.rumahsehatmannawasalwa.ui.navigation.DrawerItems
import com.android.rumahsehatmannawasalwa.ui.screens.admin.bookings.AdminBookingScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.bookings.AppointmentDetailScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.users.AdminManageUsersScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Track current route for title and active states
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AdminRoute.Home.route
    
    val currentTitle = when (currentRoute) {
        AdminRoute.Home.route -> AdminRoute.Home.title
        AdminRoute.Users.route -> AdminRoute.Users.title
        AdminRoute.Appointments.route -> AdminRoute.Appointments.title
        AdminRoute.Services.route -> AdminRoute.Services.title
        AdminRoute.Schedules.route -> AdminRoute.Schedules.title
        AdminRoute.Settings.route -> AdminRoute.Settings.title
        else -> "Admin Dashboard"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Header Drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "A",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Admin Klinik",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Administrator",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Menu Items
                DrawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    label = { Text("Keluar", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            // BottomBar removed as requested
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = AdminRoute.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(AdminRoute.Home.route) {
                    AdminHomeScreen(navController = navController, onLogout = onLogout)
                }
                
                composable(AdminRoute.Users.route) {
                    AdminManageUsersScreen(navController = navController)
                }
                
                composable(AdminRoute.Appointments.route) {
                    AdminBookingScreen(navController = navController)
                }
                
                composable(
                    route = AdminRoute.AppointmentDetail.route,
                    arguments = listOf(androidx.navigation.navArgument("bookingId") { type = androidx.navigation.NavType.IntType })
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
                    AppointmentDetailScreen(navController = navController, bookingId = bookingId)
                }
                
                // Drawer Destinations
                composable(AdminRoute.Services.route) { PlaceholderScreen("Kelola Layanan") }
                composable(AdminRoute.Schedules.route) { PlaceholderScreen("Jadwal Terapis") }
                composable(AdminRoute.Settings.route) { PlaceholderScreen("Pengaturan Akun") }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Construction, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Fitur $title belum tersedia", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        }
    }
}
