package com.android.rumahsehatmannawasalwa.ui.screens.patient.record

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ── Format helpers ──────────────────────────────────────────────────────────
internal val isoFmt     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
internal val displayFmt = SimpleDateFormat("d MMM yyyy", Locale("id"))

internal fun today(): Calendar = Calendar.getInstance()

// ── Preset Periode ──────────────────────────────────────────────────────────
internal sealed class PeriodOption(val label: String) {
    object Semua     : PeriodOption("Semua")
    object HariIni   : PeriodOption("Hari Ini")
    object Kemarin   : PeriodOption("Kemarin")
    object TujuhHari : PeriodOption("7 Hari Terakhir")
    object BulanIni  : PeriodOption("Bulan Ini")
    object BulanLalu : PeriodOption("Bulan Lalu")
}

internal val periodOptions = listOf(
    PeriodOption.Semua,
    PeriodOption.HariIni,
    PeriodOption.Kemarin,
    PeriodOption.TujuhHari,
    PeriodOption.BulanIni,
    PeriodOption.BulanLalu
)

internal fun PeriodOption.toDateRange(): Pair<String?, String?> {
    val cal      = today()
    val todayStr = isoFmt.format(cal.time)
    return when (this) {
        is PeriodOption.Semua     -> null to null
        is PeriodOption.HariIni   -> todayStr to todayStr
        is PeriodOption.Kemarin   -> {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val y = isoFmt.format(cal.time); y to y
        }
        is PeriodOption.TujuhHari -> {
            cal.add(Calendar.DAY_OF_YEAR, -6)
            isoFmt.format(cal.time) to todayStr
        }
        is PeriodOption.BulanIni  -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            isoFmt.format(cal.time) to todayStr
        }
        is PeriodOption.BulanLalu -> {
            cal.add(Calendar.MONTH, -1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val start = isoFmt.format(cal.time)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            start to isoFmt.format(cal.time)
        }
    }
}

// ── Date dropdown helpers ───────────────────────────────────────────────────
internal val months = listOf(
    "Januari","Februari","Maret","April","Mei","Juni",
    "Juli","Agustus","September","Oktober","November","Desember"
)

internal val years: List<Int> = run {
    val y = Calendar.getInstance().get(Calendar.YEAR)
    (y downTo y - 5).toList()
}

internal fun daysInMonth(month: Int, year: Int): Int =
    Calendar.getInstance().apply { set(year, month, 1) }
        .getActualMaximum(Calendar.DAY_OF_MONTH)

internal fun buildDate(day: Int, month: Int, year: Int): String =
    isoFmt.format(Calendar.getInstance().apply { set(year, month, day) }.time)

// ── Filter button label ────────────────────────────────────────────────────
internal fun buildFilterLabel(
    hasFilter: Boolean,
    selectedOption: PeriodOption,
    dateFrom: String?,
    dateTo: String?
): String = when {
    !hasFilter -> "Semua Periode"
    selectedOption != PeriodOption.Semua -> selectedOption.label
    dateFrom != null && dateTo != null -> {
        val d1 = try { displayFmt.format(isoFmt.parse(dateFrom)!!) } catch (_: Exception) { dateFrom }
        val d2 = try { displayFmt.format(isoFmt.parse(dateTo)!!)   } catch (_: Exception) { dateTo }
        if (d1 == d2) d1 else "$d1 – $d2"
    }
    else -> "Semua Periode"
}
