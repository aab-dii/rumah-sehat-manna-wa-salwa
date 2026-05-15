package com.android.rumahsehatmannawasalwa.ui.screens.admin.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.appointment.SharedSearchBar
import com.android.rumahsehatmannawasalwa.ui.components.layouts.MannaSheet
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import androidx.compose.ui.graphics.Brush



@Composable
fun AdminTherapistListScreen(
    navController: NavController,
    viewModel: AdminUserViewModel = viewModel(),
    onTherapistClick: (Int) -> Unit
) {
    // Cache the pager so it is NOT recreated on every recompose
    val therapistPager = remember { viewModel.getUserPager("terapis") }
    val therapistItems = therapistPager.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Clear search on entry
    LaunchedEffect(Unit) {
        viewModel.onSearchQueryChanged("")
    }

    // List State
    val listState = rememberLazyListState()

    Box(
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── 1. TopBar
            TopBar(
                title = "Daftar Terapis",
                subtitle = "Pilih terapis untuk melihat jadwal",
                onBackClick = { navController.popBackStack() },
                transparentBackground = true,
                hideBackground = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            SharedSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))
            // ── 2. MannaSheet Content
            MannaSheet(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Loading state
                        if (therapistItems.loadState.refresh is LoadState.Loading) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator(color = GreenPrimary) }
                            }
                        } else if (therapistItems.itemCount == 0 &&
                            therapistItems.loadState.refresh !is LoadState.Loading
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
                                            tint = GrayText.copy(0.4f),
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            "Tidak ada terapis ditemukan",
                                            color = BodyGray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                count = therapistItems.itemCount,
                                key = therapistItems.itemKey { it.id }
                            ) { index ->
                                therapistItems[index]?.let { therapist ->
                                    TherapistListItem(
                                        therapist = therapist,
                                        onClick = { onTherapistClick(therapist.id) }
                                    )
                                }
                            }

                            // Append loading footer
                            if (therapistItems.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = GreenPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

// ═════════════════════════════════════════════════════════════════════════════
//  Therapist Item Card
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun TherapistListItem(
    therapist: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = therapist.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = therapist.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (therapist.phoneNumber.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = therapist.phoneNumber,
                        fontSize = 12.sp,
                        color = BodyGray
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
