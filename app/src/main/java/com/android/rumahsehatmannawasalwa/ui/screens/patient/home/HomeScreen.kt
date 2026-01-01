package com.android.rumahsehatmannawasalwa.ui.screens.patient.home

import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.LayananViewModel
import com.android.rumahsehatmannawasalwa.data.model.service.Layanan
import com.android.rumahsehatmannawasalwa.ui.components.BottomNavigationBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController, 
    viewModel: LayananViewModel = viewModel(),
    authViewModel: AuthViewModel
) {
    val layananPagingItems = viewModel.layananPager.collectAsLazyPagingItems()
//    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    // Observe User Data
    val userState = authViewModel.currentUserData.collectAsState()
    val user = userState.value
    val userName = user?.name?.split(" ")?.firstOrNull() ?: "Tamu" // Ambil nama depan saja

    Scaffold(
        topBar = {
            TopAppBar(
                title = {}, // Kosongkan agar fokus content
                actions = {
                    IconButton(onClick = { /* TODO: Navigasi ke Layar Notifikasi */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = GreenPrimary // Primary Green
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite // Match background
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = BackgroundWhite
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundWhite),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // --- 1. Kartu Ringkasan (Top Card: Personal Greeting) ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Hai, $userName \uD83D\uDC4B", // Emoji melambai
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sudah siap untuk hidup lebih sehat hari ini?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rumah Sehat Manna wa Salwa",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Gray
                        )
                    }
                }
            }

            // --- 2. Kartu Detail & Aksi (Bottom Section: List Layanan) ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // .offset(y = (-24).dp) // Hapus offset karena banner dihapus
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White)
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "Daftar Layanan",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary // Primary Green
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pilih layanan kesehatan terbaik untuk Anda dan keluarga.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // --- List Items Loop ---
            items(
                count = layananPagingItems.itemCount,
                key = { index -> layananPagingItems[index]?.id ?: index }
            ) { index ->
                val layanan = layananPagingItems[index]
                if (layanan != null) {
                    Box(modifier = Modifier.background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp)) {
                         LayananCard(layanan, onPesanClick = {
                             navController.navigate("booking/${layanan.id}/${layanan.nama}/${layanan.harga}")
                        })
                    }
                }
            }
            
            // Loading State
            item {
                 if (layananPagingItems.loadState.append is androidx.paging.LoadState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp).background(Color.White).fillMaxWidth())
            }
        }
    }
}

@Composable
fun LayananCard(layanan: Layanan, onPesanClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey), 
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // 1. Image Section (Full Width)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(DividerColor), // Placeholder Grey
                contentAlignment = Alignment.Center
            ) {
                 if (layanan.imageUrl != null) {
                     val fixedUrl = layanan.imageUrl.replace("localhost", "10.0.2.2").replace("127.0.0.1", "10.0.2.2")
                     coil.compose.AsyncImage(
                         model = fixedUrl,
                         contentDescription = layanan.nama,
                         contentScale = ContentScale.Crop,
                         modifier = Modifier.fillMaxSize()
                     )
                 } else {
                     Icon(
                         imageVector =  Icons.Default.Spa, // Menggunakan icon Spa sebagai placeholder
                         contentDescription = "Layanan",
                         tint = Color.Gray,
                         modifier = Modifier.size(48.dp)
                     )
                 }
            }

            // 2. Content Section (Padded)
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Layanan
                Text(
                    text = layanan.nama, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Rp ${layanan.harga}", 
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), 
                        color = GreenPrimary // Primary Green
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${layanan.durasi} Min", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = layanan.deskripsi, 
                    style = MaterialTheme.typography.bodySmall, 
                    maxLines = 2, 
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onPesanClick,
                    modifier = Modifier.fillMaxWidth().height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary), 
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Pesan Sekarang", color = Color.White)
                }
            }
        }
    }
}