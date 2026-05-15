package com.android.rumahsehatmannawasalwa.ui.screens.admin.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.data.mapper.BookingMapper
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingListItem
import com.android.rumahsehatmannawasalwa.ui.components.StatusChip
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils.formatDateHuman

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVerificationScreen(
    navController: NavController,
    viewModel: AdminBookingViewModel = viewModel()
) {
    val context = LocalContext.current

    val paymentVerificationItems = viewModel.upcomingPager.collectAsLazyPagingItems()
    val scheduleConfirmationItems = viewModel.historyPager.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val activeItems = if (selectedTab == 0) paymentVerificationItems else scheduleConfirmationItems

    Scaffold(
        containerColor = BackgroundWhite
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── 1. TopBar (Green Header) ──────────────────────────────────
            com.android.rumahsehatmannawasalwa.ui.components.TopBar(
                title = "Verifikasi",
                subtitle = "Verifikasi pembayaran & konfirmasi jadwal pasien",
                onBackClick = { navController.popBackStack() },
                bottomExtra = 90.dp
            )

            // ── 2. Fixed White Sheet ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 130.dp) // Offset for overlapping sheet
                    .background(
                        color = BackgroundWhite,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
            )

            // ── 3. Search & Tabs Overlap ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp) // Start overlapping transition
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryInternalChanged(it) },
                        placeholder = { Text("Cari ID Booking...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = GreenPrimary) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color.LightGray.copy(0.5f),
                            cursorColor = GreenPrimary
                        )
                    )

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = GreenPrimary,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = GreenPrimary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { viewModel.onTabSelected(0) },
                            text = { Text("Pembayaran", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { viewModel.onTabSelected(1) },
                            text = { Text("Konfirmasi", fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                // ── 4. List Verifikasi ────────────────────────────────────────
                Box(modifier = Modifier.fillMaxSize()) {
                    if (activeItems.loadState.refresh is LoadState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GreenPrimary)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 12.dp,
                                bottom = 80.dp,
                                start = 0.dp,
                                end = 0.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(count = activeItems.itemCount) { index ->
                                activeItems[index]?.let { booking ->
                                    VerificationCard(booking = booking) {
                                        navController.navigate(Screen.AdminAppointmentDetail.createRoute(booking.id))
                                    }
                                }
                            }

                            if (activeItems.itemCount == 0 && activeItems.loadState.refresh !is LoadState.Loading) {
                                item {
                                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.CheckCircleOutline, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                                            Spacer(Modifier.height(8.dp))
                                            Text("Semua tugas selesai!", color = Color.Gray)
                                        }
                                    }
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
fun VerificationCard(booking: BookingListItem, onClick: () -> Unit) {
    val bookingUi = remember(booking) { BookingMapper.mapToUiModel(booking) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Header Card: Booking ID
                Text(
                    text = "Booking #${booking.id}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )

                Text(
                    text = booking.serviceName ?: "Layanan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Detail Pasien
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = booking.patientName ?: "-", fontSize = 13.sp, color = Color.DarkGray)
                }

                // Waktu
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Default.Event, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatDateHuman(booking.bookingTime),
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Bagian Aksi (Kanan)
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(
                    label = bookingUi.statusLabel,
                    color = bookingUi.statusColor
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Tombol aksi yang lebih menonjol untuk verifikasi
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Proses", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}