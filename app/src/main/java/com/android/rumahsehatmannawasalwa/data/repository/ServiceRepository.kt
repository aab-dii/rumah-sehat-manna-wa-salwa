package com.android.rumahsehatmannawasalwa.data.repository

import android.content.Context
import android.net.Uri
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.utils.ErrorUtils
import com.android.rumahsehatmannawasalwa.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ServiceRepository(private val apiService: ApiService) {

    /**
     * Fetch all services as a simple list (no paging needed for small data sets).
     */
    suspend fun getServiceList(limit: Int = 100): ApiResult<List<Service>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getServices(page = 1, limit = limit)
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!.data.data)
                } else {
                    ApiResult.Error(ErrorUtils.parseErrorMessage(response))
                }
            } catch (e: Exception) {
                ApiResult.Error("Error: ${e.message}")
            }
        }

    /**
     * Get detail of a single service.
     */
    suspend fun getServiceDetail(id: Int): ApiResult<Service> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getServiceDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!.data)
                } else {
                    ApiResult.Error(ErrorUtils.parseErrorMessage(response))
                }
            } catch (e: Exception) {
                ApiResult.Error("Error: ${e.message}")
            }
        }

    /**
     * Create a new service.
     */
    suspend fun createService(
        name: String,
        price: Int,
        duration: Int,
        description: String,
        imageUri: Uri?,
        context: Context
    ): ApiResult<Service> = withContext(Dispatchers.IO) {
        try {
            val dataMap = mapOf(
                "name"             to RequestBody.create(MultipartBody.FORM, name),
                "price"            to RequestBody.create(MultipartBody.FORM, price.toString()),
                "duration_minutes" to RequestBody.create(MultipartBody.FORM, duration.toString()),
                "description"      to RequestBody.create(MultipartBody.FORM, description)
            )
            val imagePart = buildImagePart(imageUri, context)
            val response = apiService.createService(dataMap, imagePart)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.data)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown Error")
        }
    }

    /**
     * Update an existing service.
     */
    suspend fun updateService(
        id: Int,
        name: String,
        price: Int,
        duration: Int,
        description: String,
        imageUri: Uri?,
        context: Context
    ): ApiResult<Service> = withContext(Dispatchers.IO) {
        try {
            val dataMap = mapOf(
                "name"             to RequestBody.create(MultipartBody.FORM, name),
                "price"            to RequestBody.create(MultipartBody.FORM, price.toString()),
                "duration_minutes" to RequestBody.create(MultipartBody.FORM, duration.toString()),
                "description"      to RequestBody.create(MultipartBody.FORM, description)
            )
            val imagePart = buildImagePart(imageUri, context)
            val response = apiService.updateService(id, dataMap, imagePart)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.data)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown Error")
        }
    }

    /**
     * Delete a service by ID.
     */
    suspend fun deleteService(id: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteService(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error")
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private fun buildImagePart(imageUri: Uri?, context: Context): MultipartBody.Part? {
        if (imageUri == null) return null
        val file = FileUtils.getFileFromUri(context, imageUri) ?: return null
        val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData("image", file.name, reqFile)
    }
}
