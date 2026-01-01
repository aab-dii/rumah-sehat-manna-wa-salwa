package com.android.rumahsehatmannawasalwa.ui.screens.auth

import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


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

    // 1. Konfigurasi Google Sign In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // Ini otomatis digenerate dari google-services.json
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // 2. Launcher untuk menangkap hasil login Google
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Ambil token dari Google
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    // Kirim token ke ViewModel
                    viewModel.signInWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Sign In Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Cek Status Login
    LaunchedEffect(authState) {
        when (authState) {
            is ApiResult.Success -> {
                // Ignore success for reset password here to avoid navigation loop if needed, 
                // but usually reset password success is short lived.
                // However, viewModel.login ALSO sets Success.
                // Ideally distinguish them or just handle success generally.
                // Since this check navigates, we should ensure it's login success.
                // But simplified: ResetPassword also sets Success(Unit).
                // Let's add a check if we are in Login flow? 
                // Or clearer: Show toast for success if dialog was open.
                
                if (showForgotPasswordDialog) {
                    showForgotPasswordDialog = false
                    Toast.makeText(context, "Link reset password telah dikirim ke email Anda.", Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                } else {
                    // Login Success
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
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Logo Placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(2f),
                color = Color.LightGray.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            ) {
                 Box(contentAlignment = Alignment.Center) {
                    Text("LOGO", color = Color.Gray, style = MaterialTheme.typography.titleLarge)
                 }
            }
            Spacer(modifier = Modifier.height(48.dp))

            // 2. Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("Masukkan Email Anda") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.small
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("Masukkan Password Anda") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                }
            )
            
            // 4. Lupa Password
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    Text("Lupa Password?", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 5. Tombol Masuk
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = authState !is ApiResult.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (authState is ApiResult.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Masuk", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Tombol Google
            Button(
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        val signInIntent = googleSignInClient.signInIntent
                        googleLauncher.launch(signInIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        tint = Color.Unspecified, // Gunakan warna asli icon
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Masuk dengan Google", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 7. Daftar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Belum punya akun? ")
                Text(
                    text = "Daftar",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable { navController.navigate("register") }
                )
            }
        }
    }
    
    if (showForgotPasswordDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showForgotPasswordDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                 Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Lupa Password", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Masukkan email Anda untuk menerima link reset password.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showForgotPasswordDialog = false }) {
                            Text("Batal")
                        }
                        TextButton(onClick = { 
                            viewModel.resetPassword(forgotPasswordEmail)
                            forgotPasswordEmail = "" // Clear field
                        }) {
                            Text("Kirim")
                        }
                    }
                 }
            }
        }
    }
}