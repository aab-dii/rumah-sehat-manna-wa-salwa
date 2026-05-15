package com.android.rumahsehatmannawasalwa.ui.screens.therapist.schedule.components

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.rumahsehatmannawasalwa.data.model.schedule.Schedule
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.GreenDark
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog
import java.util.*

@Composable
fun ScheduleListSection(
    days: List<String>,
    schedules: List<Schedule>,
    therapistId: Int,
    onSaveSchedule: (Int, String, String, String, Boolean) -> Unit
) {
    Column {
        Text(
            text = "Jadwal Rutin Mingguan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            days.forEachIndexed { index, dName ->
                val existing = schedules.find { it.day.equals(dName, ignoreCase = true) }
                ScheduleItem(
                    dayName = dName,
                    initialIsActive = existing?.isActive ?: false,
                    initialStartTime = existing?.startTime?.take(5) ?: "09:00",
                    initialEndTime = existing?.endTime?.take(5) ?: "21:00",
                    isLastItem = index == days.lastIndex,
                    onSave = { isActive, start, end ->
                        onSaveSchedule(therapistId, dName, start, end, isActive)
                    }
                )
            }
        }
    }
}

@Composable
fun ScheduleItem(
    dayName: String,
    initialIsActive: Boolean,
    initialStartTime: String,
    initialEndTime: String,
    isLastItem: Boolean,
    onSave: (Boolean, String, String) -> Unit
) {
    var isActive by remember(initialIsActive) { mutableStateOf(initialIsActive) }
    var startTime by remember(initialStartTime) { mutableStateOf(initialStartTime) }
    var endTime by remember(initialEndTime) { mutableStateOf(initialEndTime) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingToggleState by remember { mutableStateOf(false) }

// Using imported tokens

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) GreenDark else Color.Gray
                )
                Text(
                    text = if (isActive) "Beroperasi" else "Libur Rutin",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) GreenPrimary else Color.Gray.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    TimeCapsulePicker(time = startTime) {
                        startTime = it
                        onSave(isActive, it, endTime)
                    }
                    Text(" - ", color = Color.Gray, fontWeight = FontWeight.Bold)
                    TimeCapsulePicker(time = endTime) {
                        endTime = it
                        onSave(isActive, startTime, it)
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Switch(
                checked = isActive,
                onCheckedChange = {
                    pendingToggleState = it
                    showConfirmDialog = true
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GreenPrimary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)
                ),
                modifier = Modifier.scale(0.8f)
            )
        }

        if (!isLastItem) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.3f)
            )
        }
        
        if (showConfirmDialog) {
            val isTurningOff = !pendingToggleState
            CustomConfirmDialog(
                show = showConfirmDialog,
                isDanger = isTurningOff,
                title = if (pendingToggleState) "Aktifkan Jadwal?" else "Liburkan Jadwal?",
                description = if (pendingToggleState) "Apakah Anda yakin ingin mengaktifkan kembali jadwal rutin untuk hari $dayName?" else "Apakah Anda yakin ingin meliburkan / mematikan jadwal rutin untuk hari $dayName?",
                confirmText = if (pendingToggleState) "Ya, Aktifkan" else "Ya, Liburkan",
                dismissText = "Batal",
                onDismiss = { showConfirmDialog = false },
                onConfirm = {
                    isActive = pendingToggleState
                    onSave(pendingToggleState, startTime, endTime)
                    showConfirmDialog = false
                }
            )
        }
    }
}

@Composable
fun TimeCapsulePicker(time: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            onTimeSelected(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
        },
        time.split(":")[0].toInt(),
        time.split(":")[1].toInt(),
        true
    )

    Text(
        text = time,
        modifier = Modifier
            .clickable { timePickerDialog.show() }
            .padding(horizontal = 4.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = GreenPrimary
    )
}
