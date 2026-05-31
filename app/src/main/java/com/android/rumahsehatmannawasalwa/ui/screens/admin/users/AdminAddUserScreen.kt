package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.auth.RegisterRequest
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.appointment.CapsuleTabRow
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.inputs.MannaTextField
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbar
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbarVisuals
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.SnackbarType
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminAddUserScreen(
    navController: NavController,
    viewModel: AdminUserViewModel,
    currentUserRole: String = "admin" // Sprint 2.1
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Sprint 2.1: Tab "Admin" hanya muncul untuk super_admin
    val isSuperAdmin = currentUserRole == "super_admin"
    val tabs = buildList {
        add("Pasien")
        add("Terapis")
        if (isSuperAdmin) add("Admin")
    }
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf("Laki-laki") }
    var selectedGenderCode by remember { mutableStateOf("L") }
    var job by remember { mutableStateOf("") }
    // Sprint 2.1: Field khusus tab Admin
    var adminPassword by remember { mutableStateOf("") }
    var showSaveConfirm by remember { mutableStateOf(false) }

    val serviceList by viewModel.serviceList.collectAsState()
    val selectedServices = remember { mutableStateListOf<String>() }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1) viewModel.fetchServices()
    }

    val actionState by viewModel.actionState.collectAsState()
    LaunchedEffect(actionState) {
        when (actionState) {
            is ApiResult.Success -> {
                navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_msg", "Pengguna berhasil ditambahkan")
                navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_type", "SUCCESS")
                viewModel.resetActionState()
                navController.popBackStack()
            }
            is ApiResult.Error -> {
                val error = (actionState as ApiResult.Error).error
                scope.launch {
                    snackbarHostState.showSnackbar(
                        MannaSnackbarVisuals(
                            message = error,
                            type = SnackbarType.ERROR
                        )
                    )
                }
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, y, m, d ->
            birthDate = "$y-${(m + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Box paling luar sebagai root agar SnackbarHost bisa align ke BottomCenter
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
                title = if (pagerState.currentPage == 2 && isSuperAdmin) "Tambah Admin" else "Tambah Pengguna",
                subtitle = "Daftarkan pasien atau terapis baru ke sistem",
                onBackClick = { navController.popBackStack() },
                transparentBackground = true,
                hideBackground = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            MannaSheet(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {

                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        CapsuleTabRow(
                            tabs = tabs,
                            pagerState = pagerState,
                            onTabSelected = { index ->
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                        )
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            MannaTextField(
                                label = "Nama Lengkap",
                                value = name,
                                onValueChange = { name = it },
                                placeholder = "Masukkan nama lengkap",
                                leadingIcon = Icons.Outlined.Person
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            MannaTextField(
                                label = "Email",
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "Masukkan alamat email",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                leadingIcon = Icons.Outlined.Mail
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            MannaTextField(
                                label = "Nomor WhatsApp",
                                value = phone,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) {
                                        phone = input
                                    }
                                },
                                placeholder = "Contoh: 08123456789",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = Icons.Default.Phone
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (page == 0) {
                                // Tab Pasien: field pekerjaan
                                MannaTextField(
                                    label = "Pekerjaan",
                                    value = job,
                                    onValueChange = { job = it },
                                    placeholder = "Masukkan pekerjaan",
                                    leadingIcon = Icons.Outlined.Work
                                )
                            } else if (page == 1) {
                                // Tab Terapis: spesialisasi
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
                            } else if (page == 2 && isSuperAdmin) {
                                // Sprint 2.1: Tab Admin — field password
                                MannaTextField(
                                    label = "Password",
                                    value = adminPassword,
                                    onValueChange = { adminPassword = it },
                                    placeholder = "Minimal 8 karakter",
                                    leadingIcon = Icons.Default.Lock
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            ExposedDropdownMenuBox(
                                expanded = genderExpanded,
                                onExpandedChange = { genderExpanded = !genderExpanded }
                            ) {
                                MannaTextField(
                                    label = "Jenis Kelamin",
                                    value = selectedGender,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = "Pilih jenis kelamin",
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                                    },
                                    leadingIcon = Icons.Outlined.Person,
                                    modifier = Modifier.menuAnchor()
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

                            MannaTextField(
                                label = "Tanggal Lahir",
                                value = if (birthDate.isNotBlank())
                                    FormatterUtils.formatDateHuman(birthDate) else "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = "Pilih tanggal lahir",
                                trailingIcon = {
                                    IconButton(onClick = { datePickerDialog.show() }) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = GreenPrimary
                                        )
                                    }
                                },
                                leadingIcon = Icons.Default.CalendarToday,
                                modifier = Modifier.clickable { datePickerDialog.show() }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            MannaTextField(
                                label = "Alamat",
                                value = address,
                                onValueChange = { address = it },
                                placeholder = "Masukkan alamat lengkap",
                                leadingIcon = Icons.Default.LocationOn,
                                singleLine = false
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            // Sprint 2.1: Validasi per tab
                            val isFormValid = when (page) {
                                0 -> name.isNotBlank() && email.isNotBlank() && job.isNotBlank()
                                1 -> name.isNotBlank() && email.isNotBlank() && selectedServices.isNotEmpty()
                                2 -> name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && adminPassword.length >= 8
                                else -> false
                            }

                            MannaButton(
                                text = "Simpan Data",
                                onClick = { showSaveConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isFormValid && actionState !is ApiResult.Loading
                            )

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }

        // Loading overlay
        if (actionState is ApiResult.Loading) {
            com.android.rumahsehatmannawasalwa.ui.components.ActionOverlay()
        }

        // SnackbarHost di dalam Box root agar align ke BottomCenter bisa bekerja
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) { data -> MannaSnackbar(snackbarData = data) }

        // Confirmation Dialog
        CustomConfirmDialog(
            show = showSaveConfirm,
            title = "Simpan Pengguna",
            description = "Apakah Anda yakin data yang dimasukkan sudah benar?",
            onConfirm = {
                showSaveConfirm = false
                // Sprint 2.1: Role berdasarkan tab aktif
                val selectedRole = when (pagerState.currentPage) {
                    0 -> "pasien"
                    1 -> "terapis"
                    2 -> "admin"
                    else -> "pasien"
                }
                viewModel.addUser(
                    name = name,
                    email = email,
                    phone = phone,
                    role = selectedRole,
                    job = if (pagerState.currentPage == 0) job else "",
                    specialization = if (pagerState.currentPage == 1) selectedServices.toList() else null,
                    address = address,
                    birthDate = birthDate,
                    gender = selectedGenderCode,
                    password = if (pagerState.currentPage == 2) adminPassword else null
                )
            },
            onDismiss = { showSaveConfirm = false }
        )
    }
}