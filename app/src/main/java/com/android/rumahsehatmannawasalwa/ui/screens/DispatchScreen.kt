package com.android.rumahsehatmannawasalwa.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel

@Composable
fun DispatchScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
    }

    val userState = authViewModel.currentUserData.collectAsState()
    val authStateState = authViewModel.authState.collectAsState()

    val user = userState.value
    val authState = authStateState.value

    LaunchedEffect(user, authState) {
        if (user != null) {
            Log.d("DispatchScreen", "User Role: ${user?.role}")
            Log.d("DispatchScreen", "User Data - Phone: '${user?.phoneNumber}', Job: '${user?.job}', Address: '${user?.address}'")

            if (user?.role == "admin") {
                navController.navigate("admin_home") {
                    popUpTo("dispatch") { inclusive = true }
                }
            } else if (user?.role == "therapist" || user?.role == "terapis") {
                navController.navigate("therapist_home") {
                    popUpTo("dispatch") { inclusive = true }
                }
            } else {
                // Check if profile is complete (Auto-Registered users have "-" or empty fields)
                if (user?.phoneNumber == "-" || user?.job == "-" || user?.address == "-" ||
                    user?.phoneNumber.isNullOrBlank() || user?.job.isNullOrBlank() || user?.address.isNullOrBlank()) {
                    
                    // PENTING: Reset state agar CompleteProfileScreen tidak langsung redirect ke Home karena state SUCCESS
                    authViewModel.resetState()
                    
                    navController.navigate("complete_profile") {
                        popUpTo("dispatch") { inclusive = true }
                    }
                } else {
                    navController.navigate("home") {
                        popUpTo("dispatch") { inclusive = true }
                    }
                }
            }
        } else if (authState is com.android.rumahsehatmannawasalwa.data.ApiResult.Error) {
             // If fetch fails, go back to login
             navController.navigate("login") {
                 popUpTo("dispatch") { inclusive = true }
             }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
