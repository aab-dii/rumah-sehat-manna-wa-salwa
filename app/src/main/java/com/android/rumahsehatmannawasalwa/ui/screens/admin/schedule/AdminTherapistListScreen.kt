package com.android.rumahsehatmannawasalwa.ui.screens.admin.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.ui.screens.admin.users.UserListContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTherapistListScreen(
    navController: NavController,
    viewModel: AdminUserViewModel = viewModel(),
    onTherapistClick: (Int) -> Unit
) {
    val therapistPagingItems = viewModel.therapistPager.collectAsLazyPagingItems()

    val searchQuery = viewModel.searchQuery.collectAsState().value

    // Optional: Clear search on entry
    LaunchedEffect(Unit) {
        viewModel.onSearchQueryChanged("")
    }

    Column(modifier = Modifier.fillMaxSize()) { // Remove parent padding
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Add padding here
            placeholder = { Text("Cari terapis...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )
        
        // Spacer not strictly needed if List has top padding, but can keep for safety or remove.
        // UserListContent has 16dp contentPadding.
        
        UserListContent(
            userPagingItems = therapistPagingItems,
            onUserClick = { user ->
                onTherapistClick(user.id)
            }
        )
    }
}
