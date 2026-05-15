package com.android.rumahsehatmannawasalwa.data.model.booking

import com.google.gson.annotations.SerializedName

data class ApiTransaction(
    @SerializedName("id") val id: Int,
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("status") val status: String, // paid, unpaid, pending
    @SerializedName("amount") val amount: Int,
    @SerializedName("proof_of_transfer") val proofOfTransfer: String?,
    @SerializedName("rejection_note") val rejectionNote: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
