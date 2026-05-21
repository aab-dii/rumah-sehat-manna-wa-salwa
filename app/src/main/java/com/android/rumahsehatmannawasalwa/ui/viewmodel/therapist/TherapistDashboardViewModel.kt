package com.android.rumahsehatmannawasalwa.ui.viewmodel.therapist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.model.dashboard.DashboardAgendaItem
import com.android.rumahsehatmannawasalwa.data.model.dashboard.TodayStats
import com.android.rumahsehatmannawasalwa.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TherapistDailySummary(
    val stats: TodayStats        = TodayStats(),
    val agenda: List<DashboardAgendaItem> = emptyList(),
    val serverToday: String      = "",
    val isLoading: Boolean       = true
)

class TherapistDashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TherapistDailySummary())
    val state: StateFlow<TherapistDailySummary> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = TherapistDailySummary(isLoading = true)
            try {
                val data = repository.getDashboard()
                if (data != null) {
                    Log.d("TherapistDashVM", "today=${data.serverToday} stats=${data.todayStats}")
                    _state.value = TherapistDailySummary(
                        stats       = data.todayStats,
                        agenda      = data.upcomingAgenda,
                        serverToday = data.serverToday,
                        isLoading   = false
                    )
                } else {
                    Log.w("TherapistDashVM", "getDashboard returned null")
                    _state.value = TherapistDailySummary(isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("TherapistDashVM", "Error: ${e.message}")
                _state.value = TherapistDailySummary(isLoading = false)
            }
        }
    }
}
