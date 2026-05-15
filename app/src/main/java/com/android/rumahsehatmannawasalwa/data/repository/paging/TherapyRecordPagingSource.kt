package com.android.rumahsehatmannawasalwa.data.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary

class TherapyRecordPagingSource(
    private val apiService: ApiService,
    private val patientId: Int? = null,
    private val searchQuery: String? = null,
    private val dateFrom: String? = null,
    private val dateTo: String? = null
) : PagingSource<Int, TherapyHistorySummary>() {

    override fun getRefreshKey(state: PagingState<Int, TherapyHistorySummary>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TherapyHistorySummary> {
        val position = params.key ?: 1
        return try {
            val response = apiService.getTherapyRecords(
                patientId = patientId,
                page = position,
                limit = params.loadSize,
                search = searchQuery,
                dateFrom = dateFrom,
                dateTo = dateTo
            )

            val data = response.data.data

            LoadResult.Page(
                data = data,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (data.isEmpty() || data.size < params.loadSize) null else position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}