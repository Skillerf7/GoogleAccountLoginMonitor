package com.galm.monitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a security / login related event.
 *
 * IMPORTANT LIMITATION:
 * Google does not provide a public API that returns a complete automated
 * login history (IP, device fingerprint, location) for third-party apps.
 * Events in this database are either:
 *  - Manually added by the user
 *  - Derived from limited information available after Google Sign-In
 *  - Placeholder entries documenting the absence of official data
 */
@Entity(tableName = "security_events")
data class SecurityEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long,                    // Unix millis
    val eventType: String,                  // e.g. "SIGN_IN", "NEW_DEVICE", "MANUAL", "LIMITATION_NOTE"
    val deviceName: String? = null,
    val deviceOs: String? = null,
    val ipAddress: String? = null,
    val location: String? = null,
    val description: String,
    val isNewDevice: Boolean = false,
    val source: String = "LOCAL",           // LOCAL | GOOGLE_SIGN_IN | MANUAL | SYSTEM
    val rawData: String? = null             // optional JSON for debugging
)
