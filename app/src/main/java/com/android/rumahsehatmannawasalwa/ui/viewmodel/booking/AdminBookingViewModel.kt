package com.android.rumahsehatmannawasalwa.ui.viewmodel.booking


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingRequest
import com.android.rumahsehatmannawasalwa.data.repository.BookingPagingSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.android.rumahsehatmannawasalwa.data.ApiResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking
import com.android.rumahsehatmannawasalwa.data.model.service.Layanan
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.ui.state.BookingUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AdminBookingViewModel : ViewModel() {

    // --- Filter States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0 = Upcoming, 1 = History
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // UI State
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    // State for CRUD operations
    private val _operationState = MutableStateFlow<ApiResult<String>?>(null)
    val operationState: StateFlow<ApiResult<String>?> = _operationState

    init {
        // If past closing time (20:00), default to tomorrow
        if (java.time.LocalTime.now().isAfter(java.time.LocalTime.of(20, 0))) {
            _uiState.update { it.copy(selectedDate = java.time.LocalDate.now().plusDays(1)) }
        }
    }

    // --- Actions ---
    fun onSearchQueryInternalChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    // --- Pager Logic ---
    // --- Pager Logic ---
    
    // Upcoming: Pending/Confirmed, ASC
    @OptIn(ExperimentalCoroutinesApi::class)
    val upcomingPager = _searchQuery.flatMapLatest { search ->
        val searchParam = if (search.isBlank()) null else search
        Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 1),
            pagingSourceFactory = { 
                BookingPagingSource(
                    apiService = RetrofitClient.instance, 
                    dateFilter = null,
                    statusFilter = "pending,confirmed",
                    searchQuery = searchParam,
                    sortBy = "booking_date",
                    sortOrder = "asc"
                ) 
            }
        ).flow
    }.cachedIn(viewModelScope)

    // History: Completed/Cancelled, DESC
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyPager = _searchQuery.flatMapLatest { search ->
        val searchParam = if (search.isBlank()) null else search
        Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 1),
            pagingSourceFactory = { 
                BookingPagingSource(
                    apiService = RetrofitClient.instance, 
                    dateFilter = null,
                    statusFilter = "completed,cancelled,batal,selesai",
                    searchQuery = searchParam,
                    sortBy = "booking_date",
                    sortOrder = "desc"
                ) 
            }
        ).flow
    }.cachedIn(viewModelScope)

    // --- State Updaters ---

    fun onPatientSelected(patient: User) {
        _uiState.update { it.copy(selectedPatient = patient, searchQuery = patient.name) }
    }

    fun onSearchQueryChanged(query: String) {
         _uiState.update { it.copy(searchQuery = query) }
         // Note: Searching is handled by Screen's LaunchEffect or filtering internally if list is local.
         // Assumption: User list is passed from AdminUserViewModel. Logic remains there or passed here.
    }

    fun setServices(services: List<Layanan>) {
        _uiState.update { it.copy(serviceList = services) }
    }

    fun onServiceSelected(service: Layanan) {
        _uiState.update { 
            it.copy(
                selectedService = service,
                selectedTherapist = null, // Reset dependent fields
                selectedTimeSlot = null,
                filteredTherapists = filterTherapists(it.therapistList, service)
            ) 
        }
    }

    fun setTherapists(therapists: List<User>) {
        val currentService = _uiState.value.selectedService
        val filtered = if (currentService != null) filterTherapists(therapists, currentService) else emptyList()
        _uiState.update { it.copy(therapistList = therapists, filteredTherapists = filtered) }
    }

    private fun filterTherapists(therapists: List<User>, service: Layanan): List<User> {
        return therapists.filter { therapist ->
            therapist.role == "terapis" && (
                therapist.specialization.isEmpty() || 
                therapist.specialization.any { spec -> spec.contains(service.nama, ignoreCase = true) }
            )
        }
    }

    fun onTherapistSelected(therapist: User) {
        _uiState.update { it.copy(selectedTherapist = therapist, selectedTimeSlot = null) }
        fetchTimeSlots()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, selectedTimeSlot = null) }
        fetchTimeSlots()
    }

    fun onTimeSlotSelected(slot: String) {
        _uiState.update { it.copy(selectedTimeSlot = slot) }
    }

    private fun fetchTimeSlots() {
        val state = _uiState.value
        if (state.selectedTherapist == null || state.selectedService == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSlots = true) }
            try {
                val dateStr = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE) // YYYY-MM-DD
                val response = RetrofitClient.instance.getAvailableSlots(
                    therapistId = state.selectedTherapist.id,
                    date = dateStr,
                    serviceId = state.selectedService.id
                )

                if (response.isSuccessful && response.body() != null) {
                    val slots = response.body()!!.data
                    _uiState.update { it.copy(availableTimeSlots = slots, isLoadingSlots = false) }
                } else {
                    _uiState.update { it.copy(isLoadingSlots = false, error = "Gagal memuat jadwal: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingSlots = false, error = "Error: ${e.localizedMessage}") }
            }
        }
    }

    // --- CRUD ---

    fun createBooking() {
         val state = _uiState.value
         if (!state.isFormValid) return

         viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = BookingRequest(
                    patientId = state.selectedPatient!!.id,
                    serviceId = state.selectedService!!.id,
                    therapistId = state.selectedTherapist!!.id,
                    bookingDate = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    bookingTime = state.selectedTimeSlot!!,
                    totalPrice = state.selectedService.harga
                )
                
                // Assuming createBooking in API returns 201 on success
                val response = RetrofitClient.instance.createBooking(request)
                if (response.isSuccessful) {
                    _operationState.value = ApiResult.Success("Booking berhasil dibuat!")
                    _uiState.update { it.copy(isLoading = false, isBookingSuccess = true) }
                } else {
                    _operationState.value = ApiResult.Error("Gagal: ${response.errorBody()?.string()}")
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _operationState.value = ApiResult.Error(e.message ?: "Unknown Error")
                 _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Legacy Update/Delete kept...
    fun updateBooking(id: Int, request: BookingRequest) {
        viewModelScope.launch {
            try {
                Log.d("AdminBookingViewModel", "Updating booking $id")
                val response = RetrofitClient.instance.updateBooking(id, request)
                if (response.isSuccessful) {
                    _operationState.value = ApiResult.Success("Booking updated successfully")
                } else {
                    _operationState.value = ApiResult.Error("Failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _operationState.value = ApiResult.Error(e.message ?: "Unknown Error")
            }
        }
    }

//    fun deleteBooking(id: Int) {
//        viewModelScope.launch {
//            try {
//                val response = RetrofitClient.instance.deleteBooking(id)
//                if (response.isSuccessful) {
//                    _operationState.value = ApiResult.Success("Booking deleted successfully")
//                } else {
//                    _operationState.value = ApiResult.Error("Failed to delete: ${response.message()}")
//                }
//            } catch (e: Exception) {
//                _operationState.value = ApiResult.Error(e.message ?: "Unknown Error")
//            }
//        }
//    }

    fun resetState() {
        _operationState.value = null
        _uiState.update { BookingUiState() } // Reset UI
    }
}
