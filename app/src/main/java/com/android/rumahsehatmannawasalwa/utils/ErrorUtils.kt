package com.android.rumahsehatmannawasalwa.utils

import org.json.JSONObject
import retrofit2.Response

object ErrorUtils {
    fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val jsonObject = JSONObject(errorBody)
                val meta = jsonObject.optJSONObject("meta")
                meta?.optString("message") ?: "Gagal memproses permintaan"
            } else {
                "Gagal memproses permintaan"
            }
        } catch (e: Exception) {
            "Terjadi kesalahan sistem"
        }
    }
}
