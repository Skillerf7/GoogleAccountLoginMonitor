package com.galm.monitor.data

import android.content.Context
import android.os.Build
import com.galm.monitor.auth.GoogleAuthManager
import kotlinx.coroutines.flow.Flow
import java.util.Date

class EventRepository(
    private val dao: SecurityEventDao,
    private val authManager: GoogleAuthManager
) {

    fun getAllEvents(): Flow<List<SecurityEvent>> = dao.getAllEvents()

    fun searchEvents(query: String): Flow<List<SecurityEvent>> = dao.searchEvents(query)

    fun getEventsBetween(from: Long, to: Long): Flow<List<SecurityEvent>> =
        dao.getEventsBetween(from, to)

    suspend fun addManualEvent(
        description: String,
        deviceName: String? = null,
        ipAddress: String? = null,
        location: String? = null,
        isNewDevice: Boolean = false
    ) {
        val event = SecurityEvent(
            timestamp = System.currentTimeMillis(),
            eventType = "MANUAL",
            deviceName = deviceName,
            deviceOs = "${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE}",
            ipAddress = ipAddress,
            location = location,
            description = description,
            isNewDevice = isNewDevice,
            source = "MANUAL"
        )
        dao.insert(event)
    }

    /**
     * Called after a successful Google Sign-In.
     * Records the fact that the user authenticated with this app.
     * Does NOT invent any login history from Google.
     */
    suspend fun recordAppSignIn(accountEmail: String?) {
        val event = SecurityEvent(
            timestamp = System.currentTimeMillis(),
            eventType = "APP_SIGN_IN",
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            deviceOs = "Android ${Build.VERSION.RELEASE}",
            ipAddress = null,          // Not provided by Google Sign-In API
            location = null,           // Not provided by Google Sign-In API
            description = "Benutzer hat sich in dieser App mit Google angemeldet" +
                    (accountEmail?.let { " ($it)" } ?: ""),
            isNewDevice = false,
            source = "GOOGLE_SIGN_IN"
        )
        dao.insert(event)
    }

    /**
     * Inserts a permanent note explaining the official limitations.
     * This is shown so users understand why a full history is missing.
     */
    suspend fun ensureLimitationNoteExists() {
        if (dao.count() == 0) {
            val note = SecurityEvent(
                timestamp = System.currentTimeMillis(),
                eventType = "LIMITATION_NOTE",
                description = "WICHTIG: Google stellt keine öffentliche API für einen vollständigen " +
                        "automatisierten Login-Verlauf (IP-Adresse, Geräteliste, Standort pro Anmeldung) " +
                        "für Drittanbieter-Apps bereit. Diese App kann nur lokal protokollierte " +
                        "Ereignisse und die Tatsache einer Anmeldung in dieser App speichern. " +
                        "Den offiziellen Verlauf finden Sie unter: https://myaccount.google.com/security",
                source = "SYSTEM"
            )
            dao.insert(note)
        }
    }

    suspend fun deleteEvent(id: String) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.deleteAll()
    }
}
