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
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// ui/screens/DispatchScreen.kt

@Composable
fun DispatchScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // 1. Trigger pengecekan sesi saat pertama kali masuk
    LaunchedEffect(Unit) {
        val userLokal = authViewModel.currentUserData.value
        
        // Aturan: Jika ada sesi lokal (Sanctum) yang valid, langsung arahkan ke Dashboard
        if (userLokal != null && !userLokal.accessToken.isNullOrEmpty()) {
            Log.d("DispatchScreen", "Sesi Lokal Valid. Melewati sinkronisasi Firebase.")
            RetrofitClient.authToken = userLokal.accessToken
            // Tidak perlu fetchUserProfile lagi karena data lokal sudah cukup
            return@LaunchedEffect
        }

        // Jika tidak ada sesi lokal, baru cek Firebase untuk sinkronisasi ulang
        val firebaseUser = Firebase.auth.currentUser
        if (firebaseUser != null) {
            Log.d("DispatchScreen", "Sesi lokal kosong, Sinkronisasi dengan Firebase...")
            firebaseUser.getIdToken(false).addOnSuccessListener { result ->
                RetrofitClient.authToken = result.token
                authViewModel.fetchUserProfile(forceRefresh = true)
            }.addOnFailureListener {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Dispatch.route) { inclusive = true }
                }
            }
        } else {
            Log.d("DispatchScreen", "Sesi Firebase tidak ada. Menuju Login.")
            authViewModel.resetState()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Dispatch.route) { inclusive = true }
            }
        }
    }

    val user by authViewModel.currentUserData.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(user, authState) {
        if (user != null) {
            Log.d("DispatchScreen", "Navigating based on role: ${user?.role}")

            val route = when (user?.role?.lowercase()) {
                "admin" -> Screen.AdminHome.route

                "therapist", "terapis" -> Screen.TherapistHome.route

                "pasien", "patient" -> {
                    // 2. Cek Kelengkapan Profil Pasien
                    val isIncomplete = user?.phoneNumber == "-" ||
                            user?.job == "-" ||
                            user?.address == "-" ||
                            user?.phoneNumber.isNullOrBlank() ||
                            user?.job.isNullOrBlank() ||
                            user?.address.isNullOrBlank()

                    if (isIncomplete) {
                        authViewModel.resetState() // Bersihkan state agar tidak langsung redirect balik
                        Screen.CompleteProfile.createRoute(true)
                    } else {
                        Screen.PatientHome.route
                    }
                }

                else -> Screen.PatientHome.route // Default jika role tidak dikenal
            }

            // Eksekusi Navigasi
            navController.navigate(route) {
                popUpTo(Screen.Dispatch.route) { inclusive = true }
            }

        } else if (authState is ApiResult.Error) {
            // 3. Jika gagal memuat profil (atau token expired), lempar ke Login
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Dispatch.route) { inclusive = true }
            }
        }
    }

    // Tampilan Loading Center
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary)
    }
}
