package com.android.rumahsehatmannawasalwa.data.api

import com.android.rumahsehatmannawasalwa.data.model.common.ApiResponse
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingRequest
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingResponse
import com.android.rumahsehatmannawasalwa.data.model.booking.CheckoutRequest
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistoryResponse
import com.android.rumahsehatmannawasalwa.data.model.auth.RegisterRequest
import com.android.rumahsehatmannawasalwa.data.model.auth.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.android.rumahsehatmannawasalwa.data.model.auth.UserResponse
import com.android.rumahsehatmannawasalwa.data.model.service.ServiceResponse
import com.android.rumahsehatmannawasalwa.data.model.auth.UserListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


interface ApiService {
    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<UserResponse>

    @POST("users/create")
    suspend fun createUser(@Body request: RegisterRequest): Response<UserResponse>

    @GET("user/firebase/{uid}")
    suspend fun getUserProfile(@Path("uid") uid: String): Response<UserResponse>

    @POST("user/update")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    @Multipart
    @POST("users/{id}/update")
    suspend fun updateUserByAdmin(
        @Path("id") id: Int,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photo: MultipartBody.Part?
    ): Response<UserResponse>

    @GET("services")
    suspend fun getServices(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10
    ): Response<ServiceResponse>

    @GET("bookings")
    suspend fun getBookings(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10,
        @Query("date") date: String? = null, // YYYY-MM-DD
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): Response<BookingResponse>

    @POST("checkout")
    suspend fun checkout(@Body request: CheckoutRequest): Response<Any>

    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int,
        @Query("role") role: String? = null,
        @Query("search") search: String? = null,
        @Query("trash") trash: Int? = 0, // 1 = trashed, 0 = active
        @Query("limit") limit: Int = 10
    ): Response<UserListResponse>

    @POST("users/{id}/restore")
    suspend fun restoreUser(@Path("id") id: Int): Response<Any>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Any>

    @POST("bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<Any>

    @POST("bookings/{id}")
    suspend fun updateBooking(@Path("id") id: Int, @Body request: BookingRequest): Response<Any>

    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Int): Response<Any>

    @GET("bookings/{id}")
    suspend fun getBookingDetail(@Path("id") id: Int): Response<SingleBookingResponse>

    @GET("available-slots")
    suspend fun getAvailableSlots(
        @Query("therapist_id") therapistId: Int,
        @Query("booking_date") date: String, // YYYY-MM-DD
        @Query("service_id") serviceId: Int
    ): Response<ApiResponse<List<String>>>
    @GET("medical-records") // Adjust endpoint as needed by backend
    suspend fun getTherapyHistory(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10
    ): Response<TherapyHistoryResponse>
}
