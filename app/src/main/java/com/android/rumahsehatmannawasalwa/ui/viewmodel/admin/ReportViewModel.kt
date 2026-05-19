package com.android.rumahsehatmannawasalwa.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.report.*
import com.android.rumahsehatmannawasalwa.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    // ═══════════════════════════════════════════════════════
    // FILTER STATES
    // ═══════════════════════════════════════════════════════
    private val _period = MutableStateFlow("monthly") // monthly, yearly, custom
    val period = _period.asStateFlow()

    private val _startDate = MutableStateFlow<String?>(null)
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<String?>(null)
    val endDate = _endDate.asStateFlow()

    private val _selectedTherapistId = MutableStateFlow<Int?>(null)
    val selectedTherapistId = _selectedTherapistId.asStateFlow()

    // Loading & Exporting State
    private val _isExporting = MutableStateFlow(false)
    val isExporting = _isExporting.asStateFlow()

    // ═══════════════════════════════════════════════════════
    // REPORT DATA STATES
    // ═══════════════════════════════════════════════════════
    
    // 1. Financial Report State
    private val _financialState = MutableStateFlow<ApiResult<FinancialReportResponse>>(ApiResult.Loading)
    val financialState: StateFlow<ApiResult<FinancialReportResponse>> = _financialState.asStateFlow()
    private val _financialTransactions = MutableStateFlow<List<FinancialTransaction>>(emptyList())
    val financialTransactions = _financialTransactions.asStateFlow()
    private var financialCurrentPage = 1
    private var financialLastPage = 1
    private var isFetchingFinancial = false

    // 2. Visits Report State (Admin/Super Admin)
    private val _visitsState = MutableStateFlow<ApiResult<VisitReportResponse>>(ApiResult.Loading)
    val visitsState: StateFlow<ApiResult<VisitReportResponse>> = _visitsState.asStateFlow()
    private val _visitsItems = MutableStateFlow<List<VisitItem>>(emptyList())
    val visitsItems = _visitsItems.asStateFlow()
    private var visitsCurrentPage = 1
    private var visitsLastPage = 1
    private var isFetchingVisits = false

    // 3. Therapist Performance State
    private val _performanceState = MutableStateFlow<ApiResult<PerformanceReportResponse>>(ApiResult.Loading)
    val performanceState: StateFlow<ApiResult<PerformanceReportResponse>> = _performanceState.asStateFlow()

    // 4. Clinic Activity State
    private val _activityState = MutableStateFlow<ApiResult<ActivityReportResponse>>(ApiResult.Loading)
    val activityState: StateFlow<ApiResult<ActivityReportResponse>> = _activityState.asStateFlow()

    // 5. Comparative Report State (Super Admin Only)
    private val _comparativeState = MutableStateFlow<ApiResult<ComparativeReportResponse>>(ApiResult.Loading)
    val comparativeState: StateFlow<ApiResult<ComparativeReportResponse>> = _comparativeState.asStateFlow()

    init {
        // Set default date range to current month
        val today = LocalDate.now()
        val firstDay = today.withDayOfMonth(1)
        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        _startDate.value = firstDay.format(formatter)
        _endDate.value = lastDay.format(formatter)
    }

    // Set filters and trigger reload
    fun setPeriod(newPeriod: String) {
        _period.value = newPeriod
        resetPaginationAndFetch()
    }

    fun setDateRange(start: String?, end: String?) {
        _startDate.value = start
        _endDate.value = end
        resetPaginationAndFetch()
    }

    fun setSelectedTherapistId(therapistId: Int?) {
        _selectedTherapistId.value = therapistId
        resetPaginationAndFetch()
    }

    private fun resetPaginationAndFetch() {
        // Financial
        financialCurrentPage = 1
        financialLastPage = 1
        _financialTransactions.value = emptyList()

        // Visits
        visitsCurrentPage = 1
        visitsLastPage = 1
        _visitsItems.value = emptyList()

        // Fetch everything active or fetch on demand depending on what tab is visible
        fetchAllReports()
    }

    fun fetchAllReports() {
        fetchFinancialReport()
        fetchVisitsReport(isAdmin = true)
        fetchPerformanceReport(isAdmin = true)
        fetchActivityReport()
        fetchComparativeReport()
    }

    // ═══════════════════════════════════════════════════════
    // FETCH API METHODS
    // ═══════════════════════════════════════════════════════

    fun fetchFinancialReport() {
        if (isFetchingFinancial) return
        isFetchingFinancial = true

        viewModelScope.launch {
            repository.getFinancialReport(
                period = _period.value,
                startDate = _startDate.value,
                endDate = _endDate.value,
                page = financialCurrentPage
            ).collect { result ->
                _financialState.value = result
                if (result is ApiResult.Success) {
                    val newData = result.data.transactions
                    val oldData = if (financialCurrentPage == 1) emptyList() else _financialTransactions.value
                    _financialTransactions.value = oldData + newData
                    financialLastPage = result.data.pagination?.lastPage ?: 1
                }
                isFetchingFinancial = false
            }
        }
    }

    fun loadMoreFinancial() {
        if (!isFetchingFinancial && financialCurrentPage < financialLastPage) {
            financialCurrentPage++
            fetchFinancialReport()
        }
    }

    fun fetchVisitsReport(isAdmin: Boolean) {
        if (isFetchingVisits) return
        isFetchingVisits = true

        viewModelScope.launch {
            val flow = if (isAdmin) {
                repository.getVisitsReport(
                    period = _period.value,
                    startDate = _startDate.value,
                    endDate = _endDate.value,
                    therapistId = _selectedTherapistId.value,
                    page = visitsCurrentPage
                )
            } else {
                repository.getMyVisitsReport(
                    period = _period.value,
                    startDate = _startDate.value,
                    endDate = _endDate.value,
                    page = visitsCurrentPage
                )
            }

            flow.collect { result ->
                _visitsState.value = result
                if (result is ApiResult.Success) {
                    val newData = result.data.visits
                    val oldData = if (visitsCurrentPage == 1) emptyList() else _visitsItems.value
                    _visitsItems.value = oldData + newData
                    visitsLastPage = result.data.pagination?.lastPage ?: 1
                }
                isFetchingVisits = false
            }
        }
    }

    fun loadMoreVisits(isAdmin: Boolean) {
        if (!isFetchingVisits && visitsCurrentPage < visitsLastPage) {
            visitsCurrentPage++
            fetchVisitsReport(isAdmin)
        }
    }

    fun fetchPerformanceReport(isAdmin: Boolean) {
        viewModelScope.launch {
            val flow = if (isAdmin) {
                repository.getPerformanceReport(_period.value, _startDate.value, _endDate.value)
            } else {
                repository.getMyPerformanceReport(_period.value, _startDate.value, _endDate.value)
            }
            flow.collect { result ->
                _performanceState.value = result
            }
        }
    }

    fun fetchActivityReport() {
        viewModelScope.launch {
            repository.getActivityReport(_period.value, _startDate.value, _endDate.value).collect { result ->
                _activityState.value = result
            }
        }
    }

    fun fetchComparativeReport() {
        viewModelScope.launch {
            repository.getComparativeReport(_period.value, _startDate.value, _endDate.value).collect { result ->
                _comparativeState.value = result
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // EXPORT PDF METHODS
    // ═══════════════════════════════════════════════════════

    fun setExporting(exporting: Boolean) {
        _isExporting.value = exporting
    }

    suspend fun downloadFinancialPdf(): ResponseBody? {
        var responseBody: ResponseBody? = null
        repository.exportFinancialReport(_period.value, _startDate.value, _endDate.value).collect { result ->
            if (result is ApiResult.Success) {
                responseBody = result.data
            }
        }
        return responseBody
    }

    suspend fun downloadVisitsPdf(isAdmin: Boolean): ResponseBody? {
        var responseBody: ResponseBody? = null
        val flow = if (isAdmin) {
            repository.exportVisitsReport(_period.value, _startDate.value, _endDate.value, _selectedTherapistId.value)
        } else {
            repository.exportMyVisitsReport(_period.value, _startDate.value, _endDate.value)
        }
        flow.collect { result ->
            if (result is ApiResult.Success) {
                responseBody = result.data
            }
        }
        return responseBody
    }

    suspend fun downloadPerformancePdf(isAdmin: Boolean): ResponseBody? {
        var responseBody: ResponseBody? = null
        val flow = if (isAdmin) {
            repository.exportPerformanceReport(_period.value, _startDate.value, _endDate.value)
        } else {
            repository.exportMyPerformanceReport(_period.value, _startDate.value, _endDate.value)
        }
        flow.collect { result ->
            if (result is ApiResult.Success) {
                responseBody = result.data
            }
        }
        return responseBody
    }

    suspend fun downloadActivityPdf(): ResponseBody? {
        var responseBody: ResponseBody? = null
        repository.exportActivityReport(_period.value, _startDate.value, _endDate.value).collect { result ->
            if (result is ApiResult.Success) {
                responseBody = result.data
            }
        }
        return responseBody
    }

    suspend fun downloadComparativePdf(): ResponseBody? {
        var responseBody: ResponseBody? = null
        repository.exportComparativeReport(_period.value, _startDate.value, _endDate.value).collect { result ->
            if (result is ApiResult.Success) {
                responseBody = result.data
            }
        }
        return responseBody
    }
}
