package com.android.rumahsehatmannawasalwa.ui.screens.admin.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.schedule.Schedule
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.viewmodel.schedule.ScheduleViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTherapistScheduleScreen(
    navController: NavController,
    therapistId: Int,
    scheduleViewModel: ScheduleViewModel = viewModel(),
    userViewModel: AdminUserViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Fetch Data
    LaunchedEffect(therapistId) {
        scheduleViewModel.fetchSchedules(therapistId)
        userViewModel.fetchUserDetail(therapistId)
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
    val calendar = Calendar.getInstance()
    // Simple Indonesian Date Formatter
    val dayName = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Minggu"
        Calendar.MONDAY -> "Senin"
        Calendar.TUESDAY -> "Selasa"
        Calendar.WEDNESDAY -> "Rabu"
        Calendar.THURSDAY -> "Kamis"
        Calendar.FRIDAY -> "Jumat"
        Calendar.SATURDAY -> "Sabtu"
        else -> ""
    }
    val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val dateString = "${calendar.get(Calendar.DAY_OF_MONTH)} ${months[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.YEAR)}"
    val currentDateParam = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

    // State for Add Holiday Dialog
    var showAddHolidayDialog by remember { mutableStateOf(false) }
    var holidayStartDate by remember { mutableStateOf("") }
    var holidayEndDate by remember { mutableStateOf("") }
    var holidayReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Atur Jadwal Terapis") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHolidayDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal Libur")
            }
        }
    ) { paddingValues ->
        if (schedulesState is ApiResult.Loading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- HEADER: Current Date & Emergency Close ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.5f)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                                Text(
                                    text = dateString,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            
                            Button(
                                onClick = { showEmergencySheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), // Keep Red for Danger Action
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            ) {
                                Text("Tutup Sekarang")
                            }
                        }
                    }
                }

                // --- JADWAL RUTIN MINGGUAN ---
                Text(
                    text = "Jadwal Rutin Mingguan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    days.forEach { dName ->
                        val existing = schedules.find { it.day.equals(dName, ignoreCase = true) }
                        ScheduleCard(
                            dayName = dName,
                            initialIsActive = existing?.isActive ?: false,
                            initialStartTime = existing?.startTime?.take(5) ?: "09:00",
                            initialEndTime = existing?.endTime?.take(5) ?: "21:00",
                            onSave = { isActive, start, end ->
                                scheduleViewModel.updateSchedule(
                                    therapistId = therapistId,
                                    day = dName,
                                    startTime = start,
                                    endTime = end,
                                    isActive = isActive
                                )
                            }
                        )
                    }
                }

                // --- JADWAL LIBUR (Holiday) ---
                Text(
                    text = "Jadwal Libur",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                // Filter holidays (where type == 'holiday' or note is set, or specific_date set)
                val holidays = schedules.filter { it.type == "holiday" || (it.specificDate != null && it.isActive == false) }
                
                if (holidays.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Jadwal libur kosong", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    holidays.forEach { holiday ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Helper function to format date
                                    fun formatDate(dateString: String?): String {
                                        if (dateString.isNullOrEmpty()) return "-"
                                        return try {
                                            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            val date = inputFormat.parse(dateString)
                                            val outputFormat = java.text.SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
                                            outputFormat.format(date!!)
                                        } catch (e: Exception) {
                                            dateString
                                        }
                                    }

                                    val dateText = if (holiday.endDate != null && holiday.endDate != holiday.specificDate) {
                                        "${formatDate(holiday.specificDate)}\ns/d\n${formatDate(holiday.endDate)}"
                                    } else {
                                        formatDate(holiday.specificDate)
                                    }
                                    
                                    Text(dateText, fontWeight = FontWeight.Bold)
                                    Text("Libur", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                                if (!holiday.note.isNullOrEmpty()) {
                                    Text(holiday.note, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
                
                // Extra space for FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // --- EMERGENCY CLOSE BOTTOM SHEET ---
        if (showEmergencySheet) {
            ModalBottomSheet(
                onDismissRequest = { showEmergencySheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 24.dp), // Extra bottom padding
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Penutupan Darurat",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aksi ini akan membatalkan SEMUA booking aktif pada hari ini ($dateString). Pelanggan akan diberitahu.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = cancellationReason,
                        onValueChange = { cancellationReason = it },
                        label = { Text("Alasan Penutupan") },
                        placeholder = { Text("Contoh: Terapis Sakit, Keadaan Darurat") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (cancellationReason.isBlank()) {
                                android.widget.Toast.makeText(context, "Mohon isi alasan", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                scheduleViewModel.emergencyClose(therapistId, currentDateParam, cancellationReason) {
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            showEmergencySheet = false
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Konfirmasi Tutup & Batalkan Booking")
                    }
                }
            }
        }
        
        // --- ADD HOLIDAY DIALOG ---
        if (showAddHolidayDialog) {
            AlertDialog(
                onDismissRequest = { showAddHolidayDialog = false },
                title = { Text("Tambah Jadwal Libur") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Start Date
                        DatePickerField(label = "Tanggal Mulai", date = holidayStartDate) { holidayStartDate = it }
                        // End Date
                        DatePickerField(label = "Tanggal Selesai", date = holidayEndDate) { holidayEndDate = it }
                        // Reason
                        OutlinedTextField(
                            value = holidayReason,
                            onValueChange = { holidayReason = it },
                            label = { Text("Alasan") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (holidayStartDate.isBlank() || holidayEndDate.isBlank() || holidayReason.isBlank()) {
                                android.widget.Toast.makeText(context, "Mohon lengkapi data", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                scheduleViewModel.addHoliday(therapistId, holidayStartDate, holidayEndDate, holidayReason) {
                                    showAddHolidayDialog = false
                                    // Reset fields
                                    holidayStartDate = ""
                                    holidayEndDate = ""
                                    holidayReason = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddHolidayDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun DatePickerField(label: String, date: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formatted)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedTextField(
        value = date,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
            }
        },
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                            datePickerDialog.show()
                        }
                    }
                }
            }
    )
}

@Composable
fun ScheduleCard(
    dayName: String,
    initialIsActive: Boolean,
    initialStartTime: String,
    initialEndTime: String,
    onSave: (Boolean, String, String) -> Unit
) {
    var isActive by remember(initialIsActive) { mutableStateOf(initialIsActive) }
    var startTime by remember(initialStartTime) { mutableStateOf(initialStartTime) }
    var endTime by remember(initialEndTime) { mutableStateOf(initialEndTime) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Day Name & Toggle (Steker)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayName, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GreenPrimary, 
                        checkedTrackColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }

            // Row 2: Inputs & Save Button (Only if Active)
            if (isActive) {
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Time Inputs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        CompactTimePicker(time = startTime, onTimeSelected = { startTime = it })
                        Text(
                            text = " - ", 
                            style = MaterialTheme.typography.bodyMedium, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        CompactTimePicker(time = endTime, onTimeSelected = { endTime = it })
                    }

                    // Save Button
                    IconButton(
                        onClick = { onSave(isActive, startTime, endTime) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = GreenPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Simpan")
                    }
                }
            } else {
                Text(
                    text = "Libur",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CompactTimePicker(time: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    try {
        val parts = time.split(":")
        calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
        calendar.set(Calendar.MINUTE, parts[1].toInt())
    } catch (e: Exception) {}

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            onTimeSelected(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    TextButton(
        onClick = { timePickerDialog.show() },
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(
            text = time, 
            style = MaterialTheme.typography.bodyMedium, 
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}
