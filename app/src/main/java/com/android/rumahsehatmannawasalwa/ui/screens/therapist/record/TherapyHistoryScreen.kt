package com.android.rumahsehatmannawasalwa.ui.screens.therapist.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.android.rumahsehatmannawasalwa.ui.components.TherapyRecordItemCard
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyHistoryViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyRecordViewModel

@Composable
fun TherapyHistoryScreen(
    patientId: Int? = null,
    viewModel: TherapyRecordViewModel,
    navController: NavController
) {
    // 1. Ambil datanya sebagai 'LazyPagingItems'
    val historyItems = viewModel.getTherapyHistory(patientId).collectAsLazyPagingItems()

    Scaffold(
    ) { padding ->
        // 2. Tampilkan di LazyColumn
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ... di dalam LazyColumn
            items(count = historyItems.itemCount) { index ->
                val item = historyItems[index]
                if (item != null) {
                    TherapyRecordItemCard(
                        record = item,
                        onViewDetailClick = {
                            // Navigasi ke detail menggunakan ID rekam medis
                            navController.navigate("therapy_record_detail/${item.id}")
                        }
                    )
                }
            }

            // 3. Handle Status Loading & Error di bagian bawah (Append)
            handlePagingStates(historyItems)
        }
    }

}

fun LazyListScope.handlePagingStates(items: LazyPagingItems<TherapyHistorySummary>) {
    when {
        // Saat pertama kali buka (Loading awal)
        items.loadState.refresh is LoadState.Loading -> {
            item { Box(Modifier.fillParentMaxSize()) { CircularProgressIndicator(Modifier.align(
                Alignment.Center)) } }
        }
        // Saat tarik data ke bawah (Loading tambahan)
        items.loadState.append is LoadState.Loading -> {
            item { CircularProgressIndicator(Modifier.fillMaxWidth().padding(16.dp).wrapContentWidth(Alignment.CenterHorizontally)) }
        }
        // Saat terjadi Error
        items.loadState.refresh is LoadState.Error -> {
            val e = items.loadState.refresh as LoadState.Error
            item { Text("Error: ${e.error.localizedMessage}", color = Color.Red) }
        }
    }
}

