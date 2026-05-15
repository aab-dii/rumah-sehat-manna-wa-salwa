package com.android.rumahsehatmannawasalwa.ui.viewmodel.therapist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistory
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary
import com.android.rumahsehatmannawasalwa.data.repository.AppointmentRepository
import com.android.rumahsehatmannawasalwa.data.repository.TherapistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TherapistAppointmentViewModel(
    application: Application,
    private val repository: AppointmentRepository,
    private val therapistRepository: TherapistRepository
) : AndroidViewModel(application) {

    private val _upcomingList = MutableStateFlow<List<BookingUiModel>>(emptyList())
    val upcomingList: StateFlow<List<BookingUiModel>> = _upcomingList

    private val _historyList = MutableStateFlow<List<BookingUiModel>>(emptyList())
    val historyList: StateFlow<List<BookingUiModel>> = _historyList

    private val _bookingDetail = MutableStateFlow<BookingUiModel?>(null)
    val bookingDetail: StateFlow<BookingUiModel?> = _bookingDetail

    private val _bookingState = MutableStateFlow<ApiResult<Int>?>(null)
    val bookingState: StateFlow<ApiResult<Int>?> = _bookingState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

//    init {
//        viewModelScope.launch {
//            repository.bookingUpdateFlow.collect{ booking ->
//
//                launch(Dispatchers.Main) {
//                    Toast.makeText(getApplication(),"Realtime Update: ${booking.status}", Toast.LENGTH_SHORT).show()
//                }
//
//                val uiModel = BookingUiModel.fromApiBooking(booking)
//                updateList(uiModel)
//
//                if (_bookingDetail.value?.id == booking.id) {
//                    _bookingDetail.value = uiModel
//                }
//            }
//        }
//    }
    // --- Dashboard (Queue) ---
//    fun fetchTherapistBookings(){
//        viewModelScope.launch {
//            _isLoading.value = true
//            repository.getBookings(page = 1, limit = 10).collect { result ->
//            when (result) {
//                is ApiResult.Success -> {
//                    val allAppointment = result.data.map { BookingUiModel.fromApiBooking(it) }
//                    val upcoming = allAppointment.filter {
//                        it.statusRaw == "pending" || it.statusRaw == "confirmed" || it.statusRaw == "menunggu" || it.statusRaw == "konfirmasi" || it.statusRaw == "in_progress"
//                        }.sortedWith(compareBy({ it.originalDate }, { it.bookingTime }))
//                    _upcomingList.value = upcoming
//                    val history = allAppointment.filter {
//                        it.statusRaw == "completed" || it.statusRaw == "canceled" || it.statusRaw == "cancelled" || it.statusRaw == "selesai" || it.statusRaw == "batal" || it.statusRaw == "rejected"
//                    }.sortedWith(compareByDescending<BookingUiModel> { it.originalDate }.thenByDescending { it.bookingTime })
//
//                    _historyList.value = history
//                    _isLoading.value = false
//                }
//                is ApiResult.Error -> {
//                    Log.e("BookingViewModel", "Fetch Error: ${result.error}")
//                    _isLoading.value = false
//                }
//                else -> {}
//            }
//
//            }
//        }
//    }

//    fun startTherapy(bookingId: Int){
//        viewModelScope.launch {
//            _bookingState.value = ApiResult.Loading
//            repository.updateBookingStatus(bookingId, "in_progress").collect { result ->
//                if (result is ApiResult.Success) {
//                     _bookingState.value = ApiResult.Success(bookingId) // Pass ID to trigger UI update check
//                    Log.d("status booking", result.data)
//                     fetchTherapistBookings() // Refresh list
//                } else if (result is ApiResult.Error) {
//                    _bookingState.value = ApiResult.Error(result.error)
//                }
//            }
//        }
//    }

//    private fun updateList(data: BookingUiModel) {
//        val status = data.statusLabel
//        val isUpcoming = status == "pending" || status == "confirmed" || status == "menunggu" || status == "konfirmasi" || status == "in_progress"
//        val isHistory = status == "completed" || status == "canceled" || status == "cancelled" || status == "selesai" || status == "batal" || status == "rejected"
//
//        if (isUpcoming) {
//            val currentList = _upcomingList.value.toMutableList()
//            val index = currentList.indexOfFirst { it.id == booking.id }
//            if (index != -1) currentList[index] = booking else currentList.add(0, booking)
//            _upcomingList.value = currentList.sortedWith(compareBy({ it.originalDate }, { it.bookingTime }))
//        } else if (isHistory) {
//            val currentList = _historyList.value.toMutableList()
//            val index = currentList.indexOfFirst { it.id == booking.id }
//            if (index != -1) currentList[index] = booking else currentList.add(0, booking)
//            _historyList.value = currentList.sortedWith(compareByDescending<BookingUiModel> { it.originalDate }.thenByDescending { it.bookingTime })
//
//            // Remove from upcoming
//            val upcomingList = _upcomingList.value.toMutableList()
//            if (upcomingList.removeIf { it.id == booking.id }) {
//                _upcomingList.value = upcomingList
//            }
//        }
//    }
    // --- Medical Record ---
    private val _createRecordState = MutableStateFlow<ApiResult<TherapyHistory>?>(null)
    val createRecordState: StateFlow<ApiResult<TherapyHistory>?> = _createRecordState

    private val _patientHistory = MutableStateFlow<ApiResult<List<TherapyHistorySummary>>?>(null)
    val patientHistory: StateFlow<ApiResult<List<TherapyHistorySummary>>?> = _patientHistory
    
    // ...
    
    fun fetchPatientHistory(patientId: Int) {
        viewModelScope.launch {
            _patientHistory.value = ApiResult.Loading
            therapistRepository.getPatientHistory(patientId).collect { result ->
                _patientHistory.value = result
            }
        }
    }

//    fun fetchSchedules(therapistId: Int) {
//         viewModelScope.launch {
//             therapistRepository.getSchedules(therapistId).collect { result ->
//                 if (result is ApiResult.Success) {
//                     _schedules.value = result.data
//                 } else if (result is ApiResult.Error) {
//                     // Handle error (maybe show toast via separate state)
//                 }
//             }
//         }
//    }
//
//    fun updateRoutineSchedule(therapistId: Int, day: String, start: String, end: String, isActive: Boolean) {
//        viewModelScope.launch {
//            _scheduleState.value = ApiResult.Loading
//            val request = UpdateScheduleRequest(therapistId, day, start, end, isActive)
//            therapistRepository.updateSchedule(request).collect { result ->
//                if (result is ApiResult.Success) {
//                    _scheduleState.value = ApiResult.Success("Jadwal $day berhasil diupdate")
//                    fetchSchedules(therapistId) // Refresh
//                } else if (result is ApiResult.Error) {
//                    _scheduleState.value = ApiResult.Error(result.error)
//                }
//            }
//        }
//    }
//
//    fun emergencyClose(therapistId: Int) {
//        viewModelScope.launch {
//            _scheduleState.value = ApiResult.Loading
//            therapistRepository.emergencyClose(therapistId, "Emergency Close").collect { result ->
//                _scheduleState.value = result
//            }
//        }
//    }
//
//    fun addHoliday(therapistId: Int, start: String, end: String, reason: String) {
//        viewModelScope.launch {
//            _scheduleState.value = ApiResult.Loading
//            val request = AddHolidayRequest(therapistId, start, end, reason)
//            therapistRepository.addHoliday(request).collect { result ->
//                _scheduleState.value = result
//                // Optional: Refresh schedules if holiday is part of the list
//            }
//        }
//    }
//
//    fun resetScheduleState() {
//        _scheduleState.value = null
//    }
}
