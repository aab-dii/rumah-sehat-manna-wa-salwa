package com.android.rumahsehatmannawasalwa.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.components.BottomNavigationBar
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.components.auth.ProfilePhoto
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val user by viewModel.currentUserData.collectAsState()
    val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // Menghapus fetchUserProfile otomatis agar tidak membebani server/sync berlebihan
    // Data diambil dari StateFlow currentUserData yang sudah diinisialisasi dengan data lokal

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // ── SCROLLABLE CONTENT ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. GREEN GRADIENT HEADER with Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GreenDark, GreenPrimary, GreenLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Profile Avatar menggunakan komponen baru
                val activeUser = user
                val photoUrl = when {
                    // 1. Cek dari Google dulu
                    !activeUser?.fotoUrl.isNullOrBlank() -> activeUser?.fotoUrl
                    
                    // 2. Kalau Google null, baru ambil dari Database
                    !activeUser?.profilePhotoPath.isNullOrBlank() -> {
                        val baseUrl = com.android.rumahsehatmannawasalwa.BuildConfig.BASE_URL
                        val storageUrl = baseUrl.replace("/api/", "/storage/")
                        "$storageUrl${activeUser?.profilePhotoPath}"
                    }
                    else -> null
                }

                ProfilePhoto(
                    photoUrl = photoUrl,
                    size = 110.dp,
                    modifier = Modifier.statusBarsPadding()
                )
            }

            // 2. WHITE SHEET (overlapping green header)
            MannaSheet(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── User Info (Centered) ────────────────────────────────
                    Text(
                        text = user?.name ?: "Pengguna",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextDark,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BodyGray
                    )
                    if (!user?.phoneNumber.isNullOrEmpty()) {
                        Text(
                            text = user?.phoneNumber ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = BodyGray
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(color = DividerLight, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Menu Options ─────────────────────────────────────────
                    // Data Pribadi & Ubah Kata Sandi
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        title = "Data Pribadi",
                        onClick = { navController.navigate("profile_detail") }
                    )
                    HorizontalDivider(color = DividerLight, thickness = 1.dp)

                    ProfileMenuItem(
                        icon = Icons.Default.Lock,
                        title = "Ubah Kata Sandi",
                        onClick = { navController.navigate("change_password") }
                    )
                    HorizontalDivider(color = DividerLight, thickness = 1.dp)

                    // Notifikasi Aplikasi
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifikasi Aplikasi",
                        onClick = { viewModel.toggleNotification() },
                        endWidget = {
                            Switch(
                                checked = isNotificationEnabled,
                                onCheckedChange = { viewModel.setNotificationEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GreenPrimary,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color.Transparent
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    )
                    HorizontalDivider(color = DividerLight, thickness = 1.dp)

                    // Keluar Akun
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "Keluar Akun",
                        textColor = RedDanger,
                        iconColor = RedDanger,
                        onClick = { showLogoutConfirm = true }
                    )
                    HorizontalDivider(color = DividerLight, thickness = 1.dp)

                    // Bottom spacer for navbar
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        // ── FLOATING BOTTOM NAV ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomNavigationBar(navController = navController, role = user?.role ?: "patient")
        }

        // ── LOGOUT CONFIRMATION DIALOG ──────────────────────────────────────
        CustomConfirmDialog(
            show = showLogoutConfirm,
            onDismiss = { showLogoutConfirm = false },
            onConfirm = {
                showLogoutConfirm = false
                viewModel.logout()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            title = "Keluar Akun?",
            description = "Apakah Anda yakin ingin keluar dari aplikasi Rumah Sehat Manna wa Salwa?",
            confirmText = "Ya, Keluar",
            dismissText = "Batal",
            isDanger = true
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: Color = Color.Black,
    iconColor: Color = GreenPrimary,
    showChevron: Boolean = true,
    endWidget: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
            .heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        if (endWidget != null) {
            endWidget()
        } else if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
