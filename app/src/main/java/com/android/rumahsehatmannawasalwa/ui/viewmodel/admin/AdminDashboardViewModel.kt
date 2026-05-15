package com.android.rumahsehatmannawasalwa.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.model.dashboard.DashboardAgendaItem
import com.android.rumahsehatmannawasalwa.data.model.dashboard.AdminStats
import com.android.rumahsehatmannawasalwa.data.model.dashboard.TodayStats
import com.android.rumahsehatmannawasalwa.data.repository.DashboardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =====================================================
// STATE
// =====================================================

data class AdminDashboardState(
    val todayStats          : TodayStats                    = TodayStats(),
    val upcomingAgenda      : List<DashboardAgendaItem>     = emptyList(),
    val adminStats          : AdminStats                    = AdminStats(),
    val monthlyRevenue      : Long                          = 0L,
    val serverToday         : String                        = "",
    val isLoading           : Boolean                       = true,
    val isRefreshing        : Boolean                       = false,
    val errorMessage        : String?                       = null
)

// =====================================================
// VIEWMODEL
// =====================================================

class AdminDashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _adminState = MutableStateFlow(AdminDashboardState())
    val adminState: StateFlow<AdminDashboardState> = _adminState.asStateFlow()

    private var fetchJob: Job? = null

    // --------------------------------------------------
    // Public Functions
    // --------------------------------------------------

    fun loadDashboardData(isRefresh: Boolean = false) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            setLoadingState(isRefresh)

            try {
                val data = repository.getDashboard()

                if (data != null) {
                    _adminState.value = AdminDashboardState(
                        todayStats          = data.todayStats,
                        upcomingAgenda      = data.upcomingAgenda,
                        adminStats          = data.adminStats ?: AdminStats(),
                        monthlyRevenue      = data.monthlyRevenue ?: 0L,
                        serverToday         = data.serverToday,
                        isLoading           = false,
                        isRefreshing        = false
                    )
                } else {
                    setErrorState("Gagal memuat dashboard")
                }

            } catch (e: CancellationException) {
                // Wajib re-throw agar coroutine bisa berhenti dengan bersih
                // Bukan error yang perlu ditampilkan ke user
                throw e

            } catch (e: Exception) {
                setErrorState(e.message ?: "Terjadi kesalahan tidak diketahui")
            }
        }
    }

    fun refresh() = loadDashboardData(isRefresh = true)

    // --------------------------------------------------
    // Private Helpers
    // --------------------------------------------------

    private fun setLoadingState(isRefresh: Boolean) {
        _adminState.value = _adminState.value.copy(
            isLoading    = !isRefresh,
            isRefreshing = isRefresh,
            errorMessage = null
        )
    }

    private fun setErrorState(message: String) {
        _adminState.value = _adminState.value.copy(
            isLoading    = false,
            isRefreshing = false,
            errorMessage = message
        )
    }
}