package com.android.rumahsehatmannawasalwa.di

import android.content.Context
import com.android.rumahsehatmannawasalwa.data.api.RetrofitClient
import com.android.rumahsehatmannawasalwa.data.local.pref.UserPreference
import com.android.rumahsehatmannawasalwa.data.repository.AuthRepository
import com.android.rumahsehatmannawasalwa.data.repository.AppointmentRepository
import com.android.rumahsehatmannawasalwa.data.repository.NotificationRepository
import com.android.rumahsehatmannawasalwa.data.repository.ServiceRepository
import com.android.rumahsehatmannawasalwa.data.repository.TherapistRepository
import com.android.rumahsehatmannawasalwa.data.repository.TherapyRecordRepository
import com.android.rumahsehatmannawasalwa.data.repository.UserRepository
import com.google.firebase.auth.ktx.auth

object Injection {

    /**
     * Satu titik tunggal untuk membaca token dari UserPreference dan menyuntiknya
     * ke RetrofitClient sebelum repository dibuat.
     * Panggil sekali dari ViewModelFactory.getInstance() sebelum semua provider.
     */
    fun initToken(context: Context) {
        RetrofitClient.authToken = UserPreference(context).getToken()
    }

    fun provideBookingRepository(context: Context): AppointmentRepository {
        val pref = UserPreference(context)
        return AppointmentRepository(
            apiService = RetrofitClient.instance,
            pref = pref,
            context = context.applicationContext
        )
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        val pref = provideUserPreference(context)
        val apiService = RetrofitClient.instance
        val firebaseAuth = com.google.firebase.ktx.Firebase.auth
        return AuthRepository(apiService, pref, firebaseAuth)
    }

    fun provideTherapyRecordRepository(context: Context): TherapyRecordRepository {
        return TherapyRecordRepository(RetrofitClient.instance)
    }

    fun provideUserPreference(context: Context): UserPreference {
        return UserPreference(context)
    }

    fun provideTherapistRepository(context: Context): TherapistRepository {
        return TherapistRepository(RetrofitClient.instance)
    }

    fun provideUserRepository(context: Context): UserRepository {
        return UserRepository(RetrofitClient.instance, context)
    }

    fun provideDashboardRepository(context: Context): com.android.rumahsehatmannawasalwa.data.repository.DashboardRepository {
        return com.android.rumahsehatmannawasalwa.data.repository.DashboardRepository(RetrofitClient.instance)
    }

    fun provideNotificationRepository(context: Context): NotificationRepository {
        return NotificationRepository(RetrofitClient.instance)
    }

    fun provideServiceRepository(context: Context): ServiceRepository {
        return ServiceRepository(RetrofitClient.instance)
    }

}
