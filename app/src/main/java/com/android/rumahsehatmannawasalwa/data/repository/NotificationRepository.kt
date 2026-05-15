package com.android.rumahsehatmannawasalwa.data.repository

import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.notification.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class NotificationRepository(private val apiService: ApiService) {

    /**
     * Fetch paginated notification list for the current user.
     */
    fun getNotifications(
        page: Int = 1,
        limit: Int = 20,
        unreadOnly: Boolean = false
    ): Flow<ApiResult<List<Notification>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getNotifications(
                page = page,
                limit = limit,
                unreadOnly = if (unreadOnly) 1 else null
            )
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data.data))
            } else {
                emit(ApiResult.Error("Gagal memuat notifikasi"))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get the count of unread notifications.
     */
    suspend fun getUnreadCount(): ApiResult<Int> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUnreadCount()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.data.count)
            } else {
                ApiResult.Error("Gagal mengambil jumlah notifikasi")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Mark a single notification as read.
     */
    suspend fun markAsRead(id: Int): ApiResult<Any> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markNotificationAsRead(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Gagal menandai notifikasi")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Mark all notifications as read.
     */
    suspend fun markAllRead(): ApiResult<Any> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markAllNotificationsRead()
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Gagal menandai semua notifikasi")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Delete a notification.
     */
    suspend fun deleteNotification(id: Int): ApiResult<Any> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteNotification(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Gagal menghapus notifikasi")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }
}
