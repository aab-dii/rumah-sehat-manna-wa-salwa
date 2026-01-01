package com.android.rumahsehatmannawasalwa.ui.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.repository.UserPagingSource
import kotlinx.coroutines.launch
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest

class AdminUserViewModel : ViewModel() {

    // Expose flow of PagingData for Users (Main List)
    // Role Filter State (pasien, terapis)
    private val _selectedRole = MutableStateFlow<String?>(null) // null = all, or set default
    val selectedRole: StateFlow<String?> = _selectedRole

    fun setRoleFilter(role: String?) {
        _selectedRole.value = role
    }

    // Dynamic Pager
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userPager = _selectedRole.flatMapLatest { role ->
        Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 1),
            pagingSourceFactory = { UserPagingSource(RetrofitClient.instance, role) }
        ).flow
    }.cachedIn(viewModelScope)

    // State for Dropdowns (Select Patient / Therapist)
    private val _patientList = MutableStateFlow<List<User>>(emptyList())
    val patientList: StateFlow<List<User>> = _patientList

    private val _therapistList = MutableStateFlow<List<User>>(emptyList())
    val therapistList: StateFlow<List<User>> = _therapistList

    fun fetchUserList(role: String) {
        viewModelScope.launch {
            try {
                // Fetch 100 users for dropdown, filtered by role
                val response = RetrofitClient.instance.getUsers(page = 1, role = role, limit = 100)
                if (response.isSuccessful && response.body() != null) {
                    val users = response.body()!!.data.data
                    if (role == "pasien") {
                        _patientList.value = users
                    } else if (role == "terapis") {
                        _therapistList.value = users
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminUserViewModel", "Exception fetching $role", e)
            }
        }
    }
}
