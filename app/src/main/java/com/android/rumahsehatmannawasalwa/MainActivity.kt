package com.android.rumahsehatmannawasalwa

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.service.PusherService
import com.android.rumahsehatmannawasalwa.ui.components.BottomNavigationBar
import com.android.rumahsehatmannawasalwa.ui.navigation.AppNavGraph
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.RumahsehatmannawasalwaTheme
import com.android.rumahsehatmannawasalwa.ui.viewmodel.ViewModelFactory
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.BookingViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.admin.AdminDashboardViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AppointmentDetailViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyRecordViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.notification.NotificationViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.schedule.ScheduleViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.ServiceViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.therapist.TherapistAppointmentViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.therapist.TherapistDashboardViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val pendingBookingId = mutableStateOf<String?>(null)
    private val pendingScreen = mutableStateOf<String?>(null)

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val title = intent?.getStringExtra("title")
            val message = intent?.getStringExtra("message")
            val bookingId = intent?.getStringExtra("booking_id")
            showInAppNotification(title, message, bookingId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            notificationReceiver, IntentFilter("FCM_DATA_EVENT")
        )

        // Inisialisasi Pusher
        try {
            PusherService.connect()
        } catch (e: Exception) {
            Log.e("MainActivity", "Pusher Connect Error", e)
        }

        setContent {
            RumahsehatmannawasalwaTheme {
                MainApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val bookingId = it.getStringExtra("booking_id")
            val screen = it.getStringExtra("screen") ?: it.getStringExtra("type")
            Log.d("FCM_DEBUG", "MainActivity handleIntent: bookingId=$bookingId, screen=$screen")
            if (!bookingId.isNullOrEmpty()) {
                pendingBookingId.value = bookingId
                pendingScreen.value = screen
            }
        }
    }

    @Composable
    private fun MainApp() {
        val navController = rememberNavController()
        val factory = ViewModelFactory.getInstance(application)

        // ── 1. VIEWMODELS INITIALIZATION ───────────────────────────────────
        val authViewModel: AuthViewModel = viewModel(factory = factory)
        val notificationViewModel: NotificationViewModel = viewModel(factory = factory)
        val adminUserViewModel: AdminUserViewModel = viewModel(factory = factory)
        val serviceViewModel: ServiceViewModel = viewModel(factory = factory)
        val bookingViewModel: BookingViewModel = viewModel(factory = factory)
        val adminBookingViewModel: AdminBookingViewModel = viewModel(factory = factory)
        val therapyRecordViewModel: TherapyRecordViewModel = viewModel(factory = factory)
        val scheduleViewModel: ScheduleViewModel = viewModel()
        val appointmentDetailViewModel: AppointmentDetailViewModel = viewModel(factory = factory)
        val adminDashboardViewModel: AdminDashboardViewModel = viewModel(factory = factory)
        val therapistDashboardViewModel: TherapistDashboardViewModel = viewModel(factory = factory)
        val therapistAppointmentViewModel: TherapistAppointmentViewModel = viewModel(factory = factory)

        // ── 2. APP STATE & NAVIGATION ──────────────────────────────────────
        val userData by authViewModel.currentUserData.collectAsState()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Handle pending deep-linking from FCM push notifications
        LaunchedEffect(pendingBookingId.value, userData) {
            val bookingId = pendingBookingId.value
            if (bookingId != null && userData != null) {
                val role = userData?.role ?: "pasien"
                Log.d("FCM_DEBUG", "MainApp routing deep link: bookingId=$bookingId, role=$role")
                try {
                    val bid = bookingId.toInt()
                    if (role == "admin" || role == "super_admin") {
                        navController.navigate(Screen.AdminAppointmentDetail.createRoute(bid))
                    } else if (role == "terapis" || role == "therapist") {
                        navController.navigate(Screen.TherapistAppointmentDetail.createRoute(bid))
                    } else {
                        navController.navigate(Screen.PatientAppointmentDetail.createRoute(bid))
                    }
                } catch (e: NumberFormatException) {
                    Log.e("MainActivity", "Invalid bookingId format: $bookingId", e)
                } finally {
                    // Reset pending state to avoid repeating navigation on config change
                    pendingBookingId.value = null
                    pendingScreen.value = null
                }
            }
        }

        // Permission Launcher (Android 13+)
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) Log.d("MainActivity", "Izin notifikasi ditolak user")
        }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Realtime Updates (Badge)
        LaunchedEffect(userData?.id) {
            userData?.id?.let { if (it > 0) notificationViewModel.listenToRealtimeUpdates(it) }
        }

        // Keyboard Detection
        val isKeyboardVisible by rememberKeyboardVisibility()

        // ── 3. BOTTOM BAR LOGIC ────────────────────────────────────────────
        val showBottomBar = remember(currentRoute, isKeyboardVisible) {
            val mainRoutes = listOf(
                Screen.PatientHome.route,
                Screen.PatientAppointment.route,
                Screen.Record.route,
                Screen.PatientProfile.route,
                Screen.AdminHome.route,
                Screen.AdminAppointment.route,
                Screen.AdminPatientList.route,
                Screen.TherapistHome.route,
                Screen.TherapistAppointment.route,
                Screen.TherapistSchedule.route,
                Screen.TherapistPatientList.route,
                Screen.TherapistProfile.route
            )
            currentRoute in mainRoutes && !isKeyboardVisible
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(
                        navController = navController,
                        role = userData?.role ?: "patient"
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
                AppNavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    bookingViewModel = bookingViewModel,
                    adminUserViewModel = adminUserViewModel,
                    serviceViewModel = serviceViewModel,
                    therapyRecordViewModel = therapyRecordViewModel,
                    therapistAppointmentViewModel = therapistAppointmentViewModel,
                    scheduleViewModel = scheduleViewModel,
                    adminDashboardViewModel = adminDashboardViewModel,
                    adminBookingViewModel = adminBookingViewModel,
                    appointmentDetailViewModel = appointmentDetailViewModel,
                    notificationViewModel = notificationViewModel,
                    therapistDashboardViewModel = therapistDashboardViewModel
                )
            }
        }
    }

    @Composable
    private fun rememberKeyboardVisibility(): State<Boolean> {
        val keyboardState = remember { mutableStateOf(false) }
        val view = LocalView.current
        DisposableEffect(view) {
            val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
                val rect = Rect()
                view.getWindowVisibleDisplayFrame(rect)
                val screenHeight = view.rootView.height
                val keypadHeight = screenHeight - rect.bottom
                keyboardState.value = keypadHeight > screenHeight * 0.15
            }
            view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)
            onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener) }
        }
        return keyboardState
    }

    private fun showInAppNotification(title: String?, message: String?, bookingId: String?) {
        // Toast dihapus agar tidak redundant dengan notifikasi sistem
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)
    }
}
