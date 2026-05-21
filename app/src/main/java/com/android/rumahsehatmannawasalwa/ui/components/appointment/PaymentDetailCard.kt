package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AppointmentDetailViewModel
import com.android.rumahsehatmannawasalwa.utils.AppConstants
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils

@Composable
fun PaymentDetailCard(data: BookingUiModel, viewModel: AppointmentDetailViewModel) {
    var showPreview by remember { mutableStateOf(false) }

    // Total dari transaksi (sumber kebenaran), fallback ke preview
    val basePrice  = data.service?.price ?: 0
    val totalPrice = data.transaction?.amount ?: (basePrice + AppConstants.ADMIN_FEE_PREVIEW)
    val adminFee   = totalPrice - basePrice

    val remainingSeconds by viewModel.remainingSeconds.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DividerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Countdown untuk Admin
            if (data.appointment?.status?.lowercase() == "waiting_payment" && remainingSeconds != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (remainingSeconds!! > 0) "Sisa Waktu Pasien Bayar" else "Waktu Pembayaran Habis",
                        fontSize = 12.sp,
                        color = Color(0xFFF57F17),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = FormatterUtils.formatTimer(remainingSeconds!!),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingSeconds!! > 0) Color(0xFFF57F17) else RedDanger
                    )
                }
                HorizontalDivider(color = DividerLight, thickness = 1.dp)
            }

            // Rincian biaya
            PriceDetailRow("Harga Layanan", basePrice)
            PriceDetailRow("Biaya Admin", adminFee)

            HorizontalDivider(color = DividerLight, thickness = 1.dp)

            // Total pembayaran
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total Pembayaran",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateTextDark
                )
                Text(
                    text = FormatterUtils.formatRupiah(totalPrice),
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                    fontSize = 16.sp
                )
            }

            // Metode pembayaran
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Metode Pembayaran",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateTextDark
                )
                Text(
                    text = if (data.paymentMethod.lowercase() == "tunai" || data.paymentMethod.lowercase() == "cash") "Tunai" else "Transfer Bank",
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                    fontSize = 15.sp
                )
            }

            // Bukti transfer — hanya muncul saat menunggu verifikasi
            if (data.appointment?.status?.lowercase() == "waiting_verification") {
                HorizontalDivider(color = DividerLight, thickness = 1.dp)

                Text(
                    "Bukti Transfer (Klik untuk perbesar):",
                    fontSize = 12.sp,
                    color = BodyGray,
                    fontWeight = FontWeight.Medium
                )

                AsyncImage(
                    model = data.transaction?.proofOfTransfer,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceGrey)
                        .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
                        .clickable { showPreview = true },
                    contentScale = ContentScale.Fit
                )

                RejectPaymentSection(data.appointment.id, viewModel)
            }
        }
    }

    // Dialog preview bukti transfer fullscreen
    if (showPreview && data.transaction?.proofOfTransfer != null) {
        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = data.transaction.proofOfTransfer,
                        contentDescription = "Preview Bukti Transfer",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showPreview = false },
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = { showPreview = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PriceDetailRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = BodyGray)
        Text(
            text = FormatterUtils.formatRupiah(value),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SlateTextDark
        )
    }
}

@Composable
fun RejectPaymentSection(id: Int, viewModel: AppointmentDetailViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    MannaButton(
        text = "Tolak Bukti Pembayaran",
        containerColor = RedDanger,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Tolak Pembayaran", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Berikan alasan penolakan agar pasien dapat mengunggah ulang bukti yang benar.",
                        fontSize = 14.sp,
                        color = BodyGray
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Alasan Penolakan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectPayment(id, reason)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                ) { Text("Tolak") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", color = BodyGray)
                }
            }
        )
    }
}