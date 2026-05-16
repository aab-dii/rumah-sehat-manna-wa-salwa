package com.android.rumahsehatmannawasalwa.ui.screens.therapist.record

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyRecordViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils.formatDateHuman

@Composable
fun TherapyRecordFormScreen(
    bookingId: Int,
    recordId: Int? = null, // Parameter baru untuk mode Edit
    viewModel: TherapyRecordViewModel,
    navController: NavController
) {
    var complaint by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val saveResult by viewModel.saveResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val bookingDetailResult by viewModel.bookingDetail.collectAsState()
    val detailResult by viewModel.detailResult.collectAsState() // Untuk load data lama saat edit
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp

    // 1. Fetch data awal
    LaunchedEffect(bookingId, recordId) {
        viewModel.fetchBookingDetail(bookingId)
        if (recordId != null) {
            viewModel.getTherapyRecordDetail(recordId)
        }
    }

    // 2. Populasi data jika masuk mode Edit
    LaunchedEffect(detailResult) {
        if (recordId != null && detailResult is ApiResult.Success) {
            val record = (detailResult as ApiResult.Success).data
            complaint = record.patientComplaint
            action = record.therapistAction
            notes = record.additionalNotes ?: ""
        }
    }

    LaunchedEffect(saveResult) {
        when (saveResult) {
            is ApiResult.Success -> {
                Toast.makeText(context, "Catatan terapi berhasil disimpan", Toast.LENGTH_SHORT).show()
                navController.previousBackStackEntry?.savedStateHandle?.set("refresh_booking", true)
                navController.popBackStack()
            }
            is ApiResult.Error -> {
                val errorMsg = (saveResult as ApiResult.Error).error
                Toast.makeText(context, "Terjadi kesalahan: $errorMsg", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
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
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // FIXED TOP BAR
            Box(modifier = Modifier.statusBarsPadding()) {
                TopBar(
                    title = if (recordId == null) "Isi Catatan Terapi" else "Edit Catatan Terapi",
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
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (val result = bookingDetailResult) {
                            is ApiResult.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(screenHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = GreenPrimary)
                                }
                            }
                            is ApiResult.Success -> {
                                val data = result.data.data
                                val appointment = data.appointment
                                val service = data.service
                                val patient = data.patient
                                val therapist = data.therapist
                                val isLocked = appointment.status == "completed"

                                // ── 1. INFORMASI SESI ───────────────────
                                Column {
                                    SectionTitle("Informasi Sesi")
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(2.dp, DividerColor)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            InfoLabelValue(
                                                label = "Nomor", 
                                                value = "#${FormatterUtils.formatServiceBookingId(bookingId, service?.name)}"
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InfoLabelValue(
                                                label = "Waktu Reservasi", 
                                                value = formatDateHuman(appointment.bookingDate)
                                            )
                                        }
                                    }
                                }

                                // ── 2. DETAIL LAYANAN ───────────────────
                                Column {
                                    SectionTitle("Detail Layanan")
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(2.dp, DividerColor)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            // Service Row
                                            PersonRow(
                                                name = service?.name ?: "Terapi",
                                                subtitle = FormatterUtils.formatTimeRange(appointment.bookingTime),
                                                imageUrl = service?.imageUrl
                                            )
                                            
                                            HorizontalDivider(color = DividerLight, modifier = Modifier.padding(vertical = 12.dp))
                                            
                                            // Therapist Row
                                            PersonRow(
                                                name = therapist?.name ?: "-",
                                                subtitle = "Terapis",
                                                imageUrl = FormatterUtils.getFullImageUrl(therapist?.profilePhotoUrl ?: therapist?.profilePhotoPath)
                                            )
                                            
                                            HorizontalDivider(color = DividerLight, modifier = Modifier.padding(vertical = 12.dp))
                                            
                                            // Patient Row
                                            PersonRow(
                                                name = patient?.name ?: "-",
                                                subtitle = "Pasien",
                                                imageUrl = FormatterUtils.getFullImageUrl(patient?.profilePhotoUrl ?: patient?.profilePhotoPath)
                                            )
                                        }
                                    }
                                }

                                // ── 3. INPUT KELUHAN PASIEN ──────────────────────
                                Column {
                                    SectionTitle("Keluhan Pasien *")
                                    InputArea(value = complaint, onValueChange = { complaint = it }, height = 120.dp, placeholder = "Tuliskan keluhan pasien...", enabled = !isLocked)
                                }

                                // ── 4. INPUT TINDAKAN TERAPIS ────────────────────
                                Column {
                                    SectionTitle("Tindakan Terapis *")
                                    InputArea(value = action, onValueChange = { action = it }, height = 120.dp, placeholder = "Tindakan yang telah dilakukan...", enabled = !isLocked)
                                }

                                // ── 5. INPUT CATATAN TAMBAHAN ────────────────────

                                Column {
                                    SectionTitle("Catatan Tambahan (Opsional)")
                                    InputArea(
                                        value = notes, 
                                        onValueChange = { notes = it }, 
                                        height = 100.dp, 
                                        placeholder = "Saran atau resep tambahan...",
                                        enabled = !isLocked
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                // ── 6. TOMBOL SIMPAN ─────────────────────────────
                                if (!isLocked) {
                                    MannaButton(
                                        text = if (isLoading) "Menyimpan..." else if (recordId == null) "Simpan Catatan" else "Update Catatan",
                                        onClick = {
                                            if (complaint.isNotBlank() && action.isNotBlank()) {
                                                if (recordId == null) {
                                                    viewModel.createTherapyRecord(bookingId, patient?.id ?: 0, complaint, action, notes)
                                                } else {
                                                    viewModel.updateTherapyRecord(recordId, bookingId, patient?.id ?: 0, complaint, action, notes)
                                                }
                                            } else {
                                                Toast.makeText(context, "Mohon lengkapi keluhan dan tindakan", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        enabled = !isLoading
                                    )
                                } else {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color(0xFFFFF4F4),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFCCCC))
                                    ) {
                                        Text(
                                            "Sesi telah selesai. Catatan terapi tidak dapat diubah.",
                                            modifier = Modifier.padding(16.dp),
                                            color = Color(0xFFCC0000),
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                            is ApiResult.Error -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("😕", fontSize = 40.sp)
                                    Text("Gagal memuat detail booking", fontWeight = FontWeight.Bold, color = SlateText)
                                    Text(text = result.error, color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                                    Button(
                                        onClick = { viewModel.fetchBookingDetail(bookingId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                                    ) {
                                        Text("Coba Lagi", color = Color.White)
                                    }
                                }
                            }
                            else -> {}
                        }

                        Spacer(Modifier.height(30.dp))
                    }
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
        color = SlateTextDark,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

// ── Info Row: Label (gray) + Value (dark, right-aligned)
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

// ── Person Row (Avatar + Name + Subtitle)
@Composable
private fun PersonRow(name: String, subtitle: String, imageUrl: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = GreenSoft,
            modifier = Modifier.size(48.dp)
        ) {
            val photoUrl = imageUrl ?: "https://ui-avatars.com/api/?name=${name}&background=random&size=128"
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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

// ── Input Area (Styling like TextPill but editable)
@Composable
private fun InputArea(value: String, onValueChange: (String) -> Unit, height: androidx.compose.ui.unit.Dp, placeholder: String, enabled: Boolean = true) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(2.dp, DividerColor, RoundedCornerShape(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color(0xFFF9F9F9),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = GreenPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = if (enabled) SlateText else Color.Gray,
            lineHeight = 22.sp
        ),
        placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray.copy(alpha = 0.6f)) }
    )
}
