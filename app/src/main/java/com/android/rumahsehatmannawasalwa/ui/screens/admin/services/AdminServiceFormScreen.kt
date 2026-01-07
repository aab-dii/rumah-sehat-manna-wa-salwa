package com.android.rumahsehatmannawasalwa.ui.screens.admin.services

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.LayananViewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddServiceScreen(
    navController: NavController,
    viewModel: LayananViewModel,
    serviceId: Int? = null
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    val isEditMode = serviceId != null && serviceId != 0

    var isFetching by remember { mutableStateOf(isEditMode) } // Start with true if edit mode

    val scrollState = rememberScrollState()
    // Fetch details if Edit Mode
    LaunchedEffect(serviceId) {
        if (isEditMode) {
            viewModel.getServiceDetail(serviceId!!) { result ->
                isFetching = false
                if (result is ApiResult.Success) {
                    val service = result.data
                    name = service.nama
                    price = service.harga.toString()
                    duration = service.durasi.toString()
                    description = service.deskripsi
                    existingImageUrl = service.imageUrl
                } else if (result is ApiResult.Error) {
                    Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        }
    }

    // Image Cropper Launcher
    val cropImageLauncher = rememberLauncherForActivityResult(contract = CropImageContract()) { result ->
        if (result.isSuccessful) {
            selectedImageUri = result.uriContent
        } else {
            val exception = result.error
            Toast.makeText(context, "Image selection failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Layanan" else "Tambah Layanan") },
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
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isFetching) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GreenPrimary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .imePadding() // Push content up when keyboard opens
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Image Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.LightGray, RoundedCornerShape(12.dp))
                            .clickable {
                                val options = CropImageContractOptions(
                                    uri = null,
                                    cropImageOptions = CropImageOptions(
                                        imageSourceIncludeGallery = true,
                                        imageSourceIncludeCamera = true,
                                        fixAspectRatio = true,
                                        aspectRatioX = 4,
                                        aspectRatioY = 3
                                    )
                                )
                                cropImageLauncher.launch(options)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected Service Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (existingImageUrl != null) {
                            val fullUrl = if (existingImageUrl!!.startsWith("http")) existingImageUrl 
                                          else "${com.android.rumahsehatmannawasalwa.BuildConfig.BASE_URL}storage/${existingImageUrl}"
                            coil.compose.AsyncImage(
                                model = fullUrl,
                                contentDescription = "Existing Service Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Text("Ketuk untuk tambah foto", color = Color.Gray)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Layanan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { if (it.all { char -> char.isDigit() }) price = it },
                            label = { Text("Harga (Rp)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { if (it.all { char -> char.isDigit() }) duration = it },
                            label = { Text("Durasi (Menit)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi Layanan") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5
                    )

                    Button(
                        onClick = {
                            if (name.isBlank() || price.isBlank() || duration.isBlank() || description.isBlank()) {
                                Toast.makeText(context, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            // Validation logic
                            val priceInt = price.toIntOrNull() ?: 0
                            val durationInt = duration.toIntOrNull() ?: 0

                            if (isEditMode) {
                                 viewModel.updateService(
                                    id = serviceId!!,
                                    name = name,
                                    price = priceInt,
                                    duration = durationInt,
                                    description = description,
                                    imageUri = selectedImageUri,
                                    context = context,
                                    onResult = { res ->
                                        if (res is ApiResult.Loading) {
                                            isLoading = true
                                        } else if (res is ApiResult.Success) {
                                            isLoading = false
                                            Toast.makeText(context, "Proses Berhasil", Toast.LENGTH_SHORT).show()
                                            viewModel.fetchServiceList() // Refresh
                                            navController.popBackStack()
                                        } else if (res is ApiResult.Error) {
                                            isLoading = false
                                            Toast.makeText(context, res.error, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                 )
                            } else {
                                viewModel.createService(
                                    name = name,
                                    price = priceInt,
                                    duration = durationInt,
                                    description = description,
                                    imageUri = selectedImageUri,
                                    context = context,
                                    onResult = { res ->
                                        if (res is ApiResult.Loading) {
                                            isLoading = true
                                        } else if (res is ApiResult.Success) {
                                            isLoading = false
                                            Toast.makeText(context, "Layanan Berhasil Ditambahkan", Toast.LENGTH_SHORT).show()
                                            viewModel.fetchServiceList()
                                            navController.popBackStack()
                                        } else if (res is ApiResult.Error) {
                                            isLoading = false
                                            Toast.makeText(context, res.error, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (isEditMode) "Simpan Perubahan" else "Simpan Layanan")
                        }
                    }
                }
            }
        }
    }
}
