package com.android.rumahsehatmannawasalwa.ui.screens.therapist.schedule

import android.app.TimePickerDialog

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.schedule.ScheduleViewModel
import com.android.rumahsehatmannawasalwa.ui.screens.therapist.schedule.components.*
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.*
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistScheduleScreen(
    navController: NavController,
    therapistId: Int,
    scheduleViewModel: ScheduleViewModel = viewModel()
) {
    val context = LocalContext.current

    // Fetch Data
    LaunchedEffect(therapistId) {
        scheduleViewModel.fetchSchedules(therapistId)
    }

    val schedulesState by scheduleViewModel.scheduleState.collectAsState()
    val schedules by scheduleViewModel.schedules.collectAsState()

    // Day Types
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    // Emergency Close State
    var showEmergencySheet by remember { mutableStateOf(false) }
    var cancellationReason by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Date Info
    val dayName = FormatterUtils.getTodayDayName()
    val dateString = FormatterUtils.getTodayFormattedDate()
    val currentDateParam = FormatterUtils.getTodaySqlDate()

    // State for Add Holiday Dialog
    var showAddHolidayDialog by remember { mutableStateOf(false) }
    var holidayStartDate by remember { mutableStateOf("") }
    var holidayEndDate by remember { mutableStateOf("") }
    var holidayReason by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val isSnackbarShowing = snackbarHostState.currentSnackbarData != null
    val fabOffset by animateDpAsState(
        targetValue = if (isSnackbarShowing) (-80).dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "fab_movement"
    )

    Scaffold(
        containerColor = BackgroundWhite,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.offset(y = 60.dp)
            ) { data ->
                MannaSnackbar(snackbarData = data)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHolidayDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.offset(y = fabOffset)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal Libur")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. HEADER GRADIENT (TopBar)
            TopBar(
                title = "Jadwal Anda",
                subtitle = "Kelola hari kerja & libur ekstra",
                onBackClick = { navController.popBackStack() },
                bottomExtra = 90.dp
            )

            // 2. KONTEN SHEET PUTIH (Lengkungan statis, daftar di dalamnya scroll)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 160.dp) // Bottom sheet putih turun
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) // Menggunting anak-anaknya seukuran lengkungan
                    .background(color = BackgroundWhite)
            ) {
                if (schedulesState is ApiResult.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else {
                    // SCROLLABLE LISTS INSIDE THE WHITE SHEET
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

                        Spacer(modifier = Modifier.height(80.dp)) // Extra padding for FAB
                    }
                }
            }

            // 3. KONTEN CONTENT OVERLAP (STICKY HEADER TANGGAL)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 90.dp) // Start exactly in the middle of TopBar extra space
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CurrentDateSection(
                        dayName = dayName,
                        dateString = dateString,
                        onEmergencyClose = { showEmergencySheet = true }
                    )
                }
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
}





