package com.android.rumahsehatmannawasalwa.ui.viewmodel.user

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.auth.RegisterRequest
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.data.repository.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AdminUserViewModel(
    application: Application,
    private val repository: UserRepository
) : AndroidViewModel(application) {

    // -------------------------------------------------------------------------
    // STATE
    // -------------------------------------------------------------------------

    private val _refreshTrigger = MutableStateFlow(0)
    private val _searchQuery    = MutableStateFlow("")
    private val _showTrashed    = MutableStateFlow(false)

    val searchQuery:  StateFlow<String>  = _searchQuery
    val showTrashed:  StateFlow<Boolean> = _showTrashed

    private val _serviceList = MutableStateFlow<List<Service>>(emptyList())
    val serviceList: StateFlow<List<Service>> = _serviceList

    private val _patientList   = MutableStateFlow<List<User>>(emptyList())
    val patientList: StateFlow<List<User>> = _patientList

    private val _therapistList = MutableStateFlow<List<User>>(emptyList())
    val therapistList: StateFlow<List<User>> = _therapistList

    private val _userDetailState = MutableStateFlow<ApiResult<User>>(ApiResult.Loading)
    val userDetailState: StateFlow<ApiResult<User>> = _userDetailState

    private val _actionState = MutableStateFlow<ApiResult<Unit>?>(null)
    val actionState: StateFlow<ApiResult<Unit>?> = _actionState

    // -------------------------------------------------------------------------
    // PAGER — cache per role agar tidak dibuat ulang setiap recompose
    // -------------------------------------------------------------------------

    private val pagerCache = mutableMapOf<String, Flow<PagingData<User>>>()

    fun getUserPager(role: String): Flow<PagingData<User>> {
        return pagerCache.getOrPut(role) {
            combine(_searchQuery, _refreshTrigger, _showTrashed) { query, _, trash ->
                query to trash
            }
                .debounce(500)
                .flatMapLatest { (query, trash) ->
                    repository.getUserPaging(
                        role    = role,
                        search  = query,
                        isTrash = trash
                    )
                }
                .cachedIn(viewModelScope)
        }
    }

    // -------------------------------------------------------------------------
    // USER DETAIL
    // -------------------------------------------------------------------------

    fun fetchUserDetail(userId: Int) {
        viewModelScope.launch {
            _userDetailState.value = ApiResult.Loading
            repository.getUserDetail(userId).collect { result ->
                _userDetailState.value = result
            }
        }
    }

    // -------------------------------------------------------------------------
    // DROPDOWN DATA
    // -------------------------------------------------------------------------

    fun fetchUserList(role: String) {
        viewModelScope.launch {
            repository.getUsersByRole(role).collect { result ->
                if (result is ApiResult.Success) {
                    if (role == "pasien") _patientList.value = result.data
                    else _therapistList.value = result.data
                }
            }
        }
    }

    fun fetchServices() {
        viewModelScope.launch {
            repository.getServices().collect { result ->
                if (result is ApiResult.Success) {
                    _serviceList.value = result.data
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // ACTION STATE
    // -------------------------------------------------------------------------

    fun resetActionState() { _actionState.value = null }
    fun refreshData()       { _refreshTrigger.value += 1 }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _actionState.value = ApiResult.Loading
            val result = repository.deleteUser(userId)
            _actionState.value = result
            if (result is ApiResult.Success) refreshData()
        }
    }

    fun restoreUser(userId: Int) {
        viewModelScope.launch {
            _actionState.value = ApiResult.Loading
            val result = repository.restoreUser(userId)
            _actionState.value = result
            if (result is ApiResult.Success) refreshData()
        }
    }

    // Buat user baru via Firebase Secondary App agar admin tidak ikut logout
    fun addUser(
        name: String, email: String, phone: String, role: String,
        job: String, specialization: List<String>?, address: String,
        birthDate: String, gender: String
    ) {
        _actionState.value = ApiResult.Loading

        // Password default diatur di ViewModel agar UI tetap bersih
        val defaultPassword = "rumahsehat123"
        
        val appName = "AdminToolApp"
        val secondaryApp = try {
            FirebaseApp.getInstance(appName)
        } catch (e: Exception) {
            val options = FirebaseApp.getInstance().options
            FirebaseApp.initializeApp(getApplication(), options, appName)
        }

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
        secondaryAuth.createUserWithEmailAndPassword(email, defaultPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    if (uid != null) {
                        val request = RegisterRequest(
                            namaLengkap = name,
                            email = email,
                            password = defaultPassword,
                            noHp = phone,
                            role = role,
                            firebaseUid = uid,
                            pekerjaan = job,
                            specialization = specialization,
                            alamat = address,
                            tglLahir = birthDate.ifBlank { "2000-01-01" },
                            jenisKelamin = gender
                        )
                        saveUserToDb(request)
                        secondaryAuth.signOut()
                    }
                } else {
                    _actionState.value = ApiResult.Error(
                        task.exception?.message ?: "Firebase Error"
                    )
                }
            }
    }

    private fun saveUserToDb(request: RegisterRequest) {
        viewModelScope.launch {
            val result = repository.createUser(request)
            _actionState.value = result
            if (result is ApiResult.Success) refreshData()
        }
    }

    fun updateUser(
        userId: Int,
        name: String, phone: String, job: String,
        specialization: List<String>?, birthDate: String,
        address: String, gender: String, photoUri: android.net.Uri?
    ) {
        viewModelScope.launch {
            _actionState.value = ApiResult.Loading

            val data = mutableMapOf<String, RequestBody>().apply {
                put("nama_lengkap", name.toRequestBody("text/plain".toMediaTypeOrNull()))
                put("no_hp",        phone.toRequestBody("text/plain".toMediaTypeOrNull()))
                put("alamat",       address.toRequestBody("text/plain".toMediaTypeOrNull()))
                put("pekerjaan",    job.toRequestBody("text/plain".toMediaTypeOrNull()))
                put("tgl_lahir",    birthDate.toRequestBody("text/plain".toMediaTypeOrNull()))
                put("jenis_kelamin",gender.toRequestBody("text/plain".toMediaTypeOrNull()))
                specialization?.forEachIndexed { index, s ->
                    put("specialization[$index]", s.toRequestBody("text/plain".toMediaTypeOrNull()))
                }
            }

            val result = repository.updateUser(userId, data, photoUri)
            _actionState.value = result
            if (result is ApiResult.Success) refreshData()
        }
    }

    // -------------------------------------------------------------------------
    // SEARCH HELPERS
    // -------------------------------------------------------------------------

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun toggleTrashFilter(show: Boolean)    { _showTrashed.value = show  }
}