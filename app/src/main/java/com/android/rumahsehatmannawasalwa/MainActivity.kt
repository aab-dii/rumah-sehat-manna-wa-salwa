package com.android.rumahsehatmannawasalwa

import android.os.Bundle
import android.util.Log // Import untuk melihat log di Logcat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.rumahsehatmannawasalwa.ui.theme.RumahsehatmannawasalwaTheme
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.rumahsehatmannawasalwa.data.ApiResult
// 1. Import Library Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.BookingViewModel
import com.android.rumahsehatmannawasalwa.ui.screens.auth.LoginScreen
import com.android.rumahsehatmannawasalwa.ui.screens.auth.RegisterScreen
import com.android.rumahsehatmannawasalwa.ui.screens.patient.home.HomeScreen
import com.android.rumahsehatmannawasalwa.ui.screens.patient.booking.MyBookingScreen
import com.android.rumahsehatmannawasalwa.ui.screens.patient.booking.BookingScreen
import com.android.rumahsehatmannawasalwa.ui.screens.profile.ProfileScreen
import com.android.rumahsehatmannawasalwa.ui.components.BottomNavigationBar
import com.android.rumahsehatmannawasalwa.ui.screens.patient.booking.BookingSummaryData
import com.android.rumahsehatmannawasalwa.ui.screens.patient.history.TherapyHistoryScreen
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyHistoryViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek user login di awal
        val currentUser = Firebase.auth.currentUser
        // Jika sudah login, ke "dispatch" untuk cek role, jika belum ke "login"
        val startDestination = if (currentUser != null) "dispatch" else "login"

        setContent {
            RumahsehatmannawasalwaTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                // Shared ViewModel for Booking Flow
                val bookingViewModel: BookingViewModel = viewModel()

                val adminUserViewModel: com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel = viewModel()

                NavHost(navController = navController, startDestination = startDestination) {

                    // Halaman Login
                    composable("login") {
                        LoginScreen(navController, authViewModel)
                    }

                    // Halaman Register
                    composable("register") {
                        RegisterScreen(navController, authViewModel)
                    }

                    // Dispatch Screen (Routing)
                    composable("dispatch") {
                        com.android.rumahsehatmannawasalwa.ui.screens.DispatchScreen(navController, authViewModel)
                    }

                    // Halaman Home User (Pasien)
                    composable("home") {
                        HomeScreen(navController, authViewModel = authViewModel)
                    }

                    // Halaman Home Admin (Container)
                    composable("admin_home") {
                        com.android.rumahsehatmannawasalwa.ui.screens.admin.home.AdminMainScreen(
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
                            },
                            adminUserViewModel = adminUserViewModel,
                            onUserClick = { userId ->
                                navController.navigate("admin_user_detail/$userId")
                            },
                            onAddUserClick = {
                                navController.navigate("admin_add_user")
                            }
                        )
                    }

                    composable(
                        route = "admin_add_user"
                    ) {
                        com.android.rumahsehatmannawasalwa.ui.screens.admin.users.AdminAddUserScreen(
                            navController = navController,
                            viewModel = adminUserViewModel
                        )
                    }

                    composable(
                        route = "admin_user_detail/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                        com.android.rumahsehatmannawasalwa.ui.screens.admin.users.AdminUserDetailScreen(
                            navController = navController,
                            viewModel = adminUserViewModel,
                            userId = userId
                        )
                    }

                    composable(
                        route = "admin_edit_user/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                        com.android.rumahsehatmannawasalwa.ui.screens.admin.users.AdminEditUserScreen(
                            navController = navController,
                            viewModel = adminUserViewModel,
                            userId = userId
                        )
                    }

                    // ... (Other routes remain same)

                    composable("booking_history") {
                        MyBookingScreen(navController, bookingViewModel)
                    }

                    composable("history") {
                        TherapyHistoryScreen(navController = navController)
                    }

                    composable(
                        route = "booking/{sId}/{sName}/{sPrice}",
                        arguments = listOf(
                            navArgument("sId") { type = NavType.StringType },
                            navArgument("sName") { type = NavType.StringType },
                            navArgument("sPrice") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val sId = backStackEntry.arguments?.getString("sId") ?: ""
                        val sName = backStackEntry.arguments?.getString("sName") ?: ""
                        val sPrice = backStackEntry.arguments?.getInt("sPrice") ?: 0

                        BookingScreen(navController, sId, sName, sPrice, bookingViewModel)
                    }

                    composable("booking_summary") {
                        // Gather data from ViewModel
                        val serviceInfoState = bookingViewModel.selectedServiceInfo.collectAsState()
                        val therapistState = bookingViewModel.selectedTherapist.collectAsState()
                        val dateState = bookingViewModel.selectedDate.collectAsState()
                        val timeState = bookingViewModel.selectedTimeSlot.collectAsState()
                        val bookingStateState = bookingViewModel.bookingState.collectAsState()

                        val serviceInfo = serviceInfoState.value
                        val therapist = therapistState.value
                        val date = dateState.value
                        val time = timeState.value
                        val bookingState = bookingStateState.value

                        // Prepare Data
                        if (serviceInfo != null && therapist != null && time != null) {
                            val summaryData = BookingSummaryData(
                                serviceName = serviceInfo.second,
                                duration = "60 Menit", // Bisa diambil dari API kalau ada
                                date = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))),
                                time = time,
                                therapistName = therapist.name,
                                locationName = "Klinik Rumah Sehat",
                                locationType = "Datang ke Klinik",
                                servicePrice = serviceInfo.third,
                                adminFee = 0 // Atau hitung logic lain
                            )
                            
                            val context = androidx.compose.ui.platform.LocalContext.current
                            LaunchedEffect(bookingState) {
                                if (bookingState is ApiResult.Success) {
                                    android.widget.Toast.makeText(context, "Booking Berhasil Dikirim!", android.widget.Toast.LENGTH_LONG).show()
                                    bookingViewModel.resetState()
                                    navController.navigate("booking_history") {
                                        popUpTo("home")
                                    }
                                } else if (bookingState is ApiResult.Error) {
                                    val err = (bookingState as ApiResult.Error).error
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                    // Reset state so we can try again
                                    // bookingViewModel.resetState() // Don't reset everything, just error? Viewmodel handles specific reset inside buatPesanan logic potentially? No, logic is simple.
                                }
                            }

                            com.android.rumahsehatmannawasalwa.ui.screens.patient.booking.BookingSummaryScreen(
                                data = summaryData,
                                onBackClick = { navController.popBackStack() },
                                onConfirmClick = {
                                    bookingViewModel.buatPesanan(
                                        serviceId = serviceInfo!!.first,
                                        serviceName = serviceInfo!!.second,
                                        servicePrice = serviceInfo!!.third,
                                        tanggal = date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
                                        jam = time!!,
                                        therapistId = therapist!!.id
                                    )
                                }
                            )
                        } else {
                            // Fallback if data missing (should not happen in normal flow)
                             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                 Text("Data booking tidak lengkap. Silakan kembali.")
                             }
                        }
                    }

                    composable(
                        route = "booking_detail/{bookingId}",
                        arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
                        com.android.rumahsehatmannawasalwa.ui.screens.patient.booking.BookingDetailScreen(
                            navController = navController,
                            bookingId = bookingId,
                            viewModel = bookingViewModel
                        )
                    }

                    // Halaman Profil (Menu Utama)
                    composable("profile") {
                        ProfileScreen(navController, authViewModel)
                    }

                    // Halaman Detail Profil (Edit/View Detail)
                    composable("profile_detail") {
                         com.android.rumahsehatmannawasalwa.ui.screens.profile.ProfileDetailScreen(
                             navController = navController, 
                             viewModel = authViewModel
                         )
                    }

                    // Halaman Lengkapi Data (Complete Profile)
                    composable("complete_profile") {
                        val user by authViewModel.currentUserData.collectAsState()
                        com.android.rumahsehatmannawasalwa.ui.screens.profile.CompleteProfileScreen(
                            email = user?.email ?: "",
                            onSaveClick = { profile ->
                                authViewModel.updateUserProfile(
                                    name = profile.name,
                                    phone = profile.phoneNumber,
                                    job = profile.job,
                                    address = profile.address,
                                    birthDate = profile.birthDate,
                                    gender = profile.gender
                                )
                            }
                        )
                        
                        // Observe Success/Error for Navigation
                        val authState by authViewModel.authState.collectAsState()
                        val context = androidx.compose.ui.platform.LocalContext.current
                        LaunchedEffect(authState) {
                             if (authState is com.android.rumahsehatmannawasalwa.data.ApiResult.Success) {
                                  navController.navigate("home") {
                                      popUpTo("complete_profile") { inclusive = true }
                                      popUpTo("login") { inclusive = true } // Clean stack
                                  }
                                  authViewModel.resetState()
                             } else if (authState is com.android.rumahsehatmannawasalwa.data.ApiResult.Error) {
                                  val msg = (authState as com.android.rumahsehatmannawasalwa.data.ApiResult.Error).error
                                  android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                  authViewModel.resetState()
                             }
                        }
                    }

                    // --- Rute Terapis ---
                    composable("therapist_home") {
                        com.android.rumahsehatmannawasalwa.ui.screens.therapist.home.TherapistHomeScreen(navController)
                    }
                    
                    composable("therapist_appointment") {
                        com.android.rumahsehatmannawasalwa.ui.screens.therapist.appointment.TherapistAppointmentScreen(navController)
                    }
                    
                    composable("therapist_schedule") {
                        com.android.rumahsehatmannawasalwa.ui.screens.therapist.schedule.TherapistScheduleScreen(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, onLogout: () -> Unit) {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Halo, $name!")
        Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
        Button(onClick = onLogout) {
            Text("Keluar (Logout)")
        }
    }
}