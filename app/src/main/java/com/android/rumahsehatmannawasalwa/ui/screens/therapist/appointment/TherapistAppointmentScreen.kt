package com.android.rumahsehatmannawasalwa.ui.screens.therapist.appointment

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.rememberPagerState
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.ui.components.appointment.AppointmentListCard
import com.android.rumahsehatmannawasalwa.ui.components.UserRole
import com.android.rumahsehatmannawasalwa.ui.components.appointment.AppointmentListContent
import com.android.rumahsehatmannawasalwa.ui.components.appointment.therapistHistoryFilterOptions
import com.android.rumahsehatmannawasalwa.ui.components.appointment.therapistUpcomingFilterOptions
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaOutlinedButton
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundWhite
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.draw.clip
import com.android.rumahsehatmannawasalwa.data.mapper.BookingMapper.mapToUiModel
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.TextPrimary
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils.formatDateHuman
import com.android.rumahsehatmannawasalwa.ui.components.ActionOverlay
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbar
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbarVisuals
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.SnackbarType
import com.android.rumahsehatmannawasalwa.data.ApiResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistAppointmentScreen(
    navController: NavController,
    viewModel: AdminBookingViewModel
) {
    val upcomingItems = viewModel.upcomingPager.collectAsLazyPagingItems()
    val historyItems = viewModel.historyPager.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    val upcomingChipSelected by viewModel.upcomingChipFilter.collectAsState()
    val historyChipSelected by viewModel.historyChipFilter.collectAsState()

    val uiState by viewModel.uiState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    var showStartConfirmDialog by remember { mutableStateOf(false) }
    var bookingToStart by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(operationState) {
        operationState?.let { result ->
            when (result) {
                is ApiResult.Success -> {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(
                            message = result.data,
                            type = SnackbarType.SUCCESS
                        )
                    )
                    viewModel.resetState()
                }
                is ApiResult.Error -> {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(
                            message = result.error,
                            type = SnackbarType.ERROR
                        )
                    )
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.isUserAdmin = false
        // Set default filter ke "Semua" versi terapis (confirmed,in_progress) agar tidak bocor pakai filter pasien
        viewModel.setUpcomingChipFilter(therapistUpcomingFilterOptions.first().value)
        viewModel.setHistoryChipFilter(therapistHistoryFilterOptions.first().value)
        viewModel.refreshBookings()
    }

    Scaffold(
        containerColor = BackgroundWhite,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. HEADER GRADIENT
            TopBar(
                title = "Janji Temu Saya",
                subtitle = "Daftar pasien dan jadwal terapi Anda",
                bottomExtra = 90.dp
            )

            // 2. KONTEN SHEET PUTIH (Lengkungan ditarik ke bawah)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 160.dp) // Bottom sheet putih turun
                    .background(
                        color = BackgroundWhite,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
            )

            // 3. KONTEN OVERLAP
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp) // Offset ke atas
            ) {
                val isUpcomingFilterValid = therapistUpcomingFilterOptions.any { it.value == upcomingChipSelected }
                val isHistoryFilterValid = therapistHistoryFilterOptions.any { it.value == historyChipSelected }

                if (isUpcomingFilterValid && isHistoryFilterValid) {
                    AppointmentListContent(
                        padding = PaddingValues(0.dp),
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.onSearchQueryInternalChanged(it) },
                        pagerState = pagerState,
                        coroutineScope = coroutineScope,
                        upcomingItems = upcomingItems,
                        historyItems = historyItems,
                        upcomingOptions = therapistUpcomingFilterOptions,
                        upcomingChipSelected = upcomingChipSelected,
                        onUpcomingChipSelected = { viewModel.setUpcomingChipFilter(it) },
                        historyOptions = therapistHistoryFilterOptions,
                        historyChipSelected = historyChipSelected,
                        onHistoryChipSelected = { viewModel.setHistoryChipFilter(it) }
                    ) { booking, isHistory ->
                        val ui = remember(booking) { mapToUiModel(booking, "terapis") }
                        
                        AppointmentListCard(
                            serviceName = ui.service?.name ?: "Layanan",
                            statusLabel = ui.statusLabel,
                            statusColor = ui.statusColor,
                            dateInfo = "${formatDateHuman(ui.appointment?.bookingDate)}, ${ui.appointment?.bookingTime} WITA",
                            personLabel1 = "${ui.patient?.name ?: "-"} (Pasien)",
                            personLabel2 = null,
                            onClick = {
                                 ui.appointment?.let { navController.navigate(Screen.TherapistAppointmentDetail.createRoute(it.id)) }
                            },
                            actions = if (!isHistory) {
                                {
                                    val isInProgress = ui.appointment?.status == "in_progress"
                                    
                                    // Tombol Lihat Riwayat
                                    MannaOutlinedButton(
                                        text = "Riwayat",
                                        onClick = { ui.patient?.let { navController.navigate(Screen.PatientHistory.createRoute(it.id)) } },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        contentColor = GreenPrimary,
                                        borderColor = GreenPrimary
                                    )

                                    // Tombol Mulai / Selesaikan
                                    MannaButton(
                                        text = if (isInProgress) "Terapi" else "Mulai",
                                        onClick = {
                                            ui.appointment?.let { appointment ->
                                                if (isInProgress) {
                                                    // Selesaikan -> Buka form rekam medis
                                                    val patientId = ui.patient?.id
                                                    val therapistId = ui.therapist?.id
                                                    if (patientId != null && therapistId != null) {
                                                        val route = Screen.TherapyRecordForm.createRoute(
                                                             bId = appointment.id,
                                                         )
                                                         Log.d("TherapyRecordForm", "Navigating with photos - Patient: ${ui.patient.profilePhotoUrl ?: ui.patient.profilePhotoPath}, Therapist: ${ui.therapist?.profilePhotoUrl ?: ui.therapist?.profilePhotoPath}, Service: ${ui.service?.imageUrl}")
                                                        navController.navigate(route)
                                                    }
                                                } else {
                                                    // Mulai -> Tampilkan Konfirmasi
                                                    bookingToStart = appointment.id
                                                    showStartConfirmDialog = true
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        containerColor = if (isInProgress) Color(0xFF0288D1) else GreenPrimary
                                    )
                                }
                            } else null
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) { data ->
                MannaSnackbar(snackbarData = data)
            }

            if (uiState.isLoading) {
                ActionOverlay()
            }
        }

        CustomConfirmDialog(
            show = showStartConfirmDialog,
            onDismiss = { showStartConfirmDialog = false },
            onConfirm = {
                showStartConfirmDialog = false
                bookingToStart?.let { id ->
                    viewModel.updateBookingStatus(id, "in_progress")
                }
            },
            title = "Mulai Terapi?",
            description = "Apakah Anda yakin ingin memulai sesi terapi sekarang? Status akan berubah menjadi Sedang Berlangsung.",
            confirmText = "Mulai Sekarang",
            dismissText = "Batal",
            icon = Icons.Default.PlayArrow
        )
    }
}
