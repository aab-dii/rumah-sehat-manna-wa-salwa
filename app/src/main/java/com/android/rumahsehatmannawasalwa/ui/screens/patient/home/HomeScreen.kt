package com.android.rumahsehatmannawasalwa.ui.screens.patient.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundWhite
import com.android.rumahsehatmannawasalwa.ui.theme.BodyGray
import com.android.rumahsehatmannawasalwa.ui.theme.DividerLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenDark
import com.android.rumahsehatmannawasalwa.ui.theme.GreenLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.GreenSoft
import com.android.rumahsehatmannawasalwa.ui.theme.GrayText
import com.android.rumahsehatmannawasalwa.ui.theme.SlateText
import com.android.rumahsehatmannawasalwa.ui.theme.SurfaceLight
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.ServiceViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.viewmodel.notification.NotificationViewModel
import com.android.rumahsehatmannawasalwa.utils.NetworkUtils
import com.android.rumahsehatmannawasalwa.ui.components.Badge
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import java.time.LocalTime



// ═══════════════════════════════════════════════════════════════════════════════
//  Screen
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ServiceViewModel = viewModel(),
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel
) {
    val serviceList by viewModel.serviceList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user     by authViewModel.currentUserData.collectAsState()
    val userName = user?.name?.split(" ")?.firstOrNull() ?: "Tamu"
    val greeting = greetingByTime()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            navController.navigate(Screen.Offline.route) {
                popUpTo(Screen.Dispatch.route) { inclusive = true }
            }
            return@LaunchedEffect
        }
        viewModel.fetchServiceList()
    }

    // ── Bottom sheet state ────────────────────────────────────────────────────
    var selectedLayanan by remember { mutableStateOf<Service?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    selectedLayanan?.let { layanan ->
        ServiceDetailSheet(
            layanan    = layanan,
            sheetState = sheetState,
            onDismiss  = { selectedLayanan = null },
            onBook     = {
                selectedLayanan = null
                navController.navigate("booking/${layanan.id}/${layanan.name}/${layanan.price}")
            }
        )
    }

    Scaffold(containerColor = BackgroundWhite) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── 1. Header hijau ───────────────────────────────────────────────
            item {
                GreetingHeader(
                    userName     = userName,
                    unreadCount = unreadCount,
                    bottomExtra  = 50.dp,
                    onNotifClick = { navController.navigate(Screen.Notifications.route) }
                )
            }

            item {
                val greenOverlap = 30.dp
                val cardShiftUp  = 65.dp

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shiftUp(greenOverlap)
                        .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .padding(bottom = 16.dp)
                ) {
                    WelcomeCard(
                        greeting = greeting,
                        modifier = Modifier
                            .shiftUp(cardShiftUp)
                            .padding(bottom = 8.dp)
                    )

                    // Pilih Layanan
                    Text(
                        "Pilih Layanan",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateText
                    )

                    // Service list
                    when {
                        isLoading -> {
                            Box(Modifier.fillMaxWidth().height(180.dp), Alignment.Center) {
                                CircularProgressIndicator(color = GreenPrimary)
                            }
                        }
                        serviceList.isEmpty() -> {
                            Box(Modifier.fillMaxWidth().height(180.dp), Alignment.Center) {
                                Text("Belum ada layanan tersedia", color = Color.Gray)
                            }
                        }
                        else -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(serviceList, key = { it.id }) { layanan ->
                                    ServiceCard(layanan = layanan, onClick = { selectedLayanan = layanan })
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Artikel Kesehatan
                    ArtikelSection()
                }
            }

        }
    }
}

@Composable
private fun GreetingHeader(
    userName: String,
    unreadCount : Int,
    onNotifClick: () -> Unit,
    bottomExtra: Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Brush.verticalGradient(
                colors = listOf(
                    GreenDark,
                    GreenPrimary,
                    GreenLight
                )
            ))
            .padding(horizontal = 20.dp)
            .padding(top = 30.dp, bottom = 20.dp + bottomExtra)
    ) {
        // Dekorasi lingkaran pojok kanan atas
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Hai, $userName 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    "Selamat datang kembali!",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.78f)
                )
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
                                Text (if (unreadCount > 99) "99+" else unreadCount.toString(), fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifikasi",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(greeting: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(brush = Brush.verticalGradient(
                colors = listOf(Color.White, SurfaceLight)
            ),
                shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    greeting,
                    fontSize = 12.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Sudah siap untuk\nhidup lebih sehat?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateText,
                    lineHeight = 23.sp
                )
                Spacer(Modifier.height(2.dp))
                Surface(color = GreenSoft, shape = RoundedCornerShape(50.dp)) {
                    Badge(
                        text = "Rumah Sehat Manna wa Salwa",
                        color = GreenPrimary,
                    )
                }
            }
            // Ikon dekoratif
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .wrapContentSize(),
                contentAlignment = Alignment.CenterEnd
            ) {

                Text(
                    text = "🌿",
                    fontSize = 35.sp,
                    modifier = Modifier
                        .graphicsLayer(rotationY = 180f)
                        .offset(x = (75).dp, y = 12.dp)
                )

                Text(
                    text = "🌿",
                    fontSize = 60.sp,
                    modifier = Modifier
                        .zIndex(1f)
                        .offset((-10).dp)
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.Green.copy(alpha = 0.06f))
//                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                )

            }

        }
    }
}


@Composable
private fun ServiceCard(layanan: Service, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(GreenSoft),
                contentAlignment = Alignment.Center
            ) {
                if (layanan.imageUrl != null) {
                    AsyncImage(
                        model = layanan.imageUrl, contentDescription = layanan.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    )
                } else {
                    Icon(
                        Icons.Default.Spa,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
                Text(
                    text = layanan.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 8.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = GrayText
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "${layanan.duration} menit",
                        fontSize = 11.sp,
                        color = GrayText
                    )
                }
                Text(FormatterUtils.formatRupiah(layanan.price), fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceDetailSheet(
    layanan: Service,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onBook: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GreenSoft),
                contentAlignment = Alignment.Center
            ) {
                if (layanan.imageUrl != null) {
                    AsyncImage(
                        model = layanan.imageUrl,
                        contentDescription = layanan.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)))
                } else {
                    Icon(
                        Icons.Default.Spa,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(56.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = layanan.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateText)

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = FormatterUtils.formatRupiah(layanan.price),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenPrimary
                )
                Badge(
                    text = "${layanan.duration} menit",
                    color = GreenPrimary,
                    icon = Icons.Default.AccessTime
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerLight)
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Tentang Layanan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SlateText
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = layanan.description,
                fontSize = 14.sp,
                color = BodyGray,
                lineHeight = 21.sp
            )

            Spacer(Modifier.height(24.dp))

            MannaButton(
                text = "Pesan Sekarang",
                onClick = onBook,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                containerColor = GreenPrimary,
                contentColor = Color.White
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  Artikel Kesehatan — Coming Soon
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ArtikelSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Artikel Kesehatan",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateText
            )
            Badge(
                text = "Coming Soon",
                color = GreenPrimary
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp)).background(GreenSoft),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "📚",
                    fontSize = 28.sp
                )
                Text(
                    text = "Fitur artikel akan segera hadir!",
                    fontSize = 13.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ── Custom layout modifier: naik secara visual DAN kurangi layout height ───────────
private fun Modifier.shiftUp(amount: androidx.compose.ui.unit.Dp): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val shiftPx  = amount.roundToPx()
        // Kurangi height yang dilaporkan supaya item berikutnya tidak ada gap
        layout(placeable.width, (placeable.height - shiftPx).coerceAtLeast(0)) {
            placeable.placeRelative(0, -shiftPx)
        }
    }

// ── Helper ────────────────────────────────────────────────────────────────────
private fun greetingByTime(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 10 -> "Selamat Pagi ☀️"
        hour < 15 -> "Selamat Siang 🌤️"
        hour < 18 -> "Selamat Sore 🌇"
        else      -> "Selamat Malam 🌙"
    }
}