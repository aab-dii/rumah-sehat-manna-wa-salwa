package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.theme.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import com.android.rumahsehatmannawasalwa.ui.components.auth.ProfilePhoto
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.inputs.MannaTextField
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    navController: NavController,
    viewModel: AdminUserViewModel,
    userId: Int,
    currentUserRole: String = "admin",   // Sprint 2.1
    currentUserId: Int = 0               // Sprint 2.1
) {

    LaunchedEffect(userId) {
        val currentState = viewModel.userDetailState.value
        if (currentState !is ApiResult.Success || currentState.data.id != userId) {
            viewModel.fetchUserDetail(userId)
        }
    }

    val userState by viewModel.userDetailState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val resetPasswordResult by viewModel.resetPasswordResult.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(actionState) {
        if (actionState is ApiResult.Success) {
            android.widget.Toast.makeText(context, "Status user berhasil diperbarui", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetActionState()
            viewModel.fetchUserDetail(userId)
        } else if (actionState is ApiResult.Error) {
            android.widget.Toast.makeText(context, (actionState as ApiResult.Error).error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.resetActionState()
        }
    }

    when (val state = userState) {
        is ApiResult.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        }
        is ApiResult.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "User tidak ditemukan", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Kembali")
                    }
                }
            }
        }
        is ApiResult.Success -> {
            val user = state.data
            val isDeactivated = user.deletedAt != null
            var showRestoreDialog by remember { mutableStateOf(false) }
            var showDeleteDialog by remember { mutableStateOf(false) }

            // Sprint 2.1: Super Admin dialogs
            val isSuperAdmin = currentUserRole == "super_admin"
            val isSelf = user.id == currentUserId
            val isTargetAdmin = user.isAdminOrSuperAdmin
            var showToggleActiveDialog by remember { mutableStateOf(false) }
            var showResetPasswordDialog by remember { mutableStateOf(false) }
            var showTempPasswordDialog by remember { mutableStateOf(false) }
            var tempPassword by remember { mutableStateOf("") }

            // Sprint 2.1: Listen for reset password result
            LaunchedEffect(resetPasswordResult) {
                if (resetPasswordResult is ApiResult.Success) {
                    tempPassword = (resetPasswordResult as ApiResult.Success<String>).data
                    showTempPasswordDialog = true
                    viewModel.clearResetPasswordResult()
                } else if (resetPasswordResult is ApiResult.Error) {
                    android.widget.Toast.makeText(
                        context,
                        (resetPasswordResult as ApiResult.Error).error,
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    viewModel.clearResetPasswordResult()
                }
            }

            Scaffold(
                containerColor = Color.Transparent,
                floatingActionButton = {
                    if (!isDeactivated) {
                        FloatingActionButton(
                            onClick = { navController.navigate("admin_edit_user/${user.id}") },
                            containerColor = GreenPrimary,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit User")
                        }
                    }
                }
            ) { padding ->
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

                    // 1. HEADER
                    TopBar(
                        title = "Detail Pengguna",
                        onBackClick = { navController.popBackStack() },
                        transparentBackground = true,
                        hideBackground = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. MANNA SHEET
                    MannaSheet(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(32.dp))

                            // 3. FOTO PROFIL MENGGUNAKAN KOMPONEN PROFILEPHOTO (Sama dengan ProfileScreen)
                            val photoUrl = when {
                                !user.profilePhotoPath.isNullOrBlank() -> {
                                    val baseUrl = com.android.rumahsehatmannawasalwa.BuildConfig.BASE_URL
                                    val storageUrl = baseUrl.replace("/api/", "/storage/")
                                    "$storageUrl${user.profilePhotoPath}"
                                }
                                !user.fotoUrl.isNullOrBlank() -> user.fotoUrl
                                else -> null
                            }

                            ProfilePhoto(
                                photoUrl = photoUrl,
                                size = 100.dp,
                                modifier = Modifier.alpha(if (isDeactivated) 0.5f else 1f)
                            )

                            if (isDeactivated) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "User Nonaktif",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Sprint 2.1: Badge is_active untuk admin
                            if (isTargetAdmin && !user.isActive) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Akun Dinonaktifkan",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // 4. DATA FORM (Read Only)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                MannaTextField(
                                    label = "Nama Lengkap",
                                    value = user.name,
                                    onValueChange = {},
                                    placeholder = "",
                                    readOnly = true,
                                    leadingIcon = Icons.Outlined.Person
                                )

                                MannaTextField(
                                    label = "Role",
                                    value = user.role.replaceFirstChar { it.uppercase() },
                                    onValueChange = {},
                                    placeholder = "",
                                    readOnly = true,
                                    leadingIcon = Icons.Default.AdminPanelSettings
                                )

                                MannaTextField(
                                    label = "Email",
                                    value = user.email,
                                    onValueChange = {},
                                    placeholder = "",
                                    readOnly = true,
                                    leadingIcon = Icons.Outlined.Mail
                                )

                                MannaTextField(
                                    label = "Nomor WhatsApp",
                                    value = user.phoneNumber ?: "-",
                                    onValueChange = {},
                                    placeholder = "",
                                    readOnly = true,
                                    leadingIcon = Icons.Default.Phone
                                )

                                MannaTextField(
                                    label = "Alamat",
                                    value = user.address ?: "-",
                                    onValueChange = {},
                                    placeholder = "",
                                    readOnly = true,
                                    leadingIcon = Icons.Default.LocationOn
                                )

                                if (user.role == "terapis" || user.role == "therapist") {
                                    MannaTextField(
                                        label = "Spesialisasi",
                                        value = user.specialization?.joinToString(", ") ?: "-",
                                        onValueChange = {},
                                        placeholder = "",
                                        readOnly = true,
                                        leadingIcon = Icons.Outlined.Work
                                    )
                                } else {
                                    MannaTextField(
                                        label = "Pekerjaan",
                                        value = user.job ?: "-",
                                        onValueChange = {},
                                        placeholder = "",
                                        readOnly = true,
                                        leadingIcon = Icons.Outlined.Work
                                    )
                                }

                                val dobDisplay = try {
                                    if (!user.birthDate.isNullOrBlank()) {
                                        val date = LocalDate.parse(user.birthDate!!.substring(0, 10))
                                        val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
                                        date.format(fmt)
                                    } else "-"
                                } catch (e: Exception) { user.birthDate ?: "-" }

                                MannaTextField(
                                    label = "Tanggal Lahir",
                                    value = dobDisplay,
                                    onValueChange = {},
                                    placeholder = "",
                                    readOnly = true,
                                    leadingIcon = Icons.Default.CalendarToday
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // 5. ACTION BUTTONS
                            if (isDeactivated) {
                                MannaButton(
                                    text = "Aktifkan Kembali User",
                                    onClick = { showRestoreDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                MannaButton(
                                    text = "Nonaktifkan User",
                                    onClick = { showDeleteDialog = true },
                                    containerColor = Color.Red,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // ═══════════════════════════════════════════════
                            // Sprint 2.1: Super Admin Action Buttons
                            // ═══════════════════════════════════════════════
                            if (isSuperAdmin && isTargetAdmin && !isSelf) {
                                Spacer(modifier = Modifier.height(16.dp))

                                // Toggle Active (is_active, bukan soft delete)
                                MannaButton(
                                    text = if (user.isActive) "Nonaktifkan Akun Admin" else "Aktifkan Akun Admin",
                                    onClick = { showToggleActiveDialog = true },
                                    containerColor = if (user.isActive) AccentOrange else GreenPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Reset Password
                                MannaButton(
                                    text = "Reset Password",
                                    onClick = { showResetPasswordDialog = true },
                                    containerColor = AccentBlue,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                // ═════════════════════════════════════════════════════
                // DIALOGS
                // ═════════════════════════════════════════════════════

                // Existing: Delete (Soft Delete)
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Nonaktifkan Pengguna?") },
                        text = { Text("Apakah Anda yakin ingin menonaktifkan pengguna '${user.name}'?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showDeleteDialog = false
                                viewModel.deleteUser(user.id)
                            }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("Nonaktifkan") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
                        }
                    )
                }

                // Existing: Restore
                if (showRestoreDialog) {
                    AlertDialog(
                        onDismissRequest = { showRestoreDialog = false },
                        title = { Text("Aktifkan Kembali Pengguna?") },
                        text = { Text("Apakah Anda yakin ingin mengaktifkan kembali user '${user.name}'?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showRestoreDialog = false
                                viewModel.restoreUser(user.id)
                            }, colors = ButtonDefaults.textButtonColors(contentColor = GreenPrimary)) { Text("Aktifkan") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRestoreDialog = false }) { Text("Batal") }
                        }
                    )
                }

                // Sprint 2.1: Toggle Active Dialog
                if (showToggleActiveDialog) {
                    val action = if (user.isActive) "menonaktifkan" else "mengaktifkan"
                    AlertDialog(
                        onDismissRequest = { showToggleActiveDialog = false },
                        title = { Text("${if (user.isActive) "Nonaktifkan" else "Aktifkan"} Admin?") },
                        text = { Text("Apakah Anda yakin ingin $action akun '${user.name}'? ${if (user.isActive) "Admin tidak akan bisa login setelah dinonaktifkan." else ""}") },
                        confirmButton = {
                            TextButton(onClick = {
                                showToggleActiveDialog = false
                                viewModel.toggleAdminActive(user.id)
                            }, colors = ButtonDefaults.textButtonColors(
                                contentColor = if (user.isActive) Color.Red else GreenPrimary
                            )) { Text(if (user.isActive) "Nonaktifkan" else "Aktifkan") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showToggleActiveDialog = false }) { Text("Batal") }
                        }
                    )
                }

                // Sprint 2.1: Reset Password Confirmation
                if (showResetPasswordDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetPasswordDialog = false },
                        title = { Text("Reset Password?") },
                        text = { Text("Password '${user.name}' akan direset dan diganti dengan password sementara. User akan diminta login ulang.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showResetPasswordDialog = false
                                viewModel.resetAdminPassword(user.id)
                            }, colors = ButtonDefaults.textButtonColors(contentColor = AccentBlue)) { Text("Reset") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetPasswordDialog = false }) { Text("Batal") }
                        }
                    )
                }

                // Sprint 2.1: Show Temporary Password (Copyable)
                if (showTempPasswordDialog) {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    AlertDialog(
                        onDismissRequest = { showTempPasswordDialog = false },
                        title = { Text("Password Berhasil Direset", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Berikan password sementara ini kepada user:")
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tempPassword,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = GreenPrimary
                                        )
                                        IconButton(onClick = {
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("password", tempPassword))
                                            android.widget.Toast.makeText(context, "Password disalin!", android.widget.Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, "Salin", tint = GreenPrimary)
                                        }
                                    }
                                }
                                Text(
                                    "⚠️ Password ini hanya ditampilkan sekali. Pastikan sudah dicatat.",
                                    fontSize = 12.sp,
                                    color = Color.Red
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showTempPasswordDialog = false }) { Text("Saya Sudah Memberitahu User") }
                        }
                    )
                }
            }
        }
    }
}

