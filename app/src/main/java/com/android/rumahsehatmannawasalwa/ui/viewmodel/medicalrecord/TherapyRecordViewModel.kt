package com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.booking.DetailAppointmentResponse
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistory
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyRecordRequest
import com.android.rumahsehatmannawasalwa.data.repository.AppointmentRepository
import com.android.rumahsehatmannawasalwa.data.repository.TherapyRecordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TherapyRecordViewModel(
    private val repository: TherapyRecordRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _saveResult = MutableStateFlow<ApiResult<TherapyHistory>?>(null)
    val saveResult = _saveResult.asStateFlow()

    private val _detailResult = MutableStateFlow<ApiResult<TherapyHistory>?>(null)
    val detailResult = _detailResult.asStateFlow()

    private val _bookingDetail = MutableStateFlow<ApiResult<DetailAppointmentResponse>?>(null)
    val bookingDetail = _bookingDetail.asStateFlow()

    fun fetchBookingDetail(bookingId: Int) {
        viewModelScope.launch {
            appointmentRepository.fetchAppointmentDetail(bookingId).collect { result ->
                _bookingDetail.value = result
            }
        }
    }

    // ── Search & Date Range Filter ────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // null = tidak ada filter tanggal
    private val _dateFrom = MutableStateFlow<String?>(null)
    val dateFrom: StateFlow<String?> = _dateFrom.asStateFlow()

    private val _dateTo = MutableStateFlow<String?>(null)
    val dateTo: StateFlow<String?> = _dateTo.asStateFlow()

    private val _targetPatientId = MutableStateFlow<Int?>(null)

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setDateRange(from: String?, to: String?) {
        _dateFrom.value = from
        _dateTo.value = to
    }
    fun clearDateRange() {
        _dateFrom.value = null
        _dateTo.value = null
    }
    fun setTargetPatientId(id: Int?) {
        _targetPatientId.value = id
    }

    // ── Pager for PatientTherapyRecord ────────────────────────────────
    // Reaktif terhadap search (debounce 300ms), dateFrom, dateTo, dan targetPatientId
    val patientHistoryPager: Flow<PagingData<TherapyHistorySummary>> =
        combine(
            _searchQuery.debounce(300),
            _dateFrom,
            _dateTo,
            _targetPatientId
        ) { query, from, to, patId -> 
            FilterParams(query, from, to, patId) 
        }
            .flatMapLatest { params ->
                repository.getTherapyRecords(
                    patientId = params.patientId,
                    searchQuery = params.query.ifBlank { null },
                    dateFrom = params.from,
                    dateTo = params.to
                )
            }
            .cachedIn(viewModelScope)

    private data class FilterParams(
        val query: String,
        val from: String?,
        val to: String?,
        val patientId: Int?
    )

    // ── Legacy / Therapist usage ──────────────────────────────────────
    fun getTherapyHistory(patientId: Int? = null): Flow<PagingData<TherapyHistorySummary>> {
        return repository.getTherapyRecords(patientId)
            .cachedIn(viewModelScope)
    }

    // ── Create Record ─────────────────────────────────────────────────
    fun createTherapyRecord(
        bookingId: Int,
        patientId: Int,
        complaint: String,
        action: String,
        notes: String
    ) {
        viewModelScope.launch {
            val request = TherapyRecordRequest(
                bookingId = bookingId,
                patientId = patientId,
                patientComplaint = complaint,
                therapistAction = action,
                additionalNotes = notes
            )
            _isLoading.value = true
            repository.createTherapyRecord(request).collect { result ->
                _saveResult.value = result
                _isLoading.value = result is ApiResult.Loading
                if (result is ApiResult.Success) updateStatusToCompleted(bookingId)
            }
        }
    }

    private fun updateStatusToCompleted(bookingId: Int) {
        viewModelScope.launch {
            // Gunakan AppointmentRepository sebagai single source of truth untuk status booking
            val result = appointmentRepository.updateBookingStatus(bookingId, "completed")
            if (result is ApiResult.Error) {
                android.util.Log.e(
                    "TherapyRecordVM",
                    "Rekam medis tersimpan, tapi gagal update status booking ke completed: ${result.error}"
                )
                _saveResult.value = ApiResult.Error(
                    "Catatan tersimpan, namun status janji temu gagal diperbarui: ${result.error}"
                )
            }
        }
    }

    fun getTherapyRecordDetail(recordId: Int) {
        viewModelScope.launch {
            repository.getTherapyRecordDetail(recordId).collect { result ->
                _detailResult.value = result
            }
        }
    }

    fun resetState() {
        _saveResult.value = null
        _isLoading.value = false
    }
}