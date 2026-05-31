package com.android.rumahsehatmannawasalwa.ui.screens.auth

import android.app.Activity
import com.android.rumahsehatmannawasalwa.R
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.components.inputs.MannaTextField
import com.android.rumahsehatmannawasalwa.ui.components.buttons.*
import com.android.rumahsehatmannawasalwa.ui.components.layouts.AuthSheet
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.*
import com.android.rumahsehatmannawasalwa.ui.theme.*

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    // --- Focus Requesters ---
    val nameFocusRequester = remember { FocusRequester() }
    val phoneFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }
    val jobFocusRequester = remember { FocusRequester() }

    // --- State Variables ---
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Laki-laki") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Error States
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var jobError by remember { mutableStateOf<String?>(null) }

    val authStateState = viewModel.authState.collectAsState()
    val authState = authStateState.value
    val isLoading = authState is ApiResult.Loading
    val context = LocalContext.current
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- Google Sign In Logic (Similar to Login) ---
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(
                            message = "Google Sign In Gagal: ${e.message}",
                            type = SnackbarType.ERROR
                        )
                    )
                }
            }
        }
    }

    // --- Logic Date Picker ---
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            birthDate = String.format(java.util.Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
            if (birthDateError != null) birthDateError = null
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // --- Logic Observasi State (Sukses/Gagal) ---
    LaunchedEffect(authState) {
        when (authState) {
            is ApiResult.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(
                            message = "Akun berhasil dibuat! Silakan masuk.",
                            type = SnackbarType.SUCCESS
                        )
                    )
                    // Pindah ke login setelah snackbar muncul
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
                viewModel.resetState()
            }
            is ApiResult.Error -> {
                val errorMsg = (authState as ApiResult.Error).error
                scope.launch {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(
                            message = errorMsg,
                            type = SnackbarType.ERROR
                        )
                    )
                }
                viewModel.resetState()
            }
            else -> {}
        }
    }

    AuthSheet(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                MannaSnackbar(snackbarData = data)
            }
        },
        logo = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
        },
        content = {
            // Header
            Text(
                text = "Daftarkan akun anda",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextDark
                )
            )
            Text(
                text = "Silakan daftar akun Anda",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BodyGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Nama Lengkap
            MannaTextField(
                label = "Nama Lengkap",
                value = name,
                onValueChange = { 
                    name = it 
                    if (nameError != null) nameError = null
                },
                placeholder = "Nama Lengkap",
                leadingIcon = Icons.Default.AccountCircle,
                enabled = !isLoading,
                isError = nameError != null,
                errorMessage = nameError,
                focusRequester = nameFocusRequester
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Nomor WhatsApp
            MannaTextField(
                label = "Nomor WhatsApp",
                value = phone,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        phone = input
                        if (phoneError != null) phoneError = null
                    }
                },
                placeholder = "Contoh: 08123456789",
                leadingIcon = Icons.Default.Phone,
                enabled = !isLoading,
                isError = phoneError != null,
                errorMessage = phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                focusRequester = phoneFocusRequester
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Email
            MannaTextField(
                label = "Email",
                value = email,
                onValueChange = { 
                    email = it 
                    if (emailError != null) emailError = null
                },
                placeholder = "nama@email.com",
                leadingIcon = Icons.Default.Email,
                enabled = !isLoading,
                isError = emailError != null,
                errorMessage = emailError,
                focusRequester = emailFocusRequester
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Password
            MannaTextField(
                label = "Password",
                value = password,
                onValueChange = { 
                    password = it 
                    // Validasi real-time: reset error manual, lalu cek batas
                    passwordError = when {
                        it.length > 64 -> "Password maksimal 64 karakter"
                        else -> null
                    }
                },
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                enabled = !isLoading,
                isError = passwordError != null,
                errorMessage = passwordError,
                supportingText = "Minimal 8, maksimal 64 karakter",
                maxLength = 64,
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        enabled = !isLoading
                    ) {
                        Icon(imageVector = image, contentDescription = null, tint = BodyGray)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                focusRequester = passwordFocusRequester
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Ulangi Password
            MannaTextField(
                label = "Ulangi Password",
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it 
                    if (confirmPasswordError != null) confirmPasswordError = null
                },
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                enabled = !isLoading,
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                supportingText = "Harus sama dengan password di atas",
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(
                        onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                        enabled = !isLoading
                    ) {
                        Icon(imageVector = image, contentDescription = null, tint = BodyGray)
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                focusRequester = confirmPasswordFocusRequester
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Alamat
            MannaTextField(
                label = "Alamat",
                value = address,
                onValueChange = { 
                    address = it 
                    if (addressError != null) addressError = null
                },
                placeholder = "Masukkan alamat lengkap",
                leadingIcon = Icons.Default.LocationOn,
                enabled = !isLoading,
                isError = addressError != null,
                errorMessage = addressError,
                focusRequester = addressFocusRequester
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 7. Pekerjaan
            MannaTextField(
                label = "Pekerjaan",
                value = job,
                onValueChange = { 
                    job = it 
                    if (jobError != null) jobError = null
                },
                placeholder = "Pekerjaan",
                leadingIcon = Icons.Default.Work,
                enabled = !isLoading,
                isError = jobError != null,
                errorMessage = jobError,
                focusRequester = jobFocusRequester
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 8. Tanggal Lahir
            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading) { datePickerDialog.show() }
            ) {
                MannaTextField(
                    label = "Tanggal Lahir",
                    value = birthDate,
                    onValueChange = { },
                    placeholder = "YYYY-MM-DD",
                    leadingIcon = Icons.Default.CalendarToday,
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    isError = birthDateError != null,
                    errorMessage = birthDateError,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = SlateTextDark,
                        disabledBorderColor = if (birthDateError != null) MaterialTheme.colorScheme.error else GreenLight,
                        disabledLabelColor = if (birthDateError != null) MaterialTheme.colorScheme.error else GreenPrimary,
                        disabledLeadingIconColor = if (birthDateError != null) MaterialTheme.colorScheme.error else GreenPrimary,
                        disabledPlaceholderColor = BodyGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 9. Jenis Kelamin
            Text(
                text = "Jenis Kelamin",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SlateTextDark
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { gender = "Laki-laki" },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (gender == "Laki-laki") GreenPrimary else DividerLight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (gender == "Laki-laki") GreenPrimary.copy(alpha = 0.05f) else Color.Transparent,
                        contentColor = if (gender == "Laki-laki") GreenPrimary else BodyGray
                    )
                ) {
                    Text("Laki-laki", fontWeight = if (gender == "Laki-laki") FontWeight.Bold else FontWeight.Normal)
                }
                OutlinedButton(
                    onClick = { gender = "Perempuan" },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (gender == "Perempuan") GreenPrimary else DividerLight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (gender == "Perempuan") GreenPrimary.copy(alpha = 0.05f) else Color.Transparent,
                        contentColor = if (gender == "Perempuan") GreenPrimary else BodyGray
                    )
                ) {
                    Text("Perempuan", fontWeight = if (gender == "Perempuan") FontWeight.Bold else FontWeight.Normal)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Daftar
            MannaButton(
                text = "Daftar",
                onClick = {
                    var isValid = true
                    var firstInvalidRequester: FocusRequester? = null
                    
                    if (name.isBlank()) {
                        nameError = "Nama lengkap wajib diisi"
                        if (firstInvalidRequester == null) firstInvalidRequester = nameFocusRequester
                    }
                    
                    if (phone.isBlank()) {
                        phoneError = "Nomor WhatsApp wajib diisi"
                        if (firstInvalidRequester == null) firstInvalidRequester = phoneFocusRequester
                    } else if (!phone.all { it.isDigit() }) {
                        phoneError = "Nomor WhatsApp harus berupa angka"
                        if (firstInvalidRequester == null) firstInvalidRequester = phoneFocusRequester
                    }
                    
                    if (email.isBlank()) {
                        emailError = "Email wajib diisi"
                        if (firstInvalidRequester == null) firstInvalidRequester = emailFocusRequester
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Format email tidak valid"
                        if (firstInvalidRequester == null) firstInvalidRequester = emailFocusRequester
                    }
                    
                    if (password.isBlank()) {
                        passwordError = "Password wajib diisi"
                        if (firstInvalidRequester == null) firstInvalidRequester = passwordFocusRequester
                    } else if (password.length < 8) {
                        passwordError = "Password minimal 8 karakter"
                        if (firstInvalidRequester == null) firstInvalidRequester = passwordFocusRequester
                    } else if (password.length > 64) {
                        passwordError = "Password maksimal 64 karakter"
                        if (firstInvalidRequester == null) firstInvalidRequester = passwordFocusRequester
                    }
                    
                    if (confirmPassword != password) {
                        confirmPasswordError = "Password tidak cocok"
                        if (firstInvalidRequester == null) firstInvalidRequester = confirmPasswordFocusRequester
                    }
                    
                    if (address.isBlank()) {
                        addressError = "Alamat wajib diisi"
                        if (firstInvalidRequester == null) firstInvalidRequester = addressFocusRequester
                    }
                    
                    if (birthDate.isBlank()) {
                        birthDateError = "Tanggal lahir wajib diisi"
                    }
                    
                    if (job.isBlank()) {
                        jobError = "Pekerjaan wajib diisi"
                        if (firstInvalidRequester == null) firstInvalidRequester = jobFocusRequester
                    }

                    if (firstInvalidRequester != null) {
                        isValid = false
                        firstInvalidRequester.requestFocus()
                    }

                    if (isValid) {
                        viewModel.register(
                            name, email, phone, password, confirmPassword,
                            job, birthDate, address, gender
                        )
                    }
                },
                isLoading = isLoading,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hubungkan dengan Google
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = DividerLight)
                Text("  ATAU  ", style = MaterialTheme.typography.labelMedium.copy(color = GrayText))
                HorizontalDivider(modifier = Modifier.weight(1f), color = DividerLight)
            }

            MannaOutlinedButton(
                text = "Lanjutkan dengan Google",
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        val signInIntent = googleSignInClient.signInIntent
                        googleLauncher.launch(signInIntent)
                    }
                },
                enabled = !isLoading,
                borderColor = GreenLight,
                contentColor = SlateTextDark,
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Footer
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sudah punya akun? ", color = BodyGray)
                    Text(
                        text = "Masuk",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold, 
                            color = GreenPrimary
                        ),
                        modifier = if (isLoading) Modifier else Modifier.clickable { navController.navigate("login") }
                    )
                }
            }
        }
    )
}
