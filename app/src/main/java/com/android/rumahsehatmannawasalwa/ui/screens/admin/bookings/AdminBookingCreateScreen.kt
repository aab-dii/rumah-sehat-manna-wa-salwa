package com.android.rumahsehatmannawasalwa.ui.screens.admin.bookings

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.ui.components.HorizontalCalendar
import com.android.rumahsehatmannawasalwa.ui.components.SearchableDropdown
import com.android.rumahsehatmannawasalwa.ui.components.TimeSlotGrid
import com.android.rumahsehatmannawasalwa.ui.viewmodel.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.LayananViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingCreateScreen(
    navController: NavController,
    bookingViewModel: AdminBookingViewModel = viewModel(),
    userViewModel: AdminUserViewModel = viewModel(),
    serviceViewModel: LayananViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiStateState = bookingViewModel.uiState.collectAsState()
    
    // External Data Sources
    val patientListState = userViewModel.patientList.collectAsState()
    val therapistListState = userViewModel.therapistList.collectAsState()
    val serviceListState = serviceViewModel.serviceList.collectAsState()

    val uiState = uiStateState.value
    val patientList = patientListState.value
    val therapistList = therapistListState.value
    val serviceList = serviceListState.value

    // Sync Data to ViewModel
    LaunchedEffect(serviceList) {
        bookingViewModel.setServices(serviceList)
    }
    LaunchedEffect(therapistList) {
        bookingViewModel.setTherapists(therapistList)
    }
    
    // Initial Fetch
    LaunchedEffect(Unit) {
        userViewModel.fetchUserList("pasien")
        userViewModel.fetchUserList("terapis")
        serviceViewModel.fetchServiceList()
    }

    // Handle Success/Error
    LaunchedEffect(uiState.isBookingSuccess) {
        if (uiState.isBookingSuccess) {
            Toast.makeText(context, "Booking berhasil dibuat!", Toast.LENGTH_SHORT).show()
            bookingViewModel.resetState()
            navController.popBackStack()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
             Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Booking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { bookingViewModel.createBooking() },
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), 
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Buat Booking")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Section 1: Pilih Pasien
            Text("Pilih Pasien", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SearchableDropdown(
                label = "Cari Pasien...",
                items = patientList,
                selectedItem = uiState.selectedPatient,
                onItemSelected = { bookingViewModel.onPatientSelected(it) },
                itemToString = { it.name },
                placeholder = "Ketik nama pasien..."
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Detail Layanan
            Text("Detail Layanan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SearchableDropdown(
                label = "Pilih Layanan",
                items = serviceList,
                selectedItem = uiState.selectedService,
                onItemSelected = { bookingViewModel.onServiceSelected(it) },
                itemToString = { "${it.nama} (Rp ${it.harga})" },
                placeholder = "Pilih Layanan"
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            SearchableDropdown(
                label = "Pilih Terapis",
                items = uiState.filteredTherapists,
                selectedItem = uiState.selectedTherapist,
                onItemSelected = { bookingViewModel.onTherapistSelected(it) },
                itemToString = { it.name },
                placeholder = if (uiState.selectedService == null) "Pilih layanan dulu" else "Pilih Terapis"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Jadwal (Calendar)
            Text("Jadwal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalCalendar(
                selectedDate = uiState.selectedDate,
                onDateSelected = { bookingViewModel.onDateSelected(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: Waktu Tersedia
            Text("Waktu Tersedia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.isLoadingSlots) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                TimeSlotGrid(
                    slots = uiState.availableTimeSlots,
                    selectedSlot = uiState.selectedTimeSlot,
                    onSlotSelected = { bookingViewModel.onTimeSlotSelected(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom bar
        }
    }
}
