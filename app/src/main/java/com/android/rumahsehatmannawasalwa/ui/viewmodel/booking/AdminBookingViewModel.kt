package com.android.rumahsehatmannawasalwa.ui.viewmodel.booking

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingRequest
import com.android.rumahsehatmannawasalwa.data.model.booking.CreateAppointment
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.data.repository.AppointmentRepository
import com.android.rumahsehatmannawasalwa.ui.state.BookingUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AdminBookingViewModel(
    application: Application,
    private val repository: AppointmentRepository
) : AndroidViewModel(application) {

    var isUserAdmin by mutableStateOf(false)
    // --- Filter States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    // 0 = Upcoming, 1 = History
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Chip filter untuk tab "Akan Datang" (default: semua status upcoming)
    private val _upcomingChipFilter = MutableStateFlow("pending,confirmed,waiting_payment,waiting_verification,payment_rejected")
    val upcomingChipFilter: StateFlow<String> = _upcomingChipFilter.asStateFlow()

    // Chip filter untuk tab "Riwayat" (default: semua status history)
    private val _historyChipFilter = MutableStateFlow("completed,canceled,force_completed")
    val historyChipFilter: StateFlow<String> = _historyChipFilter.asStateFlow()

    // UI State
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    // State for CRUD operations
    private val _operationState = MutableStateFlow<ApiResult<String>?>(null)
    val operationState: StateFlow<ApiResult<String>?> = _operationState

    // Payment States
    private val _selectedPaymentOption = MutableStateFlow("cash") // cash, transfer
    val selectedPaymentOption: StateFlow<String> = _selectedPaymentOption.asStateFlow()

    private val _proofOfTransferUri = MutableStateFlow<Uri?>(null)
    val proofOfTransferUri: StateFlow<Uri?> = _proofOfTransferUri.asStateFlow()

    init {
        // Collect real-time booking updates dari PusherService via Repository
        viewModelScope.launch {
            repository.bookingUpdateFlow.collect { booking ->
                 _refreshTrigger.value += 1
            }
        }
    }

    // --- Actions ---
    fun onSearchQueryInternalChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun setUpcomingChipFilter(value: String) {
        _upcomingChipFilter.value = value
    }

    fun setHistoryChipFilter(value: String) {
        _historyChipFilter.value = value
    }

    fun onPatientSelected(patient: User) {
        _uiState.update { it.copy(selectedPatient = patient, searchQuery = patient.name) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTherapistSelected(therapist: User) {
        _uiState.update { it.copy(selectedTherapist = therapist, selectedTimeSlot = null) }
        fetchTherapistSchedule(therapist.id)
        
        // Fetch availability for the next 14 days
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(13)
        fetchAvailability(therapist.id, startDate, endDate)
        
        fetchTimeSlots()
    }

    fun onServiceSelected(service: Service) {
        _uiState.update {
            it.copy(
                selectedService = service,
                selectedTherapist = null,
                selectedTimeSlot = null,
                filteredTherapists = filterTherapists(it.therapistList, service)
            )
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, selectedTimeSlot = null) }
        fetchTimeSlots()
    }

    fun onTimeSlotSelected(slot: String) {
        _uiState.update { it.copy(selectedTimeSlot = slot) }
    }

    fun onPaymentOptionSelected(option: String) {
        _selectedPaymentOption.value = option
    }

    fun onProofSelected(uri: Uri?) {
        _proofOfTransferUri.value = uri
    }

    fun setTherapists(therapists: List<User>) {
        val currentService = _uiState.value.selectedService
        val filtered = if (currentService != null) filterTherapists(therapists, currentService) else emptyList()
        _uiState.update { it.copy(therapistList = therapists, filteredTherapists = filtered) }
    }

    fun refreshBookings() {
        _refreshTrigger.value += 1
    }

    // Upcoming: statuses dikendalikan chip, ASC
    @OptIn(ExperimentalCoroutinesApi::class)
    val upcomingPager = combine(_searchQuery, _refreshTrigger, _upcomingChipFilter) { query, _, chip -> Pair(query, chip) }
        .flatMapLatest { (query, chip) ->
            createPager(status = chip, search = query)
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyPager = combine(_searchQuery, _refreshTrigger, _historyChipFilter) { query, _, chip -> Pair(query, chip) }
        .flatMapLatest { (query, chip) ->
            createPager(status = chip, search = query, order = "desc")
        }.cachedIn(viewModelScope)

    private fun createPager(status: String, search: String, order: String = "asc") = Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = {
            repository.getPagingSource(
                statusFilter = status,
                searchQuery = search.ifBlank { null },
                sortBy = "booking_date",
                sortOrder = order,
            )
        }
    ).flow

    fun setServices(services: List<Service>) {
        _uiState.update { it.copy(serviceList = services) }
    }

    private fun filterTherapists(therapists: List<User>?, service: Service): List<User> {
        // 1. Cek dulu apakah list therapists-nya null
        if (therapists == null) return emptyList()

        return therapists.filter { therapist ->
            // 2. Ambil specialization secara aman
            val specs = therapist.specialization ?: emptyList() // Jika null, anggap list kosong

            therapist.role == "terapis" && (
                    specs.isEmpty() ||
                            specs.any { spec -> spec.contains(service.name, ignoreCase = true) }
                    )
        }
    }

    private fun fetchTherapistSchedule(therapistId: Int) {
        viewModelScope.launch {
            when (val result = repository.getTherapistSchedule(therapistId)) {
                is ApiResult.Success -> {
                    val scheduleData = result.data
                    _uiState.update { it.copy(
                        holidayInfo = scheduleData.holidayInfo,
                        activeDays = scheduleData.activeDays
                    )}
                }
                is ApiResult.Error -> { /* handle error */ }
                else -> {}
            }
        }
    }

    private fun fetchAvailability(therapistId: Int, start: LocalDate, end: LocalDate) {
        val service = _uiState.value.selectedService ?: return

        viewModelScope.launch {
            val result = repository.checkAvailability(
                therapistId,
                start.toString(),
                end.toString(),
                service.id
            )

            when (result) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(availabilityMap = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(availabilityError = result.error) }
                    Log.e("AdminBookingVM", "Availability Error: ${result.error}")
                }
                else -> {}
            }
        }
    }

    private fun fetchTimeSlots() {
        val state = _uiState.value
        if (state.selectedTherapist == null || state.selectedService == null || state.selectedDate == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSlots = true) }
            val dateStr = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val result = repository.getAvailableSlots(
                therapistId = state.selectedTherapist.id,
                date = dateStr,
                serviceId = state.selectedService.id
            )

            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(availableTimeSlots = result.data.slots, isLoadingSlots = false)
                }

                is ApiResult.Error -> _uiState.update { it.copy(isLoadingSlots = false, error = result.error) }
                else -> {}
            }
        }
    }

    // --- CRUD ---
    fun createBooking() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val params = CreateAppointment(
                patientId = state.selectedPatient!!.id,
                serviceId = state.selectedService!!.id,
                therapistId = state.selectedTherapist!!.id,
                date = state.selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
                time = state.selectedTimeSlot!!,
                price = state.selectedService.price,
                paymentOption = _selectedPaymentOption.value,
                proofUri = _proofOfTransferUri.value
            )

            when (val result = repository.createAppointment(params)) {
                is ApiResult.Success -> {
                    _operationState.value = ApiResult.Success("Booking berhasil!")
                    _uiState.update { it.copy(isLoading = false, isBookingSuccess = true) }
                }
                is ApiResult.Error -> {
                    _operationState.value = ApiResult.Error(result.error)
                    _uiState.update { it.copy(isLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun updateBooking(id: Int, request: BookingRequest) {
        viewModelScope.launch {
            when (val result = repository.updateBooking(id, request)) {
                is ApiResult.Success -> _operationState.value = ApiResult.Success("Booking updated successfully")
                is ApiResult.Error -> _operationState.value = ApiResult.Error(result.error)
                else -> {}
            }
        }
    }

    fun updateBookingStatus(id: Int, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.updateBookingStatus(id, status)) {
                is ApiResult.Success -> {
                    _operationState.value = ApiResult.Success("Status updated successfully")
                    _uiState.update { it.copy(isLoading = false) }
                    refreshBookings() // Auto-refresh data
                }
                is ApiResult.Error -> {
                    _operationState.value = ApiResult.Error(result.error)
                    _uiState.update { it.copy(isLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _operationState.value = null
        _uiState.update { BookingUiState() }
        _selectedPaymentOption.value = "cash"
        _proofOfTransferUri.value = null
    }
}
