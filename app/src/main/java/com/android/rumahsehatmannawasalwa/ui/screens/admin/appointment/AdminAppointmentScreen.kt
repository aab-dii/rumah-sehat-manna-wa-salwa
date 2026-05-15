package com.android.rumahsehatmannawasalwa.ui.screens.admin.appointment

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.data.mapper.BookingMapper
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.appointment.AppointmentListCard
import com.android.rumahsehatmannawasalwa.ui.components.appointment.AppointmentListContent
import com.android.rumahsehatmannawasalwa.ui.components.appointment.SharedSearchBar
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentScreen(
    navController: NavController,
    viewModel: AdminBookingViewModel = viewModel()
) {
    val upcomingItems = viewModel.upcomingPager.collectAsLazyPagingItems()
    val historyItems = viewModel.historyPager.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val upcomingChipSelected by viewModel.upcomingChipFilter.collectAsState()
    val historyChipSelected by viewModel.historyChipFilter.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.isUserAdmin = true
        viewModel.refreshBookings()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundWhite,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateBooking.route) },
                modifier = Modifier.padding(bottom = 80.dp), // Adjust for bottom nav
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Default.Add, "Tambah") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to GreenDark,
                            0.25f to GreenLight,
                            1.0f to GreenLight
                        )
                    )
                )
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── TopBar
            TopBar(
                title = "Kelola Janji Temu",
                subtitle = "Pantau & kelola semua janji temu pasien",
                transparentBackground = true,
                hideBackground = true,
            )

            Spacer(modifier = Modifier.height(20.dp))
            // ── SearchBar
            SharedSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryInternalChanged(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MannaSheet(
                modifier = Modifier.fillMaxSize()
            ) {
                AppointmentListContent(
                    padding = PaddingValues(0.dp),
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.onSearchQueryInternalChanged(it) },
                    pagerState = pagerState,
                    coroutineScope = coroutineScope,
                    upcomingItems = upcomingItems,
                    historyItems = historyItems,
                    upcomingChipSelected = upcomingChipSelected,
                    onUpcomingChipSelected = { viewModel.setUpcomingChipFilter(it) },
                    historyChipSelected = historyChipSelected,
                    onHistoryChipSelected = { viewModel.setHistoryChipFilter(it) },
                    showSearchBar = false
                ) { booking, _ ->
                    val ui = remember(booking) { BookingMapper.mapToUiModel(booking, "admin") }
                    AppointmentListCard(
                        serviceName = ui.service?.name ?: "Layanan",
                        statusLabel = ui.statusLabel,
                        statusColor = ui.statusColor,
                        dateInfo = "${FormatterUtils.formatDateHuman(ui.appointment?.bookingDate)}, ${ui.appointment?.bookingTime} WITA",
                        personLabel1 = "${ui.therapist?.name ?: "Terapis"} (Terapis)",
                        personLabel2 = "${ui.patient?.name} (Pasien)",
                        onClick = {
                            navController.navigate(Screen.AdminAppointmentDetail.createRoute(booking.id))
                        }
                    )
                }
            }
        }
    }
}