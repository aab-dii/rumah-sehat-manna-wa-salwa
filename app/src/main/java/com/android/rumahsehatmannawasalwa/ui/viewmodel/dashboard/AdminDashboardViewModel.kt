//package com.android.rumahsehatmannawasalwa.ui.viewmodel.dashboard
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
//import com.android.rumahsehatmannawasalwa.data.model.dashboard.DashboardResponse
//import com.android.rumahsehatmannawasalwa.data.model.dashboard.DashboardStats
//import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking
//import com.android.rumahsehatmannawasalwa.data.model.dashboard.AdminDashboardResponse
//import com.android.rumahsehatmannawasalwa.data.repository.DashboardRepository
//import com.android.rumahsehatmannawasalwa.utils.UiState
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//sealed class DashboardUiState {
//    object Loading : DashboardUiState()
//    data class Success(val data: DashboardResponse) : DashboardUiState()
//    data class Error(val message: String) : DashboardUiState()
//}
//
//@HiltViewModel
//class AdminDashboardViewModel @Inject constructor(
//    private val repository: DashboardRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<UiState<AdminDashboardResponse>>(UiState.Loading)
//    val uiState = _uiState.asStateFlow()
//
//    fun fetchDashboardData() {
//        viewModelScope.launch {
//            // Kita kumpulkan (collect) setiap emisi (Loading -> Success/Error)
//            repository.getAdminDashboardData().collect { state ->
//                _uiState.value = state
//            }
//        }
//    }
//}
