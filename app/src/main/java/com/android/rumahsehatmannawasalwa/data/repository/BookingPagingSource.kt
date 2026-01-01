package com.android.rumahsehatmannawasalwa.data.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.booking.ApiBooking

class BookingPagingSource(
    private val apiService: ApiService,
    private val dateFilter: String? = null,
    private val statusFilter: String? = null,
    private val searchQuery: String? = null,
    private val sortBy: String? = null,
    private val sortOrder: String? = null
) : PagingSource<Int, ApiBooking>() {

    override fun getRefreshKey(state: PagingState<Int, ApiBooking>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ApiBooking> {
        return try {
            val page = params.key ?: 1
            Log.d("BookingPagingSource", "Requesting page: $page")
            
            val response = apiService.getBookings(
                page = page, 
                date = dateFilter,
                status = statusFilter,
                search = searchQuery,
                sortBy = sortBy,
                sortOrder = sortOrder
            ) // Ensure ApiService has params

            if (response.isSuccessful && response.body() != null) {
                val bookingResponse = response.body()!!
                val data = bookingResponse.data.data
                
                Log.d("BookingPagingSource", "Success! Fetched ${data.size} bookings")

                val nextKey = if (bookingResponse.data.nextPageUrl != null) page + 1 else null
                val prevKey = if (bookingResponse.data.prevPageUrl != null) page - 1 else null

                LoadResult.Page(
                    data = data,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                Log.e("BookingPagingSource", "Failed: ${response.code()} - ${response.message()}")
                LoadResult.Error(Exception("Failed to load bookings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("BookingPagingSource", "Exception: ${e.message}")
            LoadResult.Error(e)
        }
    }
}
