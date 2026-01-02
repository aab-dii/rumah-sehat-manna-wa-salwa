package com.android.rumahsehatmannawasalwa.ui.screens.profile

import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.data.model.auth.User

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(navController: NavController, viewModel: AuthViewModel) {
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
                title = { Text("Detail Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { /* TODO: Navigate to Edit Profile */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Ubah Data")
                }
            }
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
            Spacer(modifier = Modifier.height(24.dp))

            // --- List Informasi Detail (Tanpa Card) ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                
                ProfileItem(label = "Nama Lengkap", value = userData?.name)
                Spacer(modifier = Modifier.height(12.dp))

                ProfileItem(label = "Email", value = userData?.email)
                Spacer(modifier = Modifier.height(12.dp))

                ProfileItem(label = "Nomor WhatsApp", value = userData?.phoneNumber)
                Spacer(modifier = Modifier.height(12.dp))

                ProfileItem(label = "Pekerjaan", value = userData?.job)
                Spacer(modifier = Modifier.height(12.dp))

                // Format Tanggal Lahir
                val formattedDoB = try {
                    if (!userData?.birthDate.isNullOrBlank()) {
                        val date = LocalDate.parse(userData!!.birthDate.substring(0, 10))
                        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
                        date.format(formatter)
                    } else "-"
                } catch (e: Exception) {
                    userData?.birthDate
                }
                ProfileItem(label = "Tanggal Lahir", value = formattedDoB)
                Spacer(modifier = Modifier.height(24.dp))

                // Format Jenis Kelamin
                val genderDisplay = when(userData?.gender) {
                    "L" -> "Laki-Laki"
                    "P" -> "Perempuan"
                    else -> userData?.gender
                }
                ProfileItem(label = "Jenis Kelamin", value = genderDisplay)
                Spacer(modifier = Modifier.height(24.dp))

                ProfileItem(label = "Alamat", value = userData?.address)
                
                Spacer(modifier = Modifier.height(40.dp))


            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Komponen Kecil untuk Baris Data
@Composable
fun ProfileItem(label: String, value: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
//        Spacer(modifier = Modifier.height(2.dp)) // Jarak dekat judul dan data
        Text(
            text = if (value.isNullOrBlank()) "-" else value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface 
        )
    }
}

// Extension function kecil untuk kapitalisasi huruf pertama
fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercase() }
}
