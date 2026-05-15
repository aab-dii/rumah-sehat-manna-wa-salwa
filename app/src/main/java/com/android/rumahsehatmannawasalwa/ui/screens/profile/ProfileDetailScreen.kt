package com.android.rumahsehatmannawasalwa.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.inputs.MannaTextField
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.components.auth.ProfilePhoto
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbar
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.MannaSnackbarVisuals
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.SnackbarType
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(navController: NavController, viewModel: AuthViewModel) {
    val userDataState = viewModel.currentUserData.collectAsState()
    val userData = userDataState.value
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1. Cek apakah ada sinyal update berhasil dari halaman edit
    val updateSuccess = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<Boolean>("profile_updated")

    LaunchedEffect(updateSuccess) {
        if (updateSuccess == true) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    MannaSnackbarVisuals(
                        message = "Profil berhasil diperbarui",
                        type = SnackbarType.SUCCESS
                    )
                )
            }
            // Reset state agar snackbar tidak muncul lagi saat rotasi/kembali
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("profile_updated")
        }
        viewModel.fetchUserProfile()
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                MannaSnackbar(data)
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

            // 1. HEADER (Transparent TopBar)
            TopBar(
                title = "Detail Profil",
                onBackClick = { navController.popBackStack() },
                transparentBackground = true,
                hideBackground = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. MANNA SHEET
            MannaSheet(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. FOTO PROFIL MENGGUNAKAN KOMPONEN PROFILEPHOTO
                    val photoUrl = when {
                        // Prioritaskan foto Google
                        !userData?.fotoUrl.isNullOrBlank() -> userData?.fotoUrl
                        
                        // Fallback ke foto Database
                        !userData?.profilePhotoPath.isNullOrBlank() -> {
                            val baseUrl = com.android.rumahsehatmannawasalwa.BuildConfig.BASE_URL
                            val storageUrl = baseUrl.replace("/api/", "/storage/")
                            "$storageUrl${userData?.profilePhotoPath}"
                        }
                        else -> null
                    }

                    ProfilePhoto(
                        photoUrl = photoUrl,
                        size = 100.dp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. TEXT PILLS DATA
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MannaTextField(
                            label = "Nama Lengkap",
                            value = userData?.name ?: "-",
                            onValueChange = {},
                            placeholder = "",
                            readOnly = true,
                            leadingIcon = Icons.Outlined.Person
                        )

                        MannaTextField(
                            label = "Nomor WhatsApp",
                            value = userData?.phoneNumber ?: "-",
                            onValueChange = {},
                            placeholder = "",
                            readOnly = true,
                            leadingIcon = Icons.Default.Phone
                        )

                        MannaTextField(
                            label = "Email",
                            value = userData?.email ?: "-",
                            onValueChange = {},
                            placeholder = "",
                            readOnly = true,
                            leadingIcon = Icons.Outlined.Mail
                        )

                        MannaTextField(
                            label = "Alamat",
                            value = userData?.address ?: "-",
                            onValueChange = {},
                            placeholder = "",
                            readOnly = true,
                            leadingIcon = Icons.Default.LocationOn
                        )

                        MannaTextField(
                            label = "Pekerjaan",
                            value = userData?.job ?: "-",
                            onValueChange = {},
                            placeholder = "",
                            readOnly = true,
                            leadingIcon = Icons.Outlined.Work
                        )

                        // Format Tanggal Lahir
                        val formattedDoB = try {
                            if (!userData?.birthDate.isNullOrBlank()) {
                                val date = LocalDate.parse(userData!!.birthDate.substring(0, 10))
                                val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
                                date.format(formatter)
                            } else "-"
                        } catch (e: Exception) {
                            userData?.birthDate ?: "-"
                        }
                        MannaTextField(
                            label = "Tanggal Lahir",
                            value = formattedDoB,
                            onValueChange = {},
                            placeholder = "",
                            readOnly = true,
                            leadingIcon = Icons.Default.CalendarToday
                        )

                        // Jenis Kelamin
                        val genderDisplay = when(userData?.gender) {
                            "L" -> "Laki-Laki"
                            "P" -> "Perempuan"
                            else -> userData?.gender ?: "-"
                        }
                        MannaTextField(
                            label = "Jenis Kelamin",
                            value = genderDisplay,
                            onValueChange = {},
                            placeholder = "",
                            readOnly = true,
                            leadingIcon = Icons.Outlined.Person
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 5. MANNA BUTTON
                    MannaButton(
                        text = "Ubah Data",
                        onClick = { navController.navigate(Screen.CompleteProfile.createRoute(false)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}


