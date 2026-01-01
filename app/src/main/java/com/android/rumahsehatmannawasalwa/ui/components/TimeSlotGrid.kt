package com.android.rumahsehatmannawasalwa.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotGrid(
    slots: List<String>,
    selectedSlot: String?,
    onSlotSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (slots.isEmpty()) {
        Text(
            text = "Tidak ada jadwal tersedia",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 80.dp),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.heightIn(max = 300.dp) // Constrain height
        ) {
            items(slots) { slot ->
                val isSelected = slot == selectedSlot
                
                FilterChip(
                    selected = isSelected,
                    onClick = { onSlotSelected(slot) },
                    label = {
                        Text(
                            text = slot,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
