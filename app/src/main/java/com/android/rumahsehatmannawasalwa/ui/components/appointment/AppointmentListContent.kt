package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingListItem
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundWhite
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppointmentListContent(
    padding: PaddingValues,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    upcomingItems: LazyPagingItems<BookingListItem>,
    historyItems: LazyPagingItems<BookingListItem>,
    // Chip filter params (opsional — default: tampilkan semua)
    upcomingOptions: List<FilterChipOption> = upcomingFilterOptions,
    upcomingChipSelected: String = upcomingOptions.first().value,
    onUpcomingChipSelected: (String) -> Unit = {},
    historyOptions: List<FilterChipOption> = historyFilterOptions,
    historyChipSelected: String = historyOptions.first().value,
    onHistoryChipSelected: (String) -> Unit = {},
    showSearchBar: Boolean = true,
    itemContent: @Composable (BookingListItem, isHistory: Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        // Header: Search + Tab
        Column(
            modifier = Modifier
                .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (showSearchBar) {
                SharedSearchBar(value = searchQuery, onValueChange = onSearchChange)
            }
            Spacer(modifier = Modifier.padding(top = if (showSearchBar) 4.dp else 0.dp))
            CapsuleTabRow(
                tabs = listOf("Akan Datang", "Riwayat"),
                pagerState = pagerState,
                onTabSelected = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }

        // Chip filter — berganti sesuai tab aktif
        val isUpcomingTab = pagerState.currentPage == 0
        AppointmentFilterChips(
            options = if (isUpcomingTab) upcomingOptions else historyOptions,
            selected = if (isUpcomingTab) upcomingChipSelected else historyChipSelected,
            onSelected = if (isUpcomingTab) onUpcomingChipSelected else onHistoryChipSelected,
            modifier = Modifier
                .background(BackgroundWhite)
                .padding(bottom = 8.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalAlignment = Alignment.Top
        ) { page ->
            val itemsToShow = if (page == 0) upcomingItems else historyItems
            Box(modifier = Modifier.fillMaxSize()) {
                if (itemsToShow.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GreenPrimary
                    )
                } else if (itemsToShow.itemCount == 0) {
                    Text(
                        "Tidak ada janji temu",
                        Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(count = itemsToShow.itemCount) { index ->
                            itemsToShow[index]?.let { booking ->
                                itemContent(booking, page == 1)
                            }
                        }
                    }
                }
            }
        }
    }
}