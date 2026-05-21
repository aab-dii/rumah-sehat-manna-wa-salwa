package com.android.rumahsehatmannawasalwa.data

sealed class ApiResult<out R> private constructor() {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val error: String, val code: Int? = null) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}
