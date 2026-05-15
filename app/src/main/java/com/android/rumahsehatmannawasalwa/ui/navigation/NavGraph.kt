package com.android.rumahsehatmannawasalwa.ui.navigation

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.rumahsehatmannawasalwa.ui.screens.DispatchScreen
import com.android.rumahsehatmannawasalwa.ui.screens.auth.LoginScreen
import com.android.rumahsehatmannawasalwa.ui.screens.auth.RegisterScreen
import com.android.rumahsehatmannawasalwa.ui.screens.patient.home.HomeScreen
import com.android.rumahsehatmannawasalwa.ui.screens.profile.ProfileScreen
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.BookingViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.ServiceViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.rumahsehatmannawasalwa.ui.screens.admin.appointment.AdminBookingCreateScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.appointment.AdminAppointmentScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.appointment.AppointmentDetailScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.home.AdminHomeScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.schedule.AdminTherapistListScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.schedule.AdminTherapistScheduleScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.services.AdminFormServiceScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.services.AdminManageServicesScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.services.AdminServiceDetailScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.users.AdminAddUserScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.users.AdminEditUserScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.users.AdminManageUsersScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.users.AdminUserDetailScreen
import com.android.rumahsehatmannawasalwa.ui.screens.admin.verification.AdminVerificationScreen
import com.android.rumahsehatmannawasalwa.ui.screens.patient.appointment.BookingScreen
import com.android.rumahsehatmannawasalwa.ui.screens.patient.appointment.BookingSummaryScreen
import com.android.rumahsehatmannawasalwa.ui.screens.patient.appointment.PatientAppointmentScreen
import com.android.rumahsehatmannawasalwa.ui.screens.patient.appointment.PatientAppointmentDetailScreen
import com.android.rumahsehatmannawasalwa.ui.screens.patient.record.PatientTherapyRecord
import com.android.rumahsehatmannawasalwa.ui.screens.profile.CompleteProfileScreen
import com.android.rumahsehatmannawasalwa.ui.screens.splash.SplashScreen
import com.android.rumahsehatmannawasalwa.ui.screens.therapist.appointment.TherapistAppointmentScreen
import com.android.rumahsehatmannawasalwa.ui.screens.therapist.home.TherapistHomeScreen
import com.android.rumahsehatmannawasalwa.ui.screens.therapist.record.TherapyRecordDetailScreen
import com.android.rumahsehatmannawasalwa.ui.screens.therapist.record.TherapyRecordFormScreen
import com.android.rumahsehatmannawasalwa.ui.screens.therapist.schedule.TherapistScheduleScreen
import com.android.rumahsehatmannawasalwa.ui.screens.notifications.NotificationScreen
import com.android.rumahsehatmannawasalwa.ui.screens.offline.OfflineScreen
import com.android.rumahsehatmannawasalwa.ui.screens.profile.ProfileDetailScreen
import com.android.rumahsehatmannawasalwa.ui.screens.therapist.patient.PatientListScreen
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.viewmodel.ViewModelFactory
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.admin.AdminDashboardViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AppointmentDetailViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyRecordViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.notification.NotificationViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.schedule.ScheduleViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.therapist.TherapistAppointmentViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.therapist.TherapistDashboardViewModel
import com.android.rumahsehatmannawasalwa.utils.NetworkUtils

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    bookingViewModel: BookingViewModel,
    adminUserViewModel: AdminUserViewModel,
    serviceViewModel: ServiceViewModel,
    therapyRecordViewModel: TherapyRecordViewModel,
    therapistAppointmentViewModel: TherapistAppointmentViewModel,
    scheduleViewModel: ScheduleViewModel,
    adminDashboardViewModel: AdminDashboardViewModel,
    adminBookingViewModel: AdminBookingViewModel,
    appointmentDetailViewModel: AppointmentDetailViewModel,
    notificationViewModel: NotificationViewModel,
    therapistDashboardViewModel: TherapistDashboardViewModel
) {
    val context = LocalContext.current
    val userData by authViewModel.currentUserData.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ── 1. AUTH & STARTUP ────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen {
                navController.navigate(Screen.Dispatch.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Dispatch.route) { DispatchScreen(navController, authViewModel) }
        composable(Screen.Login.route) { LoginScreen(navController, authViewModel) }
        composable(Screen.Register.route) { RegisterScreen(navController, authViewModel) }


        // ── 2. COMMON & PROFILE ──────────────────────────────────────────────
        composable(Screen.Notifications.route) {
            val role = userData?.role ?: "pasien"
            NotificationScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onNotificationClick = { bid ->
                    if (role == "admin") {
                        navController.navigate(Screen.AdminAppointmentDetail.createRoute(bid))
                    } else {
                        navController.navigate(Screen.PatientAppointmentDetail.createRoute(bid))
                    }
                }
            )
        }

        composable(Screen.ProfileDetail.route) {
            ProfileDetailScreen(navController = navController, viewModel = authViewModel)
        }

        composable(
            route = Screen.CompleteProfile.route,
            arguments = Screen.CompleteProfile.arguments
        ) { backStackEntry ->
            val isFromAuth = backStackEntry.arguments?.getBoolean("isFromAuth") ?: false
            CompleteProfileScreen(navController, authViewModel, isFromAuth)
        }

        composable(Screen.Offline.route) {
            OfflineScreen(onRetry = {
                if (NetworkUtils.isInternetAvailable(context)) navController.popBackStack()
                else Toast.makeText(context, "Masih belum ada koneksi...", Toast.LENGTH_SHORT).show()
            })
        }


        // ── 3. PATIENT FEATURES ──────────────────────────────────────────────
        composable(Screen.PatientHome.route) {
            HomeScreen(navController, serviceViewModel, authViewModel, notificationViewModel)
        }

        composable(Screen.PatientProfile.route) { ProfileScreen(navController, authViewModel) }
        
        composable(Screen.PatientAppointment.route) { 
            PatientAppointmentScreen(navController, adminBookingViewModel) 
        }

        composable(
            route = Screen.PatientAppointmentDetail.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("bookingId") ?: 0
            PatientAppointmentDetailScreen(navController, id, appointmentDetailViewModel)
        }

        composable(Screen.Record.route) { 
            PatientTherapyRecord(navController, therapyRecordViewModel) 
        }

        composable(route = Screen.Booking.route, arguments = Screen.Booking.arguments) { backStackEntry ->
            val sId = backStackEntry.arguments?.getString("sId") ?: ""
            val sName = backStackEntry.arguments?.getString("sName") ?: ""
            val sPrice = backStackEntry.arguments?.getInt("sPrice") ?: 0
            BookingScreen(navController, sId, sName, sPrice, bookingViewModel)
        }

        composable(Screen.BookingSummary.route) {
            BookingSummaryScreen(navController, bookingViewModel)
        }


        // ── 4. THERAPIST FEATURES ────────────────────────────────────────────
        composable(Screen.TherapistHome.route) {
            TherapistHomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                dashboardViewModel = therapistDashboardViewModel,
                notificationViewModel = notificationViewModel
            )
        }

        composable(Screen.TherapistProfile.route) { ProfileScreen(navController, authViewModel) }

        composable(Screen.TherapistSchedule.route) {
            val currentUser = authViewModel.currentUserData.collectAsState().value
            val therapistId = currentUser?.id ?: 0
            TherapistScheduleScreen(navController, therapistId, scheduleViewModel)
        }

        composable(Screen.TherapistAppointment.route) {
            TherapistAppointmentScreen(navController, adminBookingViewModel)
        }

        composable(Screen.TherapistPatientList.route) {
            PatientListScreen(navController = navController, viewModel = adminUserViewModel)
        }

        composable(
            route = Screen.TherapyRecordForm.route,
            arguments = Screen.TherapyRecordForm.arguments
        ) { backStackEntry ->
            val bId = backStackEntry.arguments?.getInt("bookingId") ?: 0
            val vm: TherapyRecordViewModel = viewModel(
                factory = ViewModelFactory.getInstance(context.applicationContext as Application)
            )
            TherapyRecordFormScreen(bookingId = bId, viewModel = vm, navController = navController)
        }

        composable(
            route = Screen.TherapyRecordDetail.route,
            arguments = Screen.TherapyRecordDetail.arguments
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getInt("recordId") ?: 0
            val userRole = userData?.role ?: ""
            // Guard akses
            if (recordId != 0 && userRole in listOf("pasien", "terapis", "admin")) {
                TherapyRecordDetailScreen(recordId, therapyRecordViewModel, navController)
            } else {
                navController.popBackStack()
            }
        }


        // ── 5. ADMIN FEATURES ────────────────────────────────────────────────
        // Dashboard
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(navController, adminDashboardViewModel, notificationViewModel, authViewModel)
        }

        // Appointment & Verification
        composable(Screen.AdminAppointment.route) { AdminAppointmentScreen(navController, adminBookingViewModel) }
        composable(Screen.AdminVerification.route) { AdminVerificationScreen(navController, adminBookingViewModel) }
        composable(
            route = Screen.AdminAppointmentDetail.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bid = backStackEntry.arguments?.getInt("bookingId") ?: 0
            AppointmentDetailScreen(navController, bid, appointmentDetailViewModel)
        }
        composable(Screen.CreateBooking.route) {
            AdminBookingCreateScreen(navController, adminBookingViewModel, adminUserViewModel, serviceViewModel)
        }

        // User Management
        composable(Screen.AdminManageUser.route) { AdminManageUsersScreen(navController, adminUserViewModel) }
        composable(Screen.AdminAddUser.route) { AdminAddUserScreen(navController, adminUserViewModel) }
        composable(
            route = Screen.AdminUserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getInt("userId") ?: 0
            AdminUserDetailScreen(navController, adminUserViewModel, uid)
        }
        composable(
            route = Screen.AdminEditUser.route,
            arguments = Screen.AdminEditUser.arguments
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getInt("userId") ?: 0
            AdminEditUserScreen(navController, adminUserViewModel, uid)
        }

        // Patient History (Admin context)
        composable(Screen.AdminPatientList.route) {
            PatientListScreen(navController = navController, viewModel = adminUserViewModel)
        }
        composable(
            route = Screen.PatientHistory.route,
            arguments = listOf(navArgument("patientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pid = backStackEntry.arguments?.getInt("patientId") ?: 0
            val vm: TherapyRecordViewModel = viewModel(
                factory = ViewModelFactory.getInstance(context.applicationContext as Application)
            )
            PatientTherapyRecord(navController, vm, pid, "Riwayat Pasien")
        }

        // Service Management
        composable(Screen.AdminManageService.route) {
            AdminManageServicesScreen(
                navController = navController,
                viewModel = serviceViewModel,
                onEditServiceClick = { sid -> navController.navigate(Screen.AdminEditService.createRoute(sid)) },
                onAddServiceClick = { navController.navigate(Screen.AdminAddService.route) }
            )
        }
        composable(Screen.AdminAddService.route) { AdminFormServiceScreen(navController, serviceViewModel) }
        composable(
            route = Screen.AdminServiceDetail.route,
            arguments = Screen.AdminServiceDetail.arguments
        ) { backStackEntry ->
            val sid = backStackEntry.arguments?.getInt("serviceId") ?: 0
            AdminServiceDetailScreen(navController, sid, serviceViewModel)
        }
        composable(
            route = Screen.AdminEditService.route,
            arguments = Screen.AdminEditService.arguments
        ) { backStackEntry ->
            val sid = backStackEntry.arguments?.getInt("serviceId") ?: 0
            AdminFormServiceScreen(navController, serviceViewModel, sid)
        }

        // Therapist & Schedule Management
        composable(Screen.AdminTherapistList.route) {
            AdminTherapistListScreen(
                navController = navController,
                viewModel = adminUserViewModel,
                onTherapistClick = { tid -> navController.navigate(Screen.AdminTherapistSchedule.createRoute(tid)) }
            )
        }
        composable(
            route = Screen.AdminTherapistSchedule.route,
            arguments = listOf(navArgument("therapistId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tid = backStackEntry.arguments?.getInt("therapistId") ?: 0
            AdminTherapistScheduleScreen(navController, tid, scheduleViewModel, adminUserViewModel)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = GreenPrimary)
    }
}