package com.android.rumahsehatmannawasalwa.ui.screens.therapist.report

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.report.*
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.admin.ReportViewModel
import com.android.rumahsehatmannawasalwa.utils.PdfGenerator
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import androidx.compose.ui.graphics.Brush
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistReportScreen(
    navController: NavController,
    viewModel: ReportViewModel
) {
    val period by viewModel.period.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()

    val visitsState by viewModel.visitsState.collectAsState()
    val visitsItems by viewModel.visitsItems.collectAsState()
    val performanceState by viewModel.performanceState.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Kunjungan, 1: Kinerja
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Fetch data on enter or when filters change
    LaunchedEffect(period, startDate, endDate) {
        viewModel.fetchVisitsReport(isAdmin = false)
        viewModel.fetchPerformanceReport(isAdmin = false)
    }

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

        TopBar(
            title = "Laporan Saya",
            onBackClick = { navController.popBackStack() },
            actions = {
                IconButton(
                    onClick = {
                        viewModel.setExporting(true)
                        coroutineScope.launch {
                            val responseBody = withContext(Dispatchers.IO) {
                                if (activeTab == 0) {
                                    viewModel.downloadVisitsPdf(isAdmin = false)
                                } else {
                                    viewModel.downloadPerformancePdf(isAdmin = false)
                                }
                            }
                            viewModel.setExporting(false)

                            if (responseBody != null) {
                                val reportName = if (activeTab == 0) "Laporan_Kunjungan_Terapis" else "Laporan_Kinerja_Terapis"
                                val uri = withContext(Dispatchers.IO) {
                                    PdfGenerator.savePdfToDownloads(context, responseBody, reportName)
                                }
                                if (uri != null) {
                                    Toast.makeText(context, "PDF berhasil disimpan di Downloads/MannaWaSalwa", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Gagal menyimpan PDF ke penyimpanan lokal", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Gagal mengunduh PDF dari server", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Ekspor PDF", tint = Color.White)
                    }
                }
            },
            transparentBackground = true,
            hideBackground = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        MannaSheet(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Period & Date Filter Bar ---
                FilterSection(
                    period = period,
                    startDate = startDate,
                    endDate = endDate,
                    onPeriodSelected = { viewModel.setPeriod(it) },
                    onDateRangeSelected = { start, end -> viewModel.setDateRange(start, end) }
                )

                // --- Tab Selection ---
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.White,
                    contentColor = GreenPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = GreenPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Kunjungan Bulanan", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Kinerja Diri", fontWeight = FontWeight.Bold) }
                    )
                }

                // --- Content ---
                Box(modifier = Modifier.weight(1f)) {
                    if (activeTab == 0) {
                        VisitsTabContent(
                            visitsState = visitsState,
                            visitsItems = visitsItems,
                            onLoadMore = { viewModel.loadMoreVisits(isAdmin = false) }
                        )
                    } else {
                        PerformanceTabContent(performanceState = performanceState)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    period: String,
    startDate: String?,
    endDate: String?,
    onPeriodSelected: (String) -> Unit,
    onDateRangeSelected: (String?, String?) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Period Buttons
            listOf(
                "monthly" to "Bulanan",
                "yearly" to "Tahunan",
                "custom" to "Kustom"
            ).forEach { (key, label) ->
                val isSelected = period == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) GreenPrimary else Color(0xFFF1F5F9),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onPeriodSelected(key) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else SlateTextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Custom Date Range Picker UI
        AnimatedVisibility(visible = period == "custom") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start Date Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DividerLight, RoundedCornerShape(8.dp))
                        .clickable {
                            showDatePicker(context, startDate ?: LocalDate.now().toString()) {
                                onDateRangeSelected(it, endDate)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = GrayText, modifier = Modifier.size(16.dp))
                        Text(startDate ?: "Mulai", color = SlateTextDark, fontSize = 12.sp)
                    }
                }

                Text("s/d", color = GrayText, fontSize = 12.sp)

                // End Date Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DividerLight, RoundedCornerShape(8.dp))
                        .clickable {
                            showDatePicker(context, endDate ?: LocalDate.now().toString()) {
                                onDateRangeSelected(startDate, it)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = GrayText, modifier = Modifier.size(16.dp))
                        Text(endDate ?: "Selesai", color = SlateTextDark, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun showDatePicker(context: android.content.Context, initialDate: String, onDateSelected: (String) -> Unit) {
    val date = runCatching { LocalDate.parse(initialDate) }.getOrDefault(LocalDate.now())
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formatted)
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    ).show()
}

@Composable
fun VisitsTabContent(
    visitsState: ApiResult<VisitReportResponse>,
    visitsItems: List<VisitItem>,
    onLoadMore: () -> Unit
) {
    if (visitsState is ApiResult.Loading && visitsItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenPrimary)
        }
        return
    }

    if (visitsState is ApiResult.Error && visitsItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(text = visitsState.error, color = RedDanger, fontWeight = FontWeight.SemiBold)
        }
        return
    }

    if (visitsItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌿", fontSize = 48.sp)
                Text("Belum ada data kunjungan.", color = GrayText)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(visitsItems.size) { index ->
            val visit = visitsItems[index]
            if (index == visitsItems.size - 1 && visitsState !is ApiResult.Loading) {
                LaunchedEffect(Unit) { onLoadMore() }
            }
            VisitReportCard(visit)
        }
        if (visitsState is ApiResult.Loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun VisitReportCard(visit: VisitItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (visit.isNew) GreenPrimary.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (visit.isNew) "Pasien Baru" else "Pasien Lama",
                            color = if (visit.isNew) GreenPrimary else SlateText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (visit.gender == "L") Color(0xFFE0F2FE) else Color(0xFFFCE7F3),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (visit.gender == "L") "Laki-laki" else "Perempuan",
                            color = if (visit.gender == "L") Color(0xFF0369A1) else Color(0xFFBE185D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(visit.date, fontSize = 11.sp, color = GrayText)
            }

            // Body
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = visit.patientName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateTextDark
                )
                Text(
                    text = "${visit.patientAge ?: "-"} tahun • ${visit.address}",
                    fontSize = 13.sp,
                    color = GrayText
                )
            }

            HorizontalDivider(color = DividerLight)

            // Service type and complaint
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Layanan:", fontSize = 12.sp, color = GrayText, fontWeight = FontWeight.Medium)
                    val serviceLabel = when {
                        visit.isKombinasi -> "Kombinasi"
                        visit.isKeterampilan -> "Keterampilan"
                        visit.isRamuan -> "Ramuan"
                        else -> "-"
                    }
                    Text(serviceLabel, fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                }

                if (visit.complaint.isNotEmpty()) {
                    Text(
                        text = "Keluhan: ${visit.complaint}",
                        fontSize = 12.sp,
                        color = SlateText
                    )
                }
                if (visit.notes.isNotEmpty() && visit.notes != "-") {
                    Text(
                        text = "Tindakan: ${visit.notes}",
                        fontSize = 12.sp,
                        color = SlateText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceTabContent(performanceState: ApiResult<PerformanceReportResponse>) {
    when (performanceState) {
        is ApiResult.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        }
        is ApiResult.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(text = performanceState.error, color = RedDanger, fontWeight = FontWeight.SemiBold)
            }
        }
        is ApiResult.Success -> {
            val therapistPerf = performanceState.data.therapists.firstOrNull()
            if (therapistPerf == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Data kinerja tidak ditemukan.", color = GrayText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Overall sessions
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Total Sesi Dilayani", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${therapistPerf.totalSessions} Sesi",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Key metrics
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Total Pasien", color = GrayText, fontSize = 12.sp)
                                    Text("${therapistPerf.totalPatients}", color = SlateTextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Revenue Terkumpul", color = GrayText, fontSize = 12.sp)
                                    Text(FormatterUtils.formatRupiah(therapistPerf.totalRevenue), color = GreenPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Detailed metrics card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Breakdown Pasien", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pasien Baru", color = GrayText, fontSize = 13.sp)
                                    Text("${therapistPerf.newPatients} orang", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pasien Lama", color = GrayText, fontSize = 13.sp)
                                    Text("${therapistPerf.oldPatients} orang", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Sesi Dibatalkan", color = GrayText, fontSize = 13.sp)
                                    Text("${therapistPerf.totalCanceled} sesi", fontWeight = FontWeight.Bold, color = RedDanger, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Detailed services card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Kategori Tindakan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Bekam (Bekam)", color = GrayText, fontSize = 13.sp)
                                    Text("${therapistPerf.totalBekam} sesi", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Akupunktur (Keterampilan)", color = GrayText, fontSize = 13.sp)
                                    Text("${therapistPerf.totalAkupunktur} sesi", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Ramuan (Ramuan)", color = GrayText, fontSize = 13.sp)
                                    Text("${therapistPerf.totalRamuan} sesi", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
