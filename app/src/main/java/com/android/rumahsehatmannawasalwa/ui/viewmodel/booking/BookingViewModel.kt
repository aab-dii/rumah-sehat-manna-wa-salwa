package com.android.rumahsehatmannawasalwa.ui.viewmodel.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.android.rumahsehatmannawasalwa.data.model.booking.CheckoutRequest

class BookingViewModel : ViewModel() {
    private val _upcomingList = MutableStateFlow<List<ApiBooking>>(emptyList())
    val upcomingList: StateFlow<List<ApiBooking>> = _upcomingList

    private val _historyList = MutableStateFlow<List<ApiBooking>>(emptyList())
    val historyList: StateFlow<List<ApiBooking>> = _historyList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _bookingState = MutableStateFlow<ApiResult<Unit>?>(null)
    val bookingState: StateFlow<ApiResult<Unit>?> = _bookingState

    // --- New States for Refactored Booking Screen ---
    private val _therapistList = MutableStateFlow<List<User>>(emptyList())
    val therapistList: StateFlow<List<User>> = _therapistList

    private val _filteredTherapists = MutableStateFlow<List<User>>(emptyList())
    val filteredTherapists: StateFlow<List<User>> = _filteredTherapists

    private val _availableTimeSlots = MutableStateFlow<List<String>>(emptyList())
    val availableTimeSlots: StateFlow<List<String>> = _availableTimeSlots

    private val _selectedDate = MutableStateFlow<java.time.LocalDate>(
        if (java.time.LocalTime.now().isAfter(java.time.LocalTime.of(20, 0))) 
            java.time.LocalDate.now().plusDays(1) 
        else 
            java.time.LocalDate.now()
    )
    val selectedDate: StateFlow<java.time.LocalDate> = _selectedDate

    private val _loadingSlots = MutableStateFlow(false)
    val loadingSlots: StateFlow<Boolean> = _loadingSlots
    
    // --- Selection State (Shared between Booking & Summary) ---
    private val _selectedServiceInfo = MutableStateFlow<Triple<String, String, Int>?>(null) // Id, Name, Price
    val selectedServiceInfo: StateFlow<Triple<String, String, Int>?> = _selectedServiceInfo

    private val _selectedTherapist = MutableStateFlow<User?>(null)
    val selectedTherapist: StateFlow<User?> = _selectedTherapist

    private val _selectedTimeSlot = MutableStateFlow<String?>(null)
    val selectedTimeSlot: StateFlow<String?> = _selectedTimeSlot

    private val _isLoadingTherapists = MutableStateFlow(true) // Default true, as we fetch on init
    val isLoadingTherapists: StateFlow<Boolean> = _isLoadingTherapists

    fun setServiceDetails(id: String, name: String, price: Int) {
        _selectedServiceInfo.value = Triple(id, name, price)
    }

    fun selectTherapist(therapist: User) {
        _selectedTherapist.value = therapist
        // Reset time slot if therapist changes
        _selectedTimeSlot.value = null
    }

    fun selectTimeSlot(slot: String) {
        _selectedTimeSlot.value = slot
    }

    fun fetchTherapists(serviceName: String) {
        Log.d("BookingVM", "fetchTherapists called for service: $serviceName")
        viewModelScope.launch {
            _isLoadingTherapists.value = true
            try {
                Log.d("BookingVM", "Calling API getUsers(role=terapis)...")
                val response = RetrofitClient.instance.getUsers(page = 1, role = "terapis", limit = 100)
                Log.d("BookingVM", "API Response Code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val allTherapists = response.body()!!.data.data
                    Log.d("BookingVM", "API Success. Fetched ${allTherapists.size} therapists.")
                    _therapistList.value = allTherapists
                    filterTherapists(serviceName)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("BookingVM", "API Failed. Code: ${response.code()}, Error: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("BookingVM", "Exception in fetchTherapists", e)
            } finally {
                _isLoadingTherapists.value = false
            }
        }
    }

    private fun filterTherapists(serviceName: String) {
        val currentList = _therapistList.value
        Log.d("BookingVM", "Filtering for service: '$serviceName'. Total therapists: ${currentList.size}")
        
        if (currentList.isNotEmpty()) {
            val filtered = currentList.filter { therapist ->
                // Log specialization for debugging
                Log.d("BookingVM", "Therapist: ${therapist.name}, Specs: ${therapist.specialization}")
                
                therapist.specialization.isEmpty() || 
                therapist.specialization.any { spec -> spec.contains(serviceName, ignoreCase = true) }
            }
            Log.d("BookingVM", "Filtered result count: ${filtered.size}")
            _filteredTherapists.value = filtered
        } else {
            Log.d("BookingVM", "Therapist list is empty, cannot filter.")
        }
    }

    fun onDateSelected(date: java.time.LocalDate) {
        _selectedDate.value = date
        // Clear slots when date changes, wait for therapist selection to fetch
    }

    fun fetchTimeSlots(therapistId: Int, serviceId: Int) {
        viewModelScope.launch {
            _loadingSlots.value = true
            try {
                val dateStr = _selectedDate.value.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val response = RetrofitClient.instance.getAvailableSlots(
                    therapistId = therapistId,
                    date = dateStr,
                    serviceId = serviceId
                )
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    Log.d("BookingVM", "Slots Response: ${responseBody.meta.message}")
                    var rawSlots = responseBody.data
                    
                    // Filter: Hanya tampilkan slot 30 menit dari sekarang jika tanggal hari ini
                    if (_selectedDate.value == java.time.LocalDate.now()) {
                        val cutoffTime = java.time.LocalTime.now().plusMinutes(30)
                        rawSlots = rawSlots.filter { slot ->
                            try {
                                // Asumsi format slot "HH:mm" atau "HH:mm:ss"
                                // Kita parse sebisa mungkin
                                val cleanSlot = if (slot.length == 5) "$slot:00" else slot
                                val slotTime = java.time.LocalTime.parse(cleanSlot)
                                slotTime.isAfter(cutoffTime)
                            } catch (e: Exception) {
                                true // Kalau gagal parse, loloskan saja (aman)
                            }
                        }
                    }
                    
                    _availableTimeSlots.value = rawSlots
                } else {
                    _availableTimeSlots.value = emptyList()
                    Log.e("BookingVM", "Failed to fetch slots: ${response.message()}")
                }
            } catch (e: Exception) {
                if (e !is java.util.concurrent.CancellationException) {
                     Log.e("BookingVM", "Error fetching slots", e)
                }
                _availableTimeSlots.value = emptyList()
            } finally {
                _loadingSlots.value = false
            }
        }
    }

    private var isDataLoaded = false

    fun fetchUserBookings(forceRefresh: Boolean = false) {
        if (isDataLoaded && !forceRefresh) {
            return // Use cached data
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch a large page to get most recent bookings (simulated "all")
                // In a real app we might want proper pagination or a specific "all" endpoint
                val response = RetrofitClient.instance.getBookings(page = 1, limit = 50)
                
                if (response.isSuccessful && response.body() != null) {
                    val allBookings = response.body()!!.data.data
                    
                    // Pisahkan berdasarkan status
                    // Akan Datang: status 'menunggu' atau 'konfirmasi'
                    val upcoming = allBookings.filter {
                        it.status == "pending" || it.status == "confirmed" || it.status == "menunggu" || it.status == "konfirmasi"
                    }.sortedWith(compareBy({ it.bookingDate }, { it.bookingTime })) // Terdekat dulu

                    _upcomingList.value = upcoming

                    // Riwayat: status 'selesai' atau 'batal'
                    val history = allBookings.filter {
                        it.status == "completed" || it.status == "cancelled" || it.status == "canceled" || it.status == "selesai" || it.status == "batal"
                    }.sortedWith(compareByDescending<ApiBooking> { it.bookingDate }.thenByDescending { it.bookingTime }) // Terbaru dulu

                    _historyList.value = history
                    isDataLoaded = true // Mark as loaded
                } else {
                    Log.e("BookingViewModel", "Failed to fetch bookings: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error fetching bookings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun buatPesanan(
        serviceId: String,
        serviceName: String,
        servicePrice: Int,
        tanggal: String,
        jam: String,
        therapistId: Int // Added therapistId
    ) {
        if (tanggal.isBlank() || jam.isBlank()) {
            _bookingState.value = ApiResult.Error("Silakan pilih Tanggal dan Jam")
            return
        }

        viewModelScope.launch {
            _bookingState.value = ApiResult.Loading
            try {
                // Prepare request with defaults for missing fields
                 val request = CheckoutRequest(
                    serviceId = serviceId.toIntOrNull() ?: 1,
                    therapistId = therapistId, 
                    bookingDate = tanggal, // Ensure format matches backend YYYY-MM-DD
                    bookingTime = jam,
                    locationType = "clinic",
                    address = "-",
                    totalPrice = servicePrice
                )

                val response = RetrofitClient.instance.checkout(request)
                
                if (response.isSuccessful) {
                    _bookingState.value = ApiResult.Success(Unit)
                    fetchUserBookings(forceRefresh = true) // Refresh list
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal booking"
                    _bookingState.value = ApiResult.Error("Gagal: ${response.message()}")
                    Log.e("BookingViewModel", "Checkout failed: $errorMsg")
                }
            } catch (e: Exception) {
                _bookingState.value = ApiResult.Error("Error: ${e.message}")
                Log.e("BookingViewModel", "Checkout exception", e)
            }
        }
    }

    // --- Booking Detail State ---
    private val _bookingDetail = MutableStateFlow<ApiBooking?>(null)
    val bookingDetail: StateFlow<ApiBooking?> = _bookingDetail

    fun getBookingDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.instance.getBookingDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    _bookingDetail.value = response.body()!!.data
                } else {
                    Log.e("BookingViewModel", "Failed to fetch detail: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error fetching detail", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelBooking(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _bookingState.value = ApiResult.Loading
            try {
                // Call the new Soft Delete/Cancel endpoint
                val response = RetrofitClient.instance.cancelBooking(id) 
                Log.d("BookingVM", "Cancel Response Code: ${response.code()}")
                
                if (response.isSuccessful) {
                    _bookingState.value = ApiResult.Success(Unit)
                    onSuccess()
                    fetchUserBookings(forceRefresh = true) // Refresh list to update history
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal membatalkan"
                    Log.e("BookingVM", "Cancel Failed: $errorMsg")
                    _bookingState.value = ApiResult.Error("Error: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("BookingVM", "Cancel Exception", e)
                _bookingState.value = ApiResult.Error("Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _bookingState.value = null
        _availableTimeSlots.value = emptyList()
        _loadingSlots.value = false
        _bookingDetail.value = null
        isDataLoaded = false
    }
}