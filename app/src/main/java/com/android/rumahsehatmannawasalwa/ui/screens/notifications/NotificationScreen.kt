package com.android.rumahsehatmannawasalwa.ui.screens.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.notification.Notification
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.ViewModelFactory
import com.android.rumahsehatmannawasalwa.ui.viewmodel.notification.NotificationViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun NotificationScreen(
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    onNotificationClick: ((Int) -> Unit)? = null
) {
    val context  = LocalContext.current
    val factory  = ViewModelFactory.getInstance(context.applicationContext as android.app.Application)
    val vm: NotificationViewModel = viewModel(factory = factory)

    val notificationsState by vm.notificationsState.collectAsState()
    val unreadCount        by vm.unreadCount.collectAsState()
    val actionMessage      by vm.actionMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let { vm.clearActionMessage(); snackbarHostState.showSnackbar(it) }
    }

    val scrollState = rememberScrollState()
    val isScrolled = scrollState.value > 50

    val topBarBg    by animateColorAsState(targetValue = if (isScrolled) Color.White else Color.Transparent, animationSpec = tween(300))
    val topBarTitle by animateColorAsState(targetValue = if (isScrolled) GreenPrimary else Color.White, animationSpec = tween(300))
    val topBarIcon  by animateColorAsState(targetValue = if (isScrolled) GreenPrimary else Color.White, animationSpec = tween(300))

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundWhite
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Scrollable Content ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header Hijau
                HeroHeader(unreadCount = unreadCount)

                // White Bottom Sheet Overlap
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp)
                        .background(
                            BackgroundWhite, 
                            RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDDE1E7))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(16.dp))

                        // Notification content
                        when (val state = notificationsState) {
                            is ApiResult.Loading -> LoadingContent()
                            is ApiResult.Error -> ErrorContent(message = state.error, onRetry = { vm.loadNotifications() })
                            is ApiResult.Success -> {
                                val list = state.data
                                if (list.isEmpty()) {
                                    EmptyContent()
                                } else {
                                    Column {
                                        list.forEachIndexed { i, n ->
                                            AnimatedVisibility(
                                                visible = true,
                                                enter   = fadeIn(tween(300, delayMillis = i * 20)) +
                                                          slideInVertically(tween(300, delayMillis = i * 20)) { it / 5 }
                                            ) {
                                                NotificationCard(
                                                    notification = n,
                                                    onClick = {
                                                        val bid = n.data?.bookingId ?: 0
                                                        if (!n.isRead) vm.markAsRead(n.id)
                                                        if (bid != 0) {
                                                            if (onNotificationClick != null) {
                                                                onNotificationClick(bid)
                                                            } else {
                                                                navController.navigate(Screen.PatientAppointmentDetail.createRoute(bid))
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Sticky Top Bar Overlaid ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarBg)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = topBarIcon)
                        }
                    }
                    Text(
                        text = "Notifikasi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = topBarTitle,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (onNavigateBack == null) 16.dp else 0.dp)
                    )
                    AnimatedVisibility(visible = unreadCount > 0, enter = fadeIn(), exit = fadeOut()) {
                        IconButton(onClick = { vm.markAllRead() }) {
                            Icon(Icons.Outlined.DoneAll, "Tandai Semua", tint = topBarIcon)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(unreadCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Brush.verticalGradient(listOf(GreenDark, GreenPrimary, GreenLight)))
    ) {
        Box(Modifier.size(130.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .align(Alignment.TopEnd).offset(x = 30.dp, y = (-20).dp))
        Box(Modifier.size(80.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = 0.04f))
            .align(Alignment.BottomStart).offset(x = (-20).dp, y = 30.dp))

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (unreadCount > 0) "$unreadCount pesan baru" else "Tidak ada pesan baru",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun NotificationCard(notification: Notification, onClick: () -> Unit) {
    val readBg   = BackgroundWhite
    val unreadBg = GreenSoft.copy(alpha = 0.5f)

    val bgColor by animateColorAsState(
        targetValue   = if (notification.isRead) readBg else unreadBg,
        animationSpec = tween(400), label = "notif_bg"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(notifIconBg(notification.type)),
                contentAlignment = Alignment.Center
            ) {
                Icon(notifIcon(notification.type), null, tint = notifIconTint(notification.type), modifier = Modifier.size(24.dp))
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        fontSize   = 14.sp, color = SlateText,
                        modifier   = Modifier.weight(1f),
                        maxLines   = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(formatRelativeTime(notification.createdAt), fontSize = 11.sp, color = GrayText)
                }
                Text(
                    text = notification.body,
                    fontSize = 13.sp,
                    color = GrayText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )
                NotifTypeChip(notification.type)
            }

            if (!notification.isRead) {
                Box(Modifier.padding(top = 6.dp).size(9.dp).clip(CircleShape).background(GreenPrimary))
            }
        }
        HorizontalDivider(
            modifier  = Modifier.padding(start = 78.dp, end = 16.dp),
            thickness = 0.5.dp, color = Color(0xFFEEEEEE)
        )
    }
}

@Composable
private fun NotifTypeChip(type: String?) {
    val label = when (type) {
        "payment"        -> "Pembayaran"
        "booking_status" -> "Status Booking"
        "booking"        -> "Booking Baru"
        else             -> "Umum"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(notifIconBg(type))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = notifIconTint(type))
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = GreenPrimary, strokeWidth = 3.dp)
            Text("Memuat notifikasi...", fontSize = 13.sp, color = GrayText)
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, bottom = 40.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(GreenSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.NotificationsNone, null, tint = GreenPrimary, modifier = Modifier.size(52.dp))
        }
        Text(
            text = "Inbox Kosong",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = SlateText
        )
        Text(
            text = "Notifikasi booking dan pembayaran\nakan muncul di sini",
            fontSize = 13.sp,
            color = GrayText,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFEF5350), modifier = Modifier.size(52.dp))
        }
        Text("Gagal Memuat", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = SlateText)
        Text(message, fontSize = 13.sp, color = GrayText, textAlign = TextAlign.Center)
        Button(
            onClick = onRetry,
            colors  = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape   = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Coba Lagi", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun notifIcon(type: String?): ImageVector = when (type) {
    "payment"        -> Icons.Default.Payment
    "booking_status" -> Icons.Default.CheckCircle
    "booking"        -> Icons.Default.EventNote
    else             -> Icons.Default.Notifications
}
private fun notifIconBg(type: String?): Color = when (type) {
    "payment"        -> Color(0xFFE3F2FD)
    "booking_status" -> Color(0xFFE8F5E9)
    "booking"        -> Color(0xFFFFF8E1)
    else             -> Color(0xFFF3E5F5)
}
private fun notifIconTint(type: String?): Color = when (type) {
    "payment"        -> Color(0xFF1976D2)
    "booking_status" -> Color(0xFF2E7D32)
    "booking"        -> Color(0xFFF57F17)
    else             -> Color(0xFF7B1FA2)
}

private fun formatRelativeTime(createdAt: String?): String {
    if (createdAt == null) return ""
    return try {
        val created = ZonedDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME)
        val now     = ZonedDateTime.now(created.zone)
        val minutes = ChronoUnit.MINUTES.between(created, now)
        when {
            minutes < 1    -> "Baru saja"
            minutes < 60   -> "${minutes}m"
            minutes < 1440 -> "${minutes / 60}j"
            minutes < 10080-> "${minutes / 1440}h"
            else           -> created.format(DateTimeFormatter.ofPattern("d MMM"))
        }
    } catch (e: Exception) { "" }
}
