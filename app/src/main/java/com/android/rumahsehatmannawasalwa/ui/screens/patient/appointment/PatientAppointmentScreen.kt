package com.android.rumahsehatmannawasalwa.ui.screens.patient.appointment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.data.mapper.BookingMapper
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.appointment.AppointmentListCard
import com.android.rumahsehatmannawasalwa.ui.components.appointment.AppointmentListContent
import com.android.rumahsehatmannawasalwa.ui.components.appointment.SharedSearchBar
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.theme.GreenDark
import com.android.rumahsehatmannawasalwa.ui.theme.GreenLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import kotlinx.coroutines.launch

@Composable
fun PatientAppointmentScreen(
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

    LaunchedEffect(Unit) { viewModel.isUserAdmin = false }

    BackHandler {
        navController.navigate("home") {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

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
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ── TopBar
        TopBar(
            title = "Janji Temu Saya",
            subtitle = "Kelola riwayat & janji temu Anda",
            transparentBackground = true,
            hideBackground = true,
        )

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
                val ui = remember(booking) { BookingMapper.mapToUiModel(booking, "pasien") }
                AppointmentListCard(
                    serviceName = ui.service?.name ?: "Layanan",
                    statusLabel = ui.statusLabel,
                    statusColor = ui.statusColor,
                    dateInfo = "${FormatterUtils.formatDateHuman(ui.appointment?.bookingDate)}, ${ui.appointment?.bookingTime} WITA",
                    personLabel1 = "${ui.therapist?.name ?: "Terapis"} (Terapis)",
                    personLabel2 = null,
                    onClick = {
                        navController.navigate(Screen.PatientAppointmentDetail.createRoute(booking.id))
                    }
                )
            }
        }
    }
}
