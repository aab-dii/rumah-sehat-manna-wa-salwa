package com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord // Using existing structure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistorySummary
import com.android.rumahsehatmannawasalwa.data.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class TherapyHistoryViewModel : ViewModel() {
    private val _historyList = MutableStateFlow<List<TherapyHistorySummary>>(emptyList())
    val historyList: StateFlow<List<TherapyHistorySummary>> = _historyList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Assuming we might have a specific endpoint for medical records/therapy history
    // Since I don't see one in ApiService, I will assume we might need to add one or use an existing one?
    // Wait, ApiService had no 'getMedicalRecords' or similar. 
    // I will check if I need to add it to ApiService first. 
    // For now, I will create the VM structure.
    
    // Based on user request "data classnya pakai medical record", I assume the API returns this structure.
    // I'll check ApiService content again from previous step... 
    // ApiService imports MedicalRecordResponse but I don't see a function returning it in the visible lines.
    // Ah, line 7 was importing `MedicalRecordResponse`.
    
    // Let's add the fetch function assuming the endpoint exists or will be added.
    
    private var isDataLoaded = false

//    fun fetchTherapyHistory(forceRefresh: Boolean = false) {
//        if (isDataLoaded && !forceRefresh) return
//
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                val response = RetrofitClient.instance.getTherapyHistory(page = 1)
//
//                if (response.isSuccessful && response.body() != null) {
//                    _historyList.value = response.body()!!.data.data
//                    isDataLoaded = true
//                } else {
//                    Log.e("TherapyHistoryVM", "Failed: ${response.message()}")
//                }
//            } catch (e: Exception) {
//                Log.e("TherapyHistoryVM", "Error", e)
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
}
