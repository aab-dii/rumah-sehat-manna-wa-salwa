package com.android.rumahsehatmannawasalwa.data.model.dashboard

import com.google.gson.annotations.SerializedName

// ── Unified Dashboard Response (role-aware) ───────────────────────────────

data class DashboardResponse(
    @SerializedName("today_stats")          val todayStats: TodayStats,
    @SerializedName("upcoming_agenda")      val upcomingAgenda: List<DashboardAgendaItem>,
    @SerializedName("server_today")         val serverToday: String,

    // Admin-only (null for other roles)
    @SerializedName("admin_stats")          val adminStats: AdminStats?   = null,
    @SerializedName("monthly_revenue")      val monthlyRevenue: Long?     = null,
)

data class AdminStats(
    @SerializedName("confirmed")            val confirmed: Int = 0,
    @SerializedName("pending")              val pending: Int = 0,
    @SerializedName("waiting_verification") val waitingVerification: Int = 0,
    @SerializedName("canceled")             val canceled: Int = 0,
)

data class TodayStats(
    @SerializedName("confirmed") val confirmed: Int = 0,
    @SerializedName("completed") val completed: Int = 0,
    @SerializedName("canceled")  val canceled: Int  = 0,
    @SerializedName("total")     val total: Int     = 0,
)

data class DashboardAgendaItem(
    @SerializedName("id")             val id: Int,
    @SerializedName("patient_name")   val patientName: String,
    @SerializedName("therapist_name") val therapistName: String,
    @SerializedName("service_name")   val serviceName: String,
    @SerializedName("booking_date")   val bookingDate: String,
    @SerializedName("booking_time")   val bookingTime: String,
    @SerializedName("status")         val status: String,
)
