package com.android.rumahsehatmannawasalwa.ui.viewmodel.service

import com.android.rumahsehatmannawasalwa.data.model.service.Layanan
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.android.rumahsehatmannawasalwa.data.repository.LayananPagingSource
import kotlinx.coroutines.flow.flatMapLatest
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class LayananViewModel : ViewModel() {

    // Expose flow of PagingData (for Lists)
    // Refresh Trigger
    private val _refreshTrigger = MutableStateFlow(0)

    // Expose flow of PagingData (for Lists)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val layananPager = _refreshTrigger.flatMapLatest {
        Pager(
            config = PagingConfig(pageSize = 3, prefetchDistance = 1),
            pagingSourceFactory = { LayananPagingSource(RetrofitClient.instance) }
        ).flow
    }.cachedIn(viewModelScope)

    // State for Dropdown (List)
    private val _serviceList = MutableStateFlow<List<Layanan>>(emptyList())
    val serviceList: kotlinx.coroutines.flow.StateFlow<List<Layanan>> = _serviceList

    fun fetchServiceList() {
        viewModelScope.launch {
            try {
                // Fetch up to 100 services for dropdown
                val response = RetrofitClient.instance.getServices(page = 1, limit = 100)
                if (response.isSuccessful && response.body() != null) {
                    _serviceList.value = response.body()!!.data.data
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    // CRUD Methods
    fun createService(
        name: String, 
        price: Int, 
        duration: Int, 
        description: String, 
        imageUri: android.net.Uri?,
        context: android.content.Context,
        onResult: (com.android.rumahsehatmannawasalwa.data.ApiResult<Layanan>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Loading)
                
                val namePart = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, name)
                val pricePart = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, price.toString())
                val durationPart = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, duration.toString())
                val descPart = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, description)

                val dataMap = mapOf(
                    "name" to namePart,
                    "price" to pricePart,
                    "duration_minutes" to durationPart,
                    "description" to descPart
                )

                var imagePart: okhttp3.MultipartBody.Part? = null
                if (imageUri != null) {
                    val file = com.android.rumahsehatmannawasalwa.utils.FileUtils.getFileFromUri(context, imageUri)
                    if (file != null) {
                        val reqFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        imagePart = okhttp3.MultipartBody.Part.createFormData("image", file.name, reqFile)
                    }
                }

                val response = RetrofitClient.instance.createService(dataMap, imagePart)
                if (response.isSuccessful && response.body() != null) {
                    _refreshTrigger.value += 1
                    onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Success(response.body()!!.data))
                } else {
                    onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Gagal menambah layanan"))
                }
            } catch (e: Exception) {
                onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Error(e.message ?: "Unknown Error"))
            }
        }
    }

    fun updateService(
        id: Int,
        name: String, 
        price: Int, 
        duration: Int, 
        description: String, 
        imageUri: android.net.Uri?,
        context: android.content.Context,
        onResult: (com.android.rumahsehatmannawasalwa.data.ApiResult<Layanan>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Loading)
                
                val namePart = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, name)
                val pricePart = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, price.toString())
                val durationPart = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, duration.toString())
                val descPart = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, description)

                val dataMap = mapOf(
                    "name" to namePart,
                    "price" to pricePart,
                    "duration_minutes" to durationPart,
                    "description" to descPart
                )

                var imagePart: okhttp3.MultipartBody.Part? = null
                if (imageUri != null) {
                    val file = com.android.rumahsehatmannawasalwa.utils.FileUtils.getFileFromUri(context, imageUri)
                    if (file != null) {
                        val reqFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        imagePart = okhttp3.MultipartBody.Part.createFormData("image", file.name, reqFile)
                    }
                }

                val response = RetrofitClient.instance.updateService(id, dataMap, imagePart)
                if (response.isSuccessful && response.body() != null) {
                    _refreshTrigger.value += 1
                    onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Success(response.body()!!.data))
                } else {
                    onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Gagal update layanan"))
                }
            } catch (e: Exception) {
                onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Error(e.message ?: "Unknown Error"))
            }
        }
    }

    fun deleteService(id: Int, onResult: (com.android.rumahsehatmannawasalwa.data.ApiResult<Any?>) -> Unit) {
         viewModelScope.launch {
            try {
                onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Loading)
                val response = RetrofitClient.instance.deleteService(id)
                if (response.isSuccessful) {
                    _refreshTrigger.value += 1
                    onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Success(null))
                } else {
                    onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Gagal menghapus layanan"))
                }
            } catch (e: Exception) {
                onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Error(e.message ?: "Error"))
            }
         }
    }

    fun getServiceDetail(id: Int, onResult: (com.android.rumahsehatmannawasalwa.data.ApiResult<Layanan>) -> Unit) {
         viewModelScope.launch {
            try {
                onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Loading)
                val response = RetrofitClient.instance.getServiceDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Success(response.body()!!.data))
                } else {
                    onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Error("Gagal mengambil data"))
                }
            } catch (e: Exception) {
                onResult(com.android.rumahsehatmannawasalwa.data.ApiResult.Error(e.message ?: "Error"))
            }
         }
    }
}