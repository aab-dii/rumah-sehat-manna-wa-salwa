package com.android.rumahsehatmannawasalwa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdown(
    modifier: Modifier = Modifier,
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String,
    placeholder: String = "Pilih..."
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Update text field value when selectedItem changes externally
    LaunchedEffect(selectedItem) {
        if (selectedItem != null) {
            searchQuery = itemToString(selectedItem)
        }
    }

    val filteredItems = remember(items, searchQuery, expanded) {
        if (expanded) {
            if (searchQuery.isNotEmpty() && selectedItem != null && searchQuery != itemToString(selectedItem)) {
                // User is typing to search
                items.filter { itemToString(it).contains(searchQuery, ignoreCase = true) }
            } else if (searchQuery.isNotEmpty() && selectedItem == null) {
                 // User is typing to search (nothing selected yet)
                 items.filter { itemToString(it).contains(searchQuery, ignoreCase = true) }
            } else {
                items
            }
        } else {
            items
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        expanded = false
                        // Reset query to selected item if lost focus and valid selection exists
                        if (selectedItem != null) {
                            searchQuery = itemToString(selectedItem)
                        }
                    } else {
                        // When focused, we might want to show list?
                        // Optional: expanded = true
                    }
                },
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (filteredItems.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Tidak ada data") },
                    onClick = { }
                )
            } else {
                filteredItems.take(5).forEach { item -> // Limit to 5 or scrollable
                    DropdownMenuItem(
                        text = { Text(itemToString(item)) },
                        onClick = {
                            onItemSelected(item)
                            searchQuery = itemToString(item)
                            expanded = false
                            focusManager.clearFocus() 
                        }
                    )
                }
            }
        }
    }
}
