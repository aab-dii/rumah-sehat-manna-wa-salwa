package com.android.rumahsehatmannawasalwa.ui.screens.patient.booking

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.BookingViewModel
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.components.HorizontalCalendar
import com.android.rumahsehatmannawasalwa.ui.components.TimeSlotGrid
import com.android.rumahsehatmannawasalwa.ui.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController,
    serviceId: String,
    serviceName: String,
    servicePrice: Int,
    viewModel: BookingViewModel = viewModel()
) {
    val context = LocalContext.current
    val bookingStateState = viewModel.bookingState.collectAsState()
    val bookingState = bookingStateState.value
    
    // Observed States
    val therapistsState = viewModel.filteredTherapists.collectAsState()
    val availableSlotsState = viewModel.availableTimeSlots.collectAsState()
    val selectedDateState = viewModel.selectedDate.collectAsState()
    val isLoadingSlotsState = viewModel.loadingSlots.collectAsState()

    val therapists = therapistsState.value
    val availableSlots = availableSlotsState.value
    val selectedDate = selectedDateState.value
    val isLoadingSlots = isLoadingSlotsState.value

    // Shared Selection State
    val selectedTherapistState = viewModel.selectedTherapist.collectAsState()
    val selectedTimeSlotState = viewModel.selectedTimeSlot.collectAsState()

    val selectedTherapist = selectedTherapistState.value
    val selectedTimeSlot = selectedTimeSlotState.value

    // Initial Fetch & Setup
    LaunchedEffect(Unit) {
        viewModel.setServiceDetails(serviceId, serviceName, servicePrice)
        viewModel.fetchTherapists(serviceName)
    }

    // Fetch Slots when Date or Therapist changes
    LaunchedEffect(selectedDate, selectedTherapist) {
        if (selectedTherapist != null) {
            viewModel.fetchTimeSlots(selectedTherapist!!.id, serviceId.toIntOrNull() ?: 0)
        }
    }

    // --- Colors ---


    // --- Effects ---
    // (Booking result handling moved to SummaryScreen)
    
    val isLoadingTherapists by viewModel.isLoadingTherapists.collectAsState()

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Pesan Layanan", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        bottomBar = {
            if (!isLoadingTherapists) {
                Surface(
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = {
                            if (selectedTherapist != null && selectedTimeSlot != null) {
                                // Navigate to Summary
                                navController.navigate("booking_summary")
                            }
                        },
                        enabled = selectedTherapist != null && selectedTimeSlot != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary
                        )
                    ) {
                        Text("Lanjutkan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    ) { padding ->
        if (isLoadingTherapists) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GreenPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // 1. Kartu Info Layanan
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(serviceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Durasi: Estimasi 60 Menit", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text("Rp $servicePrice", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = GreenPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Pilih Terapis (Dynamic List)
                Text("Pilih Terapis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                if (therapists.isEmpty()) {
                    Text("Tidak ada terapis tersedia untuk layanan ini.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(therapists) { therapist ->
                            val isSelected = selectedTherapist?.id == therapist.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { viewModel.selectTherapist(therapist) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) GreenPrimary else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Placeholder Image - Later replace with AsyncImage if URL available
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    therapist.name, 
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenPrimary else Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Pilih Tanggal (Using HorizontalCalendar)
                Text("Pilih Tanggal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                HorizontalCalendar(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.onDateSelected(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Pilih Waktu (Using TimeSlotGrid)
                Text("Waktu Tersedia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoadingSlots) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else if (selectedTherapist == null) {
                    Text("Silakan pilih terapis terlebih dahulu.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    TimeSlotGrid(
                        slots = availableSlots,
                        selectedSlot = selectedTimeSlot,
                        onSlotSelected = { viewModel.selectTimeSlot(it) }
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Padding bawah ekstra untuk scroll
            }
        }
    }
}