package com.android.rumahsehatmannawasalwa.data.model.auth

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("no_hp") val noHp: String,
    @SerializedName("pekerjaan") val pekerjaan: String,
    @SerializedName("alamat") val alamat: String,
    @SerializedName("tgl_lahir") val tglLahir: String,
    @SerializedName("jenis_kelamin") val jenisKelamin: String,
    @SerializedName("foto_url") val fotoUrl: String? = null,
    @SerializedName("specialization") val specialization: List<String>? = null
)
