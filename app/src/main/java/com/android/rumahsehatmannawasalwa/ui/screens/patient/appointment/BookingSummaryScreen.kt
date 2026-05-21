package com.android.rumahsehatmannawasalwa.ui.screens.patient.appointment

import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbar
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbarVisuals
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.SnackbarType
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.BookingViewModel
import com.android.rumahsehatmannawasalwa.utils.AppConstants
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils.formatRupiah
import kotlinx.coroutines.launch

@Composable
fun BookingSummaryScreen(navController: NavController, viewModel: BookingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val paymentOption by viewModel.selectedPaymentOption.collectAsState()
    val bookingResult by viewModel.bookingState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val hasConflict = (bookingResult as? ApiResult.Error)?.code == 409
    val handleBackNavigation: () -> Unit = {
        if (hasConflict) {
            viewModel.handleConflictBack()
        }
        navController.popBackStack()
    }

    BackHandler(onBack = handleBackNavigation)

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successBookingId by remember { mutableStateOf<Int?>(null) }

    // Handle booking sukses → tampilkan dialog
    LaunchedEffect(bookingResult) {
        if (bookingResult is ApiResult.Success) {
            val bookingId = (bookingResult as ApiResult.Success).data as Int
            successBookingId = bookingId
            showSuccessDialog = true
        }
    }

    // Handle booking error → tampilkan snackbar
    val bookingError by viewModel.bookingErrorMessage.collectAsState()
    LaunchedEffect(bookingError) {
        bookingError?.let { error ->
            snackbarHostState.showSnackbar(
                MannaSnackbarVisuals(message = error, type = SnackbarType.ERROR)
            )
            viewModel.resetBookingError()
        }
    }

    // Dialog sukses
    if (showSuccessDialog && successBookingId != null) {
        SummarySuccessDialog(
            onConfirmed = {
                showSuccessDialog = false
                navController.navigate(
                    Screen.PatientAppointmentDetail.createRoute(successBookingId!!)
                ) {
                    popUpTo(Screen.PatientHome.route)
                }
                viewModel.resetState()
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GreenDark, GreenPrimary)
                )
            )
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // TopBar (compact, di luar MannaSheet)
        TopBar(
            title = "Rincian Janji temu",
            subtitle = "Selamat datang kembali!",
            onBackClick = handleBackNavigation,
            transparentBackground = true,
            hideBackground = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // MannaSheet + Bottom Bar
        Box(modifier = Modifier.fillMaxSize()) {
            MannaSheet(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        // SECTION 1: DATA JANJI TEMU
                        SummarySection("Detail Janji Temu") {
                            Column {
                                SummaryItemRow(
                                    name = uiState.selectedService?.name ?: "-",
                                    subName = "${uiState.selectedService?.duration ?: 60} Menit ${formatRupiah(uiState.selectedService?.price ?: 0)}",
                                    imageUrl = uiState.selectedService?.imageUrl,
                                    isCircular = false
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerLight)

                                val tgl = uiState.selectedDate?.toString()?.let { FormatterUtils.formatDateHuman(it) } ?: "-"
                                SummaryInfoRow(Icons.Default.Event, tgl, "${uiState.selectedTimeSlot ?: "-"} WITA")
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerLight)

                                SummaryItemRow(
                                    name = uiState.selectedTherapist?.name ?: "-",
                                    subName = "Terapis",
                                    imageUrl = uiState.selectedTherapist?.profilePhotoPath,
                                    isCircular = true
                                )
                            }
                        }

                        // SECTION 2: RINCIAN PEMBAYARAN
                        SummarySection("Rincian Pembayaran") {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SummaryPriceItem("Harga Layanan",
                                    formatRupiah(uiState.selectedService?.price ?: 0)
                                )
                                SummaryPriceItem("Biaya Admin", formatRupiah(AppConstants.ADMIN_FEE_PREVIEW))
                                HorizontalDivider(color = DividerLight)

                                val totalPrice = (uiState.selectedService?.price ?: 0) + AppConstants.ADMIN_FEE_PREVIEW
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Total Pembayaran", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                                    Text(
                                        text = formatRupiah(totalPrice),
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
                                    Text(
                                        text = if (paymentOption == "cash") "Tunai" else "Transfer Bank",
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
                }
            }

            // Bottom Bar (floating)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                MannaButton(
                    text = "Pesan Sekarang",
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.createBooking() }
                )
            }
        }
    }

    // Snackbar error di atas bottom bar
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 100.dp)
    ) { data -> MannaSnackbar(snackbarData = data) }
    } // end Box
}

@Composable
fun SummarySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = SlateTextDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, DividerColor),
            content = content
        )
    }
}

// getFullImageUrl helper dipindahkan ke FormatterUtils

@Composable
fun SummaryItemRow(
    name: String,
    subName: String,
    imageUrl: String?,
    isCircular: Boolean = false
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = FormatterUtils.getFullImageUrl(imageUrl),
            contentDescription = name,
            modifier = Modifier
                .size(40.dp)
                .clip(if (isCircular) CircleShape else RoundedCornerShape(10.dp))
                .background(SurfaceGrey),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            placeholder = androidx.compose.ui.res.painterResource(com.android.rumahsehatmannawasalwa.R.drawable.ic_launcher_foreground)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = SlateTextDark
            )
            Text(
                text = subName,
                fontSize = 13.sp,
                color = BodyGray
            )
        }
    }
}

@Composable
fun SummaryInfoRow(icon: ImageVector, title: String, subTitle: String) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(GreenPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateTextDark)
            Text(subTitle, fontSize = 13.sp, color = BodyGray)
        }
    }
}

@Composable
fun SummaryPriceItem(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = BodyGray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = SlateTextDark, fontSize = 14.sp)
    }
}

@Composable
fun SummaryPaymentOptionRow(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = SlateText)
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
        )
    }
}

@Composable
fun SummarySuccessDialog(onConfirmed: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(GreenPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        title = {
            Text(
                text = "Booking Berhasil!",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = GreenPrimary
            )
        },
        text = {
            Text(
                text = "Permintaan janji temu Anda telah kami terima. Silakan cek detail pembayaran atau status pesanan Anda.",
                fontSize = 14.sp,
                color = BodyGray,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            MannaButton(
                text = "Lihat Detail Pesanan",
                onClick = onConfirmed
            )
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}
