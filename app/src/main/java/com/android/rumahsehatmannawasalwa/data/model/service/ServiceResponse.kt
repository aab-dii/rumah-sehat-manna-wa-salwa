package com.android.rumahsehatmannawasalwa.data.model.service

import com.android.rumahsehatmannawasalwa.data.model.common.Pagination
import com.google.gson.annotations.SerializedName

data class ServiceResponse(
    @SerializedName("data") val data: Pagination<Service>
)
