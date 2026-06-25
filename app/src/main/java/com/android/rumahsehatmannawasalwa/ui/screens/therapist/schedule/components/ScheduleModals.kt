package com.android.rumahsehatmannawasalwa.ui.screens.therapist.schedule.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.RedDanger
import com.android.rumahsehatmannawasalwa.ui.theme.TextPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyCloseSheet(
    dateString: String,
    sheetState: SheetState,
    scope: CoroutineScope,
    onDismissRequest: () -> Unit,
    onConfirmClose: (String) -> Unit
) {
    var cancellationReason by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Penutupan Darurat",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RedDanger
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aksi ini akan membatalkan SEMUA booking aktif pada hari ini ($dateString). Pasien akan diberitahu.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = cancellationReason,
                onValueChange = { cancellationReason = it },
                label = { Text("Alasan Penutupan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = RedDanger,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = RedDanger,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = RedDanger
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

            Button(
                onClick = {
                    if (cancellationReason.isBlank()) {
                        android.widget.Toast.makeText(context, "Mohon isi alasan", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        focusManager.clearFocus()
                        showConfirmDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Konfirmasi Tutup & Batalkan Booking")
            }

            if (showConfirmDialog) {
                com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog(
                    show = showConfirmDialog,
                    isDanger = true,
                    title = "Yakin Tutup Darurat?",
                    description = "Aksi ini tidak dapat dibatalkan dan akan membatalkan semua booking hari ini. Yakin lanjutkan?",
                    confirmText = "Ya, Tutup Darurat",
                    dismissText = "Batal",
                    onDismiss = { showConfirmDialog = false },
                    onConfirm = {
                        showConfirmDialog = false
                        onConfirmClose(cancellationReason)
                    }
                )
            }
        }
    }
}

@Composable
fun AddHolidayDialog(
    onDismissRequest: () -> Unit,
    onConfirmHoliday: (String, String, String) -> Unit
) {
    var holidayStartDate by remember { mutableStateOf("") }
    var holidayEndDate by remember { mutableStateOf("") }
    var holidayReason by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text("Tambah Hari Libur Ekstra", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePickerField(label = "Tanggal Mulai", date = holidayStartDate) { holidayStartDate = it }
                DatePickerField(label = "Tanggal Selesai", date = holidayEndDate) { holidayEndDate = it }
                OutlinedTextField(
                    value = holidayReason,
                    onValueChange = { holidayReason = it },
                    label = { Text("Alasan") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = GreenPrimary,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = GreenPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (holidayStartDate.isBlank() || holidayEndDate.isBlank() || holidayReason.isBlank()) {
                        android.widget.Toast.makeText(context, "Mohon lengkapi data", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirmHoliday(holidayStartDate, holidayEndDate, holidayReason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal", color = GreenPrimary)
            }
        }
    )
}

@Composable
fun DatePickerField(label: String, date: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formatted)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedTextField(
        value = date,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = GreenPrimary,
            unfocusedLabelColor = TextSecondary,
            cursorColor = GreenPrimary
        ),
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = GreenPrimary
                )
            }
        },
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                            datePickerDialog.show()
                        }
                    }
                }
            }
    )
}
