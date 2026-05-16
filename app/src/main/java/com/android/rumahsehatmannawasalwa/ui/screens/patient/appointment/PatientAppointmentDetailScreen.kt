package com.android.rumahsehatmannawasalwa.ui.screens.patient.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.ui.components.DetailRowSejajar
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.SectionTitle
import com.android.rumahsehatmannawasalwa.ui.components.appointment.MeetingInfoCard
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AppointmentDetailViewModel
import com.android.rumahsehatmannawasalwa.utils.AppConstants
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import android.content.Intent
import android.net.Uri
import com.android.rumahsehatmannawasalwa.BuildConfig
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAppointmentDetailScreen(
    navController: NavController,
    bookingId: Int,
    viewModel: AppointmentDetailViewModel = viewModel()
) {
    val state by viewModel.detailState.collectAsState()
    val context = LocalContext.current

    var selectedProofUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedProofUri = uri }

    LaunchedEffect(bookingId) {
        viewModel.getAppointmentDetail(bookingId)
        viewModel.subscribeToRealTimeUpdate(bookingId)
    }

    // Cleanup: unsubscribe saat screen di-dispose
    DisposableEffect(bookingId) {
        onDispose { viewModel.unsubscribeFromRealTimeUpdate(bookingId) }
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
    ) {
        // TopBar
        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.statusBarsPadding()) {

            TopBar(
                title = "Detail Janji Temu",
                onBackClick = { navController.popBackStack() },
                transparentBackground = true,
                hideBackground = true,
            )
        }

        // MannaSheet + FAB
        Box(modifier = Modifier.fillMaxSize()) {

            // Sheet tanpa scroll di luar
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(25.dp))
                MannaSheet { // ← tidak perlu modifier fillMaxHeight
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()) // ← scroll ada di dalam
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        when (val result = state) {
                            is ApiResult.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(screenHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = GreenPrimary)
                                }
                            }
                            is ApiResult.Error -> {
                                ErrorView(error = result.error) {
                                    viewModel.getAppointmentDetail(bookingId)
                                }
                            }
                            is ApiResult.Success -> {
                                DetailContent(
                                    data = result.data,
                                    viewModel = viewModel,
                                    navController = navController,
                                    launcher = launcher,
                                    selectedProofUri = selectedProofUri,
                                    onClearProof = { selectedProofUri = null }
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            // FAB dan tombol bottom tetap di Box yang sama
            if (state is ApiResult.Success) {
                val data = (state as ApiResult.Success).data
                val bStatus = data.appointment?.status?.lowercase()

                if (bStatus in listOf("completed", "selesai", "force_completed")) {
                    val recordId = data.appointment?.therapyRecordId
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .fillMaxWidth()
                    ) {
                        MannaButton(
                            text = "Lihat Catatan Terapi",
                            onClick = { 
                                if (recordId != null && recordId != 0) {
                                    navController.navigate(Screen.TherapyRecordDetail.createRoute(recordId))
                                } else {
                                    navController.navigate(Screen.Record.route)
                                }
                            }
                        )
                    }
                } else if (bStatus != "canceled" && bStatus != "cancelled") {
                    FloatingActionButton(
                        onClick = {
                            val msg = "Halo Admin, saya ingin bertanya mengenai janji temu saya " +
                                    "dengan ID: ${data.displayId}. (Status: ${data.statusLabel})"
                            val url = "https://api.whatsapp.com/send?phone=${BuildConfig.ADMIN_WHATSAPP}" +
                                    "&text=${Uri.encode(msg)}"
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW).apply { this.data = Uri.parse(url) }
                            )
                        },
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 24.dp, bottom = 24.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Hubungi Admin")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailContent(
    data: BookingUiModel,
    viewModel: AppointmentDetailViewModel,
    navController: NavController,
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    selectedProofUri: android.net.Uri?,
    onClearProof: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatusSpecificContent(
            data = data,
            context = context,
            selectedProofUri = selectedProofUri,
            onUploadClick = { launcher.launch("image/*") },
            onConfirmUpload = {
                selectedProofUri?.let { uri ->
                    data.appointment?.let { viewModel.reuploadProof(it.id, uri) }
                }
            },
            onClearProof = onClearProof
        )

        Column {
            SectionTitle("Info Janji Temu", color = SlateTextDark)
            MeetingInfoCard(data)
        }

        Column {
            SectionTitle("Detail Layanan", color = SlateTextDark)
            ConsolidatedServiceTherapistCard(data)

        }

        Column {
            SectionTitle("Rincian Pembayaran", color = SlateTextDark)
            PriceDetailSection(data)
        }
    }
}

@Composable
fun ConsolidatedServiceTherapistCard(data: BookingUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DividerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = data.service?.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceGrey),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(data.service?.name ?: "Layanan", fontWeight = FontWeight.ExtraBold, color = SlateTextDark, fontSize = 16.sp)
                    Text(
                        text = FormatterUtils.formatTimeRange(
                            data.appointment?.bookingTime,
                            data.service?.duration ?: 60
                        ),
                        fontSize = 13.sp,
                        color = BodyGray
                    )
                }
            }
            
            HorizontalDivider(color = DividerLight, thickness = 1.dp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SurfaceGrey),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = data.therapist?.profilePhotoPath ?: data.therapist?.fotoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(data.therapist?.name ?: "Terapis", fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 15.sp)
                    Text("Terapis", fontSize = 13.sp, color = BodyGray)
                }
            }
        }
    }
}

