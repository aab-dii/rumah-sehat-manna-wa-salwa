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
import com.android.rumahsehatmannawasalwa.data.model.service.Layanan
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.*

class AdminUserViewModel(application: Application) : AndroidViewModel(application) {

    // --- REFRESH TRIGGER ---
    private val _refreshTrigger = MutableStateFlow(0)
    fun refreshData() {
        _refreshTrigger.value += 1
    }

    // Role Filter State
    private val _selectedRole = MutableStateFlow<String?>(null)
    val selectedRole: StateFlow<String?> = _selectedRole

    fun setRoleFilter(role: String?) {
        _selectedRole.value = role
    }

    // --- SEARCH / FILTER STATE ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _showTrashed = MutableStateFlow(false)
    val showTrashed: StateFlow<Boolean> = _showTrashed

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleTrashFilter(show: Boolean) {
        _showTrashed.value = show
    }

    // --- PAGERS ---
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val patientPager = combine(_searchQuery, _refreshTrigger, _showTrashed) { query, _, trash ->
        Triple(query, trash, 0)
    }
        .debounce(500)
        .flatMapLatest { (query, trash, _) ->
            val isTrash = if (trash) 1 else 0
            Pager(
                config = PagingConfig(pageSize = 10),
                pagingSourceFactory = { UserPagingSource(RetrofitClient.instance, role = "pasien", search = query, trash = isTrash) }
            ).flow
        }
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val therapistPager = combine(_searchQuery, _refreshTrigger, _showTrashed) { query, _, trash ->
        Triple(query, trash, 0)
    }
        .debounce(500)
        .flatMapLatest { (query, trash, _) ->
            val isTrash = if (trash) 1 else 0
            Pager(
                config = PagingConfig(pageSize = 10),
                pagingSourceFactory = { UserPagingSource(RetrofitClient.instance, role = "terapis", search = query, trash = isTrash) }
            ).flow
        }
        .cachedIn(viewModelScope)


        .cachedIn(viewModelScope)


        .cachedIn(viewModelScope)


    // --- DETAIL & EDIT LOGIC ---
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser

    fun selectUser(user: User) {
        _selectedUser.value = user
    }
    
    // Also fetch fresh detail from API
    fun fetchUserDetail(userId: Int) {
        // Placeholder for now, can implement actual API call if needed
    }


    // --- DROPDOWN LISTS (Patient / Therapist) ---
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


    // --- SERVICE LIST (For Dropdown) ---
    private val _serviceList = MutableStateFlow<List<Layanan>>(emptyList())
    val serviceList: StateFlow<List<Layanan>> = _serviceList

    fun fetchServices() {
        viewModelScope.launch {
            try {
                // Fetch all services (limit 100 for now)
                val response = RetrofitClient.instance.getServices(page = 1, limit = 100)
                if (response.isSuccessful && response.body() != null) {
                    _serviceList.value = response.body()!!.data.data
                }
            } catch (e: Exception) {
                // Ignore error for dropdown population, maybe retry later
            }
        }
    }


    // --- ACTION STATE (Add/Delete) ---
    private val _actionState = MutableStateFlow<com.android.rumahsehatmannawasalwa.data.ApiResult<Unit>?>(null)
    val actionState: StateFlow<com.android.rumahsehatmannawasalwa.data.ApiResult<Unit>?> = _actionState

    fun resetActionState() {
        _actionState.value = null
    }

