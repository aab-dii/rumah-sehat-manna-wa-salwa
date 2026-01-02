package com.android.rumahsehatmannawasalwa.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.components.BottomNavigationBar
import com.android.rumahsehatmannawasalwa.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // Collect User Data
    val user by viewModel.currentUserData.collectAsState()
    val authState by viewModel.authState.collectAsState()

    // Fetch on Init
    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    // State
    var isNotificationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color(0xFFEFEFEF), // #FAFFF9
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- 1. Custom Header (Full Width) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Atau GreenPrimary.copy(alpha=0.1f) untuk ijo pudar
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar
                    Surface(
                        shape = CircleShape,
                        color = BackgroundLight, // Hijau sangat muda
                        border = BorderStroke(2.dp, GreenPrimary),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = GreenPrimary,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nama & Email
                    Text(
                        text = user?.name ?: "Pengguna",
                        style = MaterialTheme.typography.headlineSmall, // H2/H3
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user?.email ?: "Memuat...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                     Text(
                        text = user?.phoneNumber ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Divider(color = DividerColor, thickness = 1.dp)

            // --- 2. Menu Options ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Isi sisa space
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Item 1: Ubah Profil -> Navigasi ke ProfileDetail (Halaman Lama)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RectangleShape
                    ) {
                        Column {
                            // Data Pribadi
                            ProfileMenuItem(
                                icon = Icons.Default.Person,
                                title = "Data Pribadi",
                                onClick = { navController.navigate("profile_detail") }
                            )
                            // Garis Pemisah
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                            // Keamanan
                            ProfileMenuItem(
                                icon = Icons.Default.Lock,
                                title = "Ubah Kata Sandi",
                                onClick = { /* TODO: Navigate to Password Change */ }
                            )
                        }
                    }
                }

                // Item 2: Notifikasi -> Toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RectangleShape
                    ) {
                        ProfileMenuItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifikasi Aplikasi",
                            onClick = { isNotificationEnabled = !isNotificationEnabled },
                            endWidget = {
                                Switch(
                                    checked = isNotificationEnabled,
                                    onCheckedChange = { isNotificationEnabled = it },
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
                    }
                }

                // Item 4: Logout -> Special Style
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RectangleShape
                    ) {
                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = "Keluar Akun",
                            showChevron = true,
                            onClick = {
                                viewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
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
            .padding(horizontal = 24.dp)
            .heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        // End Widget (Switch or Chevron)
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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // Preview Only
}
