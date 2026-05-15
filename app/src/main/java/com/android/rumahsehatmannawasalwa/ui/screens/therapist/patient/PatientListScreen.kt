package com.android.rumahsehatmannawasalwa.ui.screens.therapist.patient


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.appointment.SharedSearchBar
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.BackgroundWhite
import com.android.rumahsehatmannawasalwa.ui.theme.GreenDark
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.GreenLight
import com.android.rumahsehatmannawasalwa.ui.theme.SlateTextDark
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import com.android.rumahsehatmannawasalwa.ui.components.auth.ProfilePhoto

// Palette removed - using global theme tokens

@Composable
fun PatientListScreen(
    navController: NavController,
    viewModel: AdminUserViewModel
) {
    // ── IMPORTANT: Cache the pager so it is NOT recreated on every recompose ──
    val patientPager = remember { viewModel.getUserPager("pasien") }
    val patientItems = patientPager.collectAsLazyPagingItems()
    val searchQuery  by viewModel.searchQuery.collectAsState()

    // ── List State ──────────────────────────────────────────
    val listState = rememberLazyListState()

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
            // ── 1. TopBar (Green Header) ──────────────────────────────────
        TopBar(
            title = "Daftar Pasien",
            subtitle = "Pilih pasien untuk melihat riwayat terapi",
            transparentBackground = true,
            hideBackground = true,
        )
        Spacer(modifier = Modifier.height(20.dp))
            // ── 2. Content Area with MannaSheet ──────────────────────────────
                SharedSearchBar(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                MannaSheet(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp, top = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Loading state
                        if (patientItems.loadState.refresh is LoadState.Loading) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator(color = GreenPrimary) }
                            }
                        } else if (patientItems.itemCount == 0 &&
                            patientItems.loadState.refresh !is LoadState.Loading
                        ) {
                            // Empty state
                            item {
                                Box(
                                    Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Person, null,
                                            tint = Color.Gray.copy(0.4f),
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            "Tidak ada pasien ditemukan",
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                count = patientItems.itemCount,
                                key   = patientItems.itemKey { it.id }
                            ) { index ->
                                patientItems[index]?.let { patient ->
                                    PatientListItem(
                                        patient = patient,
                                        onClick = {
                                            navController.navigate(
                                                Screen.PatientHistory.createRoute(patient.id)
                                            )
                                        }
                                    )
                                }
                            }

                            // Append loading footer
                            if (patientItems.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color    = GreenPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

// ═════════════════════════════════════════════════════════════════════════════
//  Patient Item Card
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun PatientListItem(
    patient: User,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Foto Profil (Sama dengan ProfileScreen)
            val photoUrl = when {
                !patient.profilePhotoPath.isNullOrBlank() -> {
                    val baseUrl = com.android.rumahsehatmannawasalwa.BuildConfig.BASE_URL
                    val storageUrl = baseUrl.replace("/api/", "/storage/")
                    "$storageUrl${patient.profilePhotoPath}"
                }
                !patient.fotoUrl.isNullOrBlank() -> patient.fotoUrl
                else -> null
            }

            ProfilePhoto(
                photoUrl = photoUrl,
                size = 52.dp
            )

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text         = patient.name,
                    fontWeight   = FontWeight.SemiBold,
                    fontSize     = 15.sp,
                    color        = SlateTextDark,
                    maxLines     = 1,
                    overflow     = TextOverflow.Ellipsis
                )
                if (patient.phoneNumber.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = patient.phoneNumber,
                        fontSize = 12.sp,
                        color    = Color.Gray
                    )
                }
            }

            // Right indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(GreenLight.copy(alpha = 0.65f))
            )
        }
    }
}
