package com.android.rumahsehatmannawasalwa.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.rumahsehatmannawasalwa.data.repository.AuthRepository
import com.android.rumahsehatmannawasalwa.data.repository.AppointmentRepository
import com.android.rumahsehatmannawasalwa.data.repository.DashboardRepository
import com.android.rumahsehatmannawasalwa.data.repository.ServiceRepository
import com.android.rumahsehatmannawasalwa.data.repository.TherapistRepository
import com.android.rumahsehatmannawasalwa.data.repository.TherapyRecordRepository
import com.android.rumahsehatmannawasalwa.data.repository.UserRepository
import com.android.rumahsehatmannawasalwa.data.repository.NotificationRepository
import com.android.rumahsehatmannawasalwa.di.Injection
import com.android.rumahsehatmannawasalwa.ui.viewmodel.admin.AdminDashboardViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.auth.AuthViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AdminBookingViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.AppointmentDetailViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.booking.BookingViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.medicalrecord.TherapyRecordViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.notification.NotificationViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.service.ServiceViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.therapist.TherapistAppointmentViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.therapist.TherapistDashboardViewModel
import com.android.rumahsehatmannawasalwa.ui.viewmodel.user.AdminUserViewModel

class ViewModelFactory private constructor(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val appointmentRepository: AppointmentRepository,
    private val therapistRepository: TherapistRepository,
    private val therapyRecordRepository: TherapyRecordRepository,
    private val userRepository: UserRepository,
    private val dashboardRepository: DashboardRepository,
    private val notificationRepository: NotificationRepository,
    private val serviceRepository: ServiceRepository,
    private val reportRepository: com.android.rumahsehatmannawasalwa.data.repository.ReportRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {

            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(application, authRepository) as T
            }
            modelClass.isAssignableFrom(BookingViewModel::class.java) -> {
                BookingViewModel(application, appointmentRepository) as T
            }
            modelClass.isAssignableFrom(AdminUserViewModel::class.java) -> {
                AdminUserViewModel(application, userRepository) as T
            }
            modelClass.isAssignableFrom(AppointmentDetailViewModel::class.java) -> {
                AppointmentDetailViewModel(appointmentRepository) as T
            }
            modelClass.isAssignableFrom(AdminBookingViewModel::class.java) -> {
                AdminBookingViewModel(application, appointmentRepository) as T
            }
            modelClass.isAssignableFrom(TherapistAppointmentViewModel::class.java) -> {
                TherapistAppointmentViewModel(application, appointmentRepository, therapistRepository) as T
            }
            modelClass.isAssignableFrom(TherapyRecordViewModel::class.java) -> {
                TherapyRecordViewModel(therapyRecordRepository, appointmentRepository) as T
            }
            modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) -> {
                AdminDashboardViewModel(dashboardRepository) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(notificationRepository) as T
            }
            modelClass.isAssignableFrom(ServiceViewModel::class.java) -> {
                ServiceViewModel(serviceRepository) as T
            }
            modelClass.isAssignableFrom(TherapistDashboardViewModel::class.java) -> {
                TherapistDashboardViewModel(dashboardRepository) as T
            }
            modelClass.isAssignableFrom(com.android.rumahsehatmannawasalwa.ui.viewmodel.admin.ReportViewModel::class.java) -> {
                com.android.rumahsehatmannawasalwa.ui.viewmodel.admin.ReportViewModel(reportRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(application: Application): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: run {
                    // Set token sekali sebelum semua repository dibuat
                    Injection.initToken(application)
                    ViewModelFactory(
                        application,
                        Injection.provideAuthRepository(application),
                        Injection.provideBookingRepository(application),
                        Injection.provideTherapistRepository(application),
                        Injection.provideTherapyRecordRepository(application),
                        Injection.provideUserRepository(application),
                        Injection.provideDashboardRepository(application),
                        Injection.provideNotificationRepository(application),
                        Injection.provideServiceRepository(application),
                        Injection.provideReportRepository(application)
                    )
                }
            }.also { instance = it }

        /**
         * B3 Fix: Call when user logs out so the singleton factory is reset.
         * This prevents the old token from 'sticking' after a fresh login.
         * The next instance will call initToken() with the fresh token.
         */
        fun clearInstance() {
            synchronized(this) {
                com.android.rumahsehatmannawasalwa.data.api.RetrofitClient.authToken = null
                instance = null
            }
        }
    }
}