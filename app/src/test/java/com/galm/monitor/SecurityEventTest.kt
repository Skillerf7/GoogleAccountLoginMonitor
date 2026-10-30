package com.galm.monitor

import com.galm.monitor.data.SecurityEvent
import org.junit.Assert.*
import org.junit.Test

class SecurityEventTest {

    @Test
    fun `event has unique id by default`() {
        val e1 = SecurityEvent(timestamp = 1L, eventType = "TEST", description = "A")
        val e2 = SecurityEvent(timestamp = 1L, eventType = "TEST", description = "A")
        assertNotEquals(e1.id, e2.id)
    }

    @Test
    fun `default values are correct`() {
        val event = SecurityEvent(
            timestamp = System.currentTimeMillis(),
            eventType = "MANUAL",
            description = "Test event"
        )
        assertNull(event.deviceName)
        assertNull(event.ipAddress)
        assertNull(event.location)
        assertFalse(event.isNewDevice)
        assertEquals("LOCAL", event.source)
    }

    @Test
    fun `new device flag works`() {
        val event = SecurityEvent(
            timestamp = 0L,
            eventType = "NEW_DEVICE",
            description = "New phone",
            isNewDevice = true
        )
        assertTrue(event.isNewDevice)
    }
}
