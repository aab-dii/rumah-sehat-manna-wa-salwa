package com.android.rumahsehatmannawasalwa.ui.viewmodel.booking

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.mapper.BookingMapper
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingRequest
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.data.repository.AppointmentRepository
import com.android.rumahsehatmannawasalwa.data.service.PusherService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class AppointmentDetailViewModel(private val repository: AppointmentRepository) : ViewModel() {

    private val _detailState = MutableStateFlow<ApiResult<BookingUiModel>>(ApiResult.Loading)
    val detailState = _detailState.asStateFlow()

    private val _updateStatusState = MutableStateFlow<Result<String>?>(null)
    val updateStatusState: StateFlow<Result<String>?> = _updateStatusState.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage = _actionMessage.asStateFlow()

    private val _remainingSeconds = MutableStateFlow<Long?>(null)
    val remainingSeconds = _remainingSeconds.asStateFlow()

    // Job untuk countdown & real-time subscription
    private var countdownJob: Job? = null
    private var realtimeJob: Job? = null

    fun resetUpdateState() {
        _updateStatusState.value = null
    }

    fun resetActionMessage() {
        _actionMessage.value = null
    }

    fun getAppointmentDetail(id: Int) {
        viewModelScope.launch {
            repository.fetchAppointmentDetail(id).collect { result ->
                when (result) {
                    is ApiResult.Loading -> _detailState.value = ApiResult.Loading
                    is ApiResult.Success -> {
                        val role = repository.getUserRole()
                        val mapped = BookingMapper.mapToUiModel(result.data, role)
                        
                        _detailState.value = ApiResult.Success(mapped)
                        startCountdown(mapped.appointment?.paymentRemainingSeconds ?: 0L)
                    }
                    is ApiResult.Error -> _detailState.value = ApiResult.Error(result.error)
                }
            }
        }
    }

    private fun startCountdown(initialSeconds: Long) {
        countdownJob?.cancel()
        _remainingSeconds.value = initialSeconds
        countdownJob = viewModelScope.launch {
            while (_remainingSeconds.value != null && _remainingSeconds.value!! > 0) {
                kotlinx.coroutines.delay(1000)
                _remainingSeconds.value = _remainingSeconds.value!! - 1
            }
            if (_remainingSeconds.value == 0L) {
                // Ketika waktu habis di UI, otomatis ubah status menjadi canceled secara visual
                val current = _detailState.value
                if (current is ApiResult.Success) {
                    val updatedAppointment = current.data.appointment?.copy(
                        status = "canceled"
                    )
                    val updated = current.data.copy(
                        isExpiredWarning = true,
                        appointment = updatedAppointment,
                        statusLabel = "Dibatalkan",
                        statusColor = androidx.compose.ui.graphics.Color(0xFFC62828) // RedDanger
                    )
                    _detailState.value = ApiResult.Success(updated)
                }
            }
        }
    }

    // ── Real-time Pusher Subscription ────────────────────────────────────
    fun subscribeToRealTimeUpdate(bookingId: Int) {
        // Subscribe ke channel spesifik booking ini
        PusherService.subscribeToBooking(bookingId) {}

        // Collect dari global flow, filter hanya booking yang relevan
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            PusherService.bookingUpdateFlow.collect { event ->
                if (event.id == bookingId) {
                    // Auto-refresh detail saat ada update dari Pusher
                    getAppointmentDetail(bookingId)
                }
            }
        }
    }

    fun unsubscribeFromRealTimeUpdate(bookingId: Int) {
        realtimeJob?.cancel()
        realtimeJob = null
        PusherService.unsubscribeFromBooking(bookingId)
    }

    fun updateBookingStatus(bookingId: Int, newStatus: String) {
        viewModelScope.launch {
            _isUpdating.value = true

            val result = repository.updateBookingStatus(bookingId, newStatus)

            if (result is ApiResult.Success) {
                _actionMessage.value = "Status berhasil diubah ke $newStatus"
                getAppointmentDetail(bookingId)
            } else if (result is ApiResult.Error) {
                _actionMessage.value = "Gagal: ${result.error}"
            }
            _isUpdating.value = false
        }
    }

    fun rejectPayment(bookingId: Int, reason: String) {
        viewModelScope.launch {
           _isUpdating.value = true
            val result = repository.rejectPayment(bookingId, reason)

            if (result is ApiResult.Success){
                _actionMessage.value = "Pembayaran berhasil ditolak"
                getAppointmentDetail(bookingId)
            } else if (result is ApiResult.Error) {
                _actionMessage.value = "Gagal menolak: ${result.error}"
            }
            _isUpdating.value = false
        }
    }

    fun acceptPayment(bookingId: Int) {
        viewModelScope.launch {
            _isUpdating.value = true
            val result = repository.acceptPayment(bookingId)

            if (result is ApiResult.Success) {
                _actionMessage.value = "Pembayaran diterima, booking dikonfirmasi!"
                getAppointmentDetail(bookingId)
            } else if (result is ApiResult.Error) {
                _actionMessage.value = "Gagal menerima: ${result.error}"
            }
            _isUpdating.value = false
        }
    }

    fun forceComplete(bookingId: Int) {
        updateBookingStatus(bookingId, "force_completed")
    }

    fun cancelBooking(bookingId: Int) {
        viewModelScope.launch {
            _isUpdating.value = true

            val result = repository.cancelBooking(bookingId)

            if (result is ApiResult.Success) {
                _actionMessage.value = "Booking berhasil dibatalkan"
                getAppointmentDetail(bookingId)
            } else if (result is ApiResult.Error) {
                _actionMessage.value = "Gagal membatalkan: ${result.error}"
            }
            _isUpdating.value = false
        }
    }

    fun reuploadProof(bookingId: Int, uri: Uri) {
        viewModelScope.launch {
            _isUpdating.value = true
            val result = repository.reuploadProof(bookingId, uri)

            if (result is ApiResult.Success) {
                _actionMessage.value = "Bukti transfer berhasil diunggah ulang"
                getAppointmentDetail(bookingId)
            } else if (result is ApiResult.Error) {
                _actionMessage.value = "Gagal mengunggah bukti: ${result.error}"
            }
            _isUpdating.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
        countdownJob?.cancel()
    }
}
