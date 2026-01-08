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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.android.rumahsehatmannawasalwa.ui.screens.admin.users.AdminUserDetailScreen
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    onLogout: () -> Unit,
    adminUserViewModel: AdminUserViewModel,
    layananViewModel: com.android.rumahsehatmannawasalwa.ui.viewmodel.service.LayananViewModel,
    onUserClick: (Int) -> Unit,
    onAddUserClick: () -> Unit,
    onServiceClick: (Int) -> Unit,
    onAddServiceClick: () -> Unit,
    onTherapistClick: (Int) -> Unit,
    onAddBookingClick: () -> Unit
) {
    val navController = rememberNavController()
    // ...
    // Note: I need to target the body change too, but replace_file does contiguous blocks.
    // I will do signature first then body, or try to capture both if close enough?
    // They are far apart (Lines 35 vs 221). Better to do 2 calls or use multi_replace.
    // I'll use separate calls for safety. This replacement is for Signature.

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

    // Get current user for Drawer Header
    val currentUser = com.google.firebase.ktx.Firebase.auth.currentUser
    val photoUrl = currentUser?.photoUrl

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                // Header Drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary), // Use GreenPrimary
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(64.dp)
                        ) {
                            if (photoUrl != null) {
                                coil.compose.AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Admin Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentUser?.displayName?.firstOrNull()?.toString()?.uppercase() ?: "A",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentUser?.displayName ?: "Admin Klinik",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.email ?: "Administrator",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Menu Items
                DrawerItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationDrawerItem(
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = null, 
                                tint = if (isSelected) com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary else Color.Gray 
                            ) 
                        },
                        label = { 
                            Text(
                                item.title, 
                                color = if (isSelected) com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary else Color.Black
                            ) 
                        },
                        selected = isSelected,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenContainer,
                            unselectedContainerColor = Color.Transparent
                        ),
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .padding(end = 12.dp)
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
                        containerColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary, // Header Hijau
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
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
                    AdminManageUsersScreen(
                        navController = navController,
                        viewModel = adminUserViewModel,
                        onUserClick = onUserClick,
                        onAddUserClick = onAddUserClick
                    )
                }
                
                composable(AdminRoute.Appointments.route) {
                    AdminBookingScreen(
                        navController = navController,
                        onAddClick = onAddBookingClick
                    )
                }
                
                composable(
                    route = AdminRoute.AppointmentDetail.route,
                    arguments = listOf(androidx.navigation.navArgument("bookingId") { type = androidx.navigation.NavType.IntType })
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
                    AppointmentDetailScreen(navController = navController, bookingId = bookingId)
                }
                
                // Drawer Destinations
                composable(AdminRoute.Services.route) { 
                    com.android.rumahsehatmannawasalwa.ui.screens.admin.services.AdminManageServicesScreen(
                        navController = navController,
                        viewModel = layananViewModel,
                        onServiceClick = onServiceClick,
                        onAddServiceClick = onAddServiceClick
                    ) 
                }
                composable(AdminRoute.Schedules.route) { 
                    com.android.rumahsehatmannawasalwa.ui.screens.admin.schedule.AdminTherapistListScreen(
                        navController = navController, // Inner nav controller? Or can we use it?
                        // We need to navigate to Outer Nav Controller for Detail.
                        // AdminMainScreen doesn't usually expose outer nav controller to inner screens unless passed.
                        // Wait, AdminMainScreen receives `onServiceClick` etc callbacks.
                        // I should verify how callbacks are passed.
                        // Yes, passed as params. I need to add `onScheduleClick` param to AdminMainScreen.
                        viewModel = adminUserViewModel,
                        onTherapistClick = onTherapistClick
                    ) 
                }
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
