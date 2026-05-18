package com.android.rumahsehatmannawasalwa.ui.screens.auth

import com.android.rumahsehatmannawasalwa.R
import com.android.rumahsehatmannawasalwa.ui.components.inputs.MannaTextField
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaOutlinedButton
import com.android.rumahsehatmannawasalwa.ui.components.layouts.AuthSheet
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.*
import com.android.rumahsehatmannawasalwa.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import android.util.Log

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    val authStateState = viewModel.authState.collectAsState()
    val authState = authStateState.value
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1. Konfigurasi Google Sign In (keep existing)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // 2. Launcher untuk menangkap hasil login Google (keep existing)
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

    // Cek Status Login
    LaunchedEffect(authState) {
        when (authState) {
            is ApiResult.Success -> {
                if (showForgotPasswordDialog) {
                    showForgotPasswordDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            MannaSnackbarVisuals(
                                message = "Link reset password telah dikirim ke email Anda.",
                                type = SnackbarType.SUCCESS
                            )
                        )
                    }
                    viewModel.resetState()
                } else {
                    Log.d("LoginScreen", "Login State: Success")
                    viewModel.resetState() 
                    navController.navigate("dispatch") {
                        popUpTo("login") { inclusive = true } 
                    }
                }
            }
            is ApiResult.Error -> {
                val errorMsg = (authState as ApiResult.Error).error
                Log.e("LoginScreen", "Login State: Error -> $errorMsg")
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
            // Placeholder Logo (Dua daun)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
        },
        content = {
            // Header
            Text(
                text = "Selamat Datang Kembali",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    color = SlateTextDark
                )
            )
            Text(
                text = "Silakan masuk ke akun Anda",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BodyGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Form
            MannaTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "nama@email.com",
                leadingIcon = Icons.Default.Email,
                isError = authState is ApiResult.Error && (authState as ApiResult.Error).error.contains("Email", ignoreCase = true)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password Form
            MannaTextField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                isError = authState is ApiResult.Error && (authState as ApiResult.Error).error.contains("Password", ignoreCase = true),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }) {
                        Icon(
                            imageVector = image,
                            contentDescription = null,
                            tint = BodyGray
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            // Lupa Password
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd) {
                TextButton(
                    onClick = { showForgotPasswordDialog = true
                    }) {
                    Text(
                       text = "Lupa Password?",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = GreenPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Masuk
            MannaButton(
                text = "Masuk",
                onClick = { viewModel.login(email, password) },
                isLoading = authState is ApiResult.Loading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Hubungkan dengan Google
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DividerLight)
                Text(
                    text = "  ATAU  ",
                    style = MaterialTheme.typography.labelMedium.copy(color = GrayText))
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DividerLight
                )
            }

            MannaOutlinedButton(
                text = "Lanjutkan dengan Google",
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        val signInIntent = googleSignInClient.signInIntent
                        googleLauncher.launch(signInIntent)
                    }
                },
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
                    Text("Belum punya akun? ", color = BodyGray)
                    Text(
                        text = "Daftar Sekarang",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, 
                            color = GreenPrimary
                        ),
                        modifier = Modifier.clickable { navController.navigate("register") }
                    )
                }
            }
        }
    )
    
    if (showForgotPasswordDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {
            showForgotPasswordDialog = false
        }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                 Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Lupa Password",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateTextDark
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Masukkan email Anda untuk menerima link reset password.\nCek folder spam jika tidak menerima email. Link berlaku selama 1 jam.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = BodyGray)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    MannaTextField(
                        label = "Email",
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        placeholder = "nama@email.com",
                        leadingIcon = Icons.Default.Email
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showForgotPasswordDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(25.dp),
                            border = BorderStroke(1.dp, DividerLight)
                        ) {
                            Text("Batal", color = BodyGray)
                        }
                        
                        MannaButton(
                            text = "Kirim",
                            onClick = { 
                                if (forgotPasswordEmail.isNotBlank()) {
                                    viewModel.resetPassword(forgotPasswordEmail)
                                    forgotPasswordEmail = ""
                                }
                            },
                            modifier = Modifier.weight(1f),
                            isLoading = authState is ApiResult.Loading
                        )
                    }
                 }
            }
        }
    }
}