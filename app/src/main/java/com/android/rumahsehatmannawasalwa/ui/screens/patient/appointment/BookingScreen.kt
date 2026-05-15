package com.android.rumahsehatmannawasalwa.ui.screens.patient.appointment

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.ui.components.HorizontalCalendar
import com.android.rumahsehatmannawasalwa.ui.components.TimeSlotGrid
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.BookingViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils.formatRupiah
import java.time.LocalDate

@Composable
fun BookingScreen(
    navController: NavController,
    serviceId: String, serviceName: String, servicePrice: Int,
    viewModel: BookingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val paymentOption by viewModel.selectedPaymentOption.collectAsState()
    val isLoadingTherapists by viewModel.isLoadingTherapists.collectAsState()
    val isLoadingService by viewModel.isLoadingService.collectAsState()
    
    val scrollState = rememberScrollState()

    val isInitialLoading = isLoadingTherapists || isLoadingService

    LaunchedEffect(Unit) {
        viewModel.setServiceDetails(serviceId, serviceName, servicePrice)
        viewModel.fetchTherapists(serviceName)
    }

    if (isInitialLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GreenPrimary)
        }
    } else {
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
        ) {
            // ── FIXED TOP BAR (Di luar area scroll) ──────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.statusBarsPadding()) {
                TopBar(
                    title = "Buat Janji Temu",
                    onBackClick = {
                        navController.popBackStack()
                        viewModel.resetState()
                    },
                    transparentBackground = true,
                    hideBackground = true,
                )
            }

            // ── SCROLLABLE AREA + BOTTOM BAR ─────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Jarak kecil dari TopBar agar tidak terlalu mepet
                    Spacer(modifier = Modifier.height(25.dp))

                    MannaSheet(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 600.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp)
                        ) {
                            // SECTION 1: LAYANAN
                            BookingSectionTitle("Informasi Layanan")
                            ServiceInfoCard(
                                name = uiState.selectedService?.name ?: serviceName,
                                price = uiState.selectedService?.price ?: servicePrice,
                                duration = uiState.selectedService?.duration ?: 60,
                                imageUrl = uiState.selectedService?.imageUrl
                            )

                            Spacer(Modifier.height(24.dp))

                            // SECTION 2: TERAPIS
                            BookingSectionTitle("Pilih Terapis Spesialis")
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                verticalAlignment = Alignment.Top,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                items(uiState.filteredTherapists) { therapist ->
                                    val fullPhotoUrl = FormatterUtils.getFullImageUrl(therapist.profilePhotoPath)
                                    SquareTherapistCard(
                                        name = therapist.name,
                                        photoUrl = fullPhotoUrl,
                                        isSelected = uiState.selectedTherapist?.id == therapist.id,
                                        onClick = { viewModel.onTherapistSelected(therapist) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // SECTION 3: WAKTU
                            BookingSectionTitle("Waktu Kunjungan")
                            HorizontalCalendar(
                                selectedDate = uiState.selectedDate,
                                onDateSelected = { viewModel.onDateSelected(it) },
                                activeDays = uiState.activeDays,
                                holidayInfo = uiState.holidayInfo,
                                availabilityMap = uiState.availabilityMap,
                                enabled = uiState.selectedTherapist != null && uiState.activeDays?.isNotEmpty() == true
                            )

                            Spacer(Modifier.height(16.dp))

                            if (uiState.selectedTherapist != null) {
                                TimeSlotGrid(
                                    slots = uiState.availableTimeSlots,
                                    selectedSlot = uiState.selectedTimeSlot,
                                    isLoading = uiState.isLoadingSlots,
                                    onSlotSelected = { viewModel.onTimeSlotSelected(it) }
                                )
                            }

                            Spacer(Modifier.height(24.dp))

                            // SECTION 4: PEMBAYARAN
                            BookingSectionTitle("Metode Pembayaran")
                            PaymentMethodCard(
                                selectedOption = paymentOption,
                                onOptionSelected = { viewModel.onPaymentOptionSelected(it) }
                            )

                            // Extra spacer agar konten tidak tertutup Floating Bottom Bar
                            Spacer(Modifier.height(120.dp))
                        }
                    }
                }

                // ── FIXED BOTTOM BAR (Floating di bawah) ───────────────────
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    MannaButton(
                        text = "Lanjutkan ke Ringkasan",
                        enabled = uiState.selectedTherapist != null && uiState.selectedTimeSlot != null,
                        onClick = { navController.navigate(Screen.BookingSummary.route) }
                    )
                }
            }
        }
    }
}

// getFullImageUrl helper dipindahkan ke FormatterUtils

@Composable
fun BookingSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = SlateTextDark,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun ServiceInfoCard(name: String, price: Int, duration: Int, imageUrl: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), // Sedikit lebih lega
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenSoft),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = FormatterUtils.getFullImageUrl(imageUrl),
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Spa, null, tint = GreenPrimary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextDark
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$duration Menit • Rp. ${formatRupiah(price)}",
                    fontSize = 14.sp,
                    color = BodyGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SquareTherapistCard(name: String, photoUrl: String?, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(140.dp)
            .width(107.dp) 
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, if (isSelected) GreenPrimary else DividerLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (photoUrl.isNullOrBlank()) GreenPrimary.copy(alpha = 0.1f) else SurfaceGrey),
                contentAlignment = Alignment.Center
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback Initials
                    val initials = name.split(" ").filter { it.isNotEmpty() }
                        .let { 
                            if (it.size >= 2) "${it[0][0]}${it[1][0]}" 
                            else if (it.isNotEmpty()) "${it[0][0]}" 
                            else "?"
                        }.uppercase()
                    
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }
            
            Spacer(Modifier.height(10.dp))


            Text(
                text = name.split(" ").take(2).joinToString(" "),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GreenPrimary else SlateText,
                textAlign = TextAlign.Center,
                maxLines = 2,
                softWrap = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PaymentMethodCard(selectedOption: String, onOptionSelected: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            PaymentOptionRow(
                title = "Tunai (Bayar di Tempat)",
                isSelected = selectedOption == "cash",
                onClick = { onOptionSelected("cash") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerLight, thickness = 0.5.dp)
            PaymentOptionRow(
                title = "Transfer Bank",
                isSelected = selectedOption == "transfer",
                onClick = { onOptionSelected("transfer") }
            )
        }
    }
}

@Composable
fun PaymentOptionRow(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = SlateText
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
        )
    }
}