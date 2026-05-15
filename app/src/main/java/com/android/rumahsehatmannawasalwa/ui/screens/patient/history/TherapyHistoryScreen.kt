package com.android.rumahsehatmannawasalwa.ui.screens.patient.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.appointment.TherapyHistoryItemCard
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundWhite
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyRecordViewModel

@Composable
fun TherapyHistoryScreen(
    navController: NavController,
    viewModel: TherapyRecordViewModel,
    patientId: Int? = null
) {
    val historyItems = viewModel.getTherapyHistory(patientId).collectAsLazyPagingItems()

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopBar(
                title = "Riwayat Terapi"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(count = historyItems.itemCount) { index ->
                historyItems[index]?.let { record ->
                    TherapyHistoryItemCard(
                        record = record,
                        onClick = {
                            navController.navigate("therapy_record_detail/${record.id}")
                        }
                    )
                }
            }

            // Loading & Error states
            therapyHistoryPagingStates(historyItems)
        }
    }
}

private fun LazyListScope.therapyHistoryPagingStates(items: LazyPagingItems<TherapyHistorySummary>) {
    when {
        items.loadState.refresh is LoadState.Loading -> {
            item {
                Box(modifier = Modifier.fillParentMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GreenPrimary
                    )
                }
            }
        }
        items.loadState.refresh is LoadState.Error -> {
            val e = items.loadState.refresh as LoadState.Error
            item {
                Box(modifier = Modifier.fillParentMaxSize()) {
                    Text(
                        text = "Gagal memuat data: ${e.error.localizedMessage}",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Red
                    )
                }
            }
        }
        items.itemCount == 0 && items.loadState.refresh is LoadState.NotLoading -> {
            item {
                Box(modifier = Modifier.fillParentMaxSize()) {
                    Text(
                        text = "Belum ada riwayat terapi",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }
            }
        }
        items.loadState.append is LoadState.Loading -> {
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
