package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
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
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils
import com.android.rumahsehatmannawasalwa.utils.IntentUtils.launchWhatsApp

@Composable
fun ServiceCard(
    data: BookingUiModel,
    navController: NavController,
    showPatient: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DividerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. INFO LAYANAN
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = data.service?.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceGrey),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = data.service?.name ?: "Layanan",
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateTextDark,
                        fontSize = 16.sp
                    )
                    Text(
                        text = FormatterUtils.formatTimeRange(
                            data.appointment?.bookingTime,
                            data.service?.duration ?: 60
                        ),
                        fontSize = 13.sp,
                        color = BodyGray
                    )
                }
            }

            HorizontalDivider(color = DividerLight, thickness = 1.dp)

            // 2. INFO TERAPIS
            PersonnelRowContent(
                label = "Terapis",
                name = data.therapist?.name ?: "-",
                photo = data.therapist?.profilePhotoPath ?: data.therapist?.fotoUrl,
                phoneNumber = data.therapist?.phoneNumber
            )

            if (showPatient) {
                HorizontalDivider(color = DividerLight, thickness = 1.dp)
                
                // 3. INFO PASIEN
                PersonnelRowContent(
                    label = "Pasien",
                    name = data.patient?.name ?: "-",
                    photo = data.patient?.profilePhotoPath ?: data.patient?.fotoUrl,
                    phoneNumber = data.patient?.phoneNumber,
                    onClick = { navController.navigate("admin_user_detail/${data.patient?.id}") }
                )
            }
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
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FOTO
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(SurfaceGrey),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(16.dp))
        
        // NAMA & LABEL
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextDark
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = BodyGray
            )
        }

        // WHATSAPP ICON
        if (!phoneNumber.isNullOrBlank()) {
            Surface(
                onClick = { launchWhatsApp(context, phoneNumber) },
                color = GreenSoft,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Hubungi",
                        tint = GreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}