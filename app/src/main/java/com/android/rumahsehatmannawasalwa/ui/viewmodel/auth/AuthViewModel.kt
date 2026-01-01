package com.android.rumahsehatmannawasalwa.ui.viewmodel.auth

import com.android.rumahsehatmannawasalwa.data.model.auth.User
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import android.content.Context
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.toObject
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.model.auth.RegisterRequest
import com.android.rumahsehatmannawasalwa.data.model.auth.UpdateProfileRequest
import android.util.Log

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    init {
        // Load token from SharedPreferences on init
        val token = sharedPreferences.getString("access_token", null)
        if (token != null) {
            RetrofitClient.authToken = token
            Log.d("AuthViewModel", "Token loaded from prefs: $token")
        }
    }

    // State untuk memantau status (Loading, Sukses, Gagal)
    private val _authState = MutableStateFlow<ApiResult<Unit>?>(null)
    val authState: StateFlow<ApiResult<Unit>?> = _authState

    // --- Fungsi Login (Email & Password) ---
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = ApiResult.Error("Email dan Password tidak boleh kosong")
            return
        }
        _authState.value = ApiResult.Loading
        Log.d("AuthViewModel", "Attempting login with email: $email")
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Login Firebase Success, fetching profile...")
                    fetchUserProfile() // Ambil data profil dari API
                } else {
                    Log.e("AuthViewModel", "Login Firebase Failed: ${task.exception?.message}")
                    _authState.value = ApiResult.Error(task.exception?.message ?: "Login Gagal")
                }
            }
    }

    // --- Fungsi Register Lengkap (Hybrid: Firebase + Laravel) ---
    fun register(
        name: String, email: String, phone: String,
        pass: String, confirmPass: String,
        job: String, birthDate: String, address: String, gender: String
    ) {
        Log.d("AuthViewModel", "Mulai proses registrasi untuk email: $email")

        // 1. Validasi Input Kosong
        if (name.isBlank() || email.isBlank() || phone.isBlank() ||
            pass.isBlank() || job.isBlank() || birthDate.isBlank() ||
            address.isBlank() || gender.isBlank()) {
            Log.e("AuthViewModel", "Validasi Gagal: Data tidak lengkap")
            _authState.value = ApiResult.Error("Mohon lengkapi semua data")
            return
        }

        // 2. Validasi Password Sama
        if (pass != confirmPass) {
            Log.e("AuthViewModel", "Validasi Gagal: Password tidak sama")
            _authState.value = ApiResult.Error("Password tidak sama!")
            return
        }

        _authState.value = ApiResult.Loading

        // 3. Buat Akun di Auth Firebase
        Log.d("AuthViewModel", "Mencoba create user di Firebase Auth...")
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    Log.d("AuthViewModel", "Firebase Auth Sukses! UID: $userId")
                    if (userId != null) {
                        // 4. Kirim Data Lengkap ke API Laravel
                        registerUserToApi(userId, name, email, phone, job, birthDate, address, pass)
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Register Gagal"
                    Log.e("AuthViewModel", "Firebase Auth Gagal: $errorMsg")
                    _authState.value = ApiResult.Error(errorMsg)
                }
            }
    }

    // Fungsi Privat untuk mengirim data ke API Laravel
    private fun registerUserToApi(
        uid: String, name: String, email: String, phone: String,
        job: String, birthDate: String, address: String, pass: String
    ) {
        Log.d("AuthViewModel", "Mencoba kirim data ke API Laravel...")
        val request = RegisterRequest(
            namaLengkap = name,
            email = email,
            password = pass,
            noHp = phone,
            role = "pasien",
            firebaseUid = uid,
            pekerjaan = job,
            alamat = address,
            tglLahir = birthDate
        )

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.registerUser(request)
                if (response.isSuccessful) {
                    Log.d("AuthViewModel", "API Laravel Sukses! Response Code: ${response.code()}")
                    _authState.value = ApiResult.Success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthViewModel", "API Laravel Gagal: ${response.message()} | Body: $errorBody")
                    
                    // Rollback: Hapus user Firebase jika API gagal
                    auth.currentUser?.delete()
                    _authState.value = ApiResult.Error("Gagal simpan ke Database Server: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception saat connect ke API: ${e.message}")
                // Rollback
                auth.currentUser?.delete()
                _authState.value = ApiResult.Error("Koneksi Server Gagal: ${e.message}")
            }
        }
    }

    // --- Fungsi Login Google ---
    fun signInWithGoogle(idToken: String) {
        _authState.value = ApiResult.Loading
        Log.d("AuthViewModel", "Starting Google Sign-In with ID Token")

        // 1. Buat kredensial dari token Google
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // 2. Masuk ke Firebase pakai kredensial itu
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // 3. Cek/Simpan data ke Firestore
                        checkAndSaveUserToFirestore(user)
                    }
                } else {
                    val msg = task.exception?.message ?: "Google Sign-In Gagal"
                    Log.e("AuthViewModel", "Google Firebase Auth Failed: $msg")
                    _authState.value = ApiResult.Error(msg)
                }
            }
    }

    // Fungsi Privat untuk sync user Google ke Backend Laravel
    private fun checkAndSaveUserToFirestore(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        val uid = firebaseUser.uid
        val docRef = db.collection("users").document(uid)
        Log.d("AuthViewModel", "Checking Firestore for Google User UID: $uid")

        // 1. Simpan ke Firestore (Minimal Copy)
        docRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val newUser = User(
                    firebaseUid = uid,
                    name = firebaseUser.displayName ?: "User Google",
                    email = firebaseUser.email ?: "",
                    phoneNumber = firebaseUser.phoneNumber ?: "",
                    role = "pasien"
                )
                docRef.set(newUser)
            }
        }
        
        // 2. SYNC KE BACKEND LARAVEL
        // Cek apakah user sudah ada di database Laravel?
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Syncing Google User to Laravel Backend...")
                val response = RetrofitClient.instance.getUserProfile(uid)
                if (response.isSuccessful) {
                    // USER SUDAH ADA -> Login Sukses
                    Log.d("AuthViewModel", "User found in Backend. Login Success.")
                    val userResponse = response.body()
                    userResponse?.data?.accessToken?.let { token ->
                        RetrofitClient.authToken = token
                        sharedPreferences.edit().putString("access_token", token).apply()
                    }
                    _authState.value = ApiResult.Success(Unit)
                } else {
                    if (response.code() == 404) {
                        // USER BELUM ADA -> REGISTER OTOMATIS
                        Log.d("AuthViewModel", "User NOT FOUND in Backend (404). Registering automatically...")
                        val registerRequest = RegisterRequest(
                            namaLengkap = firebaseUser.displayName ?: "Google User",
                            email = firebaseUser.email ?: "",
                            password = "GoogleLoginDefault123!", // Password default aman
                            noHp = firebaseUser.phoneNumber ?: "-",
                            role = "pasien",
                            firebaseUid = uid,
                            pekerjaan = "-", // Default dummy
                            alamat = "-", // Default dummy
                            tglLahir = "2000-01-01" // Default dummy
                        )
                        val regResponse = RetrofitClient.instance.registerUser(registerRequest)
                        if (regResponse.isSuccessful) {
                             Log.d("AuthViewModel", "Auto-Register Success! Login Success.")
                             
                             // Simpan token
                             val regBody = regResponse.body()
                             regBody?.data?.accessToken?.let { token ->
                                 RetrofitClient.authToken = token
                                 sharedPreferences.edit().putString("access_token", token).apply()
                                 Log.d("AuthViewModel", "Token saved after auto-register: $token")
                             }
                             
                             _authState.value = ApiResult.Success(Unit)
                        } else {
                             val err = regResponse.errorBody()?.string()
                             Log.e("AuthViewModel", "Auto-Register Failed: $err")
                             _authState.value = ApiResult.Error("Gagal sinkronisasi akun Google: $err")
                        }
                    } else {
                        Log.e("AuthViewModel", "Backend Error: ${response.message()}")
                        _authState.value = ApiResult.Error("Gagal cek akun: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sync Exception: ${e.message}")
                _authState.value = ApiResult.Error("Koneksi Error: ${e.message}")
            }
        }
    }

    // Reset state setelah navigasi
    fun resetState() {
        _authState.value = null
    }

    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData: StateFlow<User?> = _currentUserData

    // 2. Fungsi untuk mengambil data profil dari API Laravel
    fun fetchUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            Log.d("AuthViewModel", "Fetching user profile for UID: $uid")
            viewModelScope.launch {
                try {
                    val response = RetrofitClient.instance.getUserProfile(uid)
                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        if (userResponse != null) {
                            Log.d("AuthViewModel", "User profile fetched: ${userResponse.data.name}")
                            _currentUserData.value = userResponse.data
                            _authState.value = ApiResult.Success(Unit)
                            
                            // Save Token
                            userResponse.data.accessToken?.let { token ->
                                RetrofitClient.authToken = token
                                sharedPreferences.edit().putString("access_token", token).apply()
                                Log.d("AuthViewModel", "Token saved: $token")
                            }
                        } else {
                            Log.e("AuthViewModel", "Response body is null")
                            _authState.value = ApiResult.Error("Gagal mengambil data profil")
                        }
                    } else {
                        Log.e("AuthViewModel", "API Error: ${response.message()}")
                        _authState.value = ApiResult.Error("Gagal mengambil profil: ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Exception fetching profile: ${e.message}")
                    _authState.value = ApiResult.Error("Koneksi Error: ${e.message}")
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
        RetrofitClient.authToken = null
        sharedPreferences.edit().remove("access_token").apply()
        _authState.value = null
        _currentUserData.value = null
    }

    // --- Fungsi Reset Password ---
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = ApiResult.Error("Email tidak boleh kosong")
            return
        }
        _authState.value = ApiResult.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = ApiResult.Success(Unit)
                    Log.d("AuthViewModel", "Reset Password Email sent to $email")
                } else {
                    val error = task.exception?.message ?: "Gagal mengirim email reset"
                    Log.e("AuthViewModel", "Reset Password Failed: $error")
                    _authState.value = ApiResult.Error(error)
                }
            }
    }
    // --- Fungsi Update User Profile ---
    fun updateUserProfile(
        name: String, phone: String, job: String,
        address: String, birthDate: String, gender: String
    ) {
        if (name.isBlank() || phone.isBlank() || job.isBlank() ||
            address.isBlank() || birthDate.isBlank() || gender.isBlank()) {
            _authState.value = ApiResult.Error("Mohon lengkapi semua data")
            return
        }

        _authState.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val request = UpdateProfileRequest(
                    namaLengkap = name,
                    noHp = phone,
                    pekerjaan = job,
                    alamat = address,
                    tglLahir = birthDate,
                    jenisKelamin = gender
                )
                val response = RetrofitClient.instance.updateProfile(request)
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        Log.d("AuthViewModel", "Update Profile Success: ${userResponse.message}")
                        // Update local user data
                        _currentUserData.value = userResponse.data
                        _authState.value = ApiResult.Success(Unit)
                    } else {
                        _authState.value = ApiResult.Error("Gagal update profil: Response kosong")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthViewModel", "Update Profile Failed: ${response.message()} | Body: $errorBody")
                    _authState.value = ApiResult.Error("Gagal update profil: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception update profile: ${e.message}")
                _authState.value = ApiResult.Error("Koneksi Error: ${e.message}")
            }
        }
    }
}

// Helper Class untuk Status
