package com.android.rumahsehatmannawasalwa.data.repository

import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.report.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import org.json.JSONObject
import java.net.SocketTimeoutException

class ReportRepository(private val apiService: ApiService) {

    // Helper for error parsing
    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val json = JSONObject(errorBody ?: "")
            json.optString("message", "Gagal mengambil data laporan")
        } catch (e: Exception) {
            "Gagal mengambil data laporan"
        }
    }

    // 1. Laporan Keuangan
    fun getFinancialReport(
        period: String,
        startDate: String?,
        endDate: String?,
        page: Int?,
        export: Boolean? = false
    ): Flow<ApiResult<FinancialReportResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getFinancialReport(period, startDate, endDate, page, export)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error(parseError(response)))
            }
        } catch (e: SocketTimeoutException) {
            emit(ApiResult.Error("Koneksi timeout. Silakan periksa jaringan Anda."))
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    // 2. Laporan Kunjungan (Admin/Super Admin)
    fun getVisitsReport(
        period: String,
        startDate: String?,
        endDate: String?,
        therapistId: Int?,
        page: Int?,
        export: Boolean? = false
    ): Flow<ApiResult<VisitReportResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getVisitsReport(period, startDate, endDate, therapistId, page, export)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error(parseError(response)))
            }
        } catch (e: SocketTimeoutException) {
            emit(ApiResult.Error("Koneksi timeout. Silakan periksa jaringan Anda."))
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    // 2b. Laporan Kunjungan (Terapis)
    fun getMyVisitsReport(
        period: String,
        startDate: String?,
        endDate: String?,
        page: Int?,
        export: Boolean? = false
    ): Flow<ApiResult<VisitReportResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getMyVisitsReport(period, startDate, endDate, page, export)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error(parseError(response)))
            }
        } catch (e: SocketTimeoutException) {
            emit(ApiResult.Error("Koneksi timeout. Silakan periksa jaringan Anda."))
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    // 3. Laporan Kinerja Terapis
    fun getPerformanceReport(
        period: String,
        startDate: String?,
        endDate: String?
    ): Flow<ApiResult<PerformanceReportResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getPerformanceReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error(parseError(response)))
            }
        } catch (e: SocketTimeoutException) {
            emit(ApiResult.Error("Koneksi timeout. Silakan periksa jaringan Anda."))
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    // 3b. Laporan Kinerja Terapis (Milik Sendiri)
    fun getMyPerformanceReport(
        period: String,
        startDate: String?,
        endDate: String?
    ): Flow<ApiResult<PerformanceReportResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getMyPerformanceReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error(parseError(response)))
            }
        } catch (e: SocketTimeoutException) {
            emit(ApiResult.Error("Koneksi timeout. Silakan periksa jaringan Anda."))
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    // 4. Laporan Kegiatan Klinik
    fun getActivityReport(
        period: String,
        startDate: String?,
        endDate: String?
    ): Flow<ApiResult<ActivityReportResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getActivityReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error(parseError(response)))
            }
        } catch (e: SocketTimeoutException) {
            emit(ApiResult.Error("Koneksi timeout. Silakan periksa jaringan Anda."))
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    // 5. Laporan Komparatif Terapis
    fun getComparativeReport(
        period: String,
        startDate: String?,
        endDate: String?
    ): Flow<ApiResult<ComparativeReportResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getComparativeReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error(parseError(response)))
            }
        } catch (e: SocketTimeoutException) {
            emit(ApiResult.Error("Koneksi timeout. Silakan periksa jaringan Anda."))
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    // PDF EXPORTS
    fun exportFinancialReport(period: String, startDate: String?, endDate: String?): Flow<ApiResult<ResponseBody>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.exportFinancialReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!))
            } else {
                emit(ApiResult.Error("Gagal mengekspor PDF."))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    fun exportVisitsReport(period: String, startDate: String?, endDate: String?, therapistId: Int?): Flow<ApiResult<ResponseBody>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.exportVisitsReport(period, startDate, endDate, therapistId)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!))
            } else {
                emit(ApiResult.Error("Gagal mengekspor PDF."))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    fun exportMyVisitsReport(period: String, startDate: String?, endDate: String?): Flow<ApiResult<ResponseBody>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.exportMyVisitsReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!))
            } else {
                emit(ApiResult.Error("Gagal mengekspor PDF."))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    fun exportPerformanceReport(period: String, startDate: String?, endDate: String?): Flow<ApiResult<ResponseBody>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.exportPerformanceReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!))
            } else {
                emit(ApiResult.Error("Gagal mengekspor PDF."))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    fun exportMyPerformanceReport(period: String, startDate: String?, endDate: String?): Flow<ApiResult<ResponseBody>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.exportMyPerformanceReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!))
            } else {
                emit(ApiResult.Error("Gagal mengekspor PDF."))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    fun exportActivityReport(period: String, startDate: String?, endDate: String?): Flow<ApiResult<ResponseBody>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.exportActivityReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!))
            } else {
                emit(ApiResult.Error("Gagal mengekspor PDF."))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    fun exportComparativeReport(period: String, startDate: String?, endDate: String?): Flow<ApiResult<ResponseBody>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.exportComparativeReport(period, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!))
            } else {
                emit(ApiResult.Error("Gagal mengekspor PDF."))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }
}
