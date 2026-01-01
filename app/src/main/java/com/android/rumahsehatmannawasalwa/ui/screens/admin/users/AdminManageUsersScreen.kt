package com.android.rumahsehatmannawasalwa.ui.screens.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.ui.screens.admin.home.UserCard
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageUsersScreen(
    navController: NavController,
    viewModel: AdminUserViewModel = viewModel()
) {
    // Tab State: 0 = Pasien, 1 = Terapis
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    // Update ViewModel when tab changes
    LaunchedEffect(selectedTabIndex) {
        val role = if (selectedTabIndex == 0) "pasien" else "terapis"
        viewModel.setRoleFilter(role)
    }

    val userPagingItems = viewModel.userPager.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Pasien") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Terapis") }
            )
        }

        // List Content
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
                        UserCard(user)
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
}
