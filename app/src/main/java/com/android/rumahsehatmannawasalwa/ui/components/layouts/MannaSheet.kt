package com.android.rumahsehatmannawasalwa.ui.components.layouts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class MannaSheetState(initialExpanded: Boolean = true) {
    var isExpanded by mutableStateOf(initialExpanded)
        internal set

    fun expand() { isExpanded = true }
    fun collapse() { isExpanded = false }
    fun toggle() { isExpanded = !isExpanded }
}

@Composable
fun rememberMannaSheetState(
    initialExpanded: Boolean = true
): MannaSheetState = remember { MannaSheetState(initialExpanded) }

@Composable
fun MannaSheet(
    modifier: Modifier = Modifier,
    state: MannaSheetState? = null,
    peekHeight: Dp = 300.dp,
    expandedHeight: Dp? = null,
    content: @Composable () -> Unit
) {
    val isDraggable = state != null

    // Tinggi layar sebagai minimum — sheet selalu menutupi background
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val targetHeight = if (state != null) {
        if (state.isExpanded) expandedHeight else peekHeight
    } else null

    val animatedHeight = if (targetHeight != null) {
        animateDpAsState(
            targetValue = targetHeight,
            animationSpec = spring(stiffness = 300f),
            label = "SheetHeight"
        ).value
    } else null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                when {
                    // Mode expandable — pakai tinggi animasi
                    animatedHeight != null -> Modifier.height(animatedHeight)
                    // Mode fixed — minimal setinggi layar agar tidak ada gap hijau
                    else -> Modifier.heightIn(min = screenHeight)
                }
            )
            .then(
                if (isDraggable) {
                    Modifier.pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -30) state?.expand()
                            if (dragAmount > 30) state?.collapse()
                        }
                    }
                } else Modifier
            ),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        shadowElevation = 0.dp
    ) {
        // fillMaxSize agar konten bisa memenuhi tinggi sheet
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag handle — hanya muncul saat expandable
            if (isDraggable) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { state?.toggle() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFDDDDDD), RoundedCornerShape(50))
                    )
                }
            }

            // fillMaxSize agar konten mengisi sisa ruang sheet
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}