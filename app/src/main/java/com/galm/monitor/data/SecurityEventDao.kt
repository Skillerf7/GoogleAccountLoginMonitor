package com.galm.monitor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityEventDao {

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<SecurityEvent>>

    @Query("SELECT * FROM security_events WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getEventsBetween(from: Long, to: Long): Flow<List<SecurityEvent>>

    @Query("SELECT * FROM security_events WHERE deviceName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR eventType LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEvents(query: String): Flow<List<SecurityEvent>>

    @Query("SELECT * FROM security_events WHERE isNewDevice = 1 ORDER BY timestamp DESC")
    fun getNewDeviceEvents(): Flow<List<SecurityEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SecurityEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<SecurityEvent>)

    @Query("DELETE FROM security_events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM security_events")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM security_events")
    suspend fun count(): Int
}
