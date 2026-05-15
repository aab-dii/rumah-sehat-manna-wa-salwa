package com.android.rumahsehatmannawasalwa.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.local.pref.UserPreference
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistory
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyRecordDetailResponse
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyRecordListResponse
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyRecordRequest
import com.android.rumahsehatmannawasalwa.data.repository.paging.TherapyRecordPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject

class TherapyRecordRepository(
    private val apiService: ApiService
) {

    fun createTherapyRecord(request: TherapyRecordRequest): Flow<ApiResult<TherapyHistory>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.createTherapyRecord(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.data != null) {
                    emit(ApiResult.Success(body.data))
                } else {
                    emit(ApiResult.Error("Respons data kosong dari server"))
                }
            } else {
                // Menangkap pesan error dari Laravel (misal: validasi gagal)
                val errorJson = response.errorBody()?.string()
                val message = errorJson?.let {
                    JSONObject(it).optString("message", "Gagal menyimpan rekam medis")
                } ?: "Terjadi kesalahan server (${response.code()})"

                emit(ApiResult.Error(message))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Koneksi gagal: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getTherapyRecords(patientId: Int? = null, searchQuery: String? = null, dateFrom: String? = null, dateTo: String? = null): Flow<PagingData<TherapyHistorySummary>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = { TherapyRecordPagingSource(apiService, patientId, searchQuery, dateFrom, dateTo) }
        ).flow
    }


    fun getTherapyRecordDetail(recordId: Int) : Flow<ApiResult<TherapyHistory>> = flow {
        emit(ApiResult.Loading)

        try{
            val response = apiService.getTherapyRecordDetail(recordId)

            if (response.isSuccessful && response.body() != null){
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error("Gagal memuat data"))
            }
        } catch (e: Exception){
            emit(ApiResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }.flowOn(Dispatchers.IO)

}