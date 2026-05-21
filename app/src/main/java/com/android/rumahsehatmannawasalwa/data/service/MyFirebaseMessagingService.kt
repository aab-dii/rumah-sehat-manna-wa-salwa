package com.android.rumahsehatmannawasalwa.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.rumahsehatmannawasalwa.MainActivity
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
                val bookingId = remoteMessage.data["booking_id"]
                val type = remoteMessage.data["type"] ?: remoteMessage.data["screen"]
                
                // Kirim notifikasi FCM ke semua admin & super admin / user terkait saat ada booking baru / update.
                // Kegagalan pengiriman tidak mempengaruhi proses pembuatan booking.
                showNotification(
                    title = it.title ?: "Info Rumah Sehat",
                    body = it.body ?: "",
                    bookingId = bookingId,
                    type = type
                )
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

    private fun showNotification(
        title: String,
        body: String,
        bookingId: String? = null,
        type: String? = null
    ) {
        val channelId = "rumah_sehat_channel"

        // Casting 'as NotificationManager'
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notifikasi Transaksi",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent untuk membuka MainActivity dan membawa data deep-linking
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!bookingId.isNullOrEmpty()) {
                putExtra("booking_id", bookingId)
            }
            if (!type.isNullOrEmpty()) {
                putExtra("type", type)
            }
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // PendingIntent untuk meluncurkan activity saat notifikasi ditap
        val pendingIntent = PendingIntent.getActivity(
            this,
            Random.nextInt(),
            intent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }
}