package com.android.rumahsehatmannawasalwa.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.rumahsehatmannawasalwa.R // Pastikan import R ini benar
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.launch
import kotlin.random.Random // Import untuk Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // LOGGING PENTING
        Log.d("FCM_DEBUG", "From: ${remoteMessage.from}")
        Log.d("FCM_DEBUG", "Data Payload: ${remoteMessage.data}")
        Log.d("FCM_DEBUG", "Notification Payload: ${remoteMessage.notification?.body}")

        // Handle Data Payload (untuk Background & Foreground jika diperlukan)
        if (remoteMessage.data.isNotEmpty()) {
            // Jika ada logic khusus untuk data payload
        }

        // Handle Notification Payload (Hanya muncul otomatis jika app di Background, manual jika Foreground)
        remoteMessage.notification?.let {
            val userPreference = com.android.rumahsehatmannawasalwa.data.local.pref.UserPreference(this)
            if (userPreference.isNotificationEnabled()) {
                showNotification(it.title ?: "Info Rumah Sehat", it.body ?: "")
            } else {
                Log.d("FCM_DEBUG", "Notifikasi diabaikan karena user mematikan notifikasi di pengaturan profil")
            }

            val intent = Intent("FCM_DATA_EVENT")
            intent.putExtra("title", remoteMessage.notification?.title)
            intent.putExtra("message", remoteMessage.notification?.body)
            intent.putExtra("booking_id", remoteMessage.data["booking_id"])
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token baru dihasilkan: $token")

        // TUGAS UTAMA: Kirim token baru ini ke server (Backend) kamu
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val repository = com.android.rumahsehatmannawasalwa.di.Injection.provideAuthRepository(applicationContext)
        val userPreference = com.android.rumahsehatmannawasalwa.di.Injection.provideUserPreference(applicationContext)
        
        // Hanya kirim jika user sudah login (punya token sanctum)
        if (!userPreference.getToken().isNullOrEmpty()) {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                repository.updateFcmToken(token)
            }
        }
    }

    private fun showNotification(title: String, body: String) { // Ubah message jadi body agar konsisten
        val channelId = "rumah_sehat_channel"

        // FIX 1: Tambahkan casting 'as NotificationManager'
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notifikasi Transaksi",
                NotificationManager.IMPORTANCE_HIGH
            )
            // Sekarang fungsi ini tidak akan error lagi
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body) // FIX 2: Gunakan variabel 'body' dari parameter
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Pastikan file ic_notification ada, atau gunakan default dulu
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // FIX 3: Gunakan Random.nextInt() dengan benar
        notificationManager.notify(Random.nextInt(), notification)
    }
}