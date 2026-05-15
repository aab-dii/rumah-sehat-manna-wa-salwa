package com.android.rumahsehatmannawasalwa.data.model.medicalrecord

import com.android.rumahsehatmannawasalwa.data.model.meta.Meta
import com.google.gson.annotations.SerializedName

data class TherapyRecordDetailResponse(
    @SerializedName("data") val data: TherapyHistory,
    @SerializedName("meta") val meta: Meta
)
