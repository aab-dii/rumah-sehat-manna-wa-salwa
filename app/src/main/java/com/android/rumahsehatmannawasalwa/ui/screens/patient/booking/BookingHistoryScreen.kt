package com.android.rumahsehatmannawasalwa.ui.screens.patient.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking
import com.android.rumahsehatmannawasalwa.ui.components.BottomNavigationBar
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.BookingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- Color Palette ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingScreen(
    navController: NavController,
    viewModel: BookingViewModel
) {
    // Fetch data on open
    LaunchedEffect(Unit) {
        viewModel.fetchUserBookings()
    }

    val upcomingListState = viewModel.upcomingList.collectAsState()
    val historyListState = viewModel.historyList.collectAsState()
    val isLoadingState = viewModel.isLoading.collectAsState()

    val upcomingList = upcomingListState.value
    val historyList = historyListState.value
    val isLoading = isLoadingState.value

    // Tab State: 0 = Akan Datang, 1 = Riwayat
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Akan Datang", "Riwayat")

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            Column(modifier = Modifier.background(BackgroundWhite)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Booking Saya",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BackgroundWhite
                    )
                )
                
                // Custom Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = BackgroundWhite,
                    contentColor = GreenPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = GreenPrimary,
                            height = 3.dp
                        )
                    },
                    divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenPrimary else Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundWhite)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else {
                val currentList = if (selectedTabIndex == 0) upcomingList else historyList

                if (currentList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Belum ada janji temu",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentList) { booking ->
                            BookingItemCard(
                                booking = booking,
                                onDetailClick = { bookingId ->
                                    navController.navigate("booking_detail/$bookingId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(
    booking: ApiBooking,
    onDetailClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 1. Header Informasi
            Text(
                text = booking.service?.nama ?: "Layanan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            val therapistName = booking.therapist?.name ?: "Terapis Belum Ditentukan"
            Text(
                text = "Terapis: $therapistName",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Waktu dengan Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Format Waktu Range
                val timeDisplay = try {
                    val cleanTime = if (booking.bookingTime.length >= 5) booking.bookingTime.substring(0, 5) else booking.bookingTime
                    val startTime = java.time.LocalTime.parse(cleanTime)
                    val endTime = startTime.plusMinutes(60) // Estimasi 60 jam
                    "${startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH.mm"))} - ${endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH.mm"))} WITA"
                } catch (e: Exception) {
                    "${booking.bookingTime} WITA"
                }

                // Format Tanggal
                val dateDisplay = try {
                    // Handle ISO format like "2026-01-01T00:00:00.000000Z"
                    val isoDate = if (booking.bookingDate.contains("T")) {
                         booking.bookingDate.split("T")[0]
                    } else {
                         booking.bookingDate
                    }
                    val dateObj = LocalDate.parse(isoDate)
                    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))
                    dateObj.format(formatter)
                } catch (e: Exception) {
                    booking.bookingDate
                }
                
                Column {
                     Text(
                        text = timeDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // 2. Divider
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 3. Tombol Aksi
            Button(
                onClick = { onDetailClick(booking.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Lihat Detail",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
