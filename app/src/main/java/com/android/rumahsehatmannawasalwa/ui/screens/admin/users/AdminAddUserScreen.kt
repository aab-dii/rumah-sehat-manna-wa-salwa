package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddUserScreen(
    navController: NavController,
    viewModel: AdminUserViewModel
) {
    // State Variables
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    // var password is removed, default "rumahsehat123" used
    var phone by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    // Dropdown State
    var roleExpanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Pasien") }
    val roles = listOf("Pasien", "Terapis")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Pengguna") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Icon Profil
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile Icon",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp),
                tint = Color.Gray
            )

            // 2. Form Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Field Removed (Default: rumahsehat123)

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telepon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = job,
                onValueChange = { job = it },
                label = { Text("Pekerjaan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Tanggal Lahir (Read Only + Icon)
            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal Lahir") },
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal")
                },
                modifier = Modifier.fillMaxWidth().clickable {
                    // TODO: Show DatePicker
                },
                enabled = false, // Disable typing, enable click via modifier if needed or use Box
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Dropdown Role
            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = { roleExpanded = !roleExpanded }
            ) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                selectedRole = role
                                roleExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // 4. Tombol Simpan
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                         viewModel.addUser(
                             name = name,
                             email = email,
                             password = "rumahsehat123", // Default Password
                             phone = phone,
                             job = job,
                             birthDate = birthDate.ifBlank { "2000-01-01" }, // Default if empty
                             address = address,
                             role = selectedRole
                         )
                         navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && email.isNotBlank(), // Simple validation
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Simpan", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
