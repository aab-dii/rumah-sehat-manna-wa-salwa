package com.android.rumahsehatmannawasalwa.data.mapper.Schedule

data class ProcessedSchedule(
    val holidayInfo: Map<java.time.LocalDate, String>,
    val activeDays: List<String>
)