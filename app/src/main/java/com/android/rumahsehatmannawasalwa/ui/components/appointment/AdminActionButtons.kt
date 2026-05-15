package com.android.rumahsehatmannawasalwa.ui.components.appointment

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaButton
import com.android.rumahsehatmannawasalwa.ui.components.buttons.MannaOutlinedButton
import com.android.rumahsehatmannawasalwa.ui.components.dialog.CustomConfirmDialog
import com.android.rumahsehatmannawasalwa.ui.theme.GreenPrimary
import com.android.rumahsehatmannawasalwa.ui.theme.RedDanger
import com.android.rumahsehatmannawasalwa.ui.theme.StatusConfirmed
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AppointmentDetailViewModel

@Composable
fun AdminActionButtons(
    data: BookingUiModel,
    viewModel: AppointmentDetailViewModel
) {
    val appointmentId = data.appointment?.id ?: return

    // === State untuk dialog konfirmasi (hijau) ===
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmTitle      by remember { mutableStateOf("") }
    var confirmMessage    by remember { mutableStateOf("") }
    var confirmText       by remember { mutableStateOf("Ya, Lanjutkan") }
    var onConfirmAction   by remember { mutableStateOf<(() -> Unit)?>(null) }

    // === State untuk dialog batalkan (merah) ===
    var showCancelDialog by remember { mutableStateOf(false) }

    // === Dialog Konfirmasi (custom, hijau) ===
    CustomConfirmDialog(
        show        = showConfirmDialog,
        onDismiss   = { showConfirmDialog = false },
        onConfirm   = {
            onConfirmAction?.invoke()
            showConfirmDialog = false
        },
        title       = confirmTitle,
        description = confirmMessage,
        confirmText = confirmText,
        dismissText = "Kembali",
        isDanger    = false
    )

    // === Dialog Batalkan (custom, merah) ===
    CustomConfirmDialog(
        show        = showCancelDialog,
        onDismiss   = { showCancelDialog = false },
        onConfirm   = {
            viewModel.updateBookingStatus(appointmentId, "canceled")
            showCancelDialog = false
        },
        title       = "Batalkan Janji Temu?",
        description = "Tindakan ini akan membatalkan jadwal pasien dan tidak dapat dikembalikan.",
        confirmText = "Ya, Batalkan",
        dismissText = "Kembali",
        isDanger    = true
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        val bStatus = data.appointment.status.lowercase()

        when (bStatus) {
            "pending" -> {
                MannaButton(
                    text    = "Konfirmasi Janji Temu",
                    containerColor = GreenPrimary,
                    onClick = {
                        confirmTitle  = "Konfirmasi Janji Temu?"
                        confirmMessage = "Tindakan ini akan memberitahu pasien bahwa jadwal mereka telah disetujui."
                        confirmText   = "Ya, Konfirmasi"
                        onConfirmAction = {
                            viewModel.updateBookingStatus(appointmentId, "confirmed")
                        }
                        showConfirmDialog = true
                    }
                )
            }

            "waiting_verification" -> {
                MannaButton(
                    text    = "Terima Pembayaran",
                    containerColor = GreenPrimary,
                    onClick = {
                        confirmTitle   = "Terima Pembayaran?"
                        confirmMessage = "Pastikan Anda sudah mengecek mutasi atau bukti transfer yang diunggah pasien."
                        confirmText    = "Ya, Terima"
                        onConfirmAction = {
                            viewModel.acceptPayment(appointmentId)
                        }
                        showConfirmDialog = true
                    }
                )
            }

            "confirmed", "terjadwal", "in_progress" -> {
                MannaButton(
                    text    = "Selesaikan (Force-Complete)",
                    containerColor = Color(0xFF0288D1),
                    onClick = {
                        confirmTitle   = "Selesaikan Sesi?"
                        confirmMessage = "Janji temu ini akan diselesaikan tanpa catatan terapi. Terapis masih perlu mengisi catatan setelah ini. Lanjutkan?"
                        confirmText    = "Ya, Selesaikan"
                        onConfirmAction = {
                            viewModel.forceComplete(appointmentId)
                        }
                        showConfirmDialog = true
                    }
                )
            }
        }

        // Tombol Batalkan selalu ada di bawah
        MannaOutlinedButton(
            text    = "Batalkan Janji Temu",
            borderColor = RedDanger,
            contentColor = RedDanger,
            onClick  = { showCancelDialog = true }
        )
    }
}