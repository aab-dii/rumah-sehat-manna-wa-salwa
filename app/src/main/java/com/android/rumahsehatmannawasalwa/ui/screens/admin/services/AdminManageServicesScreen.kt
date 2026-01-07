package com.android.rumahsehatmannawasalwa.ui.screens.admin.services

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.rumahsehatmannawasalwa.data.model.service.Layanan
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.LayananViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageServicesScreen(
    navController: NavController, // Keep for potential local nav or remove if unused. Let's keep for safety but primarily use callback.
    viewModel: LayananViewModel,
    onServiceClick: (Int) -> Unit = {},
    onAddServiceClick: () -> Unit = {}
) {
    val layananPagingItems = viewModel.layananPager.collectAsLazyPagingItems()

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddServiceClick,
                containerColor = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Layanan")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header is handled by AdminMainScreen Scaffold (TopAppBar), but if this is nested, we just show CONTENT.
            // AdminMainScreen renders this inside its NavHost.
            // However, the user request says "Di bagian atas terdapat judul 'Kelola Layanan'".
            // AdminMainScreen already updates the title based on route.
            // So we just need the Content List.

            ServiceListContent(
                layananPagingItems = layananPagingItems,
                onServiceClick = onServiceClick
            )
        }
    }
}

@Composable
fun ServiceListContent(
    layananPagingItems: LazyPagingItems<Layanan>,
    onServiceClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(layananPagingItems.itemCount) { index ->
            val layanan = layananPagingItems[index]
            if (layanan != null) {
                AdminServiceCard(
                    layanan = layanan,
                    onClick = { onServiceClick(layanan.id) }
                )
            }
        }
    }
}

@Composable
fun AdminServiceCard(
    layanan: Layanan,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Gambar Layanan
            Surface(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(80.dp),
                color = Color.LightGray
            ) {
                if (layanan.imageUrl != null) {
                    val fullUrl = if (layanan.imageUrl.startsWith("http")) layanan.imageUrl 
                                  else "${com.android.rumahsehatmannawasalwa.BuildConfig.BASE_URL}storage/${layanan.imageUrl}"
                    
                    coil.compose.AsyncImage(
                        model = fullUrl,
                        contentDescription = "Foto ${layanan.nama}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                     Box(contentAlignment = Alignment.Center) {
                        Text("No Img", style = MaterialTheme.typography.labelSmall)
                     }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Info Layanan (Nama, Harga, Durasi)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = layanan.nama,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = com.android.rumahsehatmannawasalwa.ui.theme.TextPrimary,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(layanan.harga)
                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${layanan.durasi} Menit",
                    style = MaterialTheme.typography.bodySmall,
                    color = com.android.rumahsehatmannawasalwa.ui.theme.TextSecondary
                )
            }

            // 3. Icon Chevron Right
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detail",
                tint = com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
