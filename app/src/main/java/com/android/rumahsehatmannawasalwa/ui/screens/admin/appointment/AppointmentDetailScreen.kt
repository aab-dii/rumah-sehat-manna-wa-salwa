package com.android.rumahsehatmannawasalwa.ui.screens.admin.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.ui.components.ActionOverlay
import com.android.rumahsehatmannawasalwa.ui.components.ErrorPlaceholder
import com.android.rumahsehatmannawasalwa.ui.components.SectionTitle
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.appointment.AdminActionButtons
import com.android.rumahsehatmannawasalwa.ui.components.appointment.MeetingInfoCard
import com.android.rumahsehatmannawasalwa.ui.components.appointment.PaymentDetailCard
import com.android.rumahsehatmannawasalwa.ui.components.appointment.ServiceCard
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbar
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbarVisuals
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.SnackbarType
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AppointmentDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    navController: NavController,
    bookingId: Int,
    viewModel: AppointmentDetailViewModel = viewModel()
) {
    val state         by viewModel.detailState.collectAsState()
    val isUpdating    by viewModel.isUpdating.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    LaunchedEffect(bookingId) {
        viewModel.getAppointmentDetail(bookingId)
        viewModel.subscribeToRealTimeUpdate(bookingId)
    }

    // Cleanup: unsubscribe saat screen di-dispose
    DisposableEffect(bookingId) {
        onDispose { viewModel.unsubscribeFromRealTimeUpdate(bookingId) }
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let { msg ->
            val type = if (msg.startsWith("Gagal")) SnackbarType.ERROR else SnackbarType.SUCCESS
            snackbarHostState.showSnackbar(
                MannaSnackbarVisuals(message = msg, type = type)
            )
            viewModel.resetActionMessage()
        }
    }

    val scrollState = rememberScrollState()

    // Box terluar — semua child harus di dalam sini agar .align() valid
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to GreenDark,
                        0.25f to GreenLight,
                        1.0f to GreenLight
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            TopBar(
                title = "Detail Janji Temu",
                subtitle = "Manajemen data janji temu pasien",
                onBackClick = { navController.popBackStack() },
                transparentBackground = true,
                hideBackground = true,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(25.dp))

                    MannaSheet {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 32.dp)
                        ) {
                            when (val result = state) {
                                is ApiResult.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(400.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = GreenPrimary)
                                    }
                                }
                                is ApiResult.Error -> {
                                    ErrorPlaceholder(result.error) {
                                        viewModel.getAppointmentDetail(bookingId)
                                    }
                                }
                                is ApiResult.Success -> {
                                    DetailContent(
                                        data          = result.data,
                                        viewModel     = viewModel,
                                        navController = navController
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }

                // ActionOverlay tetap di Box
                if (isUpdating) ActionOverlay()
            }
        }

        // SnackbarHost tetap di Box terluar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) { data ->
            MannaSnackbar(snackbarData = data)
        }
    }
}

@Composable
fun DetailContent(
    data: BookingUiModel,
    viewModel: AppointmentDetailViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            SectionTitle("Info Janji Temu")
            MeetingInfoCard(data)
        }

        Column {
            SectionTitle("Detail Layanan")
            ServiceCard(data, navController)
        }

        Column {
            SectionTitle("Rincian Pembayaran")
            PaymentDetailCard(data, viewModel)
        }

        Spacer(Modifier.height(12.dp))

        val bStatus = data.appointment?.status?.lowercase()
        if (
            bStatus != "canceled"        &&
            bStatus != "cancelled"       &&
            bStatus != "batal"           &&
            bStatus != "completed"       &&
            bStatus != "selesai"         &&
            bStatus != "force_completed"
        ) {
            AdminActionButtons(
                data      = data,
                viewModel = viewModel
            )
        }
    }
}