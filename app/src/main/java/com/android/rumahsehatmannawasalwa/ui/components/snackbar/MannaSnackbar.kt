package com.android.rumahsehatmannawasalwa.ui.components.snackbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.ui.theme.*

enum class SnackbarType {
    SUCCESS, ERROR, INFO
}

class MannaSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false,
    val type: SnackbarType
) : SnackbarVisuals

@Composable
fun MannaSnackbar(
    snackbarData: SnackbarData,
    type: SnackbarType = SnackbarType.INFO
) {
    val finalType = (snackbarData.visuals as? MannaSnackbarVisuals)?.type ?: type
    
    val backgroundColor = when (finalType) {
        SnackbarType.SUCCESS -> PrimaryContainerSnackbar
        SnackbarType.ERROR -> ErrorContainerSnackbar
        SnackbarType.INFO -> Color(0xFF2D3133) // inverse-surface
    }

    val contentColor = when (finalType) {
        SnackbarType.SUCCESS -> Color.White
        SnackbarType.ERROR -> OnErrorContainerSnackbar
        SnackbarType.INFO -> Color.White
    }

    val icon = when (finalType) {
        SnackbarType.SUCCESS -> Icons.Default.CheckCircle
        SnackbarType.ERROR -> Icons.Default.Error
        SnackbarType.INFO -> Icons.Default.Info
    }

    val iconBgColor = when (finalType) {
        SnackbarType.SUCCESS -> Color.White.copy(alpha = 0.2f)
        SnackbarType.ERROR -> Color.Red.copy(alpha = 0.1f)
        SnackbarType.INFO -> Color.White.copy(alpha = 0.1f)
    }

    val iconTintColor = when (finalType) {
        SnackbarType.SUCCESS -> Color.White
        SnackbarType.ERROR -> Color.Red
        SnackbarType.INFO -> GreenPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(50.dp),
                ambientColor = backgroundColor.copy(alpha = 0.4f),
                spotColor = backgroundColor.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = snackbarData.visuals.message,
                    color = contentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }

            IconButton(
                onClick = { snackbarData.dismiss() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
