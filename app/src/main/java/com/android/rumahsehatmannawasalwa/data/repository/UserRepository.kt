package com.android.rumahsehatmannawasalwa.data.repository

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.auth.RegisterRequest
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.data.repository.paging.UserPagingSource
import com.android.rumahsehatmannawasalwa.utils.FileUtils
import com.android.rumahsehatmannawasalwa.utils.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    /**
     * 1. Ambil data User dengan Paging 3 (Support Search & Trash)
     */
    fun getUserPaging(
        role: String,
        search: String?,
        isTrash: Boolean
    ): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                UserPagingSource(
                    apiService = apiService,
                    role = role,
                    search = search,
                    trash = if (isTrash) 1 else 0
                )
            }
        ).flow
    }

    /**
     * 2. Ambil Detail User berdasarkan ID
     */
    fun getUserDetail(userId: Int): Flow<ApiResult<User>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getUser(userId)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data))
            } else {
                emit(ApiResult.Error(ErrorUtils.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Terjadi kesalahan jaringan"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 3. Ambil List User berdasarkan Role (Biasanya untuk Dropdown/Search)
     */
    fun getUsersByRole(role: String): Flow<ApiResult<List<User>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getUsers(page = 1, role = role, limit = 100)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data.data))
            } else {
                emit(ApiResult.Error(ErrorUtils.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Gagal mengambil daftar user"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 4. Simpan User Baru ke Database (Post-Firebase Creation)
     */
    suspend fun createUser(request: RegisterRequest): ApiResult<Unit> {
        return try {
            val response = apiService.createUser(request) // Endpoint Admin create user
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Gagal menyimpan user ke database")
        }
    }

    /**
     * 5. Update User (Support Multipart Image)
     */
    suspend fun updateUser(
        userId: Int,
        data: Map<String, RequestBody>,
        photoUri: Uri?
    ): ApiResult<Unit> {
        return try {
            val photoPart = photoUri?.let { prepareMultipartImage(it, "photo") }
            val response = apiService.updateUserByAdmin(userId, data, photoPart)

            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Gagal memperbarui data user")
        }
    }

    /**
     * 6. Hapus User (Soft Delete)
     */
    suspend fun deleteUser(userId: Int): ApiResult<Unit> {
        return try {
            val response = apiService.deleteUser(userId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Gagal menghapus user")
        }
    }

    // Di dalam UserRepository.kt

    fun getServices(): Flow<ApiResult<List<Service>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getServices(page = 1, limit = 100)
            if (response.isSuccessful && response.body() != null) {
                emit(ApiResult.Success(response.body()!!.data.data))
            } else {
                emit(ApiResult.Error(ErrorUtils.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun restoreUser(userId: Int): ApiResult<Unit> {
        return try {
            val response = apiService.restoreUser(userId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Gagal mengembalikan user")
        }
    }

    // =========================================================================
    // SUPER ADMIN (Sprint 2.1)
    // =========================================================================

    /**
     * Ambil daftar semua admin & super_admin.
     */
    suspend fun getAdminList(): ApiResult<List<User>> {
        return try {
            val response = apiService.getAdminList()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.data)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Gagal mengambil daftar admin")
        }
    }

    /**
     * Toggle aktif/nonaktif akun admin.
     */
    suspend fun toggleAdminActive(userId: Int): ApiResult<Unit> {
        return try {
            val response = apiService.toggleAdminActive(userId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Gagal mengubah status admin")
        }
    }

    /**
     * Reset password admin/terapis — return temporary password.
     */
    suspend fun resetAdminPassword(userId: Int): ApiResult<String> {
        return try {
            val response = apiService.resetAdminPassword(userId)
            if (response.isSuccessful && response.body() != null) {
                val tempPassword = response.body()!!.data["temporary_password"] ?: ""
                ApiResult.Success(tempPassword)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Gagal mereset password")
        }
    }

    private fun prepareMultipartImage(uri: Uri, partName: String): MultipartBody.Part? {
        val file = FileUtils.getFileFromUri(context, uri) ?: return null
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
}