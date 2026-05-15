package com.android.rumahsehatmannawasalwa.data.model.medicalrecord

import com.android.rumahsehatmannawasalwa.data.model.common.Pagination
import com.android.rumahsehatmannawasalwa.data.model.meta.Meta
import com.google.gson.annotations.SerializedName

data class TherapyRecordListResponse(
    @SerializedName("data") val data: Pagination<TherapyHistorySummary>,
    @SerializedName("meta") val meta: Meta
)
