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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageUsersScreen(
    navController: NavController,
    viewModel: AdminUserViewModel = viewModel(),
    onUserClick: (Int) -> Unit,
    onAddUserClick: () -> Unit // New Parameter
) {
    // Tab State: 0 = Pasien, 1 = Terapis
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Collect BOTH pagers to keep their state alive
    val patientPagingItems = viewModel.patientPager.collectAsLazyPagingItems()
    val therapistPagingItems = viewModel.therapistPager.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showTrashed by viewModel.showTrashed.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddUserClick,
                containerColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah User")
            }
        },
        containerColor = Color.Transparent // Allow background to show through if any
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(paddingValues) 
        ) {
            // --- Search Bar & Filter ---
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari pengguna berdasarkan nama...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Filter Chip for "Nonaktif"
                FilterChip(
                    selected = showTrashed,
                    onClick = { viewModel.toggleTrashFilter(!showTrashed) },
                    label = { Text("Tampilkan User Nonaktif") },
                    leadingIcon = {
                        if (showTrashed) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenContainer,
                        selectedLabelColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
                    )
                )
            }

            // Tab Row
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

            // Content
            if (selectedTabIndex == 0) {
                UserListContent(
                    userPagingItems = patientPagingItems,
                    onUserClick = { user ->
                        viewModel.selectUser(user)
                        onUserClick(user.id)
                    }
                )
            } else {
                UserListContent(
                    userPagingItems = therapistPagingItems,
                    onUserClick = { user ->
                        viewModel.selectUser(user)
                        onUserClick(user.id)
                    }
                )
            }
        }
    }
}

@Composable
fun UserListContent(
    userPagingItems: LazyPagingItems<User>,
    onUserClick: (User) -> Unit // <--- PARAMETER INI DITAMBAHKAN
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
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (userPagingItems.itemCount == 0 && userPagingItems.loadState.refresh !is LoadState.Loading) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data pengguna.", color = Color.Gray)
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
        colors = CardDefaults.cardColors(containerColor = if (isDeactivated) Color(0xFFEEEEEE) else Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Avatar (AsyncImage with UI Avatars)
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(50.dp),
                color = if (isDeactivated) Color.Gray else com.android.rumahsehatmannawasalwa.ui.theme.GreenContainer
            ) {
                if (!user.profilePhotoPath.isNullOrBlank()) {
                    // CONSTRUCT FULL URL
                    // Assuming BASE_URL ends with /api/, we strip it to get base domain and append /storage/
                    // e.g. http://10.0.2.2:8000/api/ -> http://10.0.2.2:8000/storage/profile-photos/filename.jpg
                    val baseUrl = com.android.rumahsehatmannawasalwa.BuildConfig.BASE_URL
                    val storageUrl = baseUrl.replace("/api/", "/storage/")
                    val fullPhotoUrl = "$storageUrl${user.profilePhotoPath}"

                    coil.compose.AsyncImage(
                        model = fullPhotoUrl,
                        contentDescription = "Foto ${user.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        alpha = if (isDeactivated) 0.5f else 1f,
                        error = androidx.compose.ui.res.painterResource(com.android.rumahsehatmannawasalwa.R.drawable.ic_launcher_foreground) // Fallback if load fails
                    )
                } else if (user.name.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = "https://ui-avatars.com/api/?name=${user.name}&background=random&size=128",
                        contentDescription = "Foto ${user.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        alpha = if (isDeactivated) 0.5f else 1f
                    )
                } else {
                     Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = if (isDeactivated) Color.DarkGray else com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
                    )
                }
            }

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
                        color = if (isDeactivated) Color.Gray else com.android.rumahsehatmannawasalwa.ui.theme.TextPrimary,
                        maxLines = 1
                    )
                    
                    if (isDeactivated) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Nonaktif", style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color.LightGray,
                                labelColor = Color.DarkGray
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

            // 3. Icon Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detail",
                tint = if (isDeactivated) Color.Gray else com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}