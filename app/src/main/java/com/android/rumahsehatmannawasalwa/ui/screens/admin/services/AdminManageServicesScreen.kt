package com.android.rumahsehatmannawasalwa.ui.screens.admin.services

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.R
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundWhite
import com.android.rumahsehatmannawasalwa.ui.theme.BodyGray
import com.android.rumahsehatmannawasalwa.ui.theme.DividerLight
import com.android.rumahsehatmannawasalwa.ui.theme.GrayText
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.GreenSoft
import com.android.rumahsehatmannawasalwa.ui.theme.SlateText
import com.android.rumahsehatmannawasalwa.ui.theme.StatusWarning
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.*
import com.android.rumahsehatmannawasalwa.ui.theme.GreenDark
import com.android.rumahsehatmannawasalwa.ui.theme.GreenLight
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.ServiceViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageServicesScreen(
    navController: NavController,
    viewModel: ServiceViewModel,
    onEditServiceClick: (Int) -> Unit = {},
    onAddServiceClick: () -> Unit = {}
) {
    val serviceList by viewModel.serviceList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var serviceToDelete by remember { mutableStateOf<Service?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ── LOGIC ANIMASI FAB & SNACKBAR ──
    val isSnackbarShowing = snackbarHostState.currentSnackbarData != null
    val fabOffset by animateDpAsState(
        targetValue = if (isSnackbarShowing) (-80).dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "fab_movement"
    )

    // ── Check Navigation Result for Snackbar ──
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val savedStateHandle = navBackStackEntry?.savedStateHandle
    val snackbarMsg = savedStateHandle?.getStateFlow<String?>("snackbar_msg", null)?.collectAsState()
    val snackbarTypeVal = savedStateHandle?.getStateFlow<String?>("snackbar_type", null)?.collectAsState()

    LaunchedEffect(snackbarMsg?.value) {
        snackbarMsg?.value?.let { msg ->
            val type = when (snackbarTypeVal?.value) {
                "SUCCESS" -> SnackbarType.SUCCESS
                "ERROR" -> SnackbarType.ERROR
                else -> SnackbarType.INFO
            }
            snackbarHostState.showSnackbar(MannaSnackbarVisuals(message = msg, type = type))
            savedStateHandle.remove<String>("snackbar_msg")
            savedStateHandle.remove<String>("snackbar_type")
        }
    }

    LaunchedEffect(Unit) { viewModel.fetchServiceList() }

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
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            TopBar(
                title = "Kelola Layanan",
                onBackClick = { navController.popBackStack() },
                transparentBackground = true,
                hideBackground = true,
            )
            Spacer(modifier = Modifier.height(20.dp))
            MannaSheet(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading && serviceList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else {
                    ServiceListContent(
                        layananList = serviceList,
                        onServiceClick = { serviceId ->
                            selectedService = serviceList.find { it.id == serviceId }
                            showSheet = true
                        }
                    )

                    if (isLoading || isDeleting) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GreenPrimary)
                        }
                    }
                }
            }
        }

        // --- FAB (Taruh di paling bawah) ---
        FloatingActionButton(
            onClick = onAddServiceClick,
            containerColor = GreenPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = fabOffset)
                .padding(bottom = 16.dp, end = 16.dp) // Minimal padding untuk "paling bawah"
                .navigationBarsPadding()
        ) {
            Icon(Icons.Default.Add, stringResource(id = R.string.add_services))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) { data ->
            MannaSnackbar(snackbarData = data)
        }

        // --- Detail Sheet & Dialogs ---
        if (showSheet && selectedService != null) {
            ServiceDetailSheet(
                service = selectedService!!,
                sheetState = sheetState,
                onDismissRequest = { showSheet = false },
                onEditClick = {
                    showSheet = false
                    onEditServiceClick(selectedService!!.id)
                },
                onDeleteClick = {
                    showSheet = false
                    serviceToDelete = selectedService
                    showDeleteDialog = true
                }
            )
        }

        CustomConfirmDialog(
            show = showDeleteDialog,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                isDeleting = true
                serviceToDelete?.let { service ->
                    viewModel.deleteService(
                        id = service.id,
                        onSuccess = {
                            isDeleting = false
                            viewModel.fetchServiceList()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    MannaSnackbarVisuals(
                                        message = "Layanan \"${service.name}\" Berhasil Dihapus",
                                        type = SnackbarType.SUCCESS
                                    )
                                )
                            }
                        },
                        onError = { error ->
                            isDeleting = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    MannaSnackbarVisuals(message = "Gagal: $error", type = SnackbarType.ERROR)
                                )
                            }
                        }
                    )
                }
            },
            title = "Hapus Layanan?",
            description = "Layanan \"${serviceToDelete?.name}\" akan dihapus permanen. Tindakan ini tidak dapat dibatalkan.",
            confirmText = "Ya, Hapus",
            dismissText = "Batal",
            icon = Icons.Default.Delete
        )
    }
}

@Composable
fun ServiceListContent(
    layananList: List<Service>,
    onServiceClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(layananList.size) { index ->
            val layanan = layananList[index]
            AdminServiceCard(layanan = layanan, onClick = { onServiceClick(layanan.id) })
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun AdminServiceCard(layanan: Service, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(80.dp), color = GreenSoft) {
                if (!layanan.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = layanan.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Spa, null, tint = GreenPrimary) }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(layanan.name, fontWeight = FontWeight.Bold, color = SlateText, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(FormatterUtils.formatRupiah(layanan.price), color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                Text("${layanan.duration} Menit", fontSize = 12.sp, color = GrayText)
            }
            Icon(Icons.Default.ChevronRight, null, tint = GreenPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailSheet(
    service: Service,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)).background(GreenSoft), contentAlignment = Alignment.Center) {
                if (!service.imageUrl.isNullOrEmpty()) {
                    AsyncImage(model = service.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Spa, null, tint = GreenPrimary, modifier = Modifier.size(56.dp))
                }
            }
            Text(service.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SlateText)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(FormatterUtils.formatRupiah(service.price), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
                Surface(color = GreenSoft, shape = RoundedCornerShape(50.dp)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(13.dp), tint = GreenPrimary)
                        Text("${service.duration} menit", fontSize = 12.sp, color = GreenPrimary)
                    }
                }
            }
            HorizontalDivider(color = DividerLight)
            Text(service.description, fontSize = 14.sp, color = BodyGray, lineHeight = 21.sp)

            Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = StatusWarning)) {
                Icon(Icons.Default.Edit, null, Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Ubah Layanan", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onDeleteClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.5.dp, Color.Red.copy(0.6f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                Icon(Icons.Default.Delete, null, Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Hapus Layanan", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}