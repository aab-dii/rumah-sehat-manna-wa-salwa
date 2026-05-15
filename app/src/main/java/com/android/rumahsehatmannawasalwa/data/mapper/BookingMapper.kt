package com.android.rumahsehatmannawasalwa.data.mapper

import androidx.compose.ui.graphics.Color
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.booking.AppointmentInfo
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingListItem
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.data.model.booking.DetailAppointmentResponse
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.utils.FormatterUtils

object BookingMapper {
    private fun formatPaymentMethod(m: String?) = when(m?.lowercase()) {
        "transfer" -> "Transfer"
        "cash" -> "Tunai"
        "tunai" -> "Tunai"
        else -> "Tunai"
    }

    private fun formatPaymentStatus(s: String?) = if (s?.lowercase() == "paid") "Lunas" else "Belum Lunas"

    fun mapToUiModel(item: BookingListItem, role: String? = null): BookingUiModel {
        val (label, color) = calculateStatusDisplay(item.status, "unpaid", "cash", role)

        return BookingUiModel(
            appointment = AppointmentInfo(
                id = item.id,
                bookingDate = FormatterUtils.formatDateHuman(item.bookingDate),
                bookingTime = item.bookingTime,
                totalPrice = "",
                status = item.status,
                updatedAt = null,
                cancellationReason = null,
                paymentDeadline = null,
                paymentRemainingSeconds = null
            ),
            displayId = FormatterUtils.formatServiceBookingId(item.id, item.serviceName),
            statusLabel = label,
            statusColor = color,
            paymentMethod = "-", // List view doesn't provide this
            paymentStatus = formatPaymentStatus(item.status),
            patient = User(id = item.patientId, name = item.patientName),
            therapist = User(id = item.therapistId, name = item.therapistName),
            service = Service(name = item.serviceName),
            transaction = null
        )
    }

    fun mapToUiModel(api: DetailAppointmentResponse, role: String? = null): BookingUiModel {
        val d = api.data
        val b = d.appointment
        val t = d.transaction

        val (label, color) = calculateStatusDisplay(
            b.status, t?.status ?: "unpaid",
            t?.paymentMethod ?: "later",
            role
        )

        return BookingUiModel(
            appointment = b.copy(
                totalPrice = FormatterUtils.formatRupiah(b.totalPrice),
                bookingDate = FormatterUtils.formatDateHuman(b.bookingDate)
            ),
            displayId = FormatterUtils.formatServiceBookingId(b.id, d.service?.name),
            statusLabel = label,
            statusColor = color,
            paymentMethod = formatPaymentMethod(t?.paymentMethod),
            paymentStatus = formatPaymentStatus(t?.status),
            patient = d.patient,
            therapist = d.therapist,
            service = d.service,
            transaction = t
        )
    }

    fun calculateStatusDisplay(
        bookingStatus: String,
        transactionStatus: String,
        paymentMethod: String,
        role: String? = null
    ): Pair<String, Color> {
        val bStatus = bookingStatus.lowercase()
        val tStatus = transactionStatus.lowercase()
        val method = paymentMethod.lowercase()
        val blueInfo = Color(0xFF0288D1)

        return when {
            // 1. STATUS AKHIR (Batal/Selesai)
            bStatus == "canceled" -> {
                when (tStatus) {
                    "refund_pending" -> Pair("Proses Refund", blueInfo)
                    "refunded" -> Pair("Dana Dikembalikan", Color(0xFF455A64))
                    else -> Pair("Dibatalkan", Color(0xFFC62828))
                }
            }
            bStatus == "completed" -> Pair("Selesai", Color(0xFF2E7D32))

            // 2. STATUS JADWAL FIX / SEDANG PROSES
            bStatus == "confirmed" -> Pair("Terjadwal", Color(0xFF2E7D32))
            bStatus == "in_progress" -> Pair("Sedang Dilayani", blueInfo)
            bStatus == "force_completed" -> {
                when (role) {
                    "admin" -> Pair("Force Completed", Color(0xFFC62828))
                    "pasien" -> Pair("Selesai", Color(0xFF2E7D32))
                    else -> Pair("⚠️ Perlu Isi Catatan Terapi", Color(0xFFE65100))
                }
            }

            // 3. STATUS LOGIC BARU (getStatusBaruAttribute)
            bStatus == "waiting_payment" -> Pair("Menunggu Pembayaran", Color(0xFFE65100))
            bStatus == "waiting_verification" -> Pair("Menunggu Verifikasi", Color(0xFFFFA000))
            bStatus == "payment_rejected" -> Pair("Pembayaran Ditolak", Color(0xFFD32F2F))

            // 4. ALUR PEMBAYARAN TRANSFER (Konteks Pasien)
            method == "transfer" -> {
                when (tStatus) {
                    "unpaid" -> Pair("Menunggu Pembayaran", Color(0xFFE65100))
                    "pending" -> Pair("Menunggu Verifikasi", Color(0xFFFFA000))
                    "rejected" -> Pair("Pembayaran Ditolak", Color(0xFFD32F2F))
                    else -> Pair("Menunggu Konfirmasi", Color(0xFFFFA000))
                }
            }

            // 5. ALUR CASH / DEFAULT
            else -> Pair("Menunggu Konfirmasi", Color(0xFFFFA000))
        }
    }
}