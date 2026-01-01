package com.android.rumahsehatmannawasalwa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HorizontalCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveStartDate = remember {
        if (java.time.LocalTime.now().isAfter(java.time.LocalTime.of(20, 0))) 
            LocalDate.now().plusDays(1) 
        else 
            LocalDate.now()
    }
    var currentWeekStart by remember { mutableStateOf(effectiveStartDate) }
    
    val days = (0..6).map { currentWeekStart.plusDays(it.toLong()) }
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID"))
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale("id", "ID"))
    val dateFormatter = DateTimeFormatter.ofPattern("dd")

    Column(modifier = modifier) {
        // Header (Month Year + Navigation)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) },
                enabled = currentWeekStart.isAfter(effectiveStartDate)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, 
                    contentDescription = "Previous Week",
                    tint = if (currentWeekStart.isAfter(effectiveStartDate)) MaterialTheme.colorScheme.onSurface else Color.LightGray
                )
            }
            Text(
                text = currentWeekStart.format(monthFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Week")
            }
        }

        // Days Row
        LazyRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days) { date ->
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()

                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else if (isToday) MaterialTheme.colorScheme.secondaryContainer
                            else Color.Transparent
                        )
                        .clickable { onDateSelected(date) }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.format(dayFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = date.format(dateFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
