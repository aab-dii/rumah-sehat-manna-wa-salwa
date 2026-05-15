package com.android.rumahsehatmannawasalwa.data.service

import android.util.Log
import com.android.rumahsehatmannawasalwa.data.model.booking.PusherBookingEvent
import com.google.gson.Gson
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

object PusherService {
    private const val APP_KEY = "2f7e69897f47003aabf8"
    private const val CLUSTER = "ap1"
    private const val CHANNEL_NAME = "my-channel"
    private const val EVENT_NAME = "my-event" // Mapped to MyEvent.php broadcastAs()

    private var pusher: Pusher? = null
    private val gson = Gson()

    // SharedFlow to emit updates to any interested ViewModel
    private val _bookingUpdateFlow = MutableSharedFlow<PusherBookingEvent>()
    val bookingUpdateFlow: SharedFlow<PusherBookingEvent> = _bookingUpdateFlow

    fun connect() {
        if (pusher != null && pusher!!.connection.state == ConnectionState.CONNECTED) {
            Log.d("PusherService", "Already connected.")
            return
        }

        val options = PusherOptions().apply {
            setCluster(CLUSTER)
        }

        pusher = Pusher(APP_KEY, options)

        pusher?.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.i(
                    "PusherService",
                    "State changed: ${change.previousState} -> ${change.currentState}"
                )
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e("PusherService", "Connection Error: $message ($code)", e)
            }
        }, ConnectionState.ALL)

        val channel = pusher?.subscribe(CHANNEL_NAME)

        channel?.bind(EVENT_NAME) { event ->
            Log.i("PusherService", "RAW EVENT RECEIVED: ${event.data}")
            try {
                val booking = parseBookingEvent(event.data)
                if (booking != null) {
                    Log.i("PusherService", "Parsed Booking: ID=${booking.id}, Status=${booking.status}")
                    CoroutineScope(Dispatchers.IO).launch {
                        _bookingUpdateFlow.emit(booking)
                    }
                } else {
                    Log.e("PusherService", "Parsed booking is NULL")
                }
            } catch (e: Exception) {
                Log.e("PusherService", "CRITICAL Parsing Error: ${e.message}", e)
            }
        }
    }

    /**
     * Subscribe to a specific booking's private channel for real-time status updates.
     * The backend broadcasts on "booking.$bookingId" with event "booking.updated".
     * Payload wraps booking fields under a "booking" key, matching MyEvent.broadcastWith().
     */
    fun subscribeToBooking(bookingId: Int, onUpdate: (PusherBookingEvent) -> Unit) {
        val channelName = "booking.$bookingId"
        val channel = pusher?.subscribe(channelName)

        channel?.bind("booking.updated") { event ->
            Log.i("PusherService", "Per-booking raw event: ${event.data}")
            val updatedData = parseBookingEvent(event.data)
            if (updatedData != null) {
                onUpdate(updatedData)
            } else {
                Log.e("PusherService", "Failed to parse per-booking event for booking $bookingId")
            }
        }
    }

    fun unsubscribeFromBooking(bookingId: Int) {
        pusher?.unsubscribe("booking.$bookingId")
    }

    /**
     * Subscribe to a user's private channel to receive real-time notification count updates.
     * The backend broadcasts on "user.$userId" with event "App\Events\NotificationCountUpdated".
     */
    fun subscribeToUserNotifications(userId: Int, onUpdate: (Int) -> Unit) {
        val channelName = "user.$userId"
        val channel = pusher?.subscribe(channelName)

        channel?.bind("App\\Events\\NotificationCountUpdated") { event ->
            Log.i("PusherService", "Raw NotificationCountUpdated event: ${event.data}")
            try {
                val jsonObject = JSONObject(event.data)
                if (jsonObject.has("unread_count")) {
                    val count = jsonObject.getInt("unread_count")
                    onUpdate(count)
                }
            } catch (e: Exception) {
                Log.e("PusherService", "Error parsing NotificationCountUpdated: ${e.message}", e)
            }
        }
    }

    fun unsubscribeFromUserNotifications(userId: Int) {
        pusher?.unsubscribe("user.$userId")
    }

    fun disconnect() {
        pusher?.disconnect()
        Log.i("PusherService", "Disconnected.")
    }

    /**
     * Parses raw Pusher event data into a PusherBookingEvent.
     * Handles both:
     *  - Wrapped:   { "booking": { "id": 93, ... } }
     *  - Flat:      { "id": 93, ... }
     */
    private fun parseBookingEvent(rawData: String): PusherBookingEvent? {
        return try {
            val jsonObject = JSONObject(rawData)
            val bookingJson = if (jsonObject.has("booking")) {
                val inner = jsonObject.get("booking")
                // "booking" might itself be a JSON string (double-encoded) or a JSONObject
                if (inner is String) inner else jsonObject.getJSONObject("booking").toString()
            } else {
                rawData
            }
            Log.d("PusherService", "Booking JSON to parse: $bookingJson")
            gson.fromJson(bookingJson, PusherBookingEvent::class.java)
        } catch (e: Exception) {
            Log.e("PusherService", "parseBookingEvent error: ${e.message}", e)
            null
        }
    }
}
