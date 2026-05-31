package com.android.rumahsehatmannawasalwa.ui.screens.profile

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.inputs.MannaTextField
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import java.util.*

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

// 1. Data Class untuk menampung input
data class UserProfile(
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val birthDate: String = "",
    val job: String = "",
    val gender: String = ""
)

// --- 2. STATEFUL WRAPPER (Panggil ini di AppNavGraph) ---
@Composable
fun CompleteProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    isFromAuth: Boolean = false
) {
    val context = LocalContext.current
    val user by viewModel.currentUserData.collectAsState()
    val authState by viewModel.authState.collectAsState()

    var hasInitiatedSave by remember { mutableStateOf(false) }


    val isLoading = authState is ApiResult.Loading

    LaunchedEffect(authState, hasInitiatedSave) {
        if (hasInitiatedSave) {
            when (authState) {
                is ApiResult.Success -> {
                    if (isFromAuth) {
                        // Jika dari login/registrasi, langsung ke Home
                        navController.navigate(Screen.PatientHome.route) {
                            popUpTo(Screen.CompleteProfile.route) { inclusive = true }
                        }
                    } else {
                        // Jika dari Ubah Data, kembali ke Detail Profil dengan snackbar
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("profile_updated", true)
                        
                        navController.popBackStack()
                    }
                    viewModel.resetState()
                    hasInitiatedSave = false
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, (authState as ApiResult.Error).error, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                    hasInitiatedSave = false
                }
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CompleteProfileContent(
            user = user,
            isLoading = isLoading,
            isFromAuth = isFromAuth,
            onSaveClick = { profile ->
                hasInitiatedSave = true
                viewModel.updateUserProfile(
                    name = profile.name,
                    phone = profile.phoneNumber,
                    job = profile.job,
                    address = profile.address,
                    birthDate = profile.birthDate,
                    gender = profile.gender,
                    fotoUrl = user?.fotoUrl // Sertakan foto URL yang sudah ada
                )
            },
            onBackClick = { if (!isLoading) navController.popBackStack() }
        )

        if (isLoading) {
            com.android.rumahsehatmannawasalwa.ui.components.ActionOverlay()
        }
    }
}

@Composable
fun CompleteProfileContent(
    user: User?,
    isLoading: Boolean = false,
    isFromAuth: Boolean = false,
    onSaveClick: (UserProfile) -> Unit,
    onBackClick: () -> Unit
) {
    val nameFocusRequester = remember { FocusRequester() }
    val phoneFocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }
    val jobFocusRequester = remember { FocusRequester() }

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var phoneNumber by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }
    var address by remember(user) { mutableStateOf(user?.address ?: "") }
    var birthDate by remember(user) { mutableStateOf(if (!user?.birthDate.isNullOrBlank()) user!!.birthDate.substring(0, 10).takeIf { it.length == 10 } ?: "" else "") }
    var job by remember(user) { mutableStateOf(user?.job ?: "") }
    var gender by remember(user) { mutableStateOf(user?.gender ?: "") }

    // Error States
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var jobError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            birthDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
            if (birthDateError != null) birthDateError = null
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    0.25f to GreenDark,
                    0.50f to GreenLight,
                    1.0f to GreenLight
                )
            )
            .statusBarsPadding()
    ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. HEADER GRADIENT
            TopBar(
                title = if (isFromAuth) "Lengkapi Profil" else "Ubah Profil",
                onBackClick = if (isFromAuth) null else onBackClick, // Sembunyikan tombol back dengan mengirim null
                transparentBackground = true,
                hideBackground = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. KONTEN SHEET PUTIH
            MannaSheet(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Form Fields
                    MannaTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            if (nameError != null) nameError = null 
                        },
                        label = "Nama Lengkap",
                        placeholder = "Masukkan nama lengkap",
                        leadingIcon = Icons.Outlined.Person,
                        isError = nameError != null,
                        errorMessage = nameError,
                        focusRequester = nameFocusRequester
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MannaTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                phoneNumber = input
                                if (phoneError != null) phoneError = null
                            }
                        },
                        label = "Nomor WhatsApp",
                        placeholder = "Contoh: 08123456789",
                        leadingIcon = Icons.Default.Phone,
                        isError = phoneError != null,
                        errorMessage = phoneError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        focusRequester = phoneFocusRequester
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MannaTextField(
                        value = user?.email ?: "",
                        onValueChange = { },
                        label = "Email",
                        placeholder = "",
                        readOnly = true,
                        enabled = false,
                        leadingIcon = Icons.Outlined.Mail
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MannaTextField(
                        value = address,
                        onValueChange = { 
                            address = it 
                            if (addressError != null) addressError = null
                        },
                        label = "Alamat",
                        placeholder = "Masukkan alamat",
                        leadingIcon = Icons.Default.LocationOn,
                        isError = addressError != null,
                        errorMessage = addressError,
                        focusRequester = addressFocusRequester
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Date Picker Field
                    Box(modifier = Modifier.fillMaxWidth()) {
                        MannaTextField(
                            value = birthDate,
                            onValueChange = { },
                            label = "Tanggal Lahir",
                            placeholder = "Pilih tanggal lahir",
                            readOnly = true,
                            enabled = false,
                            leadingIcon = Icons.Default.CalendarToday,
                            isError = birthDateError != null,
                            errorMessage = birthDateError
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { if (!isLoading) datePickerDialog.show() }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    MannaTextField(
                        value = job,
                        onValueChange = { 
                            job = it 
                            if (jobError != null) jobError = null
                        },
                        label = "Pekerjaan",
                        placeholder = "Masukkan pekerjaan",
                        leadingIcon = Icons.Outlined.Work,
                        isError = jobError != null,
                        errorMessage = jobError,
                        focusRequester = jobFocusRequester
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Gender Selection
                    Text(
                        "Jenis Kelamin",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (genderError != null) MaterialTheme.colorScheme.error else SlateText
                        )
                    )
                    if (genderError != null) {
                        Text(
                            text = genderError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenderButton(
                            text = "Laki-Laki",
                            isSelected = gender == "L",
                            onClick = { 
                                gender = "L" 
                                if (genderError != null) genderError = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                        GenderButton(
                            text = "Perempuan",
                            isSelected = gender == "P",
                            onClick = { 
                                gender = "P" 
                                if (genderError != null) genderError = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    MannaButton(
                        text = if (isFromAuth) "Simpan Data" else "Simpan Perubahan",
                        onClick = {
                            var isValid = true
                            var firstInvalidRequester: FocusRequester? = null
                            
                            if (name.isBlank()) {
                                nameError = "Nama lengkap wajib diisi"
                                if (firstInvalidRequester == null) firstInvalidRequester = nameFocusRequester
                            }
                            
                            if (phoneNumber.isBlank()) {
                                phoneError = "Nomor WhatsApp wajib diisi"
                                if (firstInvalidRequester == null) firstInvalidRequester = phoneFocusRequester
                            } else if (!phoneNumber.all { it.isDigit() }) {
                                phoneError = "Nomor WhatsApp harus berupa angka"
                                if (firstInvalidRequester == null) firstInvalidRequester = phoneFocusRequester
                            }
                            
                            if (address.isBlank()) {
                                addressError = "Alamat wajib diisi"
                                if (firstInvalidRequester == null) firstInvalidRequester = addressFocusRequester
                            }
                            
                            if (birthDate.isBlank()) {
                                birthDateError = "Tanggal lahir wajib dipilih"
                            }
                            
                            if (job.isBlank()) {
                                jobError = "Pekerjaan wajib diisi"
                                if (firstInvalidRequester == null) firstInvalidRequester = jobFocusRequester
                            }
                            
                            if (gender.isBlank()) {
                                genderError = "Jenis kelamin wajib dipilih"
                            }

                            if (firstInvalidRequester != null) {
                                isValid = false
                                firstInvalidRequester.requestFocus()
                            }

                            if (isValid) {
                                onSaveClick(UserProfile(name, phoneNumber, user?.email ?: "", address, birthDate, job, gender))
                            }
                        },
                        isLoading = isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
        }
    }
}

@Composable
fun GenderButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(50),
        color = if (isSelected) GreenPrimary else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, GreenPrimary),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) Color.White else GreenPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}