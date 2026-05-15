package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.ui.theme.*
import com.android.rumahsehatmannawasalwa.utils.IntentUtils.launchWhatsApp

//@Composable
//fun PersonnelSection(data: BookingUiModel, navController: NavController) {
//    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//        // 1. BARIS PASIEN
//        PersonnelRowCard(
//            label = "Pasien",
//            name = data.patient?.name ?: "-",
//            photo = data.patient?.profilePhotoPath,
//            phoneNumber = data.patient?.phoneNumber,
//            onClick = { navController.navigate("admin_user_detail/${data.patient?.id}") }
//        )
//
//        // 2. BARIS TERAPIS
//        PersonnelRowCard(
//            label = "Terapis",
//            name = data.therapist?.name ?: "-",
//            photo = data.therapist?.profilePhotoPath,
//            phoneNumber = data.therapist?.phoneNumber // Pastikan model Terapis ada phone_number-nya
//        )
//    }
//}
//
//@Composable
//fun PersonnelRowCard(
//    label: String,
//    name: String,
//    photo: String?,
//    phoneNumber: String? = null,
//    onClick: (() -> Unit)? = null
//) {
//    val context = LocalContext.current
//    val photoUrl = if (!photo.isNullOrEmpty()) {
//        "http://192.168.1.7:8000/storage/$photo"
//    } else "https://ui-avatars.com/api/?name=$name"
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp).fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            AsyncImage(
//                model = photoUrl,
//                contentDescription = null,
//                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFF5F5F5)),
//                contentScale = ContentScale.Crop
//            )
//            Spacer(Modifier.width(12.dp))
//            Column(modifier = Modifier.weight(1f)) {
//                Text(label, fontSize = 11.sp, color = Color.Gray)
//                Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
//            }
//            if (!phoneNumber.isNullOrBlank()) {
//                Surface(
//                    onClick = { launchWhatsApp(context, phoneNumber) },
//                    color = Color(0xFFE8F5E9),
//                    shape = RoundedCornerShape(8.dp),
//                    modifier = Modifier.size(36.dp)
//                ) {
//                    Box(contentAlignment = Alignment.Center) {
//                        Icon(Icons.Default.Phone, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun PersonnelSection(
    data: BookingUiModel,
    navController: NavController,
    showPatient: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DividerLight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            if (showPatient) {
                PersonnelRowContent(
                    label = "Pasien",
                    name = data.patient?.name ?: "-",
                    photo = data.patient?.profilePhotoPath,
                    phoneNumber = data.patient?.phoneNumber,
                    onClick = { navController.navigate("admin_user_detail/${data.patient?.id}") }
                )
            }


            // GARIS PEMISAH (Divider)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = SurfaceGrey,
                thickness = 1.dp
            )

            // 2. BARIS TERAPIS
            PersonnelRowContent(
                label = "Terapis",
                name = data.therapist?.name ?: "-",
                photo = data.therapist?.profilePhotoPath,
                phoneNumber = data.therapist?.phoneNumber
            )
        }
    }
}

@Composable
private fun PersonnelRowContent(
    label: String,
    name: String,
    photo: String?,
    phoneNumber: String? = null,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val photoUrl = if (!photo.isNullOrEmpty()) {
        photo
    } else "https://ui-avatars.com/api/?name=$name"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FOTO
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceGrey),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        // NAMA & LABEL
        Column(modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy((-4).dp)
        ) {
            Text(label, fontSize = 11.sp, color = GrayText)
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // WHATSAPP ICON (Tetap di ujung kanan)
        if (!phoneNumber.isNullOrBlank()) {
            Surface(
                onClick = { launchWhatsApp(context, phoneNumber) },
                color = GreenSoft,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Phone,
                        null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}