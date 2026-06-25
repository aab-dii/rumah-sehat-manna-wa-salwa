package com.android.rumahsehatmannawasalwa.ui.screens.therapist.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.SectionTitle
import com.android.rumahsehatmannawasalwa.ui.components.appointment.MeetingInfoCard
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaOutlinedButton
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AppointmentDetailViewModel
import com.android.rumahsehatmannawasalwa.ui.components.ActionOverlay
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbar
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbarVisuals
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.SnackbarType
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog
import com.android.rumahsehatmannawasalwa.utils.IntentUtils.launchWhatsApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistAppointmentDetailScreen(
    navController: NavController,
    bookingId: Int,
    viewModel: AppointmentDetailViewModel = viewModel()
) {
    val state by viewModel.detailState.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp

    var showStartConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        viewModel.getAppointmentDetail(bookingId)
        viewModel.subscribeToRealTimeUpdate(bookingId)
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let { msg ->
            val type = if (msg.contains("gagal", ignoreCase = true) || msg.contains("error", ignoreCase = true)) {
                SnackbarType.ERROR
            } else {
                SnackbarType.SUCCESS
            }
            snackbarHostState.showSnackbar(
                MannaSnackbarVisuals(message = msg, type = type)
            )
            viewModel.resetActionMessage()
        }
    }

    DisposableEffect(bookingId) {
        onDispose { viewModel.unsubscribeFromRealTimeUpdate(bookingId) }
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
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.statusBarsPadding()) {
                TopBar(
                    title = "Detail Janji Temu",
                    subtitle = "Informasi detail perawatan pasien Anda",
                    onBackClick = { navController.popBackStack() },
                    transparentBackground = true,
                    hideBackground = true,
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(25.dp))
                    MannaSheet {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 32.dp)
                        ) {
                            when (val result = state) {
                                is ApiResult.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(screenHeight / 2),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = GreenPrimary)
                                    }
                                }
                                is ApiResult.Error -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(screenHeight / 2),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(result.error, color = RedDanger, modifier = Modifier.padding(bottom = 16.dp))
                                            Button(onClick = { viewModel.getAppointmentDetail(bookingId) }) {
                                                Text("Coba Lagi")
                                            }
                                        }
                                    }
                                }
                                is ApiResult.Success -> {
                                    TherapistDetailContent(
                                        data = result.data,
                                        navController = navController,
                                        onStartClick = { showStartConfirmDialog = true }
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) { data ->
            MannaSnackbar(snackbarData = data)
        }

        if (isUpdating) {
            ActionOverlay()
        }
    }

    CustomConfirmDialog(
        show = showStartConfirmDialog,
        onDismiss = { showStartConfirmDialog = false },
        onConfirm = {
            showStartConfirmDialog = false
            viewModel.updateBookingStatus(bookingId, "in_progress")
        },
        title = "Mulai Terapi?",
        description = "Apakah Anda yakin ingin memulai sesi terapi sekarang? Status akan berubah menjadi Sedang Berlangsung.",
        confirmText = "Mulai Sekarang",
        dismissText = "Batal",
        icon = Icons.Default.PlayArrow
    )
}

@Composable
fun TherapistDetailContent(
    data: BookingUiModel,
    navController: NavController,
    onStartClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
//        TherapistStatusCard(data = data)

        Column {
            SectionTitle("Info Janji Temu", color = SlateTextDark)
            MeetingInfoCard(data)
        }

        Column {
            SectionTitle("Detail Layanan", color = SlateTextDark)
            TherapistServicePatientCard(data = data, navController = navController)
        }

        Column {
            SectionTitle("Rincian Pembayaran", color = SlateTextDark)
            TherapistPriceSection(data = data)
        }

        // Action Buttons at the bottom of detail page
        val bStatus = data.appointment?.status?.lowercase()
        val recordId = data.appointment?.therapyRecordId
        val needsRecord = bStatus == "force_completed" && (recordId == null || recordId == 0)

        if (bStatus != "canceled" && bStatus != "cancelled" && bStatus != "completed" && !needsRecord) {
            val isInProgress = bStatus == "in_progress"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button 1: History / Riwayat
                MannaOutlinedButton(
                    text = "Riwayat Pasien",
                    onClick = { data.patient?.let { navController.navigate(Screen.PatientHistory.createRoute(it.id)) } },
                    modifier = Modifier.weight(1f).height(48.dp),
                    contentColor = GreenPrimary,
                    borderColor = GreenPrimary
                )

                // Button 2: Mulai / Terapi (Selesaikan)
                MannaButton(
                    text = if (isInProgress) "Tulis Catatan Terapi" else "Mulai Sesi",
                    onClick = {
                        if (isInProgress) {
                            navController.navigate(Screen.TherapyRecordForm.createRoute(data.appointment?.id ?: 0))
                        } else {
                            onStartClick()
                        }
                    },
                    modifier = Modifier.weight(1.2f).height(48.dp),
                    containerColor = if (isInProgress) Color(0xFF0288D1) else GreenPrimary
                )
            }
        } else if (needsRecord) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button 1: History / Riwayat
                MannaOutlinedButton(
                    text = "Riwayat Pasien",
                    onClick = { data.patient?.let { navController.navigate(Screen.PatientHistory.createRoute(it.id)) } },
                    modifier = Modifier.weight(1f).height(48.dp),
                    contentColor = GreenPrimary,
                    borderColor = GreenPrimary
                )

                // Button 2: Tulis Catatan Terapi
                MannaButton(
                    text = "Tulis Catatan Terapi",
                    onClick = {
                        navController.navigate(Screen.TherapyRecordForm.createRoute(data.appointment?.id ?: 0))
                    },
                    modifier = Modifier.weight(1.2f).height(48.dp),
                    containerColor = GreenPrimary
                )
            }
        } else if (bStatus == "completed" || bStatus == "force_completed") {
            // Button to View Therapy Record
            MannaButton(
                text = "Lihat Catatan Terapi",
                onClick = {
                    if (recordId != null && recordId != 0) {
                        navController.navigate(Screen.TherapyRecordDetail.createRoute(recordId))
                    } else {
                        navController.navigate(Screen.TherapistHistory.route)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )
        }
    }
}

//@Composable
//fun TherapistStatusCard(data: BookingUiModel) {
//    val bStatus = data.appointment?.status?.lowercase() ?: ""
//    val color = data.statusColor
//    val title = data.statusLabel
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(color)
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(Icons.Default.Warning, null, tint = Color.White)
//            Spacer(Modifier.width(12.dp))
//            Column {
//                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                val description = when (bStatus) {
//                    "waiting_payment" -> "Menunggu pasien melakukan pembayaran transfer bank."
//                    "waiting_verification" -> "Pasien telah mengunggah bukti bayar, menunggu verifikasi admin."
//                    "confirmed" -> "Sesi janji temu telah dikonfirmasi dan terjadwal."
//                    "in_progress" -> "Sesi terapi sedang berlangsung."
//                    "completed", "force_completed" -> "Sesi janji temu telah selesai dilakukan."
//                    "canceled", "cancelled" -> "Sesi janji temu telah dibatalkan."
//                    else -> "Informasi status janji temu."
//                }
//                Text(description, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
//            }
//        }
//    }
//}

@Composable
fun TherapistServicePatientCard(data: BookingUiModel, navController: NavController) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DividerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Service Info
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

            // Patient Info (Only show if not null)
            if (data.patient != null) {
                HorizontalDivider(color = DividerLight, thickness = 1.dp)

                val photoUrl = if (!data.patient.profilePhotoPath.isNullOrEmpty()) {
                    data.patient.profilePhotoPath
                } else if (!data.patient.profilePhotoUrl.isNullOrEmpty()) {
                    data.patient.profilePhotoUrl
                } else if (!data.patient.fotoUrl.isNullOrEmpty()) {
                    data.patient.fotoUrl
                } else "https://ui-avatars.com/api/?name=${data.patient.name}"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SurfaceGrey),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(data.patient.name, fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 15.sp)
                        Text("Pasien", fontSize = 13.sp, color = BodyGray)
                    }

                    if (!data.patient.phoneNumber.isNullOrBlank()) {
                        Surface(
                            onClick = { launchWhatsApp(context, data.patient.phoneNumber) },
                            color = GreenSoft,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Hubungi Pasien",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TherapistPriceSection(data: BookingUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DividerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val basePrice = data.service?.price ?: 0
            val totalPrice = data.transaction?.amount ?: basePrice
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Harga Layanan", fontSize = 14.sp, color = BodyGray)
                Text(text = FormatterUtils.formatRupiah(basePrice), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateTextDark)
            }
            
            HorizontalDivider(color = DividerLight, thickness = 1.dp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total Transaksi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
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
