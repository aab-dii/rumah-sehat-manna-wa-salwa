package com.android.rumahsehatmannawasalwa.data.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.auth.User

import android.util.Log

class UserPagingSource(
    private val apiService: ApiService,
    private val role: String? = null,
    private val search: String? = null,
    private val trash: Int? = 0 // Default 0 (Active)
) : PagingSource<Int, User>() {

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return try {
            val page = params.key ?: 1
            Log.d("UserPagingSource", "Requesting page: $page, role: $role, search: $search, trash: $trash")

            // Use role filter if provided
            val response = apiService.getUsers(page = page, role = role, search = search, trash = trash)

            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                val data = userResponse.data.data
                
                Log.d("UserPagingSource", "Success! Fetched ${data.size} users")

                // Pagination logic
                val nextKey = if (userResponse.data.nextPageUrl != null) page + 1 else null
                val prevKey = if (userResponse.data.prevPageUrl != null) page - 1 else null

                LoadResult.Page(
                    data = data,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                Log.e("UserPagingSource", "Failed: Code ${response.code()} - ${response.message()}")
                LoadResult.Error(Exception("Failed to load users: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserPagingSource", "Exception: ${e.message}")
            LoadResult.Error(e)
        }
    }
}
