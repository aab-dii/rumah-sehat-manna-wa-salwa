package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary

/**
 * RecordListContent — Hanya list card riwayat terapi (LazyColumn + paging state).
 * Search bar dan filter ditaruh di luar komponen ini.
 */
@Composable
fun RecordListContent(
    items: LazyPagingItems<TherapyHistorySummary>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (TherapyHistorySummary) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            items.loadState.refresh is LoadState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GreenPrimary
                )
            }
            items.itemCount == 0 && items.loadState.refresh is LoadState.NotLoading -> {
                Text(
                    "Belum ada riwayat terapi",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            }
            items.loadState.refresh is LoadState.Error -> {
                val e = items.loadState.refresh as LoadState.Error
                Text(
                    "Gagal memuat: ${e.error.localizedMessage}",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 16.dp, bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(count = items.itemCount) { index ->
                        items[index]?.let { record ->
                            itemContent(record)
                        }
                    }
                    if (items.loadState.append is LoadState.Loading) {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally),
                                color = GreenPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
