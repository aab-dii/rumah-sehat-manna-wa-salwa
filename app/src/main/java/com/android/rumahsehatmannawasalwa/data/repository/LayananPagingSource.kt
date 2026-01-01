package com.android.rumahsehatmannawasalwa.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.service.Layanan

import android.util.Log

class LayananPagingSource(
    private val apiService: ApiService
) : PagingSource<Int, Layanan>() {

    override fun getRefreshKey(state: PagingState<Int, Layanan>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Layanan> {
        return try {
            val page = params.key ?: 1
            Log.d("LayananPagingSource", "Requesting page: $page")
            
            val response = apiService.getServices(page = page)

            if (response.isSuccessful && response.body() != null) {
                val serviceResponse = response.body()!!
                val data = serviceResponse.data.data
                
                Log.d("LayananPagingSource", "Success! Fetched ${data.size} items for page $page")

                // Pagination logic
                val nextKey = if (serviceResponse.data.nextPageUrl != null) page + 1 else null
                val prevKey = if (serviceResponse.data.prevPageUrl != null) page - 1 else null

                LoadResult.Page(
                    data = data,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                Log.e("LayananPagingSource", "Failed: Code ${response.code()} - ${response.message()}")
                LoadResult.Error(Exception("Failed to load data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LayananPagingSource", "Exception: ${e.message}")
            LoadResult.Error(e)
        }
    }
}
