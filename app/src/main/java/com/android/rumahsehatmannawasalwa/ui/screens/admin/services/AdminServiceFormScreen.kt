package com.android.rumahsehatmannawasalwa.ui.screens.admin.services

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.ServiceViewModel
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.*
import kotlinx.coroutines.launch

@Composable
fun AdminFormServiceScreen(
    navController: NavController,
    viewModel: ServiceViewModel,
    serviceId: Int? = null
) {
    // ── 1. STATES ──
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isEditMode = serviceId != null && serviceId != 0

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(isEditMode) }
    var showImageDialog by remember { mutableStateOf(false) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ── 2. LOGIC FETCH ──
    LaunchedEffect(serviceId) {
        if (isEditMode) {
            viewModel.getServiceDetail(
                id = serviceId!!,
                onSuccess = { s ->
                    isFetching = false
                    name = s.name
                    price = s.price.toString()
                    duration = s.duration.toString()
                    description = s.description
                    existingImageUrl = s.imageUrl
                },
                onError = { error ->
                    isFetching = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            MannaSnackbarVisuals(message = error, type = SnackbarType.ERROR)
                        )
                    }
                }
            )
        }
    }

    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) selectedImageUri = result.uriContent
    }

    // ── 3. UI RENDER ──
    Scaffold(
        containerColor = BackgroundWhite,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                MannaSnackbar(snackbarData = data)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // SCROLLABLE FORM CONTENT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ServiceHeroHeader(isEditMode = isEditMode)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 90.dp)
                            .background(
                                BackgroundWhite,
                                RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                            )
                            .padding(24.dp)
                    ) {
                        if (isFetching) {
                            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GreenPrimary)
                            }
                        } else {
                            ServiceImagePicker(
                                selectedImageUri = selectedImageUri,
                                existingImageUrl = existingImageUrl,
                                onPickImage = {
                                    if (selectedImageUri != null || existingImageUrl != null) showImageDialog = true
                                    else cropImageLauncher.launch(
                                        CropImageContractOptions(
                                            null,
                                            CropImageOptions(
                                                fixAspectRatio = true,
                                                aspectRatioX = 16,
                                                aspectRatioY = 9
                                            )
                                        )
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            ServiceFormFields(
                                name = name,
                                onNameChange = { name = it },
                                price = price,
                                onPriceChange = { if (it.all { c -> c.isDigit() }) price = it },
                                duration = duration,
                                onDurationChange = { if (it.all { c -> c.isDigit() }) duration = it },
                                description = description,
                                onDescriptionChange = { description = it }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            ServiceSaveButton(
                                isEditMode = isEditMode,
                                isLoading = isLoading,
                                onSaveClick = {
                                    if (name.isBlank() || price.isBlank()) {
                                        Toast.makeText(context, "Data belum lengkap", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showSaveConfirmation = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            ServiceTopBar(
                isEditMode = isEditMode,
                isScrolled = scrollState.value > 100,
                onBackClick = { navController.popBackStack() }
            )
        }
    }

    // ── 4. DIALOGS ──
    if (showImageDialog) {
        ServiceImagePreviewDialog(
            selectedImageUri = selectedImageUri,
            existingImageUrl = existingImageUrl,
            onDismiss = { showImageDialog = false },
            onEditImage = {
                showImageDialog = false
                cropImageLauncher.launch(
                    CropImageContractOptions(
                        null,
                        CropImageOptions(fixAspectRatio = true, aspectRatioX = 16, aspectRatioY = 9)
                    )
                )
            }
        )
    }

    CustomConfirmDialog(
        show = showSaveConfirmation,
        onDismiss = { showSaveConfirmation = false },
        onConfirm = {
            showSaveConfirmation = false
            isLoading = true
            val p = price.toIntOrNull() ?: 0
            val d = duration.toIntOrNull() ?: 0

            val successHandler: () -> Unit = {
                val message = if (isEditMode) "Layanan Berhasil Diperbarui" else "Layanan Berhasil Ditambahkan"
                navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_msg", message)
                navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_type", "SUCCESS")
                navController.popBackStack()
            }
            val errorHandler: (String) -> Unit = { error ->
                isLoading = false
                val prefix = if (isEditMode) "Gagal memperbarui:" else "Gagal menambahkan:"
                scope.launch {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(message = "$prefix $error", type = SnackbarType.ERROR)
                    )
                }
            }

            if (isEditMode) {
                viewModel.updateService(
                    id = serviceId!!,
                    name = name,
                    price = p,
                    duration = d,
                    description = description,
                    imageUri = selectedImageUri,
                    context = context,
                    onSuccess = successHandler,
                    onError = errorHandler
                )
            } else {
                viewModel.createService(
                    name = name,
                    price = p,
                    duration = d,
                    description = description,
                    imageUri = selectedImageUri,
                    context = context,
                    onSuccess = successHandler,
                    onError = errorHandler
                )
            }
        },
        title = if (isEditMode) "Simpan Perubahan?" else "Tambah Layanan?",
        description = "Pastikan data sudah benar sebelum disimpan.",
        confirmText = "Ya, Simpan",
        dismissText = "Batal"
    )
}

// ── SUB-COMPOSABLES ──

@Composable
fun ServiceHeroHeader(isEditMode: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    listOf(GreenDark, GreenPrimary, GreenLight)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.05f))
                .align(Alignment.TopEnd)
                .offset(30.dp, (-20).dp)
        )
    }
}

@Composable
fun ServiceImagePicker(
    selectedImageUri: Uri?,
    existingImageUrl: String?,
    onPickImage: () -> Unit
) {
    Column {
        Text(
            text = "Foto Layanan",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SlateText
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp))
                .background(GreenSoft)
                .clickable { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedImageUri != null -> {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                existingImageUrl != null -> {
                    AsyncImage(
                        model = existingImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Pilih Foto",
                            color = GreenPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceFormFields(
    name: String,
    onNameChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nama Layanan") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = price,
                onValueChange = onPriceChange,
                label = { Text("Harga (Rp)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = duration,
                onValueChange = onDurationChange,
                label = { Text("Menit") },
                modifier = Modifier.weight(0.6f),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Deskripsi") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ServiceSaveButton(
    isEditMode: Boolean,
    isLoading: Boolean,
    onSaveClick: () -> Unit
) {
    Button(
        onClick = onSaveClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
        shape = RoundedCornerShape(16.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEditMode) "Simpan Perubahan" else "Simpan Layanan",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ServiceTopBar(
    isEditMode: Boolean,
    isScrolled: Boolean,
    onBackClick: () -> Unit
) {
    val topBarBg by animateColorAsState(
        targetValue = if (isScrolled) Color.White else Color.Transparent,
        animationSpec = tween(300),
        label = "topBarBg"
    )
    val topBarIcon by animateColorAsState(
        targetValue = if (isScrolled) GreenPrimary else Color.White,
        animationSpec = tween(300),
        label = "topBarIcon"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = topBarBg,
        shadowElevation = if (isScrolled) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = topBarIcon
                    )
                }
                Text(
                    text = if (isEditMode) "Edit Layanan" else "Tambah Layanan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = topBarIcon
                )
            }
        }
    }
}

@Composable
fun ServiceImagePreviewDialog(
    selectedImageUri: Uri?,
    existingImageUrl: String?,
    onDismiss: () -> Unit,
    onEditImage: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pratinjau Foto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SlateText
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (existingImageUrl != null) {
                        AsyncImage(
                            model = existingImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Batal", color = Color.Gray)
                    }
                    Button(
                        onClick = onEditImage,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Ganti")
                    }
                }
            }
        }
    }
}

