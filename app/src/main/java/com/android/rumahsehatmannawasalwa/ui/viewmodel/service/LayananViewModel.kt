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

class LayananViewModel : ViewModel() {

    // Expose flow of PagingData (for Lists)
    val layananPager = Pager(
        config = PagingConfig(pageSize = 3, prefetchDistance = 1),
        pagingSourceFactory = { LayananPagingSource(RetrofitClient.instance) }
    ).flow.cachedIn(viewModelScope)

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
}