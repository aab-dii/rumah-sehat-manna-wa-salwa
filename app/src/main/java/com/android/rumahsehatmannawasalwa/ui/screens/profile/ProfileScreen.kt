package com.android.rumahsehatmannawasalwa.ui.screens.profile

import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.ui.components.BottomNavigationBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: AuthViewModel) {
    // Ambil data user dari ViewModel
    val userDataState = viewModel.currentUserData.collectAsState()
    val userData = userDataState.value

    // Panggil fungsi fetch data saat halaman dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Tombol Logout di pojok kanan atas
                    IconButton(onClick = {
                        viewModel.logout()
                        // Arahkan kembali ke Login dan hapus history
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- Foto Profil (Placeholder) ---
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Foto Profil",
                    modifier = Modifier.padding(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nama User (Judul Besar)
            Text(
                text = userData?.name ?: "Memuat Nama...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userData?.role?.capitalize() ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Card Informasi Detail ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)) // Warna abu muda
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileItem(label = "Email", value = userData?.email)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileItem(label = "Nomor WhatsApp", value = userData?.phoneNumber)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileItem(label = "Pekerjaan", value = userData?.job)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileItem(label = "Tanggal Lahir", value = userData?.birthDate)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileItem(label = "Jenis Kelamin", value = userData?.gender)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileItem(label = "Alamat", value = userData?.address)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Edit (Opsional, nanti disambung ke fitur edit)
            Button(
                onClick = { /* TODO: Navigasi ke Edit Profil */ },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("Ubah Profil")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Komponen Kecil untuk Baris Data
@Composable
fun ProfileItem(label: String, value: String?) {
    Column {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (value.isNullOrBlank()) "-" else value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

// Extension function kecil untuk kapitalisasi huruf pertama
fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercase() }
}