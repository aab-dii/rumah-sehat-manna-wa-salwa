package com.android.rumahsehatmannawasalwa.ui.viewmodel.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val booking: ApiBooking) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class AppointmentDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _updateStatusState = MutableStateFlow<Result<String>?>(null)
    val updateStatusState: StateFlow<Result<String>?> = _updateStatusState.asStateFlow()

    fun resetUpdateState() {
        _updateStatusState.value = null
    }

    fun fetchBookingDetail(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val response = RetrofitClient.instance.getBookingDetail(id)
                android.util.Log.d("BookingDetail", "Response Code: ${response.code()}, Message: ${response.message()}")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    android.util.Log.d("BookingDetail", "Data received: ${body.data}")
                    _uiState.value = DetailUiState.Success(body.data)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    android.util.Log.e("BookingDetail", "Failed to fetch: $errorMsg")
                    _uiState.value = DetailUiState.Error("Gagal memuat data: ${response.message()} ($errorMsg)")
                }
            } catch (e: Exception) {
                android.util.Log.e("BookingDetail", "Exception: ${e.message}", e)
                _uiState.value = DetailUiState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun updateBookingStatus(bookingId: Int, newStatus: String) {
        val currentState = _uiState.value
        if (currentState !is DetailUiState.Success) return

        val currentBooking = currentState.booking

        viewModelScope.launch {
            try {
                // Construct request with existing data but new status
                // Check if backend supports partial update or needs full object.
                // Assuming existing 'updateBooking' endpoint requires full body.
                val request = BookingRequest(
                    patientId = currentBooking.patientId,
                    serviceId = currentBooking.serviceId,
                    therapistId = currentBooking.therapistId,
                    bookingDate = currentBooking.bookingDate,
                    bookingTime = currentBooking.bookingTime,
                    totalPrice = currentBooking.totalPrice,
                    status = newStatus
                )

                val response = RetrofitClient.instance.updateBooking(bookingId, request)
                if (response.isSuccessful) {
                    _updateStatusState.value = Result.success("Status berhasil diperbarui ke $newStatus")
                    // Refresh data
                    fetchBookingDetail(bookingId)
                } else {
                    _updateStatusState.value = Result.failure(Exception("Gagal update: ${response.message()}"))
                }
            } catch (e: Exception) {
                _updateStatusState.value = Result.failure(e)
            }
        }
    }
}
