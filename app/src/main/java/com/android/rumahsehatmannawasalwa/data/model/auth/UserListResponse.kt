package com.android.rumahsehatmannawasalwa.data.model.auth

import com.android.rumahsehatmannawasalwa.data.model.common.Pagination
import com.google.gson.annotations.SerializedName

data class UserListResponse(
    @SerializedName("data") val data: Pagination<User>,
    @SerializedName("meta") val meta: Meta
)

data class Meta(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)
