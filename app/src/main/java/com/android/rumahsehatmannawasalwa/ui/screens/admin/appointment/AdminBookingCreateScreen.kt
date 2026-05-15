package com.android.rumahsehatmannawasalwa.ui.screens.admin.appointment

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.ui.components.HorizontalCalendar
import com.android.rumahsehatmannawasalwa.ui.components.SearchableDropdown
import com.android.rumahsehatmannawasalwa.ui.components.TimeSlotGrid
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.ServiceViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.*
import com.android.rumahsehatmannawasalwa.ui.components.appointment.StyledSearchableDropdown
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import kotlinx.coroutines.launch
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingCreateScreen(
    navController: NavController,
    bookingViewModel: AdminBookingViewModel = viewModel(),
    userViewModel: AdminUserViewModel = viewModel(),
    serviceViewModel: ServiceViewModel = viewModel()
) {
    val uiState by bookingViewModel.uiState.collectAsState()
    val patientList by userViewModel.patientList.collectAsState()
    val therapistList by userViewModel.therapistList.collectAsState()
    val serviceList by serviceViewModel.serviceList.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // DETEKSI SCROLL UNTUK ANIMASI TOPBAR (PERSIS DETAIL)
    val isScrolled = scrollState.value > 5

    var showConfirmDialog by remember { mutableStateOf(false) }

    // Sync Data to ViewModel
    LaunchedEffect(serviceList) {
        bookingViewModel.setServices(serviceList)
    }
    LaunchedEffect(therapistList) {
        bookingViewModel.setTherapists(therapistList)
    }

    // Initial Fetch
    LaunchedEffect(Unit) {
        userViewModel.fetchUserList("pasien")
        userViewModel.fetchUserList("terapis")
        serviceViewModel.fetchServiceList()
    }

    LaunchedEffect(uiState.isBookingSuccess) {
        if (uiState.isBookingSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_msg", "Booking berhasil dibuat!")
            navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_type", "SUCCESS")
            bookingViewModel.resetState()
            navController.popBackStack()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    MannaSnackbarVisuals(message = it, type = SnackbarType.ERROR)
                )
            }
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
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── TopBar
            Spacer(modifier = Modifier.height(12.dp))
            TopBar(
                title = "Buat Janji Temu",
                subtitle = "Lengkapi data booking pasien",
                onBackClick = { navController.popBackStack() },
                transparentBackground = true,
                hideBackground = true,
            )

            // ── Scrollable Area + Bottom Bar
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(25.dp))

                    MannaSheet(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 600.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // --- SECTION 1: PASIEN ---
                            StyledSearchableDropdown(
                                label = "Pasien",
                                items = patientList,
                                selectedItem = uiState.selectedPatient,
                                onItemSelected = { bookingViewModel.onPatientSelected(it) },
                                itemToString = { it.name },
                                placeholder = "Cari nama pasien...",
                                leadingIcon = Icons.Default.Person,
                                itemImage = { it.profilePhotoPath ?: it.profilePhotoUrl }
                            )

                            // --- SECTION 2: LAYANAN ---
                            StyledSearchableDropdown(
                                label = "Layanan",
                                items = serviceList,
                                selectedItem = uiState.selectedService,
                                onItemSelected = { bookingViewModel.onServiceSelected(it) },
                                itemToString = { "${it.name} - ${FormatterUtils.formatRupiah(it.price)}" },
                                placeholder = "Pilih Layanan",
                                leadingIcon = Icons.Default.Settings,
                                itemImage = { it.imageUrl }
                            )

                            StyledSearchableDropdown(
                                label = "Terapis",
                                items = uiState.filteredTherapists,
                                selectedItem = uiState.selectedTherapist,
                                onItemSelected = { bookingViewModel.onTherapistSelected(it) },
                                itemToString = { it.name },
                                placeholder = if (uiState.selectedService == null) "Pilih layanan dulu" else "Pilih Terapis",
                                leadingIcon = Icons.Default.Face,
                                itemImage = { it.profilePhotoPath ?: it.profilePhotoUrl }
                            )


                            // --- SECTION 3: WAKTU ---
                            CreateSectionCard(title = "Waktu Kunjungan") {
                                Column {
                                    HorizontalCalendar(
                                        selectedDate = uiState.selectedDate,
                                        onDateSelected = { bookingViewModel.onDateSelected(it) },
                                        activeDays = uiState.activeDays,
                                        holidayInfo = uiState.holidayInfo,
                                        availabilityMap = uiState.availabilityMap,
                                        enabled = uiState.selectedService != null && uiState.selectedTherapist != null,
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        "Pilih Jam",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = BodyGray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (uiState.isLoadingSlots) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.CenterHorizontally),
                                            color = GreenPrimary
                                        )
                                    } else {
                                        TimeSlotGrid(
                                            slots = uiState.availableTimeSlots,
                                            selectedSlot = uiState.selectedTimeSlot,
                                            isLoading = uiState.isLoadingSlots,
                                            onSlotSelected = { bookingViewModel.onTimeSlotSelected(it) }
                                        )
                                    }
                                }
                            }

                            // --- SECTION 4: PEMBAYARAN ---
                            CreateSectionCard(
                                title = "Metode Pembayaran",
                            ) {
                                PaymentSection(
                                    selectedOption = bookingViewModel.selectedPaymentOption.collectAsState().value,
                                    onOptionSelected = { bookingViewModel.onPaymentOptionSelected(it) },
                                    proofUri = bookingViewModel.proofOfTransferUri.collectAsState().value,
                                    onProofSelected = { bookingViewModel.onProofSelected(it) }
                                )
                            }

                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                }

                // ── FIXED BOTTOM BAR ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = uiState.isFormValid && !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Konfirmasi Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Snackbar Host (Manual Position) ---
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) { data ->
            MannaSnackbar(snackbarData = data)
        }
    }

    // ── 3. Confirmation Dialog (Sultan Detail Only) ──────────────────────
    if (showConfirmDialog) {
        val patientName = uiState.selectedPatient?.name ?: ""
        val serviceName = uiState.selectedService?.name ?: ""
        val date = uiState.selectedDate?.toString() ?: ""
        val time = uiState.selectedTimeSlot ?: ""
        val paymentMethod = if (bookingViewModel.selectedPaymentOption.collectAsState().value == "cash") "Tunai" else "Transfer Bank"

        CustomConfirmDialog(
            show = showConfirmDialog,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                bookingViewModel.createBooking()
            },
            title = "Konfirmasi Booking",
            description = "Buat janji temu untuk $patientName - $serviceName pada $date pukul $time dengan metode pembayaran $paymentMethod?",
            confirmText = "Ya, Buat",
            dismissText = "Batal"
        )
    }
}

@Composable
fun CreateSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextDark
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = DividerColor.copy(alpha = 0.5f)
            )
            content()
        }
    }
}

@Composable
fun PaymentSection(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    proofUri: Uri?,
    onProofSelected: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
            uri: Uri? -> onProofSelected(uri)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // --- PILIHAN RADIO BUTTON ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentOptionRow(
                title = "Tunai (Bayar di Tempat)",
                selected = selectedOption == "cash",
                onClick = { onOptionSelected("cash") }
            )
            PaymentOptionRow(
                title = "Transfer Bank",
                selected = selectedOption == "transfer",
                onClick = { onOptionSelected("transfer") }
            )
        }

        // --- UPLOAD BUKTI (Hanya muncul jika Transfer) ---
        if (selectedOption == "transfer") {
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceLight) // Warna abu sangat muda biar kontras
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bukti Transfer",
                    style = MaterialTheme.typography.labelMedium,
                    color = BodyGray,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (proofUri != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        AsyncImage(
                            model = proofUri,
                            contentDescription = "Preview Bukti",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Tombol Ganti Foto di atas gambar
                        IconButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    RoundedCornerShape(50.dp)
                                )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // State pas belum ada foto
                    OutlinedButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            DividerColor.copy(alpha = 0.5f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Klik untuk Upload Bukti", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(if (selected) GreenPrimary.copy(alpha = 0.05f) else Color.Transparent)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) GreenPrimary else SlateText
        )
        Spacer(modifier = Modifier.weight(1f))
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
        )
    }
}

