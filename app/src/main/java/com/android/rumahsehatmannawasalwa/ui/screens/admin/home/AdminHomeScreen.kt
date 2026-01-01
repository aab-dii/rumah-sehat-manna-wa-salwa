package com.android.rumahsehatmannawasalwa.ui.screens.admin.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.*
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavController,
    viewModel: AdminUserViewModel = viewModel(),
    onLogout: () -> Unit // Kept for compatibility but unused
) {
    val userPagingItems = viewModel.userPager.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Text(
            text = "Daftar Pengguna",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = userPagingItems.itemCount,
                key = { index -> userPagingItems[index]?.id ?: index }
            ) { index ->
                val user = userPagingItems[index]
                if (user != null) {
                    UserCard(user)
                }
            }

            item {
                if (userPagingItems.loadState.append is LoadState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Role: ${user.role}", style = MaterialTheme.typography.bodyMedium, color = if (user.role == "admin") Color.Red else Color.Blue)
            if (user.phoneNumber.isNotEmpty()) {
                Text(text = "HP: ${user.phoneNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
