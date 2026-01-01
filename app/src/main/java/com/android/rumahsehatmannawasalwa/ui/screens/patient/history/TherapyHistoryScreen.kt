package com.android.rumahsehatmannawasalwa.ui.screens.patient.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistory
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyHistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapyHistoryScreen(
    navController: androidx.navigation.NavController,
    viewModel: TherapyHistoryViewModel = viewModel()
) {
    // Data State
    val historyList by viewModel.historyList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Fetch on init
    LaunchedEffect(Unit) {
        viewModel.fetchTherapyHistory()
    }

    // State for Filter
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Bekam", "Akupunktur", "Refleksi", "Herbal")

    Scaffold(
        containerColor = BackgroundWhite,
        bottomBar = {
             com.android.rumahsehatmannawasalwa.ui.components.BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // 1. Header
            Text(
                text = "Riwayat Terapi",
                style = MaterialTheme.typography.headlineMedium, // H1 equivalent
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Filter Section
            // Baris 1: Chip tunggal (misal 'Tahun Ini')
            SuggestionChip(
                onClick = {},
                label = { Text("Tahun Ini 2025") },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color.Transparent,
                    labelColor = GreenPrimary
                ),
                border = BorderStroke(1.dp, GreenPrimary),
                shape = RoundedCornerShape(50),
                enabled = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Baris 2: LazyRow Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color.Transparent else GreenPrimary,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(50),
                        enabled = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. List Content
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom bar
                ) {
                    items(historyList) { item ->
                        TherapyHistoryCard(
                            item = item,
                            onClick = { 
                                // Internal Navigation Logic matching user request for consistency
                                navController.navigate("booking_detail/${item.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TherapyHistoryCard(
    item: TherapyHistory,
    onClick: () -> Unit
) {
    // Parse Date safely
    val (day, month) = remember(item.examinationDate) {
        try {
            val date = LocalDate.parse(item.examinationDate) // Expects YYYY-MM-DD
            val d = date.dayOfMonth.toString()
            val m = date.format(DateTimeFormatter.ofPattern("MMM", Locale("id", "ID"))) // Format Singkat bulan Indonesia
            d to m
        } catch (e: Exception) {
            "--" to "---"
        }
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat style
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bagian Kiri: Kotak Tanggal
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenContainer) // Background agak gelap/hijau muda
                    .size(width = 50.dp, height = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GreenOnContainer
                )
                Text(
                    text = month,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = GreenOnContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Bagian Tengah: Detail
            Column(modifier = Modifier.weight(1f)) {
                // Di sini 'diagnosis' dipakai sebagai judul Treatment, bisa disesuaikan kalau ada field lain
                Text(
                    text = item.diagnosis, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.patientComplaint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                val therapistName = item.therapist?.name ?: "Tidak ada data"
                Text(
                    text = "Terapis: $therapistName",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            
            // Bagian Kanan: Icon Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

// Extension to simple rotate
fun Modifier.rotate(degrees: Float) = this.then(Modifier.graphicsLayer(rotationZ = degrees))

@Preview(showBackground = true)
@Composable
fun TherapyHistoryScreenPreview() {
    RumahsehatmannawasalwaTheme {
       // Cannot preview easily with ViewModel, usually requires a mock or passing state directly. 
       // For syntax check only:
       // TherapyHistoryScreen() 
    }
}


