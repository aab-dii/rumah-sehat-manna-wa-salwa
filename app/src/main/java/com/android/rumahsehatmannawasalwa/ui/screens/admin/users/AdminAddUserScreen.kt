package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddUserScreen(
    navController: NavController,
    viewModel: AdminUserViewModel
) {
    // Context for DatePicker & Toast
    val context = LocalContext.current
    
    // --- State Variables ---
    // Mode: 0 = Pasien, 1 = Terapis
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // Form Fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") } // Used for Pasien 'Pekerjaan'
    var birthDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    // Gender
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf("Laki-laki") } // Default Display
    var selectedGenderCode by remember { mutableStateOf("L") } // Value: L/P
    
    // Specialization (Therapist Only)
    val serviceList by viewModel.serviceList.collectAsState()
    val selectedServices = remember { mutableStateListOf<String>() }

    // Fetch Services if Therapist tab selected
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) { // Terapis
            viewModel.fetchServices()
        }
    }

    // --- Action State Observer ---
    val actionState by viewModel.actionState.collectAsState()
    
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is com.android.rumahsehatmannawasalwa.data.ApiResult.Success -> {
                Toast.makeText(context, "User Berhasil Ditambahkan!", Toast.LENGTH_SHORT).show()
                viewModel.resetActionState()
                navController.popBackStack()
            }
            is com.android.rumahsehatmannawasalwa.data.ApiResult.Error -> {
                Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    // --- Date Picker Logic ---
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, y, m, d ->
            val formattedDate = "$y-${(m + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
            birthDate = formattedDate
        },
        year, month, day
    )

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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TABS: Pasien vs Terapis
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Pasien") },
                        selectedContentColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary,
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Terapis") },
                        selectedContentColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary,
                        unselectedContentColor = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // 2. FORM FIELDS
                
                // --- Shared Fields ---
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

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- Role Specific Fields ---
                if (selectedTabIndex == 0) {
                    // PASIEN: Pekerjaan (Manual Input)
                    OutlinedTextField(
                        value = job,
                        onValueChange = { job = it },
                        label = { Text("Pekerjaan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    // TERAPIS: Spesialisasi (Multi-select Checkbox)
                    Text("Spesialisasi (Layanan)", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
                    
                    if (serviceList.isEmpty()) {
                        Text("Memuat layanan...", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Column {
                            serviceList.forEach { service ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (selectedServices.contains(service.nama)) {
                                                selectedServices.remove(service.nama)
                                            } else {
                                                selectedServices.add(service.nama)
                                            }
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = selectedServices.contains(service.nama),
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) selectedServices.add(service.nama)
                                            else selectedServices.remove(service.nama)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
                                        )
                                    )
                                    Text(
                                        text = service.nama,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- Gender Dropdown ---
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = !genderExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedGender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis Kelamin") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Laki-laki") },
                            onClick = {
                                selectedGender = "Laki-laki"
                                selectedGenderCode = "L"
                                genderExpanded = false
                            }
                        )
                         DropdownMenuItem(
                            text = { Text("Perempuan") },
                            onClick = {
                                selectedGender = "Perempuan"
                                selectedGenderCode = "P"
                                genderExpanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // --- Date Picker ---
                Box {
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = {},
                        readOnly = true, // Prevent keyboard
                        label = { Text("Tanggal Lahir") },
                        placeholder = { Text("YYYY-MM-DD") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = true, // Must be true for clicks
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    // Invisible overlay to capture clicks anywhere on the field
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { datePickerDialog.show() }
                    )
                }
                // Overlay invisible box to capture clicks on disabled field if needed, 
                // but usually trailing icon or enabled=false + clickable modifier on parent Box works better. 
                // Let's use a Clickable Box over it or rely on TrailingIcon.
                // Better approach for Material3: Use `interactionSource` or just make it ReadOnly (enabled=true) but ignore input.
                // Fixed approach below:
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                // 3. Action Button
                Button(
                    onClick = {
                        val role = if (selectedTabIndex == 0) "Pasien" else "Terapis"
                        
                        val jobValue = if (selectedTabIndex == 0) job else ""
                        val specValue = if (selectedTabIndex == 1 && selectedServices.isNotEmpty()) selectedServices.toList() else null
                        
                        viewModel.addUser(
                            name = name,
                            email = email,
                            password = "rumahsehat123", // Default Password
                            phone = phone,
                            job = jobValue,
                            specialization = specValue,
                            birthDate = birthDate.ifBlank { "2000-01-01" },
                            address = address,
                            role = role,
                            gender = selectedGenderCode
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    // Validation: Required fields must be filled
                    enabled = name.isNotBlank() && email.isNotBlank() && 
                              (if (selectedTabIndex == 0) job.isNotBlank() else selectedServices.isNotEmpty()),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
                    )
                ) {
                    Text(text = "Simpan Data", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            
            // Loading Overlay
            if (actionState is com.android.rumahsehatmannawasalwa.data.ApiResult.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary)
                }
            }
        }
    }
}
