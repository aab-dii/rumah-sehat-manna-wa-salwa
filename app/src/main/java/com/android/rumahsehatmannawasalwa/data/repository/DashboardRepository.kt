package com.android.rumahsehatmannawasalwa.data.repository

import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.dashboard.AdminDashboardResponse
import com.android.rumahsehatmannawasalwa.data.model.dashboard.DashboardResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DashboardRepository(
    private val apiService: ApiService
) {
    fun getAdminDashboardData(): Flow<ApiResult<AdminDashboardResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getAdminDashboard()
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!))
            } else {
                emit(ApiResult.Error("Gagal memuat dashboard: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Koneksi gagal: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Unified role-aware dashboard endpoint.
     * Backend scopes data automatically based on the authenticated user's role.
     */
    suspend fun getDashboard(): DashboardResponse? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboard()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            android.util.Log.e("DashboardRepo", "getDashboard error: ${e.message}")
            null
        }
    }
}
