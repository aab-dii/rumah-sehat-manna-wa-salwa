package com.android.rumahsehatmannawasalwa.ui.screens.patient.booking

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.BookingViewModel
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- Color Palette (Konsisten) ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    navController: NavController,
    bookingId: Int,
    viewModel: BookingViewModel
) {
    val context = LocalContext.current
    val bookingDetailState = viewModel.bookingDetail.collectAsState()
    val isLoadingState = viewModel.isLoading.collectAsState()
    val bookingStateState = viewModel.bookingState.collectAsState()

    val bookingDetail = bookingDetailState.value
    val isLoading = isLoadingState.value
    val bookingState = bookingStateState.value
    var showCancelDialog by remember { mutableStateOf(false) }

    // Fetch detail on load
    LaunchedEffect(bookingId) {
        viewModel.getBookingDetail(bookingId)
    }

    // Handle Cancellation Error/Success
    LaunchedEffect(bookingState) {
        if (bookingState is ApiResult.Error) {
            Toast.makeText(context, (bookingState as ApiResult.Error).error, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rincian Booking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundWhite)
            )
        },
        bottomBar = {
            // Tombol Batalkan hanya jika status PENDING / MENUNGGU / TERJADWAL
            val status = bookingDetail?.status?.uppercase() ?: ""
            // Pastikan tidak loading dan data ada
            if (!isLoading && bookingDetail != null && (status == "PENDING" || status == "MENUNGGU" || status == "TERJADWAL" || status == "CONFIRMED" || status == "KONFIRMASI")) {
                Surface(
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                // 1. Validasi Waktu
                                try {
                                   val isoDate = if (bookingDetail!!.bookingDate.contains("T")) {
                                        bookingDetail!!.bookingDate.split("T")[0]
                                   } else {
                                        bookingDetail!!.bookingDate
                                   }
                                   val timePart = if (bookingDetail!!.bookingTime.length >= 5) bookingDetail!!.bookingTime.substring(0, 5) else "00:00"
                                   
                                   val bookingDateTime = java.time.LocalDateTime.parse("${isoDate}T${timePart}")
                                   val now = java.time.LocalDateTime.now()
                                   
                                   // Cek apakah sekarang masih sebelum (Jadwal - 30 menit)
                                   val limit = bookingDateTime.minusMinutes(30)
                                   
                                   if (now.isBefore(limit)) {
                                       showCancelDialog = true
                                   } else {
                                       Toast.makeText(context, "Pembatalan hanya bisa dilakukan minimal 30 menit sebelum jadwal.", Toast.LENGTH_LONG).show()
                                   }
                                } catch (e: Exception) {
                                    // Fallback jika parsing gagal (aman)
                                    showCancelDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            enabled = bookingState !is ApiResult.Loading
                        ) {
                             if (bookingState is ApiResult.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Batalkan Pesanan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        
        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text(text = "Batalkan Booking?") },
                text = { Text("Apakah Anda yakin ingin membatalkan booking ini? Tindakan ini tidak dapat dibatalkan.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCancelDialog = false
                            viewModel.cancelBooking(bookingId) {
                                Toast.makeText(context, "Booking berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Text("Ya, Batalkan", color = StatusCancelled)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Tidak", color = Color.Gray)
                    }
                },
                containerColor = Color.White,
                titleContentColor = Color.Black,
                textContentColor = Color.Black
            )
        }
        if (isLoading || bookingDetail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            val booking = bookingDetail!!
            
            // Debug Log
            LaunchedEffect(booking) {
                android.util.Log.d("BookingDetail", "RAW Date: '${booking.bookingDate}'")
                android.util.Log.d("BookingDetail", "RAW Time: '${booking.bookingTime}'")
            }

            val rawStatus = booking.status.uppercase()
            val displayStatus = when (rawStatus) {
                "PENDING", "MENUNGGU" -> "MENUNGGU"
                "CONFIRMED", "KONFIRMASI", "TERJADWAL" -> "TERJADWAL"
                "COMPLETED", "SELESAI" -> "SELESAI"
                "CANCELLED", "BATAL" -> "DIBATALKAN"
                else -> rawStatus
            }
            
            val statusColor = when (displayStatus) {
                "MENUNGGU" -> StatusPending
                "TERJADWAL" -> GreenPrimary
                "SELESAI" -> Color.Blue
                "DIBATALKAN" -> StatusCancelled
                else -> Color.Gray
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Kartu Status
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Status Booking Sekarang", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        SuggestionChip(
                            onClick = {},
                            label = { 
                                Text(
                                    displayStatus, 
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                ) 
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = statusColor.copy(alpha = 0.1f),
                                labelColor = statusColor
                            ),
                            border = BorderStroke(1.dp, statusColor)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kode Booking: #BK-${booking.id}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }

                // 2. Kartu Informasi Layanan
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Gambar Layanan
                        AsyncImage(
                            model = booking.service?.imageUrl ?: "https://placehold.co/150",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                booking.service?.nama ?: "Layanan", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Format Waktu Range
                            val timeDisplay = try {
                                val cleanTime = if (booking.bookingTime.length >= 5) booking.bookingTime.substring(0, 5) else booking.bookingTime
                                val startTime = java.time.LocalTime.parse(cleanTime)
                                val endTime = startTime.plusMinutes(60) // Estimasi 60 menit
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
                                booking.bookingDate // Fallback to raw if all else fails
                            }

                            Text(timeDisplay, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(dateDisplay, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }

                // 3. Kartu Terapis
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                booking.therapist?.name ?: "Belum Ditentukan", // Using safe call
                                fontWeight = FontWeight.Bold, 
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text("Terapis", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }

                // 4. Section Pembayaran
                Text("Rincian Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Harga Layanan
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Harga Layanan", color = Color.Gray)
                            Text("Rp ${booking.totalPrice}", fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Biaya Admin (Dummy 0 for now)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Biaya Admin", color = Color.Gray)
                            Text("Rp 0", fontWeight = FontWeight.Medium)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray, thickness = 1.dp) // Menggunakan HorizontalDivider
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Total
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL", fontWeight = FontWeight.Bold)
                            Text("Rp ${booking.totalPrice}", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 18.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom bar
            }
        }
    }
}
