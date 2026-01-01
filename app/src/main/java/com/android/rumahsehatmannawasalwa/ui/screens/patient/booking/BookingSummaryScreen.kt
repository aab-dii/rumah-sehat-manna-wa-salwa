package com.android.rumahsehatmannawasalwa.ui.screens.patient.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import com.android.rumahsehatmannawasalwa.ui.theme.*



// --- Data Model Simulasi ---
data class BookingSummaryData(
    val serviceName: String,
    val duration: String,
    val date: String,
    val time: String,
    val therapistName: String,
    val locationName: String,
    val locationType: String, // "Klinik" atau "Home Care"
    val servicePrice: Int,
    val adminFee: Int
) {
    val totalPrice: Int get() = servicePrice + adminFee
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryScreen(
    data: BookingSummaryData,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rincian Booking",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Pakai AutoMirrored untuk support RTL yang benar
                            contentDescription = "Kembali",
                            tint = GreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        bottomBar = {
            // Container Bottom Bar dengan Shadow halus
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, spotColor = Color.Black.copy(alpha = 0.1f))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Pesan Sekarang",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Kartu 1: Informasi Utama ---
            SummaryCard {
                // Baris 1: Layanan
                InfoRow(
                    icon = Icons.Default.Spa,
                    label = "Layanan",
                    value = data.serviceName,
                    subValue = data.duration
                )
                CustomDivider()
                // Baris 2: Waktu
                InfoRow(
                    icon = Icons.Default.Event,
                    label = "Waktu",
                    value = data.date,
                    subValue = data.time
                )
                CustomDivider()
                // Baris 3: Terapis
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Terapis",
                    value = data.therapistName,
                    subValue = "Spesialis" // Bisa diganti data dinamis jika ada
                )
            }

            // --- Kartu 2: Lokasi ---
            SummaryCard {
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Lokasi",
                    value = data.locationName,
                    subValue = data.locationType
                )
            }

            // --- Kartu 3: Rincian Harga ---
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundLight), // Sedikit abu untuk beda
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Rincian Pembayaran",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    PriceRow(label = "Harga Layanan", price = data.servicePrice)
                    PriceRow(label = "Biaya Admin", price = data.adminFee)
                    
                    Divider(color = DividerColor, thickness = 1.dp)
                    
                    // Baris TOTAL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = formatRupiah(data.totalPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp)) // Jarak ekstra di bawah
        }
    }
}

// --- Helper Components ---

@Composable
fun SummaryCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Shadow sedikit lebih naik
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), // Jarak antar elemen dalam card
            content = content
        )
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top // Icon tetap di atas jika teks panjang
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GreenPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (subValue != null) {
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PriceRow(label: String, price: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = formatRupiah(price),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
fun CustomDivider() {
    Divider(
        color = DividerColor,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 40.dp) // Indentasi agar sejajar teks, bukan icon
    )
}

fun formatRupiah(amount: Int): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    format.maximumFractionDigits = 0 // Hilangkan ,00
    return format.format(amount)
}


// --- Preview ---

@Preview(showBackground = true)
@Composable
fun BookingSummaryScreenPreview() {
    val dummyData = BookingSummaryData(
        serviceName = "Bekam Basah (Cupping)",
        duration = "60 Menit",
        date = "Senin, 18 Oktober 2025",
        time = "10:00 - 11:00",
        therapistName = "Ustadz Abdullah",
        locationName = "Klinik Rumah Sehat",
        locationType = "Datang ke Klinik",
        servicePrice = 150000,
        adminFee = 5000 // Asumsi ada biaya admin/platform
    )
    
    MaterialTheme {
        BookingSummaryScreen(
            data = dummyData,
            onBackClick = {},
            onConfirmClick = {}
        )
    }
}
