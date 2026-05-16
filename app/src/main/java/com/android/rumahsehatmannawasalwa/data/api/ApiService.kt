package com.android.rumahsehatmannawasalwa.data.api

import com.android.rumahsehatmannawasalwa.data.model.schedule.Schedule
import com.android.rumahsehatmannawasalwa.data.model.schedule.UpdateScheduleRequest

import com.android.rumahsehatmannawasalwa.data.model.common.ApiResponse
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingRequest
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingResponse
import com.android.rumahsehatmannawasalwa.data.model.booking.SingleBookingResponse
import com.android.rumahsehatmannawasalwa.data.model.auth.RegisterRequest
import com.android.rumahsehatmannawasalwa.data.model.auth.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.android.rumahsehatmannawasalwa.data.model.auth.UserResponse
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.data.model.service.ServiceResponse
import com.android.rumahsehatmannawasalwa.data.model.auth.UserListResponse
import com.android.rumahsehatmannawasalwa.data.model.booking.AvailableSlots
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingCreateResponse
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingListResponse
import com.android.rumahsehatmannawasalwa.data.model.booking.DetailAppointmentResponse
import com.android.rumahsehatmannawasalwa.data.model.booking.UpdateBookingStatusRequest
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyHistory
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyRecordDetailResponse
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyRecordListResponse
import com.android.rumahsehatmannawasalwa.data.model.medicalrecord.TherapyRecordRequest
import com.android.rumahsehatmannawasalwa.data.model.notification.NotificationListResponse
import com.android.rumahsehatmannawasalwa.data.model.notification.UnreadCountResponse
import com.android.rumahsehatmannawasalwa.data.model.dashboard.DashboardResponse
import com.android.rumahsehatmannawasalwa.data.model.schedule.EmergencyCloseRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


interface ApiService {

    @POST("user/sync-firebase")
    suspend fun syncFirebase(
        @Body request: Map<String, String>
    ): Response<UserResponse>

    @POST("logout")
    suspend fun logout(): Response<ApiResponse<Nothing>>

    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<UserResponse>

    @POST("users/create")
    suspend fun createUser(@Body request: RegisterRequest): Response<UserResponse>

    @GET("user/firebase/{uid}")
    suspend fun getUserProfile(@Path("uid") uid: String): Response<UserResponse>

    @POST("user/update")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    @POST("update-fcm-token")
    suspend fun updateFcmToken(@Body request: com.android.rumahsehatmannawasalwa.data.model.auth.UpdateFcmTokenRequest): Response<ApiResponse<Any>>

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

    @POST("services")
    @Multipart
    suspend fun createService(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<ApiResponse<Service>>

    @POST("services/{id}")
    @Multipart
    suspend fun updateService(
        @Path("id") id: Int,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<ApiResponse<Service>>

    @GET("services/{id}")
    suspend fun getServiceDetail(@Path("id") id: Int): Response<ApiResponse<Service>>

    @DELETE("services/{id}")
    suspend fun deleteService(@Path("id") id: Int): Response<ApiResponse<Nothing>>

    // Schedule
    @GET("schedules/{therapistId}")
    suspend fun getSchedules(@Path("therapistId") therapistId: Int): Response<ApiResponse<List<Schedule>>>

    @POST("schedules/update")
    suspend fun updateSchedule(@Body request: UpdateScheduleRequest): Response<ApiResponse<Schedule>>

    @POST("schedules/close-now")
    suspend fun emergencyClose(@Body request: EmergencyCloseRequest): Response<ApiResponse<com.android.rumahsehatmannawasalwa.data.model.schedule.EmergencyCloseResponse>>

    @POST("schedules/add-holiday")
    suspend fun addHoliday(@Body request: com.android.rumahsehatmannawasalwa.data.model.schedule.AddHolidayRequest): Response<ApiResponse<com.android.rumahsehatmannawasalwa.data.model.schedule.AddHolidayResponse>>

    @GET("bookings")
    suspend fun getBookings(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10,
        @Query("date") date: String? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
    ): BookingListResponse

    @GET("bookings/{id}")
    suspend fun getDetailBookings(@Path ("id") id: Int) : BookingResponse

    @POST("checkout")
    @Multipart
    suspend fun checkout(
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part proofOfTransfer: MultipartBody.Part? = null
    ): Response<SingleBookingResponse>

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

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<UserResponse>

    @POST("bookings")
    @Multipart
    suspend fun createAppointment(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part proof: MultipartBody.Part?
    ): Response<BookingCreateResponse>

    @POST("bookings/{id}")
    suspend fun updateBooking(@Path("id") id: Int, @Body request: BookingRequest): Response<Any>

    @POST("bookings/{id}")
    suspend fun updateBookingStatus(@Path("id") id: Int, @Body request: UpdateBookingStatusRequest): Response<Any>

    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Int): Response<Any>

    @GET("bookings/{id}")
    suspend fun getBookingDetail(@Path("id") id: Int): DetailAppointmentResponse

    @FormUrlEncoded
    @POST("bookings/{id}/reject-payment")
    suspend fun rejectPayment(
        @Path("id") id: Int,
        @Field("rejection_note") reason: String
    ): Response<SingleBookingResponse>

    @POST("bookings/{id}/accept-payment")
    suspend fun acceptPayment(@Path("id") id: Int): Response<SingleBookingResponse>

    @Multipart
    @POST("bookings/{id}/reupload-proof")
    suspend fun reuploadProof(
        @Path("id") id: Int,
        @Part proof: MultipartBody.Part
    ): Response<SingleBookingResponse>

    @GET("available-slots")
    suspend fun getAvailableSlots(
        @Query("therapist_id") therapistId: Int,
        @Query("booking_date") date: String,
        @Query("service_id") serviceId: Int
    ): Response<ApiResponse<AvailableSlots>>

    @GET("therapy-records")
    suspend fun getTherapyRecords(
        @Query("patient_id") patientId: Int? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = 10,
        @Query("search") search: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): TherapyRecordListResponse

    @GET("therapy-records")
    suspend fun getTherapyRecordDetail(
        @Query("id") id: Int
    ): Response<TherapyRecordDetailResponse>

    @POST("therapy-records")
    suspend fun createTherapyRecord(@Body request: TherapyRecordRequest): Response<TherapyRecordDetailResponse>

    @POST("therapy-records/{id}")
    suspend fun updateTherapyRecord(
        @Path("id") id: Int,
        @Body request: TherapyRecordRequest
    ): Response<TherapyRecordDetailResponse>

    @GET("admin/dashboard")
    suspend fun getAdminDashboard(
        @Query("timezone") timezone: String = java.util.TimeZone.getDefault().id
    ): Response<com.android.rumahsehatmannawasalwa.data.model.dashboard.AdminDashboardResponse>

    /** Unified role-aware dashboard — works for admin, terapis, pasien */
    @GET("dashboard")
    suspend fun getDashboard(
        @Query("timezone") timezone: String = java.util.TimeZone.getDefault().id
    ): Response<DashboardResponse>

    @GET("check-availability")
    suspend fun checkAvailability(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("therapist_id") therapistId: Int,
        @Query("service_id") serviceId: Int
    ): Response<ApiResponse<Map<String, String>>>

    // Notifications
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("unread_only") unreadOnly: Int? = null
    ): Response<NotificationListResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @POST("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: Int): Response<ApiResponse<Any>>

    @POST("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<ApiResponse<Any>>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): Response<ApiResponse<Any>>
}
