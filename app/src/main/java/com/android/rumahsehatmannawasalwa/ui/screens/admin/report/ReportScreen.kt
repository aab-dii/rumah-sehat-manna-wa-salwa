package com.android.rumahsehatmannawasalwa.ui.screens.admin.report

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.report.*
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.admin.ReportViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import com.android.rumahsehatmannawasalwa.utils.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel,
    authViewModel: AuthViewModel,
    adminUserViewModel: AdminUserViewModel
) {
    val period by viewModel.period.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val selectedTherapistId by viewModel.selectedTherapistId.collectAsState()

    // Auth & Role
    val currentUser by authViewModel.currentUserData.collectAsState()
    val isSuperAdmin = currentUser?.role == "super_admin"

    // Report States
    val financialState by viewModel.financialState.collectAsState()
    val financialTransactions by viewModel.financialTransactions.collectAsState()

    val visitsState by viewModel.visitsState.collectAsState()
    val visitsItems by viewModel.visitsItems.collectAsState()

    val performanceState by viewModel.performanceState.collectAsState()
    val activityState by viewModel.activityState.collectAsState()
    val comparativeState by viewModel.comparativeState.collectAsState()

    // Therapists list for filtering visits
    val therapistList by adminUserViewModel.therapistList.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Load data
    LaunchedEffect(period, startDate, endDate, selectedTherapistId) {
        viewModel.fetchAllReports()
    }

    LaunchedEffect(Unit) {
        adminUserViewModel.fetchUserList("terapis")
    }

    // Tabs listing
    val tabs = remember(isSuperAdmin) {
        if (isSuperAdmin) {
            listOf("Keuangan", "Kunjungan", "Kinerja", "Kegiatan", "Komparatif")
        } else {
            listOf("Keuangan", "Kunjungan", "Kinerja", "Kegiatan")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Klinik", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.setExporting(true)
                            coroutineScope.launch {
                                val responseBody = withContext(Dispatchers.IO) {
                                    when (activeTab) {
                                        0 -> viewModel.downloadFinancialPdf()
                                        1 -> viewModel.downloadVisitsPdf(isAdmin = true)
                                        2 -> viewModel.downloadPerformancePdf(isAdmin = true)
                                        3 -> viewModel.downloadActivityPdf()
                                        4 -> if (isSuperAdmin) viewModel.downloadComparativePdf() else null
                                        else -> null
                                    }
                                }
                                viewModel.setExporting(false)

                                if (responseBody != null) {
                                    val reportName = when (activeTab) {
                                        0 -> "Laporan_Keuangan"
                                        1 -> "Laporan_Kunjungan"
                                        2 -> "Laporan_Kinerja_Terapis"
                                        3 -> "Laporan_Kegiatan_Klinik"
                                        4 -> "Laporan_Komparatif_Terapis"
                                        else -> "Laporan"
                                    }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
        ) {
            // --- Period & Date Filter Bar ---
            FilterSection(
                period = period,
                startDate = startDate,
                endDate = endDate,
                onPeriodSelected = { viewModel.setPeriod(it) },
                onDateRangeSelected = { start, end -> viewModel.setDateRange(start, end) }
            )

            // --- Tab Selection ---
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = GreenPrimary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = GreenPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // --- Content ---
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> FinancialTabContent(
                        financialState = financialState,
                        transactions = financialTransactions,
                        onLoadMore = { viewModel.loadMoreFinancial() }
                    )
                    1 -> VisitsTabContent(
                        visitsState = visitsState,
                        visitsItems = visitsItems,
                        therapists = therapistList,
                        selectedTherapistId = selectedTherapistId,
                        onTherapistSelected = { viewModel.setSelectedTherapistId(it) },
                        onLoadMore = { viewModel.loadMoreVisits(isAdmin = true) }
                    )
                    2 -> PerformanceTabContent(performanceState = performanceState)
                    3 -> ActivityTabContent(activityState = activityState)
                    4 -> if (isSuperAdmin) {
                        ComparativeTabContent(comparativeState = comparativeState)
                    }
                }
            }
        }
    }
}

// Reuse showDatePicker from TherapistReportScreen or declare here
fun showAdminDatePicker(context: android.content.Context, initialDate: String, onDateSelected: (String) -> Unit) {
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
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

        AnimatedVisibility(visible = period == "custom") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DividerLight, RoundedCornerShape(8.dp))
                        .clickable {
                            showAdminDatePicker(context, startDate ?: LocalDate.now().toString()) {
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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DividerLight, RoundedCornerShape(8.dp))
                        .clickable {
                            showAdminDatePicker(context, endDate ?: LocalDate.now().toString()) {
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

// ═══════════════════════════════════════════════════════
// TAB CONTENT: FINANCIAL
// ═══════════════════════════════════════════════════════
@Composable
fun FinancialTabContent(
    financialState: ApiResult<FinancialReportResponse>,
    transactions: List<FinancialTransaction>,
    onLoadMore: () -> Unit
) {
    if (financialState is ApiResult.Loading && transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenPrimary)
        }
        return
    }

    if (financialState is ApiResult.Error && transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(text = financialState.error, color = RedDanger, fontWeight = FontWeight.SemiBold)
        }
        return
    }

    val idrFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (financialState is ApiResult.Success) {
            val data = financialState.data

            // Metrics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Total Pendapatan", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = idrFormat.format(data.totalRevenue),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Selesai (Paid)", color = GrayText, fontSize = 12.sp)
                            Text("${data.totalSuccess} Booking", color = SlateTextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Canceled", color = GrayText, fontSize = 12.sp)
                            Text("${data.totalCanceled} Booking", color = RedDanger, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Revenue by Service Breakdown
            if (data.revenueByService.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Pendapatan Per Layanan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                            data.revenueByService.forEach { serviceRev ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(serviceRev.serviceName, color = SlateText, fontSize = 13.sp)
                                    Text(idrFormat.format(serviceRev.revenue), fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transactions list section
        item {
            Text("Daftar Transaksi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateTextDark)
        }

        if (transactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada data transaksi.", color = GrayText)
                }
            }
        } else {
            items(transactions.size) { index ->
                val tx = transactions[index]
                if (index == transactions.size - 1 && financialState !is ApiResult.Loading) {
                    LaunchedEffect(Unit) { onLoadMore() }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tx.bookingDate ?: "-", fontSize = 11.sp, color = GrayText)
                            Text(
                                text = tx.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.status.lowercase() == "canceled" || tx.status.lowercase() == "dibatalkan") RedDanger else GreenPrimary
                            )
                        }
                        Text(tx.patientName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                        Text(
                            text = "${tx.serviceName} • ${tx.therapistName} (${tx.paymentMethod ?: "-"})",
                            fontSize = 13.sp,
                            color = GrayText
                        )
                        Text(
                            text = idrFormat.format(tx.totalAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GreenPrimary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        if (financialState is ApiResult.Loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// TAB CONTENT: VISITS
// ═══════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitsTabContent(
    visitsState: ApiResult<VisitReportResponse>,
    visitsItems: List<VisitItem>,
    therapists: List<User>,
    selectedTherapistId: Int?,
    onTherapistSelected: (Int?) -> Unit,
    onLoadMore: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Therapist Filter Dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val selectedName = therapists.find { it.id == selectedTherapistId }?.name ?: "Semua Terapis"
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter Terapis") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Semua Terapis") },
                        onClick = {
                            onTherapistSelected(null)
                            expanded = false
                        }
                    )
                    therapists.forEach { therapist ->
                        DropdownMenuItem(
                            text = { Text(therapist.name) },
                            onClick = {
                                onTherapistSelected(therapist.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (visitsState is ApiResult.Success) {
                val sum = visitsState.data.summary

                // Summary stats
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Ringkasan Kunjungan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Kunjungan", color = GrayText, fontSize = 13.sp)
                                Text("${sum.totalVisits} orang", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Laki-laki / Perempuan", color = GrayText, fontSize = 13.sp)
                                Text("${sum.totalMale} L / ${sum.totalFemale} P", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Baru / Lama", color = GrayText, fontSize = 13.sp)
                                Text("${sum.totalNew} Baru / ${sum.totalOld} Lama", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bekam & Akupunktur (Keterampilan)", color = GrayText, fontSize = 13.sp)
                                Text("${sum.totalKeterampilan} sesi", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ramuan (Ramuan)", color = GrayText, fontSize = 13.sp)
                                Text("${sum.totalRamuan} sesi", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Visits Items list
            item {
                Text("Daftar Kunjungan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateTextDark)
            }

            if (visitsItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada data kunjungan.", color = GrayText)
                    }
                }
            } else {
                items(visitsItems.size) { index ->
                    val visit = visitsItems[index]
                    if (index == visitsItems.size - 1 && visitsState !is ApiResult.Loading) {
                        LaunchedEffect(Unit) { onLoadMore() }
                    }
                    VisitReportCard(visit)
                }
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
}

// ═══════════════════════════════════════════════════════
// TAB CONTENT: THERAPIST PERFORMANCE
// ═══════════════════════════════════════════════════════
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
            val list = performanceState.data.therapists
            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada data kinerja terapis.", color = GrayText)
                }
                return
            }

            val idrFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(item.therapistName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateTextDark)
                            Divider(color = DividerLight)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Sesi", color = GrayText, fontSize = 13.sp)
                                Text("${item.totalSessions} sesi", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Pasien (Baru/Lama)", color = GrayText, fontSize = 13.sp)
                                Text("${item.totalPatients} orang (${item.newPatients} B / ${item.oldPatients} L)", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Keterampilan (Bekam/Akupunktur)", color = GrayText, fontSize = 13.sp)
                                Text("${item.totalBekam} / ${item.totalAkupunktur} sesi", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ramuan", color = GrayText, fontSize = 13.sp)
                                Text("${item.totalRamuan} sesi", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pendapatan Terkumpul", color = GrayText, fontSize = 13.sp)
                                Text(idrFormat.format(item.totalRevenue), fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// TAB CONTENT: CLINIC ACTIVITY
// ═══════════════════════════════════════════════════════
@Composable
fun ActivityTabContent(activityState: ApiResult<ActivityReportResponse>) {
    when (activityState) {
        is ApiResult.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        }
        is ApiResult.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(text = activityState.error, color = RedDanger, fontWeight = FontWeight.SemiBold)
            }
        }
        is ApiResult.Success -> {
            val data = activityState.data
            val idrFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main stats card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Aktivitas Klinik", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Kunjungan", color = GrayText, fontSize = 13.sp)
                                Text("${data.summary.totalVisits} Kunjungan", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pasien Baru / Lama", color = GrayText, fontSize = 13.sp)
                                Text("${data.summary.newPatients} Baru / ${data.summary.oldPatients} Lama", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Pendapatan", color = GrayText, fontSize = 13.sp)
                                Text(idrFormat.format(data.summary.totalRevenue), fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Layanan Terlaris", color = GrayText, fontSize = 13.sp)
                                Text(data.summary.topService, fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Terapis Teraktif", color = GrayText, fontSize = 13.sp)
                                Text(data.summary.topTherapist, fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Services breakdown with visual progress bar
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Kontribusi Layanan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                            data.serviceBreakdown.forEach { service ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(service.serviceName, fontSize = 13.sp, color = SlateText)
                                        Text("${service.totalSessions} Sesi (${String.format("%.1f", service.percentage)}%)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateTextDark)
                                    }
                                    LinearProgressIndicator(
                                        progress = (service.percentage / 100.0).toFloat(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp),
                                        color = GreenPrimary,
                                        trackColor = Color(0xFFF1F5F9)
                                    )
                                    Text(idrFormat.format(service.revenue), color = GrayText, fontSize = 11.sp, modifier = Modifier.align(Alignment.End))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// TAB CONTENT: COMPARATIVE PERFORMANCE
// ═══════════════════════════════════════════════════════
@Composable
fun ComparativeTabContent(comparativeState: ApiResult<ComparativeReportResponse>) {
    when (comparativeState) {
        is ApiResult.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        }
        is ApiResult.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(text = comparativeState.error, color = RedDanger, fontWeight = FontWeight.SemiBold)
            }
        }
        is ApiResult.Success -> {
            val list = comparativeState.data.comparative
            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada data komparatif terapis.", color = GrayText)
                }
                return
            }

            val idrFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Peringkat Performa Terapis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateTextDark)
                }

                items(list) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Rank Badge
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        when (item.ranking) {
                                            1 -> Color(0xFFFDE047) // Gold
                                            2 -> Color(0xFFE2E8F0) // Silver
                                            3 -> Color(0xFFFED7AA) // Bronze
                                            else -> Color(0xFFF1F5F9)
                                        },
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${item.ranking}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = SlateTextDark
                                )
                            }

                            // Details
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.therapistName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                                    Text(
                                        text = item.trend,
                                        color = if (item.trend.contains("naik", ignoreCase = true)) GreenPrimary else RedDanger,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.totalSessions} Sesi • ${item.totalPatients} Pasien", fontSize = 13.sp, color = GrayText)
                                    Text(idrFormat.format(item.revenue), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                // Visual bar and percentage contribution
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    LinearProgressIndicator(
                                        progress = (item.percentage / 100.0).toFloat(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp),
                                        color = GreenPrimary,
                                        trackColor = Color(0xFFF1F5F9)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", item.percentage)}% kontribusi",
                                        fontSize = 11.sp,
                                        color = GrayText,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(90.dp)
                                    )
                                }
                            }
                        }
                    }
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

            Divider(color = DividerLight)

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
