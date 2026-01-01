package com.android.rumahsehatmannawasalwa.ui.screens.profile

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
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
import java.util.Calendar

data class UserProfile(
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val birthDate: String = "",
    val job: String = "",
    val gender: String = "" // "L" or "P"
)

@Composable
fun CompleteProfileScreen(
    email: String = "abdi@gmail.com", // Default dummy for preview/initial
    onSaveClick: (UserProfile) -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Date Picker Logic
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Format: YYYY-MM-DD (Backend compatible)
            // Note: Month is 0-indexed, so we add 1.
            // Using String.format to ensure 2 digits for month and day.
            birthDate = String.format(java.util.Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        bottomBar = {
            // Footer: Tombol Simpan
            Button(
                onClick = {
                    val profile = UserProfile(
                        name = name,
                        phoneNumber = phoneNumber,
                        email = email,
                        address = address,
                        birthDate = birthDate,
                        job = job,
                        gender = gender
                    )
                    onSaveClick(profile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black // Dark Grey/Black as requested
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Simpan", color = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // A. Header
            Text(
                text = "Satu langkah lagi untuk melengkapi data Anda.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(24.dp))

            // B. Form Input

            // 1. Nama Lengkap
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Nomor WhatsApp
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Nomor WhatsApp") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Email (Read-Only)
            OutlinedTextField(
                value = email,
                onValueChange = { }, // Read-only
                label = { Text("Email") },
                enabled = false, // Disabled
                trailingIcon = { Icon(Icons.Default.Lock, contentDescription = "Locked") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Gray,
                    disabledBorderColor = Color.LightGray,
                    disabledLabelColor = Color.Gray,
                    disabledTrailingIconColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Alamat
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 5. Tanggal Lahir (Read-Only, Clickable)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { },
                    label = { Text("Tanggal Lahir") },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                    modifier = Modifier.fillMaxWidth()
                        .clickable { datePickerDialog.show() }, // Make the box clickable
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                // Transparent box overlay to capture click
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { datePickerDialog.show() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 6. Pekerjaan
            OutlinedTextField(
                value = job,
                onValueChange = { job = it },
                label = { Text("Pekerjaan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 7. Jenis Kelamin (Custom Component)
            Text("Jenis Kelamin", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Laki-Laki Button
                GenderButton(
                    text = "Laki-Laki",
                    isSelected = gender == "L",
                    onClick = { gender = "L" },
                    modifier = Modifier.weight(1f)
                )
                
                // Perempuan Button
                GenderButton(
                    text = "Perempuan",
                    isSelected = gender == "P",
                    onClick = { gender = "P" },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Extra Spacer for bottom padding
            Spacer(modifier = Modifier.height(100.dp))
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
    val containerColor = if (isSelected) Color.DarkGray else Color.White
    val contentColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) Color.Transparent else Color.LightGray

    Surface(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompleteProfileScreenPreview() {
    MaterialTheme {
        CompleteProfileScreen()
    }
}
