package com.android.rumahsehatmannawasalwa.ui.screens.admin.bookings

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminBookingScreen(
    navController: NavController,
    viewModel: AdminBookingViewModel = viewModel()
) {
    // Pagers
    val upcomingItems = viewModel.upcomingPager.collectAsLazyPagingItems()
    val historyItems = viewModel.historyPager.collectAsLazyPagingItems()
    
    val operationStateState = viewModel.operationState.collectAsState()
    val searchQueryState = viewModel.searchQuery.collectAsState()
    val selectedTabState = viewModel.selectedTab.collectAsState()

    val operationState = operationStateState.value
    val searchQuery = searchQueryState.value
    val selectedTab = selectedTabState.value
    
    // Determine active list for display
    val activeItems = if (selectedTab == 0) upcomingItems else historyItems
    
    val context = LocalContext.current

    LaunchedEffect(operationState) {
        when (val result = operationState) {
            is ApiResult.Success -> {
                Toast.makeText(context, result.data, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                // Refresh both lists on change
                upcomingItems.refresh()
                historyItems.refresh()
            }
            is ApiResult.Error -> {
                Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_booking") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Appointment")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight) 
        ) {
            // 1. Top Bar & Search
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryInternalChanged(it) },
                    placeholder = { Text("Cari pasien/terapis...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 2. Segmented Filter (Tab Row)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { viewModel.onTabSelected(0) },
                        text = { Text("Akan Datang") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { viewModel.onTabSelected(1) },
                        text = { Text("Riwayat") }
                    )
                }
            }

            // 3. List Appointment
            if (activeItems.loadState.refresh is LoadState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = activeItems.itemCount,
                        key = { index -> activeItems[index]?.id ?: index }
                    ) { index ->
                        val booking = activeItems[index]
                        
                        if (booking != null) {
                            AppointmentCard(
                                booking = booking, 
                                onClick = { 
                                    navController.navigate(com.android.rumahsehatmannawasalwa.ui.navigation.AdminRoute.AppointmentDetail.createRoute(booking.id))
                                }
                            )
                        }
                    }

                    if (activeItems.itemCount == 0 && activeItems.loadState.refresh !is LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Belum ada janji temu", color = Color.Gray)
                                }
                            }
                        }
                    }
                    
                    if (activeItems.loadState.append is LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(booking: ApiBooking, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booking.service?.nama ?: "Layanan Dihapus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Time & Date Consolidated
                // Format: "19 Des, 09.00 - 10.00"
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                     Spacer(modifier = Modifier.width(4.dp))
                     Text(
                        text = formatAppointmentDate(booking.bookingDate, booking.bookingTime, booking.service?.durasi ?: 60),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                     Spacer(modifier = Modifier.width(4.dp))
                     Text(
                        text = "${booking.therapist?.name ?: "-"} (Terapis)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                     Spacer(modifier = Modifier.width(4.dp))
                     Text(
                        text = "${booking.patient?.name ?: "-"} (Pasien)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }

            // Right Column
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(status = booking.status)
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(onClick = onClick) {
                     Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Detail",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}


// Helper
fun formatAppointmentDate(dateStr: String, timeStr: String, durationMinutes: Int): String {
    return try {
        // 1. FIX: Bersihkan string tanggal. Ambil cuma "2025-12-19"
        val cleanDateStr = dateStr.substringBefore("T")
        val date = LocalDate.parse(cleanDateStr)

        // Format Tanggal (Contoh: 19 Des)
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("id", "ID"))
        val formattedDate = date.format(dateFormatter)

        // 2. Parse Waktu
        // Cara lebih bersih parsing waktu menggunakan LocalTime.parse
        // Handle format "09:00:00" atau "09:00" otomatis
        val startTime = try {
            java.time.LocalTime.parse(timeStr)
        } catch (e: Exception) {
            // Fallback manual jika format aneh (seperti logika kamu sebelumnya)
            val cleanTime = timeStr.substringBeforeLast(":") // Hapus detik jika ada double colon
            val parts = cleanTime.split(":")
            java.time.LocalTime.of(parts[0].toInt(), parts[1].toInt())
        }

        // Hitung End Time
        val endTime = startTime.plusMinutes(durationMinutes.toLong())

        // Format Waktu (Contoh: 09.00 - 10.00)
        val timeFormatter = DateTimeFormatter.ofPattern("HH.mm", Locale("id", "ID"))
        val startFormatted = startTime.format(timeFormatter)
        val endFormatted = endTime.format(timeFormatter)

        "$formattedDate, $startFormatted - $endFormatted"

    } catch (e: Exception) {
        android.util.Log.e("AppointmentFormat", "Error formatting: ${e.message}", e)
        // Fallback return raw strings biar aplikasi gak crash/kosong
        val simpleDate = dateStr.take(10) // Ambil 10 huruf pertama aja (YYYY-MM-DD)
        val simpleTime = timeStr.take(5)  // Ambil 5 huruf pertama (HH:mm)
        "$simpleDate, $simpleTime"
    }
}

@Composable
fun StatusChip(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "pending", "menunggu" -> StatusPending to Color.Black
        "confirmed", "konfirmasi" -> StatusConfirmed to Color.White
        "completed", "selesai" -> StatusCompleted to Color.White
        "cancelled", "canceled", "batal" -> StatusCancelled to Color.White
        else -> Color.Gray to Color.White
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = Modifier.height(24.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
