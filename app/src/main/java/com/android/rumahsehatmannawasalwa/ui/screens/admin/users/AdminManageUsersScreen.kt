package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.R
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.ui.navigation.Screen
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.scale
import com.android.rumahsehatmannawasalwa.ui.components.TopBar
import com.android.rumahsehatmannawasalwa.ui.components.auth.ProfilePhoto
import com.android.rumahsehatmannawasalwa.ui.components.appointment.SharedSearchBar
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.alpha
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.rumahsehatmannawasalwa.ui.components.appointment.CapsuleTabRow
import com.android.rumahsehatmannawasalwa.ui.components.snackbar.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminManageUsersScreen(
    navController: NavController,
    viewModel: AdminUserViewModel = viewModel(),
) {
    // Collect BOTH pagers to keep their state alive
    val patientPagingItems = viewModel.getUserPager("pasien").collectAsLazyPagingItems()
    val therapistPagingItems = viewModel.getUserPager("terapis").collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showTrashed by viewModel.showTrashed.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val isSnackbarShowing = snackbarHostState.currentSnackbarData != null
    
    // Animate FAB up when snackbar is showing
    val fabOffset by animateDpAsState(
        targetValue = if (isSnackbarShowing) (-80).dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "fab_movement"
    )

    // Check Navigation Result for Snackbar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val savedStateHandle = navBackStackEntry?.savedStateHandle
    val snackbarMsg = savedStateHandle?.getStateFlow<String?>("snackbar_msg", null)?.collectAsState()
    val snackbarTypeVal = savedStateHandle?.getStateFlow<String?>("snackbar_type", null)?.collectAsState()

    LaunchedEffect(snackbarMsg?.value) {
        snackbarMsg?.value?.let { msg ->
            val type = when (snackbarTypeVal?.value) {
                "SUCCESS" -> SnackbarType.SUCCESS
                "ERROR"   -> SnackbarType.ERROR
                else      -> SnackbarType.INFO
            }
            snackbarHostState.showSnackbar(MannaSnackbarVisuals(message = msg, type = type))
            savedStateHandle?.remove<String>("snackbar_msg")
            savedStateHandle?.remove<String>("snackbar_type")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // 1. TopBar Gradient
        TopBar(
            title = "Kelola Pengguna",
            subtitle = "Atur dan kelola data pasien & terapis",
            onBackClick = { navController.navigateUp() },
            bottomExtra = 130.dp
        )

        // 2. Fixed White Sheet
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 230.dp)
                .clipToBounds()
                .background(
                    color = BackgroundWhite,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                )
        )

            // 3. Floating Overlapping Controls (Search & Toggle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 90.dp)
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SharedSearchBar(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    InactiveUsersToggle(
                        isChecked = showTrashed,
                        onCheckedChange = { viewModel.toggleTrashFilter(!showTrashed) }
                    )
                }
            }

            // 4. Content Area (Tabs + List)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 230.dp)
            ) {
                val pagerState = rememberPagerState(pageCount = { 2 })
                val coroutineScope = rememberCoroutineScope()
                val tabs = listOf(stringResource(id = R.string.patient), stringResource(id = R.string.therapist))

                // Capsule Tab Row (needs horizontal padding)
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    CapsuleTabRow(
                        tabs = tabs,
                        pagerState = pagerState,
                        onTabSelected = { index ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }

                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    if (page == 0) {
                        UserListContent(
                            userPagingItems = patientPagingItems,
                            onUserClick = { user ->
                                navController.navigate(
                                    Screen.AdminUserDetail.route.replace(
                                        "{userId}",
                                        user.id.toString()
                                    )
                                )
                            }
                        )
                    } else {
                        UserListContent(
                            userPagingItems = therapistPagingItems,
                            onUserClick = { user ->
                                navController.navigate(
                                    Screen.AdminUserDetail.route.replace(
                                        "{userId}",
                                        user.id.toString()
                                    )
                                )
                            }
                        )
                    }
            }
        }

        // 5. FAB
        FloatingActionButton(
            onClick = { navController.navigate(Screen.AdminAddUser.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .offset(y = fabOffset),
            containerColor = GreenPrimary,
            contentColor = Color.White
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Tambah Pengguna"
            )
        }

        // 6. Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) { data -> MannaSnackbar(snackbarData = data) }
    }
}


@Composable
fun UserListContent(
    userPagingItems: LazyPagingItems<User>,
    onUserClick: (User) -> Unit
) {
    if (userPagingItems.loadState.refresh is LoadState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(userPagingItems.itemCount) { index ->
                val user = userPagingItems[index]
                if (user != null) {
                    UserCard(
                        user = user,
                        onClick = { onUserClick(user) } // <--- Pass user ke callback
                    )
                }
            }

            if (userPagingItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (userPagingItems.itemCount == 0 && userPagingItems.loadState.refresh !is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada data pengguna.", color = BodyGray)
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit
) {
    val isDeactivated = user.deletedAt != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeactivated) DividerLight else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Avatar menggunakan komponen ProfilePhoto (Sama dengan ProfileScreen)
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
                size = 52.dp,
                modifier = Modifier.alpha(if (isDeactivated) 0.5f else 1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Info User (Nama, Email, No HP)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // A. Nama (Tebal & Jelas)
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDeactivated) GrayText else TextPrimary,
                        maxLines = 1
                    )

                    if (isDeactivated) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    "Nonaktif",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = DividerColor,
                                labelColor = SlateText
                            ),
                            border = null,
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                // B. Email
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.android.rumahsehatmannawasalwa.ui.theme.TextSecondary,
                    maxLines = 1
                )

                // C. No HP
                if (!user.phoneNumber.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.phoneNumber ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.android.rumahsehatmannawasalwa.ui.theme.TextSecondary
                    )
                }
            }

            // 3. Right indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDeactivated) GrayText else GreenLight.copy(
                            alpha = 0.65f
                        )
                    )
            )
        }
    }
}

@Composable
fun InactiveUsersToggle(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = if (isChecked) RedDanger else GrayText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tampilkan Pengguna Nonaktif",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isChecked) RedDanger else SlateText
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RedDanger,
                ),
                modifier = Modifier.scale(0.8f)
            )
        }
    }
}