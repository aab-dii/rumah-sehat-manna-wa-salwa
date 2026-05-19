package com.android.rumahsehatmannawasalwa.data.model.report

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════
// SHARED
// ═══════════════════════════════════════════════════════

data class PaginationMeta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int
)

// ═══════════════════════════════════════════════════════
// 1. LAPORAN KEUANGAN
// ═══════════════════════════════════════════════════════

data class FinancialReportResponse(
    @SerializedName("period") val period: String,
    @SerializedName("total_revenue") val totalRevenue: Long,
    @SerializedName("total_success") val totalSuccess: Int,
    @SerializedName("total_canceled") val totalCanceled: Int,
    @SerializedName("revenue_by_service") val revenueByService: List<ServiceRevenue>,
    @SerializedName("transactions") val transactions: List<FinancialTransaction>,
    @SerializedName("pagination") val pagination: PaginationMeta?
)

data class ServiceRevenue(
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("revenue") val revenue: Long
)

data class FinancialTransaction(
    @SerializedName("id") val id: Int,
    @SerializedName("booking_date") val bookingDate: String?,
    @SerializedName("booking_no") val bookingNo: String,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("therapist_name") val therapistName: String,
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("status") val status: String,
    @SerializedName("total_amount") val totalAmount: Long
)

// ═══════════════════════════════════════════════════════
// 2. LAPORAN KUNJUNGAN
// ═══════════════════════════════════════════════════════

data class VisitReportResponse(
    @SerializedName("therapist_name") val therapistName: String,
    @SerializedName("therapist_address") val therapistAddress: String,
    @SerializedName("period") val period: String,
    @SerializedName("summary") val summary: VisitSummary,
    @SerializedName("visits") val visits: List<VisitItem>,
    @SerializedName("pagination") val pagination: PaginationMeta?
)

data class VisitSummary(
    @SerializedName("total_male") val totalMale: Int,
    @SerializedName("total_female") val totalFemale: Int,
    @SerializedName("total_new") val totalNew: Int,
    @SerializedName("total_old") val totalOld: Int,
    @SerializedName("total_ramuan") val totalRamuan: Int,
    @SerializedName("total_keterampilan") val totalKeterampilan: Int,
    @SerializedName("total_kombinasi") val totalKombinasi: Int,
    @SerializedName("total_visits") val totalVisits: Int
)

data class VisitItem(
    @SerializedName("no") val no: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("date") val date: String,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("patient_age") val patientAge: Int?,
    @SerializedName("address") val address: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("is_new") val isNew: Boolean,
    @SerializedName("complaint") val complaint: String,
    @SerializedName("is_ramuan") val isRamuan: Boolean,
    @SerializedName("is_keterampilan") val isKeterampilan: Boolean,
    @SerializedName("is_kombinasi") val isKombinasi: Boolean,
    @SerializedName("notes") val notes: String
)

// ═══════════════════════════════════════════════════════
// 3. LAPORAN KINERJA TERAPIS
// ═══════════════════════════════════════════════════════

data class PerformanceReportResponse(
    @SerializedName("period") val period: String,
    @SerializedName("therapists") val therapists: List<TherapistPerformance>
)

data class TherapistPerformance(
    @SerializedName("therapist_name") val therapistName: String,
    @SerializedName("total_sessions") val totalSessions: Int,
    @SerializedName("total_patients") val totalPatients: Int,
    @SerializedName("new_patients") val newPatients: Int,
    @SerializedName("old_patients") val oldPatients: Int,
    @SerializedName("total_bekam") val totalBekam: Int,
    @SerializedName("total_akupunktur") val totalAkupunktur: Int,
    @SerializedName("total_ramuan") val totalRamuan: Int,
    @SerializedName("total_revenue") val totalRevenue: Long,
    @SerializedName("total_canceled") val totalCanceled: Int
)

// ═══════════════════════════════════════════════════════
// 4. LAPORAN KEGIATAN KLINIK
// ═══════════════════════════════════════════════════════

data class ActivityReportResponse(
    @SerializedName("period") val period: String,
    @SerializedName("summary") val summary: ActivitySummary,
    @SerializedName("service_breakdown") val serviceBreakdown: List<ServiceBreakdown>
)

data class ActivitySummary(
    @SerializedName("total_visits") val totalVisits: Int,
    @SerializedName("new_patients") val newPatients: Int,
    @SerializedName("old_patients") val oldPatients: Int,
    @SerializedName("total_revenue") val totalRevenue: Long,
    @SerializedName("top_service") val topService: String,
    @SerializedName("top_therapist") val topTherapist: String
)

data class ServiceBreakdown(
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("total_sessions") val totalSessions: Int,
    @SerializedName("percentage") val percentage: Double,
    @SerializedName("revenue") val revenue: Long
)

// ═══════════════════════════════════════════════════════
// 5. LAPORAN KOMPARATIF TERAPIS
// ═══════════════════════════════════════════════════════

data class ComparativeReportResponse(
    @SerializedName("period") val period: String,
    @SerializedName("comparative") val comparative: List<ComparativeItem>
)

data class ComparativeItem(
    @SerializedName("ranking") val ranking: Int,
    @SerializedName("therapist_name") val therapistName: String,
    @SerializedName("total_sessions") val totalSessions: Int,
    @SerializedName("total_patients") val totalPatients: Int,
    @SerializedName("revenue") val revenue: Long,
    @SerializedName("percentage") val percentage: Double,
    @SerializedName("trend") val trend: String,
    @SerializedName("visual_bar") val visualBar: String
)
