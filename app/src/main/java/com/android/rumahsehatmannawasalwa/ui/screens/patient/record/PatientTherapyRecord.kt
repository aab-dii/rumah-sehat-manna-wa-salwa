package com.android.rumahsehatmannawasalwa.ui.screens.patient.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.appointment.RecordListContent
import com.android.rumahsehatmannawasalwa.ui.components.appointment.SharedSearchBar
import com.android.rumahsehatmannawasalwa.ui.components.appointment.TherapyHistoryItemCard
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyRecordViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientTherapyRecord(
    navController: NavController,
    viewModel: TherapyRecordViewModel,
    patientId: Int? = null, // Optional, jika dipanggil oleh Terapis
    patientName: String? = null // Optional, jika dipanggil oleh Terapis
) {
    // Terapkan argumen patientId ke ViewModel saat pertama di-load
    LaunchedEffect(patientId) {
        viewModel.setTargetPatientId(patientId)
    }

    // ── State dari ViewModel ─────────────────────────────────────────────────
    val historyItems  = viewModel.patientHistoryPager.collectAsLazyPagingItems()
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val dateFrom      by viewModel.dateFrom.collectAsState()
    val dateTo        by viewModel.dateTo.collectAsState()

    // ── UI state lokal ───────────────────────────────────────────────────────
    var showSheet       by remember { mutableStateOf(false) }
    val sheetState      = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedOption  by remember { mutableStateOf<PeriodOption>(PeriodOption.Semua) }

    val nowCal = remember { today() }
    var fromDay   by remember { mutableIntStateOf(1) }
    var fromMonth by remember { mutableIntStateOf(nowCal.get(Calendar.MONTH)) }
    var fromYear  by remember { mutableIntStateOf(nowCal.get(Calendar.YEAR)) }
    var toDay     by remember { mutableIntStateOf(nowCal.get(Calendar.DAY_OF_MONTH)) }
    var toMonth   by remember { mutableIntStateOf(nowCal.get(Calendar.MONTH)) }
    var toYear    by remember { mutableIntStateOf(nowCal.get(Calendar.YEAR)) }

    // ── Label tombol filter ──────────────────────────────────────────────────
    val hasFilter   = dateFrom != null || dateTo != null
    val filterLabel = buildFilterLabel(hasFilter, selectedOption, dateFrom, dateTo)

    // ── Bottom Sheet ─────────────────────────────────────────────────────────
    if (showSheet) {
        TherapyRecordFilterSheet(
            sheetState     = sheetState,
            selectedOption = selectedOption,
            fromDay = fromDay, fromMonth = fromMonth, fromYear = fromYear,
            toDay   = toDay,   toMonth   = toMonth,   toYear   = toYear,
            onOptionChange = { selectedOption = it },
            onFromChange   = { d, m, y -> fromDay = d; fromMonth = m; fromYear = y },
            onToChange     = { d, m, y -> toDay = d;   toMonth = m;   toYear = y },
            onApply        = { from, to ->
                viewModel.setDateRange(from, to)
                showSheet = false
            },
            onDismiss = { showSheet = false }
        )
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    Column(
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
        Spacer(modifier = Modifier.height(12.dp))

        // TopBar (compact)
        TopBar(
            title = "Riwayat Terapi",
            subtitle = patientName,
            onBackClick = if (patientId != null) { { navController.popBackStack() } } else null,
            transparentBackground = true,
            hideBackground = true,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SearchBar
        SharedSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // MannaSheet (white sheet)
        MannaSheet(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .clickable { showSheet = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = filterLabel,
                        fontSize = 13.sp,
                        fontWeight = if (hasFilter) FontWeight.SemiBold else FontWeight.Normal,
                        color = GreenPrimary
                    )
                    if (hasFilter) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reset filter",
                            tint = GreenPrimary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    viewModel.clearDateRange()
                                    selectedOption = PeriodOption.Semua
                                }
                        )
                    }
                }

                // List content
                RecordListContent(
                    items = historyItems,
                    modifier = Modifier.weight(1f)
                ) { record ->
                    TherapyHistoryItemCard(
                        record = record,
                        onClick = { navController.navigate("therapy_record_detail/${record.id}") }
                    )
                }
            }
        }
    }
}


