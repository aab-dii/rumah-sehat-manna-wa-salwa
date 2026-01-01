package com.android.rumahsehatmannawasalwa.ui.screens.auth

import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.data.ApiResult

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    // --- State Variables ---
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") } // "Laki-laki" atau "Perempuan"

    val authStateState = viewModel.authState.collectAsState()
    val authState = authStateState.value
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- Logic Date Picker ---
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            birthDate = "$dayOfMonth-${month + 1}-$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // --- Logic Observasi State (Sukses/Gagal) ---
    LaunchedEffect(authState) {
        when (authState) {
            is ApiResult.Success -> {
                Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_LONG).show()
                viewModel.resetState()
                navController.popBackStack()
            }
            is ApiResult.Error -> {
                Toast.makeText(context, (authState as ApiResult.Error).error, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // --- UI Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState), // Agar bisa di-scroll
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Daftar", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Nama Lengkap
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 2. Nomor WhatsApp
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Nomor WhatsApp") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3. Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 4. Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 5. Ulangi Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Ulangi Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 6. Alamat
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 7. Tanggal Lahir (Read Only - Click to Open Calendar)
        OutlinedTextField(
            value = birthDate,
            onValueChange = { },
            label = { Text("Tanggal Lahir") },
            readOnly = true, // Tidak bisa diketik manual
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() } // Klik field juga buka kalender
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 8. Pekerjaan
        OutlinedTextField(
            value = job,
            onValueChange = { job = it },
            label = { Text("Pekerjaan") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 9. Jenis Kelamin (Radio Button)
        Text(text = "Jenis Kelamin", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.Start))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = gender == "Laki-laki",
                onClick = { gender = "Laki-laki" }
            )
            Text("Laki-laki", modifier = Modifier.clickable { gender = "Laki-laki" })

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = gender == "Perempuan",
                onClick = { gender = "Perempuan" }
            )
            Text("Perempuan", modifier = Modifier.clickable { gender = "Perempuan" })
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Tombol Daftar ---
        Button(
            onClick = {
                viewModel.register(
                    name, email, phone, password, confirmPassword,
                    job, birthDate, address, gender
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = authState !is ApiResult.Loading
        ) {
            if (authState is ApiResult.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Daftar Sekarang")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Sudah punya akun? Masuk")
        }

        // Spacer bawah agar scroll tidak mentok
        Spacer(modifier = Modifier.height(50.dp))
    }
}