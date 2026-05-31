package com.android.rumahsehatmannawasalwa.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary

@Composable
fun SectionTitle(
    title: String, 
    color: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = color,
        modifier = modifier
    )
}

@Composable
fun DetailRowSejajar(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Black,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
fun ActionOverlay() {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = GreenPrimary)
                    Spacer(Modifier.width(16.dp))
                    Text("Memproses...", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun ErrorPlaceholder(msg: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(msg, color = Color.Red, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Coba Lagi") }
    }
}