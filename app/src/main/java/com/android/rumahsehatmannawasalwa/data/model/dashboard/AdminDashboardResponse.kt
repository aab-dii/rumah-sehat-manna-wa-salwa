package com.android.rumahsehatmannawasalwa.data.model.dashboard

import com.google.gson.annotations.SerializedName

data class AdminDashboardResponse(
    @SerializedName("summary_stats") val summaryStats: SummaryStats,
    @SerializedName("upcoming_schedule") val upcomingSchedule: List<DashboardSchedule>,
    @SerializedName("server_time") val serverTime: String
)

data class SummaryStats(
    @SerializedName("today_revenue") val todayRevenue: Long,
    @SerializedName("verification") val verification: Int,
    @SerializedName("pending") val pending: Int,
    @SerializedName("scheduled_today") val confirmed: Int,

)
