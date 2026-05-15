package com.android.rumahsehatmannawasalwa.data.model.common

import com.android.rumahsehatmannawasalwa.data.model.meta.Meta
import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("meta") val meta: Meta,
    @SerializedName("data") val data: T
)

