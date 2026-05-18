package com.android.rumahsehatmannawasalwa.data.repository

import android.util.Log
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.local.pref.UserPreference
import com.android.rumahsehatmannawasalwa.data.model.auth.RegisterRequest
import com.android.rumahsehatmannawasalwa.data.model.auth.UpdateProfileRequest
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.auth.UpdateFcmTokenRequest
import com.android.rumahsehatmannawasalwa.data.model.common.ApiResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference,
    private val firebaseAuth: FirebaseAuth
) {

    // Pulihkan sesi dari lokal saat app dibuka
    init {
        val savedUser = userPreference.getUser()
        if (savedUser != null && !savedUser.accessToken.isNullOrEmpty()) {
            RetrofitClient.authToken = savedUser.accessToken
        }
    }

    // -------------------------------------------------------------------------
    // AUTENTIKASI
    // -------------------------------------------------------------------------

    // Login dengan Google
    suspend fun signInWithGoogle(idToken: String): ApiResult<User> {
        return try {
            // Verifikasi ke Firebase
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            // Ambil data dari Google
            val firebaseUser = authResult.user
                ?: return ApiResult.Error("Gagal mendapatkan data akun Google")

            val firebaseToken = firebaseUser.getIdToken(false).await().token
                ?: return ApiResult.Error("Gagal mendapatkan token Firebase")

            // Sync ke Laravel
            val syncResponse = apiService.syncFirebase(
                mapOf(
                    "id_token"  to firebaseToken,
                    "name"      to (firebaseUser.displayName ?: ""),
                    "photo_url" to (firebaseUser.photoUrl?.toString() ?: "")
                )
            )

            when {
                // Akun sudah ada
                syncResponse.isSuccessful && syncResponse.body() != null -> {
                    val userData = syncResponse.body()!!.data
                    val localUser = userPreference.getUser()
                    
                    // Gabungkan data: pertahankan foto database lokal jika API mengembalikan null
                    val finalUser = if (localUser != null && userData.id == localUser.id) {
                        userData.copy(
                            profilePhotoPath = userData.profilePhotoPath ?: localUser.profilePhotoPath,
                            profilePhotoUrl = userData.profilePhotoUrl ?: localUser.profilePhotoUrl
                        )
                    } else userData

                    attachSanctumToken(finalUser.accessToken)
                    userPreference.saveUser(finalUser)
                    ApiResult.Success(finalUser)
                }

                // Akun belum ada, register otomatis
                syncResponse.code() == 404 -> {
                    val registerRequest = RegisterRequest(
                        namaLengkap  = firebaseUser.displayName ?: "Google User",
                        email        = firebaseUser.email ?: "",
                        password     = "RumahSehat123",
                        noHp         = firebaseUser.phoneNumber ?: "-",
                        role         = "pasien",
                        firebaseUid  = firebaseUser.uid,
                        pekerjaan    = "-",
                        alamat       = "-",
                        tglLahir     = "2000-01-01",
                        jenisKelamin = "-",
                        fotoUrl      = firebaseUser.photoUrl?.toString()
                            ?.replace("s96-c", "s400-c")
                    )

                    val registerResponse = apiService.registerUser(registerRequest)
                    if (registerResponse.isSuccessful && registerResponse.body() != null) {
                        val newUser = registerResponse.body()!!.data
                        // Wajib dipasang agar CompleteProfile tidak 401
                        attachSanctumToken(newUser.accessToken)
                        userPreference.saveUser(newUser)
                        ApiResult.Success(newUser)
                    } else {
                        ApiResult.Error(getErrorMessage(registerResponse))
                    }
                }

                else -> ApiResult.Error(getErrorMessage(syncResponse))
            }
        } catch (e: Exception) {
            ApiResult.Error("Login Google gagal: ${e.localizedMessage}")
        }
    }

    // Login dengan email & password
    suspend fun login(email: String, pass: String): ApiResult<User> {
        return try {
            // Verifikasi ke Firebase
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user
                ?: return ApiResult.Error("User tidak ditemukan")

            val firebaseToken = firebaseUser.getIdToken(false).await().token
                ?: return ApiResult.Error("Gagal mendapatkan token")

            // Sync ke Laravel, ambil token Sanctum
            val response = apiService.syncFirebase(mapOf("id_token" to firebaseToken))
            if (response.isSuccessful && response.body() != null) {
                val userData = response.body()!!.data
                val localUser = userPreference.getUser()
                
                // Gabungkan data agar foto tidak hilang
                val finalUser = if (localUser != null && userData.id == localUser.id) {
                    userData.copy(
                        profilePhotoPath = userData.profilePhotoPath ?: localUser.profilePhotoPath,
                        profilePhotoUrl = userData.profilePhotoUrl ?: localUser.profilePhotoUrl
                    )
                } else userData

                attachSanctumToken(finalUser.accessToken)
                userPreference.saveUser(finalUser)
                ApiResult.Success(finalUser)
            } else {
                ApiResult.Error(getErrorMessage(response))
            }
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthInvalidUserException        -> "Email tidak terdaftar."
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Email atau password salah."
                else -> e.localizedMessage ?: "Terjadi kesalahan saat masuk."
            }
            ApiResult.Error(message)
        }
    }

    // Daftar akun baru dengan email
    // Buat akun Firebase dulu, lalu kirim ke Laravel
    // Kalau salah satu gagal, akun Firebase langsung dihapus
    suspend fun register(request: RegisterRequest): ApiResult<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(request.email, request.password).await()
            val uid = firebaseAuth.currentUser?.uid
                ?: return ApiResult.Error("Gagal membuat akun Firebase")

            val response = apiService.registerUser(request.copy(firebaseUid = uid))
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                firebaseAuth.currentUser?.delete()?.await()
                ApiResult.Error(getErrorMessage(response))
            }
        } catch (e: Exception) {
            firebaseAuth.currentUser?.delete()?.await()
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthUserCollisionException      -> "Email sudah digunakan."
                is com.google.firebase.auth.FirebaseAuthWeakPasswordException       -> "Password terlalu lemah."
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Format email tidak valid."
                else -> e.localizedMessage ?: "Gagal melakukan registrasi."
            }
            ApiResult.Error(message)
        }
    }

    // Logout dari Firebase dan hapus semua data lokal
    suspend fun logOut() {
        try {
            apiService.logout()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Gagal memanggil logout API: ${e.message}")
        }

        firebaseAuth.signOut()
        userPreference.logout()
        RetrofitClient.authToken = null
    }

    // Kirim email reset password dengan validasi Laravel terlebih dahulu
    suspend fun resetPassword(email: String): ApiResult<Unit> {
        return try {
            if (email.isBlank()) return ApiResult.Error("Email tidak boleh kosong")
            
            // 1. Validasi ke Laravel (cek exist & pastikan role pasien)
            val response = apiService.forgotPassword(mapOf("email" to email))
            if (!response.isSuccessful) {
                return ApiResult.Error(getErrorMessage(response))
            }

            // 2. Kirim email via Firebase
            firebaseAuth.sendPasswordResetEmail(email).await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Gagal mengirim email reset password")
        }
    }

    // Ubah Password via Halaman Profil
    suspend fun changePassword(oldPass: String, newPass: String): ApiResult<Unit> {
        return try {
            // 1. Reauthenticate Firebase
            val firebaseUser = firebaseAuth.currentUser
                ?: return ApiResult.Error("Sesi telah berakhir. Silakan login kembali.")
            
            val email = firebaseUser.email
                ?: return ApiResult.Error("Akun login Google tidak memiliki password, gunakan menu reset.")

            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPass)
            firebaseUser.reauthenticate(credential).await()

            // 2. Beritahu Laravel untuk mengupdate Hash dan Firebase Server
            val request = mapOf(
                "old_password" to oldPass,
                "new_password" to newPass
            )
            val response = apiService.changePassword(request)
            
            if (response.isSuccessful) {
                logOut()
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(getErrorMessage(response))
            }
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Password lama tidak sesuai."
                is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException -> "Sesi terlalu lama. Silakan login ulang."
                else -> e.localizedMessage ?: "Gagal mengubah kata sandi."
            }
            ApiResult.Error(message)
        }
    }

    // -------------------------------------------------------------------------
    // DATA USER
    // -------------------------------------------------------------------------

    // Ambil data user dari lokal
    fun getCurrentUser(): User? {
        return userPreference.getUser()
    }

    // Cek apakah sesi lokal masih ada
    fun isSessionValid(): Boolean {
        val user = userPreference.getUser()
        return user != null && !user.accessToken.isNullOrEmpty()
    }

    // Ambil profil user
    // Pakai data lokal dulu, sync ke backend hanya kalau forceRefresh = true
    fun fetchUserProfile(forceRefresh: Boolean = false): Flow<ApiResult<User>> = flow {
        val localUser = userPreference.getUser()

        if (!forceRefresh && localUser != null && !localUser.accessToken.isNullOrEmpty()) {
            emit(ApiResult.Success(localUser))
            return@flow
        }

        emit(ApiResult.Loading)
        try {
            val firebaseUser = firebaseAuth.currentUser
                ?: throw Exception("Sesi Firebase berakhir, silakan login kembali")

            val firebaseToken = firebaseUser.getIdToken(forceRefresh).await().token
                ?: throw Exception("Gagal memperbarui token")

            // Sync ke Laravel
            val response = apiService.syncFirebase(
                mapOf(
                    "id_token"  to firebaseToken,
                    "photo_url" to (firebaseUser.photoUrl?.toString() ?: "")
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val userData = response.body()!!.data
                val localUser = userPreference.getUser()
                
                // Gabungkan data: Jangan biarkan API yang tidak lengkap menghapus foto database kita
                val finalUser = if (localUser != null && userData.id == localUser.id) {
                    userData.copy(
                        profilePhotoPath = userData.profilePhotoPath ?: localUser.profilePhotoPath,
                        profilePhotoUrl = userData.profilePhotoUrl ?: localUser.profilePhotoUrl
                    )
                } else userData

                attachSanctumToken(finalUser.accessToken)
                userPreference.saveUser(finalUser)
                emit(ApiResult.Success(finalUser))
            } else {
                emit(ApiResult.Error(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }.flowOn(Dispatchers.IO)

    // Update profil user
    suspend fun updateUserProfile(request: UpdateProfileRequest): ApiResult<User> {
        return try {
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!.data
                userPreference.saveUser(updatedUser)
                ApiResult.Success(updatedUser)
            } else {
                ApiResult.Error(getErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    // -------------------------------------------------------------------------
    // FCM & NOTIFIKASI
    // -------------------------------------------------------------------------

    // Kirim FCM token ke backend
    suspend fun updateFcmToken(token: String) {
        if (token == userPreference.getFcmToken()) {
            Log.d("AuthRepository", "FCM Token sama, tidak perlu update ke server.")
            return
        }

        try {
            val response = apiService.updateFcmToken(UpdateFcmTokenRequest(token))
            if (response.isSuccessful) {
                userPreference.saveFcmToken(token)
                Log.d("AuthRepository", "FCM Token berhasil diperbarui di server.")
            } else {
                Log.e("AuthRepository", "Gagal update FCM token: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error saat update FCM token", e)
        }
    }

    fun setNotificationEnabled(isEnabled: Boolean) {
        userPreference.setNotificationEnabled(isEnabled)
    }

    fun isNotificationEnabled(): Boolean {
        return userPreference.isNotificationEnabled()
    }

    // -------------------------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------------------------

    // Pasang token Sanctum ke Retrofit
    private fun attachSanctumToken(token: String?) {
        if (!token.isNullOrEmpty()) {
            RetrofitClient.authToken = token
        }
    }

    // Ambil pesan error dari response backend
    private fun getErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
            apiResponse.meta.message ?: response.message()
        } catch (e: Exception) {
            response.message() ?: "Kesalahan tidak diketahui"
        }
    }
}