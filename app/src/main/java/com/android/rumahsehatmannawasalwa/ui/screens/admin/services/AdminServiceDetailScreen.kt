package com.android.rumahsehatmannawasalwa.ui.screens.admin.services

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.service.Layanan
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.LayananViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServiceDetailScreen(
    navController: NavController,
    serviceId: Int,
    viewModel: LayananViewModel
) {
    var service by remember { mutableStateOf<Layanan?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    LaunchedEffect(serviceId) {
        viewModel.getServiceDetail(serviceId) { result ->
            isLoading = false
            if (result is ApiResult.Success) {
                service = result.data
            } else if (result is ApiResult.Error) {
                Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Layanan") },
            text = { Text("Apakah Anda yakin ingin menghapus layanan ini? Data yang dihapus dapat dipulihkan oleh admin database.") }, // Mentioning safe delete conceptually
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteService(serviceId) { result ->
                            if (result is ApiResult.Success) {
                                Toast.makeText(context, "Layanan berhasil dihapus", Toast.LENGTH_SHORT).show()
                                showDeleteDialog = false
                                viewModel.fetchServiceList() // Refresh list
                                navController.popBackStack()
                            } else if (result is ApiResult.Error) {
                                Toast.makeText(context, "Gagal hapus: ${result.error}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Layanan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (service != null) {
            val s = service!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color.Gray)
                ) {
                    if (s.imageUrl != null) {
                        val fullUrl = if (s.imageUrl.startsWith("http")) s.imageUrl 
                                      else "${com.android.rumahsehatmannawasalwa.BuildConfig.BASE_URL}storage/${s.imageUrl}"
                        coil.compose.AsyncImage(
                            model = fullUrl,
                            contentDescription = s.nama,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = s.nama,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.android.rumahsehatmannawasalwa.ui.theme.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val price = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(s.harga)
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleLarge,
                        color = GreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Durasi: ${s.durasi} Menit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Text(
                        text = "Deskripsi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s.deskripsi,
                        style = MaterialTheme.typography.bodyLarge,
                        color = com.android.rumahsehatmannawasalwa.ui.theme.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hapus")
                        }

                        Button(
                            onClick = { navController.navigate("admin_edit_service/${s.id}") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit")
                        }
                    }
                }
            }
        }
    }
}
