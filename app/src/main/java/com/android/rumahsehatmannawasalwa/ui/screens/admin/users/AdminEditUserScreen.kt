package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import java.util.Calendar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import android.net.Uri
import androidx.compose.foundation.background
import com.android.rumahsehatmannawasalwa.ui.theme.*
// Add coil dependency if missing elsewhere, but usually present.
import androidx.compose.ui.res.painterResource
// R class might be needed if using placeholder
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditUserScreen(
    navController: NavController,
    viewModel: AdminUserViewModel,
    userId: Int
) {
    val context = LocalContext.current
    val user by viewModel.selectedUser.collectAsState()
    
    // Service List for Therapists
    val serviceList by viewModel.serviceList.collectAsState()

    // Local State for Form
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") } // Read Only usually
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var job by remember { mutableStateOf(user?.job ?: "") }
    var address by remember { mutableStateOf(user?.address ?: "") }
    var birthDate by remember { mutableStateOf(user?.birthDate ?: "") }
    var selectedGender by remember { mutableStateOf(user?.gender ?: "L") }
    
    // Specialization State
    val selectedServices = remember { mutableStateListOf<String>() }

    // Image Picker State
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract(),
        onResult = { result ->
            if (result.isSuccessful) {
                selectedImageUri = result.uriContent
            } else {
                val exception = result.error
                Toast.makeText(context, "Crop Error: ${exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Init Logic
    LaunchedEffect(user) {
        user?.let {
            name = it.name
            email = it.email
            phone = it.phoneNumber ?: ""
            job = it.job ?: ""
            address = it.address ?: ""
            birthDate = it.birthDate ?: ""
            selectedGender = it.gender ?: "L"
            // Reset image uri on user change
            selectedImageUri = null
            
            if (it.role == "terapis") {
                viewModel.fetchServices()
                selectedServices.clear()
                selectedServices.addAll(it.specialization)
            }
        }
    }

    // --- Action State Observer ---
    val actionState by viewModel.actionState.collectAsState()
    
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is com.android.rumahsehatmannawasalwa.data.ApiResult.Success -> {
                Toast.makeText(context, "Data Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit User") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val role = user?.role ?: "pasien"
                    
                    val finalJob = if (role == "pasien") job else ""
                    val finalSpecs = if (role == "terapis") selectedServices.toList() else null

                    viewModel.updateUser(
                        userId = userId,
                        name = name,
                        phone = phone,
                        job = finalJob,
                        specialization = finalSpecs,
                        birthDate = birthDate,
                        address = address,
                        gender = selectedGender,
                        photoUri = selectedImageUri
                    )
                },
                containerColor = GreenPrimary,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                if (actionState is com.android.rumahsehatmannawasalwa.data.ApiResult.Loading) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Simpan")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Read-Only Role Display
            Text(
                text = "Role: ${user?.role?.uppercase() ?: "-"}", 
                style = MaterialTheme.typography.titleMedium,
                color = GreenPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // PROFILE PHOTO UPLOAD
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                 Box(
                     modifier = Modifier
                         .size(100.dp)
                         .clip(CircleShape)
                         .background(GreenPrimary.copy(alpha = 0.3f))
                         .clickable {
                             val options = CropImageContractOptions(
                                 uri = null, // Null to prompt picker
                                 cropImageOptions = CropImageOptions(
                                     imageSourceIncludeGallery = true,
                                     imageSourceIncludeCamera = true,
                                     cropShape = CropImageView.CropShape.OVAL, // Circle for profile
                                     fixAspectRatio = true,
                                     aspectRatioX = 1,
                                     aspectRatioY = 1
                                 )
                             )
                             cropImageLauncher.launch(options)
                         },
                     contentAlignment = Alignment.Center
                 ) {
                     if (selectedImageUri != null) {
                         AsyncImage(
                             model = selectedImageUri,
                             contentDescription = "Selected Photo",
                             modifier = Modifier.fillMaxSize(),
                             contentScale = ContentScale.Crop
                         )
                     } else if (!user?.profilePhotoPath.isNullOrBlank()) {
                         // TODO: Need base URL for image or full path logic
                         // Assume stored path needs base url. For now just placeholder or check if http
                         val photoUrl = user?.profilePhotoPath // Adjust if needed
                         // Use Coil to load from URL if available, else placeholder
                         // Ideally we stick to simple placeholder if not implementing full URL logic yet
                         // But for now let's just show placeholder unless new image selected
                         // Or try to load existing
                         // For simplicity, just icon if no new image
                          Icon(Icons.Default.Save, contentDescription = "Edit Photo", tint = GreenPrimary) // Placeholder icon
                     } else {
                         Text("Upload Foto", style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                     }
                 }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { }, // Email usually immutable in simple edit
                readOnly = true,
                label = { Text("Email (Tidak dapat diubah)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                     disabledTextColor = MaterialTheme.colorScheme.onSurface,
                     disabledLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                enabled = false 
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor HP") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (user?.role == "pasien") {
                OutlinedTextField(
                    value = job,
                    onValueChange = { job = it },
                    label = { Text("Pekerjaan") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else if (user?.role == "terapis") {
                // Multi-select Specialization
                 Text("Spesialisasi (Layanan)", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
                
                if (serviceList.isEmpty()) {
                    Text("Memuat layanan...", color = androidx.compose.ui.graphics.Color.Gray, style = MaterialTheme.typography.bodySmall)
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
                                        checkedColor = GreenPrimary
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            // Spacer for FAB visibility
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
