package com.android.rumahsehatmannawasalwa.ui.screens.therapist.record

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistory
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyRecordViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils.formatDateHuman

@Composable
fun TherapyRecordDetailScreen(
    recordId: Int,
    viewModel: TherapyRecordViewModel,
    navController: NavController
) {
    val detailResult by viewModel.detailResult.collectAsState()
    val scrollState = rememberScrollState()
    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp

    LaunchedEffect(recordId) {
        viewModel.getTherapyRecordDetail(recordId)
    }

    Scaffold(
        floatingActionButton = {
            if (detailResult is ApiResult.Success) {
                val record = (detailResult as ApiResult.Success).data
                // Cek apakah catatan sudah dikunci (Status Completed)
                val isLocked = record.booking?.status == "completed"
                
                if (!isLocked) {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("therapy_record_form/${record.booking?.id}?record_id=${record.id}")
                        },
                        containerColor = GreenPrimary,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                            contentDescription = "Edit Catatan"
                        )
                    }
                }
            }
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
        ) {
            // FIXED TOP BAR
            Box(modifier = Modifier.statusBarsPadding()) {
                TopBar(
                    title = "Catatan Terapi",
                    onBackClick = { navController.popBackStack() },
                    transparentBackground = true,
                    hideBackground = true
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                Spacer(modifier = Modifier.height(25.dp))
                MannaSheet() {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        when (val result = detailResult) {
                            is ApiResult.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(screenHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = GreenPrimary)
                                }
                            }
                            is ApiResult.Success -> {
                                val record = result.data
                                // Tampilkan Banner Kunci jika sudah completed
                                if (record.booking?.status == "completed") {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                                        color = Color(0xFFFFF4F4),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFCCCC))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFFCC0000),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Catatan Terapi Terkunci (Sesi Selesai)",
                                                color = Color(0xFFCC0000),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                RecordDetailContent(record = record)
                            }
                            is ApiResult.Error -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("😕", fontSize = 40.sp)
                                    Text(
                                        "Gagal memuat data",
                                        fontWeight = FontWeight.Bold,
                                        color = SlateText
                                    )
                                    Text(
                                        text = result.error,
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center)
                                    Button(
                                        onClick = { viewModel.getTherapyRecordDetail(recordId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                                    ) {
                                        Text(
                                            text = "Coba Lagi",
                                            color = Color.White)
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

// ── Detail Content
@Composable
private fun RecordDetailContent(record: TherapyHistory) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // INFORMASI SESI
        Column {
            SectionTitle("Informasi Sesi")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, DividerColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Nomor
                    InfoLabelValue(
                        label = "Nomor",
                        value = "#${FormatterUtils.formatServiceBookingId(record.booking?.id ?: record.id, record.booking?.service?.name)}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Waktu Reservasi
                    InfoLabelValue(
                        label = "Waktu Reservasi",
                        value = formatDateHuman(record.booking?.bookingDate ?: record.examinationDate)
                    )
                }
            }
        }

        // DETAIL LAYANAN
        Column {
            SectionTitle("Detail Layanan")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, DividerColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Service + Time
                    PersonRow(
                        name = record.booking?.service?.name ?: "Terapi",
                        subtitle = FormatterUtils.formatTimeRange(record.booking?.bookingTime),
                        imageUrl = record.booking?.service?.imageUrl
                    )

                    if (record.therapist != null) {
                        HorizontalDivider(color = DividerLight, modifier = Modifier.padding(vertical = 12.dp))
                        PersonRow(
                            name = record.therapist.name,
                            subtitle = "Terapis",
                            user = record.therapist
                        )
                    }

                    if (record.patient != null) {
                        HorizontalDivider(color = DividerLight, modifier = Modifier.padding(vertical = 12.dp))
                        PersonRow(
                            name = record.patient.name,
                            subtitle = "Pasien",
                            user = record.patient
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // KELUHAN PASIEN
            Column {
                SectionTitle("Keluhan Pasien")
                TextPill(record.patientComplaint)
            }
            // TINDAKAN TERAPIS
            Column {
                SectionTitle("Tindakan Terapis")
                TextPill(record.therapistAction)
            }

            // CATATAN TAMBAHAN
            Column {
                if (!record.additionalNotes.isNullOrBlank()) {
                    SectionTitle("Catatan Tambahan")
                    TextPill(record.additionalNotes)
                }
            }
        }
    }
}

// ── Section Title
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = SlateTextDark
    )
}

// ── Info Row: Label (green) + Value (dark, right-aligned) ────────────────────
@Composable
private fun InfoLabelValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = BodyGray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = SlateTextDark,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Person Row (Avatar + Name + Subtitle) ────────────────────────────────────
@Composable
private fun PersonRow(name: String, subtitle: String, user: User? = null, imageUrl: String? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar
        Surface(
            shape = CircleShape,
            color = GreenSoft,
            modifier = Modifier.size(60.dp)
        ) {
            val photoUrl = imageUrl ?: user?.let { FormatterUtils.getFullImageUrl(it.profilePhotoUrl ?: it.profilePhotoPath) }
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback avatar
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=${name}&background=random&size=64",
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = SlateTextDark
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = BodyGray
            )
        }
    }
}

// ── Text
@Composable
private fun TextPill(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, DividerColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = SlateText,
            lineHeight = 22.sp
        )
    }
}

// Helpers are now handled by FormatterUtils

