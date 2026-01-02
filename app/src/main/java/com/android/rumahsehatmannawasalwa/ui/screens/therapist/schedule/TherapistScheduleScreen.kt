package com.android.rumahsehatmannawasalwa.ui.screens.therapist.schedule

import com.android.rumahsehatmannawasalwa.ui.components.TherapistBottomNavigationBar
import com.android.rumahsehatmannawasalwa.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController

@Composable
fun TherapistScheduleScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            TherapistBottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Jadwal Praktik",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
                Text(text = "Atur Ketersediaan Jadwal")
            }
        }
    }
}