    // --- DELETE USER ---
    fun deleteUser(userId: Int) {
        _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteUser(userId)
                if (response.isSuccessful) {
                    _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Success(Unit)
                    refreshData() // Trigger Refresh
                } else {
                    _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(response.message())
                }
            } catch (e: Exception) {
                _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(e.message ?: "Error")
            }
        }
    }

    // --- RESTORE USER ---
    fun restoreUser(userId: Int) {
        _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.restoreUser(userId)
                if (response.isSuccessful) {
                    _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Success(Unit)
                    refreshData() // Trigger Refresh
                } else {
                    _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(response.message())
                }
            } catch (e: Exception) {
                _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(e.message ?: "Error")
            }
        }
    }

    // --- ADD USER ---
    fun addUser(
        name: String, email: String, password: String,
        phone: String, job: String, specialization: List<String>?, birthDate: String,
        address: String, role: String, gender: String
    ) {
        _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Loading
        
        val appName = "SecondaryAppForCreation"
        var secondaryApp: FirebaseApp? = null
        try {
            secondaryApp = FirebaseApp.getInstance(appName)
        } catch (e: Exception) {
            android.util.Log.e("AdminUserViewModel", "Exception initializing Firebase", e)
            val options = FirebaseApp.getInstance().options
            secondaryApp = FirebaseApp.initializeApp(getApplication(), options, appName)
        }

        if (secondaryApp == null) {
            _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Failed to initialize Firebase Secondary App")
            return
        }

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        android.util.Log.d("AdminUserViewModel", "Starting Add User: Role=$role, Gender=$gender, Job=$job")

        secondaryAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = secondaryAuth.currentUser?.uid
                    if (uid != null) {
                        android.util.Log.d("AdminUserViewModel", "Firebase Created UID: $uid")
                        saveUserToDb(uid, name, email, password, phone, job, specialization, birthDate, address, role, gender)
                        secondaryAuth.signOut()
                    } else {
                        android.util.Log.e("AdminUserViewModel", "Firebase UID is null")
                        _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Firebase UID is null")
                    }
                } else {
                    android.util.Log.e("AdminUserViewModel", "Firebase Creation Failed", task.exception)
                    _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(task.exception?.message ?: "Firebase Creation Failed")
                }
            }
    }

    private fun saveUserToDb(
        uid: String, name: String, email: String, password: String,
        phone: String, job: String, specialization: List<String>?, birthDate: String,
        address: String, role: String, gender: String
    ) {
        viewModelScope.launch {
             try {
                 android.util.Log.d("AdminUserViewModel", "Saving to DB: Role=$role, Job=$job")
                 val request = RegisterRequest(
                     namaLengkap = name,
                     email = email,
                     password = password,
                     noHp = phone,
                     role = role.lowercase(),
                     firebaseUid = uid,
                     pekerjaan = job,
                     specialization = specialization,
                     alamat = address,
                     tglLahir = birthDate,
                     jenisKelamin = gender
                 )
                 val response = RetrofitClient.instance.createUser(request) // Changed to use Admin Endpoint
                 if (response.isSuccessful) {
                     android.util.Log.d("AdminUserViewModel", "DB Save Success: ${response.body()}")
                     _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Success(Unit)
                     refreshData() // Trigger Refresh List
                 } else {
                     val errorMsg = response.errorBody()?.string() ?: response.message()
                     android.util.Log.e("AdminUserViewModel", "DB Save Error: $errorMsg")
                     _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error("DB Error: $errorMsg")
                 }
             } catch (e: Exception) {
                 android.util.Log.e("AdminUserViewModel", "DB Save Exception", e)
                 _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Network Error: ${e.message}")
             }
        }
    }

    // --- UPDATE USER ---
    fun updateUser(
        userId: Int,
        name: String, phone: String, job: String, specialization: List<String>?, birthDate: String,
        address: String, gender: String, photoUri: Uri?
    ) {
        _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Loading
        viewModelScope.launch {
            try {
                // Prepare Multipart Data
                val data = mutableMapOf<String, RequestBody>()
                data["nama_lengkap"] = name.toRequestBody("text/plain".toMediaTypeOrNull())
                data["no_hp"] = phone.toRequestBody("text/plain".toMediaTypeOrNull())
                data["address"] = address.toRequestBody("text/plain".toMediaTypeOrNull()) // Check param name in Controller
                data["alamat"] = address.toRequestBody("text/plain".toMediaTypeOrNull()) // Map both to be safe or check validation
                if (job.isNotBlank()) data["pekerjaan"] = job.toRequestBody("text/plain".toMediaTypeOrNull())
                data["tgl_lahir"] = birthDate.toRequestBody("text/plain".toMediaTypeOrNull())
                data["jenis_kelamin"] = gender.toRequestBody("text/plain".toMediaTypeOrNull())
                if (specialization != null) {
                    // Pass as array? Or handle manual array formatting if generic map. 
                    // Arrays in Multipart via Retrofit typically need separate logic or specific key syntax like "specialization[]"
                    // For now, let's send JSON string if backend supports, OR repeatedly add parts. 
                    // Controller expects array.
                    // Simplified: We send it as array parts. But Map<String, RequestBody> doesn't support multiple values per key easily.
                    // Workaround: Send keys like "specialization[0]", "specialization[1]"
                   specialization.forEachIndexed { index, s ->
                        data["specialization[$index]"] = s.toRequestBody("text/plain".toMediaTypeOrNull())
                   }
                }

                var photoPart: MultipartBody.Part? = null
                photoUri?.let { uri ->
                    val file = getFileFromUri(uri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        photoPart = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                    }
                }
                
                val response = RetrofitClient.instance.updateUserByAdmin(userId, data, photoPart)
                if (response.isSuccessful) {
                     _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Success(Unit)
                     refreshData()
                } else {
                     _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(response.message())
                }
            } catch (e: Exception) {
                _actionState.value = com.android.rumahsehatmannawasalwa.data.ApiResult.Error(e.message ?: "Error")
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val contentResolver = getApplication<Application>().contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("profile_photo", ".jpg", getApplication<Application>().cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
