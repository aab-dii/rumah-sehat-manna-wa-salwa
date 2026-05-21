package com.android.rumahsehatmannawasalwa.ui.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.notification.Notification
import com.android.rumahsehatmannawasalwa.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.android.rumahsehatmannawasalwa.data.service.PusherService

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notificationsState =
        MutableStateFlow<ApiResult<List<Notification>>>(ApiResult.Loading)
    val notificationsState: StateFlow<ApiResult<List<Notification>>> =
        _notificationsState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    // Local mutable list so UI updates are instant (optimistic)
    private val _localNotifications = MutableStateFlow<List<Notification>>(emptyList())

    init {
        // Only load if we have a valid token (e.g. on auto-login startup)
        if (!com.android.rumahsehatmannawasalwa.data.api.RetrofitClient.authToken.isNullOrEmpty()) {
            loadNotifications()
            loadUnreadCount()
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            repository.getNotifications().collect { result ->
                _notificationsState.value = result
                if (result is ApiResult.Success) {
                    _localNotifications.value = result.data
                }
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            val result = repository.getUnreadCount()
            if (result is ApiResult.Success) {
                _unreadCount.value = result.data
            }
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            val result = repository.markAsRead(id)
            if (result is ApiResult.Success) {
                // Optimistic: update local list
                val updated = _localNotifications.value.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
                _localNotifications.value = updated
                _notificationsState.value = ApiResult.Success(updated)
                _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            val result = repository.markAllRead()
            if (result is ApiResult.Success) {
                // Optimistic: mark all as read locally
                val updated = _localNotifications.value.map { it.copy(isRead = true) }
                _localNotifications.value = updated
                _notificationsState.value = ApiResult.Success(updated)
                _unreadCount.value = 0
                _actionMessage.value = "Semua notifikasi ditandai sudah dibaca"
            } else if (result is ApiResult.Error) {
                _actionMessage.value = "Gagal: ${result.error}"
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    private var currentUserId: Int? = null

    fun listenToRealtimeUpdates(userId: Int) {
        if (currentUserId == userId) return

        // Unsubscribe from previous if there's any
        currentUserId?.let { PusherService.unsubscribeFromUserNotifications(it) }

        currentUserId = userId
        
        // Load fresh data when user/session is established
        loadNotifications()
        loadUnreadCount()

        PusherService.subscribeToUserNotifications(userId) { count ->
            val isNewNotification = count > _unreadCount.value
            _unreadCount.value = count

            // Bug #9 Fix: Refresh daftar notifikasi saat ada notif baru masuk via Pusher.
            // Hanya reload jika count benar-benar naik (notif baru), bukan sekedar sync.
            if (isNewNotification) {
                loadNotifications()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentUserId?.let { PusherService.unsubscribeFromUserNotifications(it) }
    }
}
