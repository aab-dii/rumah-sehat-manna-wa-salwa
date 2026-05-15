package com.android.rumahsehatmannawasalwa.ui.viewmodel.schedule

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.model.schedule.Schedule
import com.android.rumahsehatmannawasalwa.data.model.schedule.UpdateScheduleRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val api = RetrofitClient.instance

    private val _scheduleState = MutableStateFlow<ApiResult<List<Schedule>>>(ApiResult.Loading)
    val scheduleState: StateFlow<ApiResult<List<Schedule>>> = _scheduleState

    // Cache local schedules to easy access defaults if API returns empty
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    fun fetchSchedules(therapistId: Int) {
        viewModelScope.launch {
            _scheduleState.value = ApiResult.Loading
            try {
                val response = api.getSchedules(therapistId)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.meta.status == "success") {
                        val data = result.data ?: emptyList()
                        _schedules.value = data
                        _scheduleState.value = ApiResult.Success(data)
                    } else {
                        _scheduleState.value = result.meta.message?.let { ApiResult.Error(it) }!!
                    }
                } else {
                    _scheduleState.value = ApiResult.Error("Gagal mengambil jadwal: ${response.message()}")
                }
            } catch (e: Exception) {
                _scheduleState.value = ApiResult.Error("Error: ${e.message}")
            }
        }
    }

    fun updateSchedule(
        therapistId: Int, day: String, startTime: String, endTime: String, isActive: Boolean,
        onSuccess: (String) -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = UpdateScheduleRequest(
                    therapistId = therapistId,
                    day = day,
                    startTime = startTime,
                    endTime = endTime,
                    isActive = isActive
                )
                val response = api.updateSchedule(request)
                if (response.isSuccessful && response.body() != null) {
                    onSuccess("Jadwal hari $day berhasil ${if(isActive) "beroperasi" else "diliburkan"}")
                } else {
                    val errMsg = response.errorBody()?.string() ?: response.message()
                    onError("Gagal menyimpan: $errMsg")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    fun emergencyClose(
        therapistId: Int, date: String, reason: String, 
        onSuccess: (String) -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = com.android.rumahsehatmannawasalwa.data.model.schedule.EmergencyCloseRequest(
                    therapistId = therapistId,
                    date = date,
                    reason = reason
                )
                val response = api.emergencyClose(request)
                if (response.isSuccessful && response.body() != null) {
                     val count = response.body()!!.data.cancelledCount
                     onSuccess("Berhasil menutup jadwal. $count booking dibatalkan.")
                } else {
                    val errMsg = response.errorBody()?.string() ?: response.message()
                    onError("Gagal menutup jadwal: $errMsg")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    fun addHoliday(therapistId: Int, startDate: String, endDate: String, reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val request = com.android.rumahsehatmannawasalwa.data.model.schedule.AddHolidayRequest(
                    therapistId = therapistId,
                    startDate = startDate,
                    endDate = endDate,
                    reason = reason
                )
                val response = api.addHoliday(request)
                if (response.isSuccessful && response.body() != null) {
                    val count = response.body()!!.data.cancelledBookings
                    Toast.makeText(getApplication(), "Libur ditambahkan. $count booking dibatalkan.", Toast.LENGTH_LONG).show()
//                    fetchSchedules(therapistId) // Refresh to show new holidays
                    onSuccess()
                } else {
                    val errMsg = response.errorBody()?.string() ?: response.message()
                    Toast.makeText(getApplication(), "Gagal tambah libur: $errMsg", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
