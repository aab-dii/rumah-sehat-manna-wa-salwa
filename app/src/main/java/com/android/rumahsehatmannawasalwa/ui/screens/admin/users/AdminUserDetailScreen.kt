package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Navigation & Effect for Action (Delete)
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(actionState) {
        if (actionState is ApiResult.Success) {
            android.widget.Toast.makeText(context, "User berhasil dihapus", android.widget.Toast.LENGTH_SHORT).show()
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
            FloatingActionButton(
                onClick = { 
                    navController.navigate("admin_edit_user/${user.id}")
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit User")
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
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (user.name.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = "https://ui-avatars.com/api/?name=${user.name}&background=random&size=200",
                        contentDescription = "Foto Profil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
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

            // --- List Detail ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AdminDetailItem("Nama Lengkap", user.name)
                AdminDetailItem("Role", user.role.replaceFirstChar { it.uppercase() })
                AdminDetailItem("Email", user.email)
                AdminDetailItem("Nomor HP", user.phoneNumber)
                AdminDetailItem("Pekerjaan", user.job)
                
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
            
            // Delete Button (moved to bottom)
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
                Text("Hapus User")
            }
            
            // Space for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus User?") },
            text = { Text("Apakah Anda yakin ingin menghapus user '${user.name}'? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteUser(user.id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
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
