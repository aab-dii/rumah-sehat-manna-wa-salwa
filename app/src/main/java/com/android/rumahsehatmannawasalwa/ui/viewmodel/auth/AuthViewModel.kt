package com.android.rumahsehatmannawasalwa.ui.viewmodel.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.auth.RegisterRequest
import com.android.rumahsehatmannawasalwa.data.model.auth.UpdateProfileRequest
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    application: Application,
    private val repository: AuthRepository
) : AndroidViewModel(application) {

    // State untuk memantau status login/register (Loading, Success, Error)
    private val _authState = MutableStateFlow<ApiResult<Unit>?>(null)
    val authState: StateFlow<ApiResult<Unit>?> = _authState.asStateFlow()

    // State terpisah untuk Change Password agar tidak bertabrakan dengan authState
    private val _changePasswordState = MutableStateFlow<ApiResult<Unit>?>(null)
    val changePasswordState: StateFlow<ApiResult<Unit>?> = _changePasswordState.asStateFlow()

    // State untuk menyimpan data user yang sedang aktif
    private val _currentUserData = MutableStateFlow<User?>(repository.getCurrentUser())
    val currentUserData: StateFlow<User?> = _currentUserData.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(repository.isNotificationEnabled())
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    fun setNotificationEnabled(isEnabled: Boolean) {
        _isNotificationEnabled.value = isEnabled
        repository.setNotificationEnabled(isEnabled)
    }

    fun toggleNotification() {
        setNotificationEnabled(!_isNotificationEnabled.value)
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = ApiResult.Loading
            when (val result = repository.signInWithGoogle(idToken)) {
                is ApiResult.Success -> {
                    _currentUserData.value = result.data
                    syncFcmToken() // Update FCM token saat login sukses
                    _authState.value = ApiResult.Success(Unit)
                }
                is ApiResult.Error -> {
                    _authState.value = ApiResult.Error(result.error)
                }
                else -> {}
            }
        }
    }

    // --- Fungsi Login ---
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = ApiResult.Error("Email dan Password tidak boleh kosong")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = ApiResult.Error("Format email tidak valid")
            return
        }

        viewModelScope.launch {
            _authState.value = ApiResult.Loading
            when (val result = repository.login(email, pass)) {
                is ApiResult.Success -> {
                    _currentUserData.value = result.data
                    syncFcmToken() // Update FCM token saat login sukses
                    _authState.value = ApiResult.Success(Unit)
                }
                is ApiResult.Error -> {
                    _authState.value = ApiResult.Error(result.error)
                }
                else -> {}
            }
        }
    }

    // --- Fungsi Register ---
    fun register(
        name: String, email: String, phone: String,
        pass: String, confirmPass: String,
        job: String, birthDate: String, address: String, gender: String
    ) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || pass.isBlank() || address.isBlank() || job.isBlank() || birthDate.isBlank()) {
            _authState.value = ApiResult.Error("Semua field wajib diisi")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = ApiResult.Error("Format email tidak valid")
            return
        }

        if (pass.length < 8) {
            _authState.value = ApiResult.Error("Password minimal 8 karakter")
            return
        }

        if (pass.length > 64) {
            _authState.value = ApiResult.Error("Password maksimal 64 karakter")
            return
        }

        if (pass != confirmPass) {
            _authState.value = ApiResult.Error("Konfirmasi password tidak cocok")
            return
        }

        viewModelScope.launch {
            _authState.value = ApiResult.Loading
            val request = RegisterRequest(
                namaLengkap = name,
                email = email,
                password = pass,
                noHp = phone,
                role = "pasien",
                firebaseUid = "",
                pekerjaan = job,
                alamat = address,
                tglLahir = birthDate,
                jenisKelamin = gender,
                specialization = null
            )
            val result = repository.register(request)
            if (result is ApiResult.Success) {
                syncFcmToken() // Update FCM token saat registrasi sukses
                _authState.value = ApiResult.Success(Unit)
            } else if (result is ApiResult.Error) {
                _authState.value = ApiResult.Error(result.error)
            }
        }
    }

    // --- Fungsi Fetch Profile (Sync Data) ---
    private var fetchJob: kotlinx.coroutines.Job? = null

    /**
     * Mengambil profil user. Secara default akan mencoba data lokal dulu (Optimasi).
     */
    fun fetchUserProfile(forceRefresh: Boolean = false) {
        Log.d("TRACKER_STATE", "ViewModel: Memulai fetchUserProfile (forceRefresh=$forceRefresh)...")
        fetchJob?.cancel() 
        fetchJob = viewModelScope.launch {
            repository.fetchUserProfile(forceRefresh).collect { result ->
                _authState.value = when (result) {
                    is ApiResult.Loading -> ApiResult.Loading
                    is ApiResult.Success -> {
                        _currentUserData.value = result.data
                        ApiResult.Success(Unit)
                    }
                    is ApiResult.Error -> {
                        Log.e("TRACKER_STATE", "ViewModel: ERROR -> ${result.error}")
                        ApiResult.Error(result.error)
                    }
                    else -> null
                }
            }
        }
    }

    fun updateUserProfile(name: String, phone: String, job: String, address: String, birthDate: String, gender: String, fotoUrl: String? = null) {
        viewModelScope.launch {
            _authState.value = ApiResult.Loading
            val request = UpdateProfileRequest(
                namaLengkap = name,
                noHp = phone,
                pekerjaan = job,
                alamat = address,
                tglLahir = birthDate,
                jenisKelamin = gender,
                fotoUrl = fotoUrl
            )
            val result = repository.updateUserProfile(request)
            if (result is ApiResult.Success) {
                _currentUserData.value = result.data
                _authState.value = ApiResult.Success(Unit)
            } else if (result is ApiResult.Error) {
                _authState.value = ApiResult.Error(result.error)
            }
        }
    }

    fun logout() {
        Log.d("AUTH_DEBUG", "Logging out... canceling fetch job.")
        fetchJob?.cancel()
        viewModelScope.launch {
            repository.logOut()
            _currentUserData.value = null
            _authState.value = null
        }
    }

    fun resetState() {
        _authState.value = null
        _changePasswordState.value = null
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = ApiResult.Loading
            val result = repository.resetPassword(email)
            _authState.value = result
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        if (newPass.length < 8) {
            _changePasswordState.value = ApiResult.Error("Password baru minimal 8 karakter")
            return
        }
        if (newPass.length > 64) {
            _changePasswordState.value = ApiResult.Error("Password baru maksimal 64 karakter")
            return
        }
        viewModelScope.launch {
            _changePasswordState.value = ApiResult.Loading
            val result = repository.changePassword(oldPass, newPass)
            _changePasswordState.value = result
        }
    }

    fun updateFcmToken(token: String) {
        viewModelScope.launch {
            repository.updateFcmToken(token)
        }
    }

    private fun syncFcmToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                repository.updateFcmToken(token)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to sync FCM Token: ${e.message}")
            }
        }
    }
}