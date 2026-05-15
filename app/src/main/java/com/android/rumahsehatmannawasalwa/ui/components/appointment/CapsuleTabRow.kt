package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CapsuleTabRow(
    tabs: List<String>,
    pagerState: PagerState,
    onTabSelected: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(DividerLight, RoundedCornerShape(50.dp))
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / tabs.size

        // LOGIK SAKTI: Hitung posisi berdasarkan currentPage + offset (swipe jari)
        val indicatorOffset by remember {
            derivedStateOf {
                tabWidth * (pagerState.currentPage + pagerState.currentPageOffsetFraction)
            }
        }

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .shadow(2.dp, RoundedCornerShape(50.dp))
                .background(Color.White, RoundedCornerShape(50.dp))
        )

        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, title ->
                // Warna teks juga berubah perlahan berdasarkan progress swipe
                val isSelected = pagerState.currentPage == index
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) GreenPrimary else GrayText,
                    label = "TextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
        }
    }
}