package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    navController: NavController,
    viewModel: AdminUserViewModel,
    userId: Int
) {
    // Collect Selected User from VM
    val userState by viewModel.selectedUser.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    
    // Local state for dialog
    val isDeactivated = userState?.deletedAt != null
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Navigation & Effect for Action (Delete/Restore)
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(actionState) {
        if (actionState is ApiResult.Success) {
            val message = if (isDeactivated) "User berhasil diaktifkan kembali" else "User berhasil dinonaktifkan"
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetActionState()
            navController.popBackStack() // Go back to list
        } else if (actionState is ApiResult.Error) {
            android.widget.Toast.makeText(context, (actionState as ApiResult.Error).error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.resetActionState()
        }
    }

    if (userState == null) {
        // Fallback or loading if needed
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User data not found. Please go back.")
        }
        return
    }
    
    val user = userState!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail User") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isDeactivated) {
                FloatingActionButton(
                    onClick = { 
                        navController.navigate("admin_edit_user/${user.id}")
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit User")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- Foto Profil ---
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = if (isDeactivated) Color.Gray else MaterialTheme.colorScheme.primaryContainer
            ) {
                if (user.name.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = "https://ui-avatars.com/api/?name=${user.name}&background=random&size=200",
                        contentDescription = "Foto Profil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        alpha = if (isDeactivated) 0.5f else 1f
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Foto Profil",
                        modifier = Modifier.padding(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            if (isDeactivated) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color(0xFFEEEEEE), MaterialTheme.shapes.small)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "User ini sedang dinonaktifkan.",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- List Detail ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AdminDetailItem("Nama Lengkap", user.name)
                AdminDetailItem("Role", user.role.replaceFirstChar { it.uppercase() })
                AdminDetailItem("Email", user.email)
                AdminDetailItem("Nomor HP", user.phoneNumber)
                if (user.role == "terapis") {
                     val specs = user.specialization.joinToString(", ")
                     AdminDetailItem("Spesialisasi", if (specs.isBlank()) "-" else specs)
                } else {
                     AdminDetailItem("Pekerjaan", user.job)
                }
                
                // Format Tanggal
                val dobDisplay = try {
                    if (!user.birthDate.isNullOrBlank()) {
                         val date = LocalDate.parse(user.birthDate!!.substring(0, 10))
                         val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
                         date.format(fmt)
                    } else "-"
                } catch (e: Exception) { user.birthDate }
                AdminDetailItem("Tanggal Lahir", dobDisplay)
                
                val genderDisplay = when(user.gender) {
                    "L" -> "Laki-Laki"
                    "P" -> "Perempuan"
                    else -> user.gender
                }
                AdminDetailItem("Jenis Kelamin", genderDisplay)
                AdminDetailItem("Alamat", user.address)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Delete / Restore Button
            if (isDeactivated) {
                Button(
                    onClick = { showRestoreDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aktifkan Kembali (Restore)")
                }
            } else {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nonaktifkan User")
                }
            }
            
            // Space for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Nonaktifkan User?") },
            text = { Text("Apakah Anda yakin ingin menonaktifkan user '${user.name}'? Data tidak akan hilang permanen, tapi user tidak bisa login.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteUser(user.id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Nonaktifkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Restore Confirmation Dialog
    if (showRestoreDialog) {
         AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Aktifkan Kembali User?") },
            text = { Text("Apakah Anda yakin ingin mengaktifkan kembali user '${user.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = false
                        viewModel.restoreUser(user.id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary)
                ) {
                    Text("Aktifkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun AdminDetailItem(label: String, value: String?) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(
            text = if (value.isNullOrBlank() || value == "-") "-" else value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface 
        )
        Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(top = 8.dp))
    }
}
