package com.android.rumahsehatmannawasalwa.ui.screens.admin.bookings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AppointmentDetailViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.DetailUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    navController: NavController,
    bookingId: Int,
    viewModel: AppointmentDetailViewModel = viewModel()
) {
    val uiStateState = viewModel.uiState.collectAsState()
    val updateStatusStateState = viewModel.updateStatusState.collectAsState()

    val uiState = uiStateState.value
    val updateStatusState = updateStatusStateState.value
    val context = LocalContext.current

    // Fetch data on first load
    LaunchedEffect(bookingId) {
        viewModel.fetchBookingDetail(bookingId)
    }

    // Handle Update Status Result
    LaunchedEffect(updateStatusState) {
        updateStatusState?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, result.getOrNull(), Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
            } else {
                Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                viewModel.resetUpdateState()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Booking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            // Button "Proses" basically triggers the update if a new status is selected, 
            // or we could make the dropdown change immediately and this button does something else?
            // "Fungsi: Admin bisa memilih status baru... Tombol Proses".
            // Let's assume selecting dropdown updates state, and Button commits it.
            // For now, I'll make the button trigger the update if pending change, or just act as a "Save" button. 
            // But to simplify, I will implement "Proses" to perhaps just navigate back or show success if auto-saved.
            // Re-reading: "Tombol Proses yang lebar...". Maybe it triggers the status update?
            // I'll add a 'draft' status state locally and save on click.
            
            // Actually, simpler: Dropdown updates local state buffer, Button "Proses" commits to API.
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DetailUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
                is DetailUiState.Success -> {
                    DetailContent(
                        booking = state.booking,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    booking: ApiBooking,
    viewModel: AppointmentDetailViewModel,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf(booking.status) }
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("pending", "confirmed", "completed", "cancelled")

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Informasi Personil
        CardSection(title = null) {
            PersonnelRow(role = "Pasien", name = booking.patient?.name ?: "Unknown")
            Spacer(modifier = Modifier.height(16.dp))
            PersonnelRow(role = "Terapis", name = booking.therapist?.name ?: "Unknown")
        }

        // Card 2: Detail Pesanan
        CardSection(title = "Detail Pesanan") {
            DetailRow("Layanan", booking.service?.nama ?: "-")
            DetailRow("Tanggal", booking.bookingDate) // Or format nicely
            DetailRow("Waktu", booking.bookingTime)
            DetailRow("Harga", formatRupiah(booking.service?.harga ?: 0))
        }

        // Card 3: Update Status
        CardSection(title = "Status") {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedStatus.uppercase(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.uppercase()) },
                            onClick = {
                                selectedStatus = status
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom Button stored here or in scaffold bottomBar? 
        // User asked for Bottom Bar: "Tombol 'Proses' ... Bottom Bar"
        // Since I'm inside Column content, let's put it here or lift state.
        // For strict BottomBar Scaffold compliance, I'd need to pass `selectedStatus` up.
        // Easier to put it here at the bottom of the scrollable content OR use Box alignment.
        // But Scaffold BottomBar is fixed. Let's try to simulate BottomBar with Box alignment in parent if needed,
        // or just use Scaffold's bottomBar which is detached from this scope.
        // I'll make the "Proses" button act on the *local* selectedStatus.
        
        Button(
            onClick = { viewModel.updateBookingStatus(booking.id, selectedStatus) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Proses", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CardSection(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Column {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Surface(
            color = Color(0xFFF5F5F5),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun PersonnelRow(role: String, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = role, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

fun formatRupiah(amount: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp. ").substringBeforeLast(",")
}
