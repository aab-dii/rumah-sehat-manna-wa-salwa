package com.android.rumahsehatmannawasalwa.data.model.auth

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("firebase_uid") val firebaseUid: String = "",
    @SerializedName("name", alternate = ["nama_lengkap"]) val name: String,
    @SerializedName("email") val email: String = "",
    @SerializedName("phone_number", alternate = ["no_hp"]) val phoneNumber: String = "",
    @SerializedName("job", alternate = ["pekerjaan"]) val job: String = "",
    @SerializedName("birth_date", alternate = ["tgl_lahir"]) val birthDate: String = "",
    @SerializedName("address", alternate = ["alamat"]) val address: String = "",
    @SerializedName("gender", alternate = ["jenis_kelamin"]) val gender: String = "",
    @SerializedName("role") val role: String = "pasien",
    @SerializedName("specialization") val specialization: List<String> = emptyList(),
    @SerializedName("profile_photo_path") val profilePhotoPath: String? = null,
    @SerializedName("profile_photo_url") val profilePhotoUrl: String? = null,
    @SerializedName("foto_url", alternate = ["photo_url"]) val fotoUrl: String? = null,
    @SerializedName("deleted_at") val deletedAt: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,            // Sprint 2.1
    @SerializedName("last_active_at") val lastActiveAt: String? = null    // Sprint 2.1
) {
    // Sprint 2.1: Helper properties untuk cek role
    val isSuperAdmin get() = role == "super_admin"
    val isAdmin get() = role == "admin"
    val isAdminOrSuperAdmin get() = role == "admin" || role == "super_admin"
}