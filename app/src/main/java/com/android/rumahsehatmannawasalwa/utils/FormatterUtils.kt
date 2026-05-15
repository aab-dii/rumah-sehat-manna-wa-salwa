package com.android.rumahsehatmannawasalwa.utils

import android.util.Log
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object FormatterUtils {

    fun formatRupiah(amount: String?): String {
        val numericAmount = amount?.toLongOrNull() ?: 0L
        return formatRupiah(numericAmount)
    }

    fun formatRupiah(amount: Int): String {
        return formatRupiah(amount.toLong())
    }

    fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp. ").substringBeforeLast(",")
    }

    /**
     * Format tanggal dari berbagai format input (ISO, SQL, dsb) ke format manusia Indonesia.
     * Contoh: "2026-03-09 08:35:22" -> "Senin, 9 Maret 2026"
     */
    fun formatDateHuman(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"
        val inputFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "EEEE, d MMMM yyyy"
        )
        val locale = Locale("id", "ID")
        for (pattern in inputFormats) {
            try {
                // Gunakan SimpleDateFormat untuk kompatibilitas yang luas dengan pattern lama
                val parsed = SimpleDateFormat(pattern, Locale.US).parse(raw) ?: continue
                return SimpleDateFormat("EEEE, d MMMM yyyy", locale).format(parsed)
            } catch (_: Exception) {}
        }
        return raw // fallback mentah jika parsing gagal
    }

    fun formatServiceBookingId(id: Int?, serviceName: String?): String {
        if (id == null) return "-"

        // Ambil 2 huruf pertama dari nama layanan, jadikan huruf kapital.
        // Jika nama layanan kosong, gunakan "RS" sebagai default.
        val prefix = serviceName?.trim()?.take(2)?.uppercase(Locale.ROOT) ?: "RS"

        // Format angka ID menjadi 5 digit dengan awalan nol (00099)
        val paddedId = String.format(Locale.US, "%05d", id)

        return "$prefix-$paddedId"
    }

    fun formatTimeRange(bookingTime: String?, durationMinutes: Int = 60): String {
        if (bookingTime.isNullOrBlank()) return "-"
        
        // Jika sudah ada range (mengandung "-"), tambahkan WITA jika belum ada
        if (bookingTime.contains("-")) {
            return if (bookingTime.contains("WITA")) bookingTime else "$bookingTime WITA"
        }

        return try {
            // Ambil bagian jam dan menit, antisipasi format HH:mm:ss
            val parts = bookingTime.trim().split(":")
            if (parts.size < 2) return if (bookingTime.contains("WITA")) bookingTime else "$bookingTime WITA"
            
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            
            // Gunakan Calendar untuk perhitungan waktu yang aman
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                add(java.util.Calendar.MINUTE, durationMinutes)
            }
            
            val endHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val endMinute = calendar.get(java.util.Calendar.MINUTE)
            
            String.format(Locale.US, "%02d:%02d – %02d:%02d WITA", hour, minute, endHour, endMinute)
        } catch (e: Exception) {
            if (bookingTime.contains("WITA")) bookingTime else "$bookingTime WITA"
        }
    }

    fun getTodayDayName(): String {
        val calendar = java.util.Calendar.getInstance()
        return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.SUNDAY -> "Minggu"
            java.util.Calendar.MONDAY -> "Senin"
            java.util.Calendar.TUESDAY -> "Selasa"
            java.util.Calendar.WEDNESDAY -> "Rabu"
            java.util.Calendar.THURSDAY -> "Kamis"
            java.util.Calendar.FRIDAY -> "Jumat"
            java.util.Calendar.SATURDAY -> "Sabtu"
            else -> ""
        }
    }

    fun getTodayFormattedDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        return "${calendar.get(java.util.Calendar.DAY_OF_MONTH)} ${months[calendar.get(java.util.Calendar.MONTH)]} ${calendar.get(java.util.Calendar.YEAR)}"
    }

    fun getTodaySqlDate(): String {
        val calendar = java.util.Calendar.getInstance()
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
    }

    fun formatShortDateIndo(dString: String?): String {
        if (dString.isNullOrEmpty()) return "-"
        return try {
            val locale = Locale("id", "ID")
            val inputFormats = listOf("yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss")
            for (pattern in inputFormats) {
                try {
                    val parsed = SimpleDateFormat(pattern, Locale.US).parse(dString) ?: continue
                    return SimpleDateFormat("d MMM yyyy", locale).format(parsed)
                } catch (_: Exception) {}
            }
            dString
        } catch (e: Exception) {
            dString
        }
    }

    fun formatTimer(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    fun getFullImageUrl(path: String?): String? {
        if (path == null) return null
        if (path.startsWith("http")) return path

        val baseUrl = com.android.rumahsehatmannawasalwa.data.api.RetrofitClient.BASE_URL
        val domain = if (baseUrl.endsWith("/api/")) baseUrl.removeSuffix("api/")
        else if (baseUrl.endsWith("/api")) baseUrl.removeSuffix("api")
        else baseUrl

        // Jika path sudah mengandung 'storage/', jangan didoubel
        val cleanPath = path.removePrefix("/")
        return if (cleanPath.startsWith("storage/")) "$domain$cleanPath" else "${domain}storage/$cleanPath"
    }
}