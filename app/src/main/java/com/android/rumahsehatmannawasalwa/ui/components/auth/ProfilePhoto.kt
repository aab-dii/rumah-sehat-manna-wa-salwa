package com.android.rumahsehatmannawasalwa.ui.components.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary

/**
 * Composable untuk menampilkan foto profil user.
 * Mendukung foto dari URL (Google) atau fallback ke icon default.
 */
@Composable
fun ProfilePhoto(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    isEditMode: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(enabled = isEditMode) { onEditClick() },
            shape = CircleShape,
            color = GreenPrimary.copy(alpha = 0.1f)
        ) {
            if (!photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile Icon",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
        }

        if (isEditMode) {
            Surface(
                onClick = onEditClick,
                modifier = Modifier
                    .size(size * 0.35f)
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Photo",
                        modifier = Modifier.size(size * 0.2f),
                        tint = GreenPrimary
                    )
                }
            }
        }
    }
}
