package com.android.rumahsehatmannawasalwa.ui.viewmodel.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.repository.UserPagingSource
import kotlinx.coroutines.launch
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.auth.RegisterRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.app
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.debounce

class AdminUserViewModel(application: Application) : AndroidViewModel(application) {

    // Expose flow of PagingData for Users (Main List)
    // Role Filter State (pasien, terapis)
    private val _selectedRole = MutableStateFlow<String?>(null) // null = all, or set default
    val selectedRole: StateFlow<String?> = _selectedRole

    fun setRoleFilter(role: String?) {
        _selectedRole.value = role
    }

    // Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Separate Pagers to persist state (avoid reload on switch), but reload on SEARCH change
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val patientPager = _searchQuery
        .debounce(500) // Tunggu 600ms setelah user berhenti ngetik (biar hemat request)
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(pageSize = 10),
                pagingSourceFactory = {
                    // Pastikan UserPagingSource kamu menerima parameter 'search'
                    UserPagingSource(RetrofitClient.instance, role = "pasien", search = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val therapistPager = _searchQuery
        .debounce(500)
        .flatMapLatest { query ->
        Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 1),
            pagingSourceFactory = { UserPagingSource(RetrofitClient.instance, "terapis", query) }
        ).flow
    }.cachedIn(viewModelScope)

    // State for Dropdowns (Select Patient / Therapist)
    private val _patientList = MutableStateFlow<List<User>>(emptyList())
    val patientList: StateFlow<List<User>> = _patientList

    private val _therapistList = MutableStateFlow<List<User>>(emptyList())
    val therapistList: StateFlow<List<User>> = _therapistList

    
    // --- Detail & Delete Logic ---
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser

    fun selectUser(user: User) {
        _selectedUser.value = user
    }
    
    // Also fetch fresh detail from API
    fun fetchUserDetail(userId: Int) {
        // ... (Optional, if we want fresh data, but list data is usually enough for now)
    }

    private val _actionState = MutableStateFlow<com.android.rumahsehatmannawasalwa.data.ApiResult<Unit>?>(null)
    val actionState: StateFlow<com.android.rumahsehatmannawasalwa.data.ApiResult<Unit>?> = _actionState

    fun deleteUser(userId: Int) {
        _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteUser(userId)
                if (response.isSuccessful) {
                    _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Success(Unit)
                    // Invalidate pagers to refresh list
                    // Trigger refresh if needed, usually simple navigation back or refresh trigger
                } else {
                    _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(response.message())
                }
            } catch (e: Exception) {
                _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(e.message ?: "Error")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = null
    }

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

    // --- ADD USER LOGIC ---
    fun addUser(
        name: String, email: String, password: String,
        phone: String, job: String, birthDate: String,
        address: String, role: String
    ) {
        _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Loading
        
        // Use a Secondary App instance to avoid logging out the current Admin
        val appName = "SecondaryAppForCreation"
        var secondaryApp: FirebaseApp? = null
        try {
            secondaryApp = FirebaseApp.getInstance(appName)
        } catch (e: IllegalStateException) {
            // App not initialized yet
            val options = FirebaseApp.getInstance().options
            secondaryApp = FirebaseApp.initializeApp(getApplication(), options, appName)
        }

        if (secondaryApp == null) {
            _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Failed to initialize Firebase Secondary App")
            return
        }

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        secondaryAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = secondaryAuth.currentUser?.uid
                    if (uid != null) {
                        // Success in Firebase, now save to DB
                        saveUserToDb(uid, name, email, password, phone, job, birthDate, address, role)
                        // Sign out the secondary instance just in case
                        secondaryAuth.signOut()
                    } else {
                        _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Firebase UID is null")
                    }
                } else {
                    _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(task.exception?.message ?: "Firebase Creation Failed")
                }
            }
    }

    private fun saveUserToDb(
        uid: String, name: String, email: String, password: String,
        phone: String, job: String, birthDate: String,
        address: String, role: String
    ) {
        viewModelScope.launch {
             try {
                 val request = RegisterRequest(
                     namaLengkap = name,
                     email = email,
                     password = password,
                     noHp = phone,
                     role = role.lowercase(), // Ensure lowercase matches backend expectation (pasien/terapis)
                     firebaseUid = uid,
                     pekerjaan = job,
                     alamat = address,
                     tglLahir = birthDate,
                     jenisKelamin = "-" // Default dummy gender as strictly required by validation usually
                 )
                 val response = RetrofitClient.instance.registerUser(request)
                 if (response.isSuccessful) {
                     _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Success(Unit)
                 } else {
                     val errorMsg = response.errorBody()?.string() ?: response.message()
                     _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error("DB Error: $errorMsg")
                     // Optionally delete firebase user here if transaction logic is strict
                 }
             } catch (e: Exception) {
                 _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Network Error: ${e.message}")
             }
        }
    }
}
