package com.galm.monitor.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.galm.monitor.GalmApplication
import com.galm.monitor.MainActivity
import com.galm.monitor.R
import com.galm.monitor.data.SecurityEvent

object NotificationHelper {

    fun showNewEventNotification(context: Context, event: SecurityEvent) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            event.isNewDevice -> context.getString(R.string.new_device_detected)
            event.eventType == "APP_SIGN_IN" -> "Anmeldung in der App"
            else -> "Neues Sicherheitsereignis"
        }

        val notification = NotificationCompat.Builder(context, GalmApplication.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(title)
            .setContentText(event.description.take(100))
            .setStyle(NotificationCompat.BigTextStyle().bigText(event.description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(event.id.hashCode(), notification)
    }
}
