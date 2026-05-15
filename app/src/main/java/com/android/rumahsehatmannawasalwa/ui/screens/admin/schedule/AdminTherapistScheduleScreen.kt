package com.android.rumahsehatmannawasalwa.ui.screens.admin.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.screens.therapist.schedule.components.*
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.schedule.ScheduleViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTherapistScheduleScreen(
    navController: NavController,
    therapistId: Int,
    scheduleViewModel: ScheduleViewModel,
    userViewModel: AdminUserViewModel
) {
    // Fetch Data
    LaunchedEffect(therapistId) {
        scheduleViewModel.fetchSchedules(therapistId)
        userViewModel.fetchUserDetail(therapistId)
    }

    val schedulesState by scheduleViewModel.scheduleState.collectAsState()
    val schedules by scheduleViewModel.schedules.collectAsState()

    val therapistDetailState by userViewModel.userDetailState.collectAsState()
    val therapistDetail = (therapistDetailState as? ApiResult.Success)?.data

    // Day Types
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    // Emergency Close State
    var showEmergencySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Date Info
    val dayName = FormatterUtils.getTodayDayName()
    val dateString = FormatterUtils.getTodayFormattedDate()
    val currentDateParam = FormatterUtils.getTodaySqlDate()

    // State for Add Holiday Dialog
    var showAddHolidayDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val isSnackbarShowing = snackbarHostState.currentSnackbarData != null
    val fabOffset by animateDpAsState(
        targetValue = if (isSnackbarShowing) (-80).dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "fab_movement"
    )

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
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. HEADER (TopBar)
            TopBar(
                title = therapistDetail?.name?.let { "Jadwal $it" } ?: "Atur Jadwal Terapis",
                subtitle = "Kelola hari kerja & libur ekstra",
                onBackClick = { navController.popBackStack() },
                transparentBackground = true,
                hideBackground = true,
            )

            Spacer(modifier = Modifier.height(12.dp))
            // 2. KONTEN SHEET PUTIH
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    CurrentDateSection(
                        dayName = dayName,
                        dateString = dateString,
                        onEmergencyClose = { showEmergencySheet = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MannaSheet(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (schedulesState is ApiResult.Loading) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GreenPrimary)
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {


                                ScheduleListSection(
                                    days = days,
                                    schedules = schedules,
                                    therapistId = therapistId,
                                    onSaveSchedule = { tId, dName, start, end, active ->
                                        scheduleViewModel.updateSchedule(
                                            therapistId = tId, day = dName, startTime = start, endTime = end, isActive = active,
                                            onSuccess = { msg ->
                                                scope.launch {
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                    snackbarHostState.showSnackbar(MannaSnackbarVisuals(message = msg, type = SnackbarType.SUCCESS))
                                                }
                                            },
                                            onError = { msg ->
                                                scope.launch {
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                    snackbarHostState.showSnackbar(MannaSnackbarVisuals(message = msg, type = SnackbarType.ERROR))
                                                }
                                            }
                                        )
                                    }
                                )

                                val todayStr = FormatterUtils.getTodaySqlDate()
                                val holidays = schedules.filter { schedule ->
                                    val isHolidayType = schedule.type == "holiday" || (schedule.specificDate != null && schedule.isActive == false)
                                    if (!isHolidayType) return@filter false
                                    val relevantDate = schedule.endDate ?: schedule.specificDate ?: return@filter true
                                    relevantDate >= todayStr
                                }
                                HolidayListSection(holidays)

                                Spacer(modifier = Modifier.height(100.dp)) // Extra padding for FAB
                            }
                        }
                    }
                }
            }
        }

        // --- FAB (Manual Position) ---
        FloatingActionButton(
            onClick = { showAddHolidayDialog = true },
            containerColor = GreenPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = fabOffset)
                .padding(bottom = 32.dp, end = 24.dp)
                .navigationBarsPadding()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal Libur")
        }

        // --- Snackbar Host (Manual Position) ---
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) { data ->
            MannaSnackbar(snackbarData = data)
        }
    }

    // --- EMERGENCY CLOSE BOTTOM SHEET ---
    if (showEmergencySheet) {
        EmergencyCloseSheet(
            dateString = dateString,
            sheetState = sheetState,
            scope = scope,
            onDismissRequest = { showEmergencySheet = false },
            onConfirmClose = { reason ->
                scheduleViewModel.emergencyClose(
                    therapistId = therapistId, date = currentDateParam, reason = reason,
                    onSuccess = { msg ->
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showEmergencySheet = false
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(MannaSnackbarVisuals(message = msg, type = SnackbarType.SUCCESS))
                            }
                        }
                    },
                    onError = { msg ->
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(MannaSnackbarVisuals(message = msg, type = SnackbarType.ERROR))
                        }
                    }
                )
            }
        )
    }

    // --- ADD HOLIDAY DIALOG ---
    if (showAddHolidayDialog) {
        AddHolidayDialog(
            onDismissRequest = { showAddHolidayDialog = false },
            onConfirmHoliday = { start, end, reason ->
                scheduleViewModel.addHoliday(therapistId, start, end, reason) {
                    showAddHolidayDialog = false
                }
            }
        )
    }
}
