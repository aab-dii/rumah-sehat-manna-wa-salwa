package com.android.rumahsehatmannawasalwa.ui.state

import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import java.time.LocalDate

data class BookingUiState(
    val searchQuery: String = "",
    val selectedPatient: User? = null,
    
    val serviceList: List<Service> = emptyList(),
    val selectedService: Service? = null,
    
    val therapistList: List<User> = emptyList(),
    val filteredTherapists: List<User> = emptyList(),
    val selectedTherapist: User? = null,
    
    val activeDays: List<String>? = null,
    val availabilityError: String? = null,
    val selectedDate: LocalDate? = null,
    val fullDates: Set<LocalDate> = emptySet(),
    val availabilityMap: Map<String, String> = emptyMap(), // "YYYY-MM-DD" -> "available" | "full" | "unavailable"
    val holidayInfo: Map<LocalDate, String> = emptyMap(),
    val availableTimeSlots: List<String> = emptyList(),
    val selectedTimeSlot: String? = null,
    
    val isLoading: Boolean = false,
    val isLoadingSlots: Boolean = false,
    val error: String? = null,
    val isBookingSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = selectedPatient != null && 
                selectedService != null && 
                selectedTherapist != null && 
                selectedTimeSlot != null
}
