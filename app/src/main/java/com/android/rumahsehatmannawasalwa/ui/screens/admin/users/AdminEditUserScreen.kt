package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.auth.ProfilePhoto
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog
import com.android.rumahsehatmannawasalwa.ui.components.inputs.MannaTextField
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbar
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbarVisuals
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.SnackbarType
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditUserScreen(
    navController: NavController,
    viewModel: AdminUserViewModel,
    userId: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.fetchUserDetail(userId)
    }

    val userState by viewModel.userDetailState.collectAsState()
    val serviceList by viewModel.serviceList.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("L") }
    val selectedServices = remember { mutableStateListOf<String>() }
    var userRole by remember { mutableStateOf("") }
    var profilePhotoPath by remember { mutableStateOf<String?>(null) }
    var isActive by remember { mutableStateOf(true) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var jobError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var servicesError by remember { mutableStateOf<String?>(null) }

    var showToggleConfirm by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }

    // Isi form saat data berhasil dimuat, hanya sekali
    LaunchedEffect(userState) {
        if (userState is ApiResult.Success && !isDataLoaded) {
            val user = (userState as ApiResult.Success).data
            name = user.name
            email = user.email
            phone = user.phoneNumber
            job = user.job
            address = user.address
            birthDate = user.birthDate
            selectedGender = user.gender
            userRole = user.role
            profilePhotoPath = user.profilePhotoPath
            isActive = user.deletedAt == null

            if (user.role == "terapis") {
                viewModel.fetchServices()
                selectedServices.clear()
                selectedServices.addAll(user.specialization)
            }
            isDataLoaded = true
        }
    }

    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract(),
        onResult = { result ->
            if (result.isSuccessful) {
                selectedImageUri = result.uriContent
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(
                            message = "Gagal memotong foto: ${result.error?.message}",
                            type = SnackbarType.ERROR
                        )
                    )
                }
            }
        }
    )

    // Handle hasil aksi update & toggle status
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is ApiResult.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(
                            message = "Data pengguna berhasil diperbarui",
                            type = SnackbarType.SUCCESS
                        )
                    )
                }
                viewModel.resetActionState()
                viewModel.fetchUserDetail(userId)
            }
            is ApiResult.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(
                            message = state.error,
                            type = SnackbarType.ERROR
                        )
                    )
                }
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    CustomConfirmDialog(
        show = showToggleConfirm,
        onDismiss = { showToggleConfirm = false },
        onConfirm = {
            if (isActive) viewModel.deleteUser(userId)
            else viewModel.restoreUser(userId)
            showToggleConfirm = false
        },
        title = if (isActive) "Nonaktifkan Pengguna?" else "Aktifkan Pengguna?",
        description = if (isActive)
            "Pengguna ini tidak akan bisa login sampai diaktifkan kembali."
        else
            "Pengguna akan dapat login kembali ke sistem.",
        confirmText = if (isActive) "Ya, Nonaktifkan" else "Ya, Aktifkan",
        isDanger = isActive
    )

    // Box root agar SnackbarHost dan loading overlay bisa align ke BottomCenter
    Box(modifier = Modifier.fillMaxSize()) {
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
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            TopBar(
                title = "Edit Pengguna",
                subtitle = "Perbarui informasi profil pasien atau terapis",
                onBackClick = { navController.popBackStack() },
                transparentBackground = true,
                hideBackground = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            MannaSheet(modifier = Modifier.fillMaxSize()) {
                if (!isDataLoaded && userState is ApiResult.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))

                        // Foto profil dengan mode edit
                        ProfilePhoto(
                            photoUrl = if (selectedImageUri != null) {
                                selectedImageUri.toString()
                            } else {
                                if (!profilePhotoPath.isNullOrBlank()) {
                                    val storageUrl = com.android.rumahsehatmannawasalwa.BuildConfig.BASE_URL
                                        .replace("/api/", "/storage/")
                                    "$storageUrl$profilePhotoPath"
                                } else null
                            },
                            size = 100.dp,
                            isEditMode = true,
                            onEditClick = {
                                cropImageLauncher.launch(
                                    CropImageContractOptions(
                                        uri = null,
                                        cropImageOptions = CropImageOptions(
                                            imageSourceIncludeGallery = true,
                                            imageSourceIncludeCamera = true,
                                            cropShape = CropImageView.CropShape.OVAL,
                                            fixAspectRatio = true,
                                            aspectRatioX = 1,
                                            aspectRatioY = 1
                                        )
                                    )
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Toggle aktif/nonaktif pengguna
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (isActive) "Status: Aktif" else "Status: Nonaktif",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) GreenPrimary else RedDanger
                                    )
                                    Text(
                                        text = if (isActive) "Pengguna dapat mengakses sistem"
                                        else "Pengguna sedang ditangguhkan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Switch(
                                    checked = isActive,
                                    onCheckedChange = { showToggleConfirm = true },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = GreenPrimary,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.LightGray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        MannaTextField(
                            label = "Nama Lengkap",
                            value = name,
                            onValueChange = {
                                name = it
                                if (nameError != null) nameError = null
                            },
                            placeholder = "Masukkan nama lengkap",
                            leadingIcon = Icons.Outlined.Person,
                            isError = nameError != null,
                            errorMessage = nameError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        MannaTextField(
                            label = "Email",
                            value = email,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = "",
                            enabled = false,
                            leadingIcon = Icons.Outlined.Mail
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        MannaTextField(
                            label = "Nomor WhatsApp",
                            value = phone,
                            onValueChange = {
                                phone = it
                                if (phoneError != null) phoneError = null
                            },
                            placeholder = "Contoh: 08123456789",
                            leadingIcon = Icons.Default.Phone,
                            isError = phoneError != null,
                            errorMessage = phoneError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (userRole == "pasien") {
                            MannaTextField(
                                label = "Pekerjaan",
                                value = job,
                                onValueChange = {
                                    job = it
                                    if (jobError != null) jobError = null
                                },
                                placeholder = "Masukkan pekerjaan",
                                leadingIcon = Icons.Outlined.Work,
                                isError = jobError != null,
                                errorMessage = jobError
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        } else if (userRole == "terapis") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Spesialisasi (Layanan)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextDark,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                if (serviceList.isEmpty()) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = GreenPrimary
                                    )
                                } else {
                                    serviceList.forEach { service ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (selectedServices.contains(service.name))
                                                        selectedServices.remove(service.name)
                                                    else
                                                        selectedServices.add(service.name)
                                                }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Checkbox(
                                                checked = selectedServices.contains(service.name),
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = GreenPrimary
                                                )
                                            )
                                            Text(
                                                text = service.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SlateTextDark,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        MannaTextField(
                            label = "Alamat",
                            value = address,
                            onValueChange = {
                                address = it
                                if (addressError != null) addressError = null
                            },
                            placeholder = "Masukkan alamat lengkap",
                            leadingIcon = Icons.Default.LocationOn,
                            isError = addressError != null,
                            errorMessage = addressError,
                            singleLine = false
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        MannaButton(
                            text = "Simpan Perubahan",
                            onClick = {
                                var isValid = true
                                if (name.isBlank()) { nameError = "Nama lengkap wajib diisi"; isValid = false }
                                if (phone.isBlank()) { phoneError = "Nomor HP wajib diisi"; isValid = false }
                                if (address.isBlank()) { addressError = "Alamat wajib diisi"; isValid = false }
                                if (userRole == "pasien" && job.isBlank()) { jobError = "Pekerjaan wajib diisi"; isValid = false }
                                if (userRole == "terapis" && selectedServices.isEmpty()) { servicesError = "Minimal pilih satu spesialisasi"; isValid = false }

                                if (isValid) {
                                    viewModel.updateUser(
                                        userId = userId,
                                        name = name,
                                        phone = phone,
                                        job = if (userRole == "pasien") job else "",
                                        specialization = if (userRole == "terapis") selectedServices.toList() else null,
                                        birthDate = birthDate,
                                        address = address,
                                        gender = selectedGender,
                                        photoUri = selectedImageUri
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = actionState !is ApiResult.Loading
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }

        // Loading overlay
        if (actionState is ApiResult.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        }

        // SnackbarHost di Box root agar align ke BottomCenter bekerja
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) { data -> MannaSnackbar(snackbarData = data) }
    }
}