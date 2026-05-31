package com.android.rumahsehatmannawasalwa.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.Typography
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val oldPasswordFocusRequester = remember { FocusRequester() }
    val newPasswordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isGoogleUser = remember {
        FirebaseAuth.getInstance().currentUser?.providerData?.any { it.providerId == "google.com" } ?: false
    }

    val changePasswordState by viewModel.changePasswordState.collectAsState()
    val isLoading = changePasswordState is ApiResult.Loading
    val context = LocalContext.current

    LaunchedEffect(changePasswordState) {
        when (changePasswordState) {
            is ApiResult.Success -> {
                Toast.makeText(context, "Kata sandi berhasil diubah. Silakan login kembali.", Toast.LENGTH_LONG).show()
                viewModel.resetState()
                navController.navigate("login") {
                    popUpTo(0) // Clear all stack
                }
            }
            is ApiResult.Error -> {
                val msg = (changePasswordState as ApiResult.Error).error
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ubah Kata Sandi", style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }, enabled = !isLoading) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            containerColor = BackgroundLight
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isGoogleUser) {
                    // Tampilan Khusus Pengguna Google
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.android.rumahsehatmannawasalwa.R.drawable.ic_google),
                                contentDescription = "Google",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Login via Google",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            )
                            Text(
                                text = "Kamu login menggunakan akun Google. Untuk mengubah kata sandi, silakan kelola melalui pengaturan keamanan akun Google kamu.",
                                style = Typography.bodyMedium,
                                color = Color(0xFF4B5563),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Pastikan kata sandi baru Anda minimal 8 karakter dan maksimal 64 karakter dengan kombinasi huruf serta angka agar lebih aman.",
                        style = Typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Old Password
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Kata Sandi Saat Ini") },
                        modifier = Modifier.fillMaxWidth().focusRequester(oldPasswordFocusRequester),
                        singleLine = true,
                        enabled = !isLoading,
                        visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            val image = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }, enabled = !isLoading) {
                                Icon(image, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            focusedLabelColor = GreenPrimary
                        )
                    )

                    // New Password
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Kata Sandi Baru") },
                        modifier = Modifier.fillMaxWidth().focusRequester(newPasswordFocusRequester),
                        singleLine = true,
                        enabled = !isLoading,
                        isError = newPassword.length > 64,
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            val image = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }, enabled = !isLoading) {
                                Icon(image, contentDescription = null)
                            }
                        },
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (newPassword.length > 64) "Melebihi batas maksimal" else "Minimal 8, maksimal 64 karakter",
                                    color = if (newPassword.length > 64) MaterialTheme.colorScheme.error else Color.Gray
                                )
                                Text(
                                    text = "${newPassword.length}/64",
                                    color = if (newPassword.length > 64) MaterialTheme.colorScheme.error else Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            focusedLabelColor = GreenPrimary,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error
                        )
                    )

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Konfirmasi Kata Sandi Baru") },
                        modifier = Modifier.fillMaxWidth().focusRequester(confirmPasswordFocusRequester),
                        singleLine = true,
                        enabled = !isLoading,
                        isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }, enabled = !isLoading) {
                                Icon(image, contentDescription = null)
                            }
                        },
                        supportingText = {
                            if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                                Text("Kata sandi tidak cocok", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Harus sama dengan kata sandi baru", color = Color.Gray)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            focusedLabelColor = GreenPrimary,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MannaButton(
                        text = "Simpan Kata Sandi",
                        onClick = {
                            if (oldPassword.isBlank()) {
                                Toast.makeText(context, "Harap lengkapi Kata Sandi Saat Ini", Toast.LENGTH_SHORT).show()
                                oldPasswordFocusRequester.requestFocus()
                                return@MannaButton
                            }
                            if (newPassword.isBlank()) {
                                Toast.makeText(context, "Harap lengkapi Kata Sandi Baru", Toast.LENGTH_SHORT).show()
                                newPasswordFocusRequester.requestFocus()
                                return@MannaButton
                            }
                            if (confirmPassword.isBlank()) {
                                Toast.makeText(context, "Harap lengkapi Konfirmasi Kata Sandi Baru", Toast.LENGTH_SHORT).show()
                                confirmPasswordFocusRequester.requestFocus()
                                return@MannaButton
                            }
                            if (newPassword.length < 8) {
                                Toast.makeText(context, "Kata sandi baru minimal 8 karakter", Toast.LENGTH_SHORT).show()
                                newPasswordFocusRequester.requestFocus()
                                return@MannaButton
                            }
                            if (newPassword.length > 64) {
                                Toast.makeText(context, "Kata sandi baru maksimal 64 karakter", Toast.LENGTH_SHORT).show()
                                newPasswordFocusRequester.requestFocus()
                                return@MannaButton
                            }
                            if (newPassword != confirmPassword) {
                                Toast.makeText(context, "Konfirmasi kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
                                confirmPasswordFocusRequester.requestFocus()
                                return@MannaButton
                            }
                            if (oldPassword == newPassword) {
                                Toast.makeText(context, "Kata sandi baru tidak boleh sama dengan yang lama", Toast.LENGTH_SHORT).show()
                                newPasswordFocusRequester.requestFocus()
                                return@MannaButton
                            }
                            viewModel.changePassword(oldPassword, newPassword)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isLoading
                    )
                }
            }
        }

        if (isLoading) {
            com.android.rumahsehatmannawasalwa.ui.components.ActionOverlay()
        }
    }
}
