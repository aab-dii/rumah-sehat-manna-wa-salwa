package com.android.rumahsehatmannawasalwa.ui.viewmodel.booking

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.mapper.BookingMapper
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingUiModel
import com.android.rumahsehatmannawasalwa.data.model.booking.CreateAppointment
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.data.repository.AppointmentRepository
import com.android.rumahsehatmannawasalwa.ui.state.BookingUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class BookingViewModel(
    application: Application,
    private val repository: AppointmentRepository
) : AndroidViewModel(application) {

    // --- 1. LISTING STATES (Identik dengan Admin) ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- 2. UI STATE (Gunakan BookingUiState agar konsisten) ---
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    // Payment States
    private val _selectedPaymentOption = MutableStateFlow("cash")
    val selectedPaymentOption: StateFlow<String> = _selectedPaymentOption.asStateFlow()

    private val _proofOfTransferUri = MutableStateFlow<Uri?>(null)
    val proofOfTransferUri: StateFlow<Uri?> = _proofOfTransferUri.asStateFlow()

    private val _isLoadingTherapists = MutableStateFlow(false)
    val isLoadingTherapists: StateFlow<Boolean> = _isLoadingTherapists.asStateFlow()

    private val _isLoadingService = MutableStateFlow(false)
    val isLoadingService: StateFlow<Boolean> = _isLoadingService.asStateFlow()

    private val _bookingState = MutableStateFlow<ApiResult<Any>?>(null)
    val bookingState: StateFlow<ApiResult<Any>?> = _bookingState.asStateFlow()

    // Pesan error booking untuk ditampilkan sebagai snackbar di UI
    private val _bookingErrorMessage = MutableStateFlow<String?>(null)
    val bookingErrorMessage: StateFlow<String?> = _bookingErrorMessage.asStateFlow()



    fun resetBookingError() { _bookingErrorMessage.value = null }

    // 2. Tambahkan bookingSummaryState (Derived State)
    // State ini akan "menghasilkan" data hanya jika Service, Terapis, dan Jam sudah terpilih
    val bookingSummaryState: StateFlow<BookingUiState?> = _uiState.map { state ->
        if (state.selectedService != null && state.selectedTherapist != null && state.selectedTimeSlot != null) {
            state
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 2. Tambahkan fungsi setServiceDetails
    fun setServiceDetails(id: String, name: String, price: Int) {
        // Set info awal dulu
        _uiState.update { it.copy(
            selectedService = Service(id = id.toIntOrNull() ?: 0, name = name, price = price)
        )}
        
        // Lalu ambil info lengkap (termasuk imageUrl dan price terbaru jika ada)
        id.toIntOrNull()?.let { serviceId ->
            viewModelScope.launch {
                _isLoadingService.value = true
                val result = repository.getServiceDetail(serviceId)
                if (result is ApiResult.Success) {
                    _uiState.update { it.copy(selectedService = result.data) }
                }
                _isLoadingService.value = false
            }
        }
    }

    // 3. Tambahkan fungsi fetchTherapists
    fun fetchTherapists(serviceName: String) {
        viewModelScope.launch {
            _isLoadingTherapists.value = true
            repository.getTherapistsByService(serviceName).collect { result ->
                if (result is ApiResult.Success) {
                    _uiState.update { it.copy(
                        therapistList = result.data,
                        filteredTherapists = result.data // Langsung set ke filtered karena ini pencarian spesifik
                    )}
                }
                _isLoadingTherapists.value = false
            }
        }
    }
    // ── Satu fetch tunggal yang di-share ke upcoming & history ────────
    /** Raw list dari server — hanya satu request API, di-cache, berhenti saat tidak ada collector */
    private val _allBookings: StateFlow<List<BookingUiModel>> =
        combine(_refreshTrigger, _searchQuery) { _, query ->
            _isLoading.value = true
            val result = repository.getBookings(page = 1, limit = 100).first()
            _isLoading.value = false
            if (result is ApiResult.Success) {
                result.data.map { BookingMapper.mapToUiModel(it) }
                    .filter { booking ->
                        val q = query.lowercase()
                        q.isBlank() ||
                        booking.service?.name?.lowercase()?.contains(q) == true ||
                        booking.therapist?.name?.lowercase()?.contains(q) == true ||
                        booking.patient?.name?.lowercase()?.contains(q) == true
                    }
            } else emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived — tidak memanggil API lagi
    val upcomingList: StateFlow<List<BookingUiModel>> = _allBookings.map { list ->
        list.filter { booking ->
            val status = booking.appointment?.status?.lowercase() ?: ""
            status in listOf("pending", "confirmed", "terjadwal", "in_progress",
                             "waiting_payment", "waiting_verification")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyList: StateFlow<List<BookingUiModel>> = _allBookings.map { list ->
        list.filter { booking ->
            val status = booking.appointment?.status?.lowercase() ?: ""
            status !in listOf("pending", "confirmed", "terjadwal", "in_progress",
                              "waiting_payment", "waiting_verification")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Handle Realtime Update via Pusher
        viewModelScope.launch {
            repository.bookingUpdateFlow.collect {
                _refreshTrigger.value += 1
            }
        }
    }


    // --- 4. ACTIONS: LISTING & SEARCH ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun fetchUserBookings() {
        _refreshTrigger.value += 1
    }


    fun onServiceSelected(service: Service) {
        _uiState.update { it.copy(
            selectedService = service,
            selectedTherapist = null,
            selectedTimeSlot = null,
            filteredTherapists = filterTherapists(it.therapistList, service)
        )}
    }

    fun onTherapistSelected(therapist: User) {
        _uiState.update {
            it.copy(
                selectedTherapist = therapist,
                selectedDate = null,
                availableTimeSlots = emptyList(),
                selectedTimeSlot = null,
                isLoadingSlots = false
            )
        }
        fetchTherapistSchedule(therapist.id)
        // Fetch availability for the current month/next 30 days
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(13)
        fetchAvailability(therapist.id, startDate, endDate)
    }

    private fun fetchAvailability(therapistId: Int, start: LocalDate, end: LocalDate) {
        val service = _uiState.value.selectedService ?: return

        viewModelScope.launch {
            Log.d("ViewModelDebug", "Nembak API Availability...")
            val result = repository.checkAvailability(
                therapistId,
                start.toString(),
                end.toString(),
                service.id
            )

            when (result) {
                is ApiResult.Success -> {
                    Log.d("ViewModelDebug", "API SUKSES: ${result.data}")
                    _uiState.update { it.copy(availabilityMap = result.data) }
                }
                is ApiResult.Error -> {
                    // DI SINI BIANG KEROKNYA BIASA MUNCUL
                    Log.e("ViewModelDebug", "API ERROR: ${result.error}")
                }
                else -> {}
            }
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

    // --- 6. DATA FETCHING (Schedule & Slots) ---

    private fun fetchTherapistSchedule(therapistId: Int) {
        viewModelScope.launch {
            when (val result = repository.getTherapistSchedule(therapistId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(
                        holidayInfo = result.data.holidayInfo,
                        activeDays = result.data.activeDays
                    )}
                }
                is ApiResult.Error -> { /* Handle Error */ }
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
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            availableTimeSlots = result.data.slots,
                            isLoadingSlots = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingSlots = false,
                            error = result.error,
                            availableTimeSlots = emptyList()
                        )
                    }
                    // Opsional: Tampilkan pesan error ke user
                    Log.e("BookingVM", "Gagal ambil slot: ${result.error}")
                }
                else -> {
                    _uiState.update { it.copy(isLoadingSlots = false) }
                }
            }
        }
    }

    // --- 7. CRUD OPERATIONS ---

    fun createBooking() {
        val state = _uiState.value
        if (state.selectedService == null || state.selectedTherapist == null || state.selectedTimeSlot == null) return

        viewModelScope.launch {
            _bookingState.value = ApiResult.Loading // Set loading state
            _uiState.update { it.copy(isLoading = true) }

            val params = CreateAppointment(
                serviceId = state.selectedService.id,
                therapistId = state.selectedTherapist.id,
                date = state.selectedDate!!.toString(),
                time = state.selectedTimeSlot,
                price = state.selectedService.price,
                paymentOption = _selectedPaymentOption.value,
                proofUri = _proofOfTransferUri.value
            )

            val result = repository.createAppointment(params)
            _bookingState.value = result // Kirim hasil ke Summary Screen

            if (result is ApiResult.Success) {
                _uiState.update { it.copy(isLoading = false, isBookingSuccess = true) }
            } else if (result is ApiResult.Error) {
                _uiState.update { it.copy(isLoading = false) }
                _bookingErrorMessage.value = result.error
            }
        }
    }

    fun cancelBooking(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.cancelBooking(id)
            if (result is ApiResult.Success) {
                _refreshTrigger.value += 1
            }
            _isLoading.value = false
        }
    }

    // --- HELPER ---
    private fun filterTherapists(therapists: List<User>?, service: Service): List<User> {
        if (therapists == null) return emptyList()
        return therapists.filter { therapist ->
            val specs = therapist.specialization ?: emptyList()
            therapist.role == "terapis" && (specs.isEmpty() || specs.any { it.contains(service.name, ignoreCase = true) })
        }
    }

    fun handleConflictBack() {
        _uiState.update { it.copy(selectedTimeSlot = null) }
        fetchTimeSlots()
        _bookingState.value = null
        _bookingErrorMessage.value = null
    }

    fun resetState() {
        _uiState.value = BookingUiState() // ✅ Cara paling cepet buat balik ke default (null)
        _selectedPaymentOption.value = "cash"
        _proofOfTransferUri.value = null
        _searchQuery.value = ""
        _bookingState.value = null
        _bookingErrorMessage.value = null
    }
}