package com.android.rumahsehatmannawasalwa.data.model.auth

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("no_hp") val noHp: String,
    @SerializedName("role") val role: String = "pasien",
    @SerializedName("firebase_uid") val firebaseUid: String,
    @SerializedName("pekerjaan") val pekerjaan: String,
    @SerializedName("alamat") val alamat: String,
    @SerializedName("tgl_lahir") val tglLahir: String,
    @SerializedName("jenisKelamin") val jenisKelamin: String,
    @SerializedName("specialization") val specialization: List<String>? = null
)
