package com.android.rumahsehatmannawasalwa.ui.components.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.android.rumahsehatmannawasalwa.ui.theme.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun AuthSheet(
    logo: @Composable BoxScope.() -> Unit,
    snackbarHost: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val maxHeaderHeight = 300.dp

    // Animation calculations based on scroll
    // Logo shrinks and fades as user scrolls up
    val headerHeight = (300 - (scrollState.value / 2)).coerceAtLeast(120).dp
    val logoScale = (1f - (scrollState.value / 600f)).coerceAtLeast(0.5f)
    val logoAlpha = (1f - (scrollState.value / 400f)).coerceAtLeast(0f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GreenDark,
                        GreenPrimary,
                        GreenLight
                    )
                )
            )
    ) {
        // 1. Logo Section (Background area) - Animated
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .graphicsLayer {
                    alpha = logoAlpha
                    scaleX = logoScale
                    scaleY = logoScale
                },
            contentAlignment = Alignment.Center
        ) {
            logo()
        }

        // 2. Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Spacer to keep the sheet below the logo initially
            Spacer(modifier = Modifier.height(260.dp))

            // The Sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    content()

                    // Extra spacer at bottom for comfortable scrolling
                    Spacer(modifier = Modifier.height(128.dp))
                }
            }
        }

        // 3. Snackbar Host (Anchored to screen bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            snackbarHost()
        }
    }
}
