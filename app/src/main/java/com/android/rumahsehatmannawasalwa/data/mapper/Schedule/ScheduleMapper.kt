package com.android.rumahsehatmannawasalwa.data.mapper.Schedule

import com.android.rumahsehatmannawasalwa.data.model.schedule.Schedule

object ScheduleMapper {
    fun mapResponseToProcessedSchedule(apiSchedules: List<Schedule>): ProcessedSchedule {
        val holidayMap = mutableMapOf<java.time.LocalDate, String>()
        val dateFormatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

        apiSchedules.filter {
            it.type.equals("holiday", ignoreCase = true) || it.type.equals("libur", ignoreCase = true)
        }.forEach { schedule ->
            try {
                val startStr = schedule.specificDate?.take(10) ?: return@forEach
                val endStr = schedule.endDate?.take(10) ?: startStr

                var current = java.time.LocalDate.parse(startStr, dateFormatter)
                val end = java.time.LocalDate.parse(endStr, dateFormatter)

                while (!current.isAfter(end)) {
                    holidayMap[current] = schedule.note ?: "Libur"
                    current = current.plusDays(1)
                }
            } catch (e: Exception) {
                // Log error parsing jika perlu
            }
        }
        val activeDaysList = apiSchedules.filter {
            (it.type.isNullOrEmpty() || it.type.equals("routine", ignoreCase = true)) && it.isActive
        }.mapNotNull { it.day?.trim() }.distinct()

        return ProcessedSchedule(
            holidayInfo = holidayMap,
            activeDays = activeDaysList
        )
    }
}