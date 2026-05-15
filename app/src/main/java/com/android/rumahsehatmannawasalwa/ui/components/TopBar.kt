package com.android.rumahsehatmannawasalwa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.ui.theme.GreenDark
import com.android.rumahsehatmannawasalwa.ui.theme.GreenLight
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary

@Composable
fun TopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomExtra: androidx.compose.ui.unit.Dp = 0.dp,
    transparentBackground: Boolean = false,
    hideBackground: Boolean = false,
    hideContent: Boolean = false,
    contentColor: Color = Color.White
) {
    if (hideBackground && hideContent) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (hideBackground || transparentBackground) {
                    Modifier.wrapContentHeight()
                } else {
                    Modifier
                        .height(220.dp + bottomExtra)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    GreenDark,
                                    GreenPrimary,
                                    GreenLight
                                )
                            )
                        )
                }
            )
    ) {
        // Dekorasi lingkaran Pojok Kanan Atas (Sultan Pattern) - Sembunyikan jika transparan atau disembunyikan
        if (!transparentBackground && !hideBackground) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .align(Alignment.TopEnd)
            )
        }
        
        if (!hideContent) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                .then(
                    if (transparentBackground) {
                        Modifier.padding(vertical = 8.dp) // Lebih rapat & seimbang untuk sticky
                    } else {
                        Modifier
                            .statusBarsPadding()
                            .padding(top = 28.dp)
                    }
                )
                .padding(horizontal = 24.dp)
        ) {
            // Baris Atas: Back button (kalo ada) dan Judul
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = contentColor
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = contentColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp, // Ukuran disesuaikan agar pas di atas
                            lineHeight = 32.sp
                        )
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = contentColor.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Aksi di pojok kanan (notif, dll)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    actions()
                }
            }
            }
        }
    }
}
