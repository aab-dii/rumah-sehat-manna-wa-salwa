package com.android.rumahsehatmannawasalwa.ui.screens.admin.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.viewmodel.admin.AdminDashboardViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.notification.NotificationViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.data.model.dashboard.DashboardAgendaItem
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.android.rumahsehatmannawasalwa.ui.theme.AccentAmber
import com.android.rumahsehatmannawasalwa.ui.theme.AccentBlue
import com.android.rumahsehatmannawasalwa.ui.theme.AccentEmerald
import com.android.rumahsehatmannawasalwa.ui.theme.AccentOrange
import com.android.rumahsehatmannawasalwa.ui.theme.AccentPink
import com.android.rumahsehatmannawasalwa.ui.theme.AccentTeal
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundWhite
import com.android.rumahsehatmannawasalwa.ui.theme.DividerColor
import com.android.rumahsehatmannawasalwa.ui.theme.GrayText
import com.android.rumahsehatmannawasalwa.ui.theme.GreenDark
import com.android.rumahsehatmannawasalwa.ui.theme.GreenLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.GreenSoft
import com.android.rumahsehatmannawasalwa.ui.theme.SlateText
import com.android.rumahsehatmannawasalwa.ui.theme.SurfaceLight
import com.android.rumahsehatmannawasalwa.ui.viewmodel.admin.AdminDashboardState
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import java.time.LocalTime



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel,
    notificationViewModel: NotificationViewModel,
    authViewModel: AuthViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
        notificationViewModel.loadUnreadCount()
    }

    val user by authViewModel.currentUserData.collectAsState()
    
    // Aktifkan update real-time jika user ID tersedia
    LaunchedEffect(user) {
        user?.id?.let { notificationViewModel.listenToRealtimeUpdates(it) }
    }

    val adminState by viewModel.adminState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        PullToRefreshBox(
            isRefreshing = adminState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                adminState.isLoading -> {
                    DashboardShimmer()
                }
                adminState.errorMessage != null -> {
                    val msg = adminState.errorMessage
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Error: $msg", color = Color.Red)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                else -> {
                    DashboardContent(navController, adminState, unreadCount)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    navController: NavController,
    state: AdminDashboardState,
    unreadCount: Int
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            AdminGreetingHeader(
                bookingCount = state.todayStats.confirmed,
                unreadCount = unreadCount,
                onNotifClick = { navController.navigate(Screen.Notifications.route) }
            )
        }
        // white sheet
        item {
            val greenOverlap = 40.dp
            val cardShiftUp  = 65.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shiftUp(greenOverlap)
                    .background(BackgroundWhite, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(bottom = 16.dp)
            ) {

                Column(modifier = Modifier.shiftUp(cardShiftUp)) {
                    // 2. Stats Card
                    AdminStatsCard(state = state)

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Quick Access Card
                    AdminQuickActionsCard(navController)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Agenda Section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("Jadwal Terdekat")
                    TextButton(onClick = { navController.navigate(Screen.AdminAppointment.route) }) {
                        Text("Lihat Semua", fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                if (state.upcomingAgenda.isEmpty()) {
                    UpcomingEmptyState()
                } else {
                    state.upcomingAgenda.forEach { booking ->
                        AdminAgendaCard(booking, navController)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminGreetingHeader(bookingCount: Int, unreadCount: Int, onNotifClick: () -> Unit) {
    val greeting = adminGreeting()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Brush.verticalGradient(colors = listOf(GreenDark, GreenPrimary, GreenLight)))
            .padding(horizontal = 20.dp)
            .padding(top = 30.dp, bottom = 80.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(greeting, fontSize = 12.sp, color = Color.White.copy(alpha = 0.78f), fontWeight = FontWeight.Medium)
                Text("Halo, Admin! 👋", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$bookingCount Pasien hari ini",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onNotifClick() },
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White,
                                modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                            ) {
                                Text(if (unreadCount > 99) "99+" else unreadCount.toString(), fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Outlined.Notifications, "Notifikasi", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun AdminStatsCard(
    state: AdminDashboardState,
    modifier: Modifier = Modifier
) {
    Column {
        Card(
            modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, GreenPrimary)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pendapatan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminRevenueItem(
                            modifier = Modifier.weight(1f),
                            value = FormatterUtils.formatRupiah(state.monthlyRevenue),
                            onClick = { }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, GreenPrimary)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Statistik",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminStatItem(
                            modifier = Modifier.weight(1f),
                            title = "Terjadwal",
                            value = state.adminStats.confirmed.toString(),
                            onClick = { }
                        )
                        AdminStatItem(
                            modifier = Modifier.weight(1f),
                            title = "Menunggu Konfirmasi",
                            value = state.adminStats.pending.toString(),
                            onClick = { }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminStatItem(
                            modifier = Modifier.weight(1f),
                            title = "Menunggu Verifikasi",
                            value = state.adminStats.waitingVerification.toString(),
                            onClick = { }
                        )
                        AdminStatItem(
                            modifier = Modifier.weight(1f),
                            title = "Batal",
                            value = state.adminStats.canceled.toString(),
                            onClick = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatItem(
    modifier: Modifier,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = DividerColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GreenPrimary)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, color = GreenPrimary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = value, color = GreenPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun AdminRevenueItem(
    modifier: Modifier,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = SurfaceLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GreenPrimary)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column() {
                Text(text = value, color = GreenPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminQuickActionsCard(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, GreenPrimary),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Akses Cepat", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SlateText, modifier = Modifier.padding(bottom = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AdminQuickActionItem(
                    icon = Icons.Default.Group,
                    label = "Akun",
                    color = GreenPrimary,
                    onClick = { navController.navigate(Screen.AdminManageUser.route) }
                )
                AdminQuickActionItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Jadwal",
                    color = GreenPrimary,
                    onClick = { navController.navigate(Screen.AdminTherapistList.route) }
                )
                AdminQuickActionItem(
                    icon = Icons.Default.Spa,
                    label = "Layanan",
                    color = GreenPrimary,
                    onClick = { navController.navigate(Screen.AdminManageService.route) }
                )


            }
        }
    }
}

@Composable
fun AdminQuickActionItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(colors = listOf(color, color.copy(alpha = 0.7f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SlateText)
    }
}

@Composable
fun AdminAgendaCard(booking: DashboardAgendaItem, navController: NavController) {
    val timeFormatted = booking.bookingTime.take(5)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { navController.navigate(Screen.AdminAppointmentDetail.createRoute(booking.id)) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
                Text(
                    text = timeFormatted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenPrimary)
                Text(
                    text = "WITA",
                    fontSize = 9.sp,
                    color = GrayText)
            }
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(40.dp)
                    .background(GreenSoft))
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = booking.patientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(
                    text = "Terapis: ${booking.therapistName}",
                    fontSize = 12.sp,
                    color = GrayText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(
                    text = booking.serviceName,
                    fontSize = 11.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Medium)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GrayText,
                modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun UpcomingEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GreenSoft)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🗓️", fontSize = 32.sp)
            Text("Belum ada jadwal terdekat", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GreenPrimary)
            Text("Jadwal operasional akan muncul di sini", fontSize = 12.sp, color = GrayText)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = SlateText)
}

// --- Modifiers & Helpers ---
private fun Modifier.shiftUp(amount: Dp): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val shiftPx   = amount.roundToPx()
        layout(placeable.width, (placeable.height - shiftPx).coerceAtLeast(0)) {
            placeable.placeRelative(0, -shiftPx)
        }
    }

private fun adminGreeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 10 -> "Selamat Pagi"
        hour < 15 -> "Selamat Siang"
        hour < 18 -> "Selamat Sore"
        else      -> "Selamat Malam"
    }
}

@Composable
fun DashboardShimmer() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = GreenPrimary)
    }
}