@Composable
fun PriceDetailSection(data: BookingUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DividerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Total dari transaksi (sumber kebenaran), fallback ke preview
            val basePrice  = data.service?.price ?: 0
            val totalPrice = data.transaction?.amount ?: (basePrice + AppConstants.ADMIN_FEE_PREVIEW)
            val adminFee   = totalPrice - basePrice

            PriceRow("Harga Layanan", basePrice)
            PriceRow("Biaya Admin", adminFee)
            
            HorizontalDivider(color = DividerLight, thickness = 1.dp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total Pembayaran", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                Text(
                    text = FormatterUtils.formatRupiah(totalPrice),
                    fontWeight = FontWeight.Bold, 
                    color = GreenPrimary, 
                    fontSize = 16.sp
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                Text(
                    text = if (data.transaction?.paymentMethod?.lowercase() == "cash") "Tunai" else "Transfer Bank",
                    fontWeight = FontWeight.Bold, 
                    color = GreenPrimary, 
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun PriceRow(label: String, value: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 14.sp, color = BodyGray)
        Text(text = FormatterUtils.formatRupiah(value), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateTextDark)
    }
}

@Composable
fun StatusSpecificContent(
    data: BookingUiModel,
    context: android.content.Context,
    selectedProofUri: android.net.Uri?,
    onUploadClick: () -> Unit,
    onConfirmUpload: () -> Unit,
    onClearProof: () -> Unit
) {
    val bStatus = data.appointment?.status?.lowercase()

    when (bStatus) {
        "waiting_payment" -> {
            PaymentStatusCard(title = "Menunggu Pembayaran", color = PaymentWarning, icon = Icons.Default.Payment) {
                var timeLeft by remember { mutableLongStateOf(data.appointment?.paymentRemainingSeconds ?: 0L) }
                LaunchedEffect(Unit) {
                    while (timeLeft > 0) {
                        delay(1000L)
                        timeLeft--
                    }
                }

                if (timeLeft > 0) {
                    val timerText = FormatterUtils.formatTimer(timeLeft)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PaymentWarningBackground, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("Sisa Waktu Pembayaran", fontSize = 12.sp, color = PaymentWarning)
                        Text(timerText, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PaymentWarning)
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    BankAccountInfo(context)
                    Spacer(Modifier.height(16.dp))
                    
                    if (selectedProofUri != null) {
                        ProofPreviewSection(selectedProofUri, onClearProof, onUploadClick, onConfirmUpload)
                    } else {
                        Button(onClick = onUploadClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                            Icon(Icons.Default.Upload, null); Spacer(Modifier.width(8.dp)); Text("Upload Bukti Transfer")
                        }
                    }
                } else {
                    // Tampilan Expired
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF4F4), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Warning, null, tint = RedDanger, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Batas Waktu Habis", fontWeight = FontWeight.Bold, color = RedDanger)
                        Text(
                            "Booking ini telah kedaluwarsa karena melewati batas waktu pembayaran.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
        "payment_rejected" -> {
            PaymentStatusCard(title = "Pembayaran Ditolak", color = RedDanger, icon = Icons.Default.Warning) {
                Text("Alasan: ${data.transaction?.rejectionNote ?: "Data tidak valid"}", color = RedDanger, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                if (selectedProofUri != null) {
                    ProofPreviewSection(selectedProofUri, onClearProof, onUploadClick, onConfirmUpload)
                } else {
                    Button(onClick = onUploadClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                        Text("Upload Ulang Bukti")
                    }
                }
            }
        }
        "canceled", "cancelled" -> {
            PaymentStatusCard(title = "Dibatalkan", color = RedDanger, icon = Icons.Default.Warning) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Janji temu ini telah dibatalkan.",
                        color = RedDanger,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (data.isExpiredWarning) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Waktu tunggu pembayaran telah habis.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentStatusCard(
    title: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
fun BottomActionBar(data: BookingUiModel, onCancel: () -> Unit, onViewRecord: () -> Unit = {}) {
    val bStatus = data.appointment?.status?.lowercase()
    if (bStatus in listOf("completed", "selesai")) {
        Surface(shadowElevation = 8.dp, color = Color.White) {
            Box(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()) {
                Button(
                    onClick = onViewRecord,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Lihat Catatan Terapi")
                }
            }
        }
    }
}

@Composable
fun BankAccountInfo(context: android.content.Context) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceGrey), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Transfer ke:", fontSize = 12.sp, color = GrayText)
            Text(AppConstants.BANK_NAME, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(AppConstants.BANK_ACCOUNT_NUMBER, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                IconButton(onClick = { /* copy logic */ }) { Icon(Icons.Default.ContentCopy, null, Modifier.size(20.dp), tint = GrayText) }
            }
        }
    }
}

@Composable
fun ProofPreviewSection(uri: android.net.Uri, onCancel: () -> Unit, onReplace: () -> Unit, onConfirm: () -> Unit) {
    Column {
        AsyncImage(model = uri, contentDescription = null, modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, GrayText, RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
        Row(Modifier.padding(vertical = 8.dp), Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancel, Modifier.weight(1f)) { Text("Batal") }
            OutlinedButton(onClick = onReplace, Modifier.weight(1f)) { Text("Ganti") }
        }
        Button(onClick = onConfirm, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) { Text("Kirim Bukti") }
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text(error, color = RedDanger)
        Button(onClick = onRetry) { Text("Coba Lagi") }
    }
}
