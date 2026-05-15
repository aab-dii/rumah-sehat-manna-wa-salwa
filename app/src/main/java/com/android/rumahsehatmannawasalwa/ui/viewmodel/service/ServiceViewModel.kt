package com.android.rumahsehatmannawasalwa.ui.viewmodel.service

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceViewModel(private val repository: ServiceRepository) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────
    private val _serviceList = MutableStateFlow<List<Service>>(emptyList())
    val serviceList: StateFlow<List<Service>> = _serviceList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ── Fetch list ────────────────────────────────────────────────────────────
    fun fetchServiceList() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getServiceList()) {
                is ApiResult.Success -> _serviceList.value = result.data
                is ApiResult.Error   -> { /* errors logged; UI shows empty state */ }
                ApiResult.Loading    -> { /* handled by isLoading */ }
            }
            _isLoading.value = false
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────
    fun createService(
        name: String,
        price: Int,
        duration: Int,
        description: String,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createService(name, price, duration, description, imageUri, context)
            _isLoading.value = false
            
            when (result) {
                is ApiResult.Success -> {
                    fetchServiceList()
                    onSuccess()
                }
                is ApiResult.Error -> onError(result.error)
                else -> {}
            }
        }
    }

    fun updateService(
        id: Int,
        name: String,
        price: Int,
        duration: Int,
        description: String,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateService(id, name, price, duration, description, imageUri, context)
            _isLoading.value = false
            
            when (result) {
                is ApiResult.Success -> {
                    fetchServiceList()
                    onSuccess()
                }
                is ApiResult.Error -> onError(result.error)
                else -> {}
            }
        }
    }

    fun deleteService(
        id: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteService(id)
            _isLoading.value = false
            
            when (result) {
                is ApiResult.Success -> {
                    fetchServiceList()
                    onSuccess()
                }
                is ApiResult.Error -> onError(result.error)
                else -> {}
            }
        }
    }

    fun getServiceDetail(
        id: Int,
        onSuccess: (Service) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = repository.getServiceDetail(id)) {
                is ApiResult.Success -> onSuccess(result.data)
                is ApiResult.Error -> onError(result.error)
                else -> {}
            }
        }
    }
}
