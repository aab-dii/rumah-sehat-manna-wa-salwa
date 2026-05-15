package com.android.rumahsehatmannawasalwa.utils

import android.content.Intent
import android.net.Uri
import android.widget.Toast

object IntentUtils {
    fun launchWhatsApp(context: android.content.Context, phone: String) {
        try {
            var cleanPhone = phone.trim()
            if (cleanPhone.startsWith("0")) {
                cleanPhone = "62" + cleanPhone.substring(1)
            }
            cleanPhone = cleanPhone.replace(Regex("[^0-9]"), "")

            val url = "https://api.whatsapp.com/send?phone=$cleanPhone"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}