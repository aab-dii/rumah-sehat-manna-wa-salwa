package com.android.rumahsehatmannawasalwa.ui.screens.patient.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rumahsehatmannawasalwa.ui.theme.DividerColor
import com.android.rumahsehatmannawasalwa.ui.theme.DividerLight
import com.android.rumahsehatmannawasalwa.ui.theme.GrayText
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.SlateTextDark

/**
 * Bottom sheet untuk memilih periode filter riwayat terapi.
 *
 * @param selectedOption    Pilihan radio yang sedang aktif
 * @param fromDay/Month/Year State tanggal mulai (custom range)
 * @param toDay/Month/Year  State tanggal akhir (custom range)
 * @param onApply           Dipanggil saat tombol "Terapkan" ditekan dengan (from, to) date string
 * @param onDismiss         Dipanggil saat sheet ditutup
 * @param onOptionChange    Dipanggil saat radio button berubah
 * @param onFromChange      Dipanggil saat custom from-date berubah (day, month, year)
 * @param onToChange        Dipanggil saat custom to-date berubah (day, month, year)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TherapyRecordFilterSheet(
    sheetState: SheetState,
    selectedOption: PeriodOption,
    fromDay: Int, fromMonth: Int, fromYear: Int,
    toDay: Int, toMonth: Int, toYear: Int,
    onOptionChange: (PeriodOption) -> Unit,
    onFromChange: (day: Int, month: Int, year: Int) -> Unit,
    onToChange:   (day: Int, month: Int, year: Int) -> Unit,
    onApply: (from: String?, to: String?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filter Periode",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = SlateTextDark
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                }
            }
            Spacer(Modifier.height(4.dp))

            // ── Radio list ──────────────────────────────────────────────
            Column(modifier = Modifier.selectableGroup()) {
                periodOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedOption == option,
                                onClick  = { onOptionChange(option) },
                                role     = Role.RadioButton
                            )
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.label,
                            fontSize = 15.sp,
                            fontWeight = if (selectedOption == option) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedOption == option) GreenPrimary else Color(0xFF334155)
                        )
                        RadioButton(
                            selected = selectedOption == option,
                            onClick  = null,
                            colors   = RadioButtonDefaults.colors(
                                selectedColor   = GreenPrimary,
                                unselectedColor = GrayText
                            )
                        )
                    }
                    if (option != periodOptions.last()) {
                        HorizontalDivider(color = DividerLight)
                    }
                }
            }

            // ── Custom range ─────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(16.dp))
            Text("Atau Tentukan Rentang", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateTextDark)
            Spacer(Modifier.height(12.dp))

            FilterDateDropdownRow(
                label = "Dari tanggal",
                day = fromDay, month = fromMonth, year = fromYear,
                onDayChange   = { onFromChange(it, fromMonth, fromYear); onOptionChange(PeriodOption.Semua) },
                onMonthChange = { onFromChange(fromDay, it, fromYear);   onOptionChange(PeriodOption.Semua) },
                onYearChange  = { onFromChange(fromDay, fromMonth, it);  onOptionChange(PeriodOption.Semua) }
            )
            Spacer(Modifier.height(12.dp))
            FilterDateDropdownRow(
                label = "Sampai tanggal",
                day = toDay, month = toMonth, year = toYear,
                onDayChange   = { onToChange(it, toMonth, toYear);  onOptionChange(PeriodOption.Semua) },
                onMonthChange = { onToChange(toDay, it, toYear);    onOptionChange(PeriodOption.Semua) },
                onYearChange  = { onToChange(toDay, toMonth, it);   onOptionChange(PeriodOption.Semua) }
            )

            Spacer(Modifier.height(24.dp))

            // ── Terapkan ─────────────────────────────────────────────────
            Button(
                onClick = {
                    val (from, to) = if (selectedOption != PeriodOption.Semua) {
                        selectedOption.toDateRange()
                    } else {
                        buildDate(fromDay, fromMonth, fromYear) to buildDate(toDay, toMonth, toYear)
                    }
                    onApply(from, to)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Terapkan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ── Reusable date row (day / month / year dropdowns) ────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDateDropdownRow(
    label: String,
    day: Int, month: Int, year: Int,
    onDayChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit
) {
    val days = (1..daysInMonth(month, year)).toList()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterDropdown(
                value    = "$day",
                options  = days.map { "$it" },
                onSelect = { onDayChange(it.toInt()) },
                modifier = Modifier.weight(1.2f)
            )
            FilterDropdown(
                value    = months[month],
                options  = months,
                onSelect = { onMonthChange(months.indexOf(it)) },
                modifier = Modifier.weight(2f)
            )
            FilterDropdown(
                value    = "$year",
                options  = years.map { "$it" },
                onSelect = { onYearChange(it.toInt()) },
                modifier = Modifier.weight(1.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ExpandMore, null, modifier = Modifier.size(18.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GreenPrimary,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape     = RoundedCornerShape(10.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
            modifier  = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text    = { Text(opt, fontSize = 13.sp) },
                    onClick = { onSelect(opt); expanded = false }
                )
            }
        }
    }
}
