package com.android.rumahsehatmannawasalwa.data.model.medicalrecord

import com.android.rumahsehatmannawasalwa.data.model.common.Pagination
import com.google.gson.annotations.SerializedName

data class TherapyHistoryResponse(
    @SerializedName("data") val data: Pagination<TherapyHistory>
)
