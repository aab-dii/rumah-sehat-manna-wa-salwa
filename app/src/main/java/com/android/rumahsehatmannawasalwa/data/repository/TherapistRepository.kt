package com.android.rumahsehatmannawasalwa.data.repository

import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary
import com.android.rumahsehatmannawasalwa.data.model.schedule.AddHolidayRequest
import com.android.rumahsehatmannawasalwa.data.model.schedule.EmergencyCloseRequest
import com.android.rumahsehatmannawasalwa.data.model.schedule.Schedule
import com.android.rumahsehatmannawasalwa.data.model.schedule.UpdateScheduleRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TherapistRepository(
    private val apiService: ApiService
) {

    // PATIENT HISTORY
    fun getPatientHistory(patientId: Int): Flow<ApiResult<List<TherapyHistorySummary>>> = flow {
        emit(ApiResult.Loading)
        try {
            // Updated to use the new signature from ApiService
            val response = apiService.getTherapyRecords(page = 1, limit = 50, patientId = patientId)
            // Response is now the object directly, not Response<T>
            if (response.data.data.isNotEmpty()) {
                emit(ApiResult.Success(response.data.data))
            } else {
                 emit(ApiResult.Success(emptyList())) // Return empty list if no data
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    // --- Schedules ---

    fun getSchedules(therapistId: Int): Flow<ApiResult<List<Schedule>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getSchedules(therapistId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error("Gagal mengambil jadwal"))
            }
        } catch (e: Exception) {
             emit(ApiResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    fun updateSchedule(request: UpdateScheduleRequest): Flow<ApiResult<Schedule>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.updateSchedule(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error("Gagal update jadwal"))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    fun emergencyClose(therapistId: Int, reason: String): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading)
        try {
            val request = EmergencyCloseRequest(
                therapistId = therapistId, 
                date = java.time.LocalDate.now().toString(), 
                reason = reason
            )
            val response = apiService.emergencyClose(request)
            if (response.isSuccessful) {
                emit(ApiResult.Success(response.body()?.meta?.message ?: "Berhasil menutup klinik"))
            } else {
                val errorBody = response.errorBody()?.string()
                emit(ApiResult.Error(errorBody ?: "Gagal menutup klinik"))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    fun addHoliday(request: AddHolidayRequest): Flow<ApiResult<String>> = flow {
         emit(ApiResult.Loading)
         try {
             val response = apiService.addHoliday(request)
             if (response.isSuccessful) {
                 emit(ApiResult.Success(response.body()?.meta?.message ?: "Berhasil menambahkan hari libur"))
             } else {
                 emit(ApiResult.Error("Gagal menambahkan hari libur"))
             }
         } catch (e: Exception) {
             emit(ApiResult.Error(e.message ?: "Terjadi kesalahan"))
         }
    }

}
