package com.android.rumahsehatmannawasalwa.ui.state

import com.android.rumahsehatmannawasalwa.data.model.service.Layanan
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import java.time.LocalDate

data class BookingUiState(
    val searchQuery: String = "",
    val selectedPatient: User? = null,
    
    val serviceList: List<Layanan> = emptyList(),
    val selectedService: Layanan? = null,
    
    val therapistList: List<User> = emptyList(),
    val filteredTherapists: List<User> = emptyList(),
    val selectedTherapist: User? = null,
    
    val selectedDate: LocalDate = LocalDate.now(),
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
