package com.android.rumahsehatmannawasalwa.ui.screens.admin.services

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.*
import kotlinx.coroutines.launch
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundWhite
import com.android.rumahsehatmannawasalwa.ui.theme.BodyGray
import com.android.rumahsehatmannawasalwa.ui.theme.DividerLight
import com.android.rumahsehatmannawasalwa.ui.theme.GrayText
import com.android.rumahsehatmannawasalwa.ui.theme.GreenDark
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.GreenSoft
import com.android.rumahsehatmannawasalwa.ui.theme.SlateText
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.ServiceViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServiceDetailScreen(
    navController: NavController,
    serviceId: Int,
    viewModel: ServiceViewModel
) {
    var service by remember { mutableStateOf<Service?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 1. Ambil Data Detail
    LaunchedEffect(serviceId) {
        viewModel.getServiceDetail(
            id = serviceId,
            onSuccess = { s ->
                isLoading = false
                service = s
            },
            onError = { error ->
                isLoading = false
                navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_msg", "Gagal memuat detail: $error")
                navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_type", "ERROR")
                navController.popBackStack()
            }
        )
    }

    // 2. Dialog Hapus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Layanan", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus layanan ini? Data yang sudah dihapus tidak dapat dikembalikan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteService(
                            id = serviceId,
                            onSuccess = {
                                navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_msg", "Layanan Berhasil Dihapus")
                                navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_type", "SUCCESS")
                                navController.popBackStack()
                            },
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        MannaSnackbarVisuals(message = "Gagal menghapus: $error", type = SnackbarType.ERROR)
                                    )
                                }
                            }
                        )
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (service != null) {
            val s = service!!
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ── HEADER HIJAU ──
                Box(modifier = Modifier.statusBarsPadding()) {
                    TopBar(
                        title = "Detail Layanan",
                        subtitle = "Informasi lengkap layanan terapi",
                        onBackClick = { navController.popBackStack() },
                        transparentBackground = true,
                        hideBackground = true
                    )
                }

                MannaSheet(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Gambar Layanan (Rounded ala BottomSheet)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(GreenSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            if (s.imageUrl != null) {
                                AsyncImage(
                                    model = s.imageUrl,
                                    contentDescription = s.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Spa, null, tint = GreenPrimary, modifier = Modifier.size(60.dp))
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Nama Layanan
                        Text(
                            text = s.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateText,
                            lineHeight = 28.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        // Row Harga & Durasi (Chips Style)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = FormatterUtils.formatRupiah(s.price),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenPrimary
                            )

                            Surface(
                                color = GreenSoft,
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = GreenPrimary)
                                    Text("${s.duration} menit", fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = DividerLight)
                        Spacer(Modifier.height(24.dp))

                        // Deskripsi Section
                        Text(
                            "Tentang Layanan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateText
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = s.description,
                            fontSize = 15.sp,
                            color = BodyGray,
                            lineHeight = 24.sp
                        )

                        Spacer(Modifier.height(40.dp))

                        // ACTION BUTTONS (Rounded & Strong)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Button Hapus (Outline Style)
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.weight(1f).height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Hapus", fontWeight = FontWeight.Bold)
                            }

                            // Button Edit (Solid Style)
                            Button(
                                onClick = { navController.navigate("admin_edit_service/${s.id}") },
                                modifier = Modifier.weight(1f).height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Edit", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }

        // --- Snackbar Host (Manual Position) ---
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) { data ->
            MannaSnackbar(snackbarData = data)
        }
    }
}