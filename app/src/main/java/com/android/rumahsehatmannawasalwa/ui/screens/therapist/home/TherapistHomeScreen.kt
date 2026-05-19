package com.android.rumahsehatmannawasalwa.ui.screens.therapist.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch
import com.android.rumahsehatmannawasalwa.data.model.dashboard.DashboardAgendaItem
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.AccentOrange
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundWhite
import com.android.rumahsehatmannawasalwa.ui.theme.BodyGray
import com.android.rumahsehatmannawasalwa.ui.theme.DividerLight
import com.android.rumahsehatmannawasalwa.ui.theme.GrayText
import com.android.rumahsehatmannawasalwa.ui.theme.GreenDark
import com.android.rumahsehatmannawasalwa.ui.theme.GreenLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.GreenSoft
import com.android.rumahsehatmannawasalwa.ui.theme.SlateText
import com.android.rumahsehatmannawasalwa.ui.theme.SlateTextDark
import com.android.rumahsehatmannawasalwa.ui.theme.StatusCancelled
import com.android.rumahsehatmannawasalwa.ui.theme.StatusConfirmed
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.notification.NotificationViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.therapist.TherapistDashboardViewModel
import java.time.LocalTime



// ═════════════════════════════════════════════════════════════════════════════
//  Screen
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun TherapistHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    dashboardViewModel: TherapistDashboardViewModel,
    notificationViewModel: NotificationViewModel
) {
    val user        by authViewModel.currentUserData.collectAsState()
    val userName    = user?.name?.split(" ")?.firstOrNull() ?: "Terapis"
    val greeting    = therapistGreeting()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    // Unified dashboard state from backend
    val dashState by dashboardViewModel.state.collectAsState()
    val summary    = dashState.stats
    val agendaList = dashState.agenda
    val isLoading  = dashState.isLoading

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = BackgroundWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── 1. Header ─────────────────────────────────────────────────
            item {
                TherapistGreetingHeader(
                    userName     = userName,
                    greeting     = greeting,
                    unreadCount  = unreadCount,
                    bottomExtra  = 60.dp,
                    onNotifClick = { navController.navigate(Screen.Notifications.route) }
                )
            }

            // ── 2+3+4. White Sheet ────────────────────────────────────────
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
                    // ── 2. Summary Card ────────────────────────────────────
                    StatistikSection(
                        total         = summary.total,
                        confirmed     = summary.confirmed,
                        completed     = summary.completed,
                        isLoading     = isLoading,
                        modifier      = Modifier.shiftUp(cardShiftUp).padding(horizontal = 20.dp)
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── 3. Quick Actions ───────────────────────────────────

                    QuickActionsGrid(
                        navController = navController,
                        onArticleClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Fitur Kelola Artikel akan segera hadir!")
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    // ── 4. Agenda Terdekat ─────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        SectionTitle("Agenda Terdekat", modifier = Modifier)
                        TextButton(onClick = { navController.navigate(Screen.TherapistAppointment.route) }) {
                            Text("Lihat Semua", fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (agendaList.isEmpty() && !isLoading) {
                        EmptyAgendaCard()
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            agendaList.forEach { item ->
                                AgendaCard(
                                    item          = item,
                                    onDetailClick = {
                                        navController.navigate(Screen.PatientAppointmentDetail.createRoute(item.id))
                                    }
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  1. Header
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun TherapistGreetingHeader(
    userName: String,
    greeting: String,
    unreadCount: Int,
    bottomExtra: Dp = 0.dp,
    onNotifClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Brush.verticalGradient(colors = listOf(GreenDark, GreenPrimary, GreenLight)))
            .padding(horizontal = 20.dp)
            .padding(top = 30.dp, bottom = 20.dp + bottomExtra)
    ) {
        Box(modifier = Modifier.size(120.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f)).align(Alignment.TopEnd).offset(x = 30.dp, y = (-30).dp))
        Box(modifier = Modifier.size(70.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = 0.04f)).align(Alignment.BottomStart).offset(x = (-20).dp, y = 20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(greeting, fontSize = 12.sp, color = Color.White.copy(alpha = 0.78f), fontWeight = FontWeight.Medium)
                Text("Semangat, $userName! 🌿", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Box(modifier = Modifier.size(44.dp).clickable { onNotifClick() }, contentAlignment = Alignment.Center) {
                BadgedBox(badge = {
                    if (unreadCount > 0) {
                        Badge(containerColor = Color.Red, contentColor = Color.White, modifier = Modifier.offset(x = (-4).dp, y = 4.dp)) {
                            Text(if (unreadCount > 99) "99+" else unreadCount.toString(), fontSize = 10.sp)
                        }
                    }
                }) {
                    Icon(Icons.Default.Notifications, "Notifikasi", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  2. Summary Card
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun StatistikSection(
    total: Int,
    confirmed: Int,
    completed: Int,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DividerLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Statistik",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SlateTextDark
            )

            if (isLoading) {
                Box(
                    Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = GreenPrimary
                    )
                }
            } else {
                // Total Sesi (Bulan Ini) - Full width
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DividerLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total Sesi (Bulan Ini)",
                            fontSize = 13.sp,
                            color = BodyGray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            total.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenPrimary
                        )
                    }
                }

                // Two stat cards side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        label = "Belum Isi Catatan",
                        value = completed,
                        color = GreenPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        label = "Terjadwal",
                        value = confirmed,
                        color = GreenPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DividerLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                fontSize = 12.sp,
                color = BodyGray,
                fontWeight = FontWeight.Medium
            )
            Text(
                value.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  3. Quick Actions
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun QuickActionsGrid(navController: NavController, onArticleClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Akses Cepat",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = SlateText,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickActionItem(
                    icon = Icons.Default.EditCalendar,
                    label = "Jadwal",
                    color = GreenPrimary,
                    onClick = { navController.navigate(Screen.TherapistSchedule.route) }
                )
                QuickActionItem(
                    icon = Icons.Default.Assessment,
                    label = "Laporan",
                    color = GreenPrimary,
                    onClick = { navController.navigate(Screen.TherapistReport.route) }
                )
                QuickActionItem(
                    icon = Icons.Default.Article,
                    label = "Kelola Artikel",
                    color = AccentOrange,
                    onClick = onArticleClick
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
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
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = SlateText
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  4. Agenda Card
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun AgendaCard(item: DashboardAgendaItem, onDetailClick: () -> Unit) {
    val timeFormatted = runCatching {
        java.time.LocalTime.parse(item.bookingTime).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrDefault(item.bookingTime.take(5))

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onDetailClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
                Text(timeFormatted, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
                Text("WITA", fontSize = 9.sp, color = GrayText)
            }
            Box(modifier = Modifier.width(1.5.dp).height(40.dp).background(GreenSoft))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.patientName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.serviceName, fontSize = 12.sp, color = GrayText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.ChevronRight, null, tint = GrayText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun EmptyAgendaCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = GreenSoft)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🌿", fontSize = 32.sp)
            Text("Belum ada agenda hari ini", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GreenPrimary)
            Text("Jadwal konfirmasimu akan muncul di sini", fontSize = 12.sp, color = GrayText)
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Section Title
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier.padding(horizontal = 20.dp)) {
    Text(title, modifier = modifier, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = SlateText)
}

// ═════════════════════════════════════════════════════════════════════════════
//  Helpers
// ═════════════════════════════════════════════════════════════════════════════
private fun Modifier.shiftUp(amount: Dp): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val shiftPx   = amount.roundToPx()
        layout(placeable.width, (placeable.height - shiftPx).coerceAtLeast(0)) {
            placeable.placeRelative(0, -shiftPx)
        }
    }

private fun therapistGreeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 10 -> "Selamat Pagi ☀️"
        hour < 15 -> "Selamat Siang 🌤️"
        hour < 18 -> "Selamat Sore 🌇"
        else      -> "Selamat Malam 🌙"
    }
}
