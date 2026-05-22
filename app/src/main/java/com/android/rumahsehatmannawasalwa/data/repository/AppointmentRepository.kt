package com.android.rumahsehatmannawasalwa.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.rumahsehatmannawasalwa.data.ApiResult
import com.android.rumahsehatmannawasalwa.data.api.ApiService
import com.android.rumahsehatmannawasalwa.data.local.pref.UserPreference
import com.android.rumahsehatmannawasalwa.data.mapper.Schedule.ProcessedSchedule
import com.android.rumahsehatmannawasalwa.data.mapper.Schedule.ScheduleMapper
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.android.rumahsehatmannawasalwa.data.model.booking.AvailableSlots
import com.android.rumahsehatmannawasalwa.data.model.booking.DetailAppointmentResponse
import com.android.rumahsehatmannawasalwa.data.model.booking.PusherBookingEvent
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingListItem
import com.android.rumahsehatmannawasalwa.data.model.booking.BookingRequest
import com.android.rumahsehatmannawasalwa.data.model.booking.CreateAppointment
import com.android.rumahsehatmannawasalwa.data.model.booking.UpdateBookingStatusRequest
import com.android.rumahsehatmannawasalwa.data.model.service.Service
import com.android.rumahsehatmannawasalwa.data.repository.paging.AppointmentPagingSource
import com.android.rumahsehatmannawasalwa.data.service.PusherService
import com.android.rumahsehatmannawasalwa.utils.AppConstants
import com.android.rumahsehatmannawasalwa.utils.FileUtils
import com.android.rumahsehatmannawasalwa.utils.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AppointmentRepository(
    private val apiService: ApiService,
    private val pref: UserPreference,
    private val context: Context // For File/URI handling
) {

    val bookingUpdateFlow = PusherService.bookingUpdateFlow

    fun getPatientId(): Int {
        return pref.getUser()?.id ?: 0
    }

    fun getUserRole(): String? {
        return pref.getUser()?.role
    }

    fun getBookings(
        page: Int,
        limit: Int = 10,
        status: String? = null
    ): Flow<ApiResult<List<BookingListItem>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getBookings(page = page, limit = limit, status = status)
            val items = response.data?.data
            if (items != null) {
                emit(ApiResult.Success(items))
            } else {
                emit(ApiResult.Error("Data booking tidak tersedia"))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Gagal memuat booking: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch all bookings for a specific date — used for home screen stats.
     * Returns a plain list (not paged), intended for small daily sets.
     */
    suspend fun getTodayBookings(date: String, status: String? = null): List<BookingListItem> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    apiService.getBookings(page = 1, limit = 100, date = date, status = status)
                val items = response.data?.data
                android.util.Log.d(
                    "AppointmentRepo",
                    "getTodayBookings($date, $status): total=${response.data?.total}, items=${items?.size ?: 0}"
                )
                items ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("AppointmentRepo", "getTodayBookings error: ${e.message}")
                emptyList()
            }
        }
    }


    fun fetchAppointmentDetail(id: Int): Flow<ApiResult<DetailAppointmentResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getBookingDetail(id)
            emit(ApiResult.Success(response))
            Log.d("BookingRepository", "Detail Booking: ${response.data}")
        } catch (e: Exception) {
            emit(ApiResult.Error("Error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun createAppointment(params: CreateAppointment): ApiResult<Int> {
        return try {
            val paymentMethod = params.paymentOption

            // Rakit Body — KEAMANAN: Hanya kirim field yang diperlukan.
            // Field 'price', 'total_price', 'status', 'payment_status'
            // TIDAK dikirim karena ditentukan oleh server dari database.
            val requestMap = mutableMapOf<String, RequestBody>().apply {
                params.patientId?.let {
                    if (it > 0) put("patient_id", createPartFromString(it.toString()))
                }
                put("service_id", createPartFromString(params.serviceId.toString()))
                put("therapist_id", createPartFromString(params.therapistId.toString()))
                put("booking_date", createPartFromString(params.date))
                put("booking_time", createPartFromString(params.time))
                put("location_type", createPartFromString(AppConstants.DEFAULT_LOCATION_TYPE))
                put("payment_method", createPartFromString(paymentMethod))
            }
            val imagePart = params.proofUri?.let { prepareMultipartImage(it, "proof_of_transfer") }
            val response = apiService.createAppointment(requestMap, imagePart)
            if (response.isSuccessful && response.body() != null) {
                val bookingId = response.body()!!.data.id
                android.util.Log.d("BookingRepo", "✅ Sukses ID: $bookingId")
                ApiResult.Success(bookingId)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }

    suspend fun updateBookingStatus(bookingId: Int, status: String): ApiResult<String> {
        return try {
            val response =
                apiService.updateBookingStatus(bookingId, UpdateBookingStatusRequest(status))
            if (response.isSuccessful) {
                ApiResult.Success("Status berhasil diperbarui")
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }


    suspend fun cancelBooking(id: Int): ApiResult<Any> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.cancelBooking(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }

    suspend fun rejectPayment(id: Int, reason: String): ApiResult<Any> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.rejectPayment(id, reason)
                if (response.isSuccessful) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Error(ErrorUtils.parseErrorMessage(response))
                }
            } catch (e: Exception) {
                ApiResult.Error("Error: ${e.message}")
            }
        }

    suspend fun acceptPayment(id: Int): ApiResult<Any> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.acceptPayment(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }

    suspend fun reuploadProof(bookingId: Int, uri: Uri): ApiResult<Any> =
        withContext(Dispatchers.IO) {
            try {
                val filePart = prepareMultipartImage(uri, "proof_of_transfer")
                if (filePart == null) return@withContext ApiResult.Error("Gagal memproses file")

                val response = apiService.reuploadProof(bookingId, filePart)
                if (response.isSuccessful) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Error(ErrorUtils.parseErrorMessage(response))
                }
            } catch (e: Exception) {
                ApiResult.Error("Error: ${e.message}")
            }
        }

    suspend fun updateBooking(id: Int, request: BookingRequest): ApiResult<Any> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateBooking(id, request)
                if (response.isSuccessful) {
                    ApiResult.Success("Booking updated successfully")
                } else {
                    ApiResult.Error(ErrorUtils.parseErrorMessage(response))
                }
            } catch (e: Exception) {
                ApiResult.Error("Error: ${e.message}")
            }
        }

    suspend fun getAvailableSlots(
        therapistId: Int,
        date: String,
        serviceId: Int
    ): ApiResult<AvailableSlots> {
        return try {
            val response = apiService.getAvailableSlots(therapistId, date, serviceId)

            if (response.isSuccessful && response.body() != null) {
                val availableSlots = response.body()!!.data
                ApiResult.Success(availableSlots)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    // --- 2. Ambil Terapis Berdasarkan Layanan (Pindahan dari VM) ---
    fun getTherapistsByService(serviceName: String): Flow<ApiResult<List<User>>> = flow {
        emit(ApiResult.Loading)
        try {
            // Kita ambil semua terapis, lalu filter berdasarkan spesialisasi
            val response = apiService.getUsers(page = 1, role = "terapis", limit = 100)
            if (response.isSuccessful && response.body() != null) {
                val allTherapists = response.body()!!.data.data

                // Logika Filter: Cari terapis yang spesialisasinya mengandung nama layanan
                val filtered = allTherapists.filter { therapist ->
                    // B4 Fix: Jangan loloskan terapis tanpa spesialisasi ke semua layanan.
                    // Terapis hanya muncul jika setidaknya satu spesialisasinya cocok dengan layanan.
                    therapist.specialization.isNotEmpty() &&
                            therapist.specialization.any { spec ->
                                spec.contains(serviceName, ignoreCase = true)
                            }
                }
                emit(ApiResult.Success(filtered))
            } else {
                emit(ApiResult.Error(ErrorUtils.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun getTherapistSchedule(therapistId: Int): ApiResult<ProcessedSchedule> {
        return try {
            val response =
                apiService.getSchedules(therapistId) // Pastikan apiService sudah di-inject
            if (response.isSuccessful && response.body() != null) {
                val processedData =
                    ScheduleMapper.mapResponseToProcessedSchedule(response.body()!!.data)
                ApiResult.Success(processedData)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan")
        }
    }

    suspend fun checkAvailability(
        therapistId: Int,
        startDate: String,
        endDate: String,
        serviceId: Int
    ): ApiResult<Map<String, String>> {
        return try {
            val response = apiService.checkAvailability(startDate, endDate, therapistId, serviceId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.data)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan")
        }
    }

    fun getPagingSource(
        dateFilter: String? = null,
        statusFilter: String? = null,
        searchQuery: String? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
    ): AppointmentPagingSource {
        return AppointmentPagingSource(
            apiService = apiService,
            dateFilter = dateFilter,
            statusFilter = statusFilter,
            searchQuery = searchQuery,
            sortBy = sortBy,
            sortOrder = sortOrder,
        )
    }

    fun createPartFromString(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun prepareMultipartImage(uri: Uri, partName: String): MultipartBody.Part? {
        val file = FileUtils.getFileFromUri(context, uri) ?: return null
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    suspend fun getServiceDetail(id: Int): ApiResult<Service> {
        return try {
            val response = apiService.getServiceDetail(id)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.data)
            } else {
                ApiResult.Error(ErrorUtils.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }
}
