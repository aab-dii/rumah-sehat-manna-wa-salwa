package com.android.rumahsehatmannawasalwa.data.model.booking

import com.google.gson.annotations.SerializedName

/**
 * Flat structure that matches the Pusher "my-event" payload after extracting
 * the top-level "booking" key. Example:
 * {
 *   "id": 93, "booking_date": "2026-02-21", "booking_time": "10:30:00",
 *   "status": "completed", "total_price": 100000,
 *   "patient": {"name": "Abdi"},
 *   "therapist": {"name": "Dr. Siti Aminah"},
 *   "service": {"nama": "Bekam", "durasi": 60},
 *   "transaction": {"status": "paid", "payment_method": "transfer"}
 * }
 */
data class PusherBookingEvent(
    @SerializedName("id")                  val id: Int,
    @SerializedName("booking_date")        val bookingDate: String?,
    @SerializedName("booking_time")        val bookingTime: String?,
    @SerializedName("status")              val status: String?,
    @SerializedName("cancellation_reason") val cancellationReason: String?,
    @SerializedName("total_price")         val totalPrice: Long?,
    @SerializedName("patient")             val patient: PusherPerson?,
    @SerializedName("therapist")           val therapist: PusherPerson?,
    @SerializedName("service")             val service: PusherService?,
    @SerializedName("transaction")         val transaction: PusherTransaction?
)

data class PusherPerson(
    @SerializedName("name") val name: String?
)

data class PusherService(
    @SerializedName("nama")   val nama: String?,
    @SerializedName("durasi") val durasi: Int?
)

data class PusherTransaction(
    @SerializedName("status")         val status: String?,
    @SerializedName("payment_method") val paymentMethod: String?
)
