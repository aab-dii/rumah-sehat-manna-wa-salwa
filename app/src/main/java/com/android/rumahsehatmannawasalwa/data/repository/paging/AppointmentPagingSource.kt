package com.android.rumahsehatmannawasalwa.data.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingListItem

// Tipe data diubah ke BookingListItem agar memori lebih hemat
class AppointmentPagingSource(
    private val apiService: ApiService,
    private val dateFilter: String? = null,
    private val statusFilter: String? = null,
    private val searchQuery: String? = null,
    private val sortBy: String? = null,
    private val sortOrder: String? = null,
) : PagingSource<Int, BookingListItem>() {

    override fun getRefreshKey(state: PagingState<Int, BookingListItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BookingListItem> {
        val position = params.key ?: 1

        return try {
            val response = apiService.getBookings(
                page = position,
                limit = params.loadSize,
                status = statusFilter,
                search = searchQuery,
            )

            val data = response.data.data
            android.util.Log.d("PAGING_DEBUG", "Jumlah data masuk: ${data.size}")
            val nextKey = if (data.isEmpty() || data.size < params.loadSize) {
                null
            } else {
                position + 1
            }

            LoadResult.Page(
                data = data,
                prevKey = if (position == 1) null else position - 1,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}