package net.android.st069_fakecallphoneprank.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.android.st069_fakecallphoneprank.data.entity.FakeCall

@Dao
interface FakeCallDao {

    // Insert a new fake call
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fakeCall: FakeCall): Long

    // Update existing fake call
    @Update
    suspend fun update(fakeCall: FakeCall)

    // Delete a fake call
    @Delete
    suspend fun delete(fakeCall: FakeCall)

    // Delete by ID
    @Query("DELETE FROM fake_calls WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Get all fake calls
    @Query("SELECT * FROM fake_calls ORDER BY createdAt DESC")
    fun getAllFakeCalls(): Flow<List<FakeCall>>

    // Get active fake calls only
    @Query("SELECT * FROM fake_calls WHERE isActive = 1 ORDER BY scheduledTime ASC")
    fun getActiveFakeCalls(): Flow<List<FakeCall>>

    // Get fake call by ID
    @Query("SELECT * FROM fake_calls WHERE id = :id")
    suspend fun getFakeCallById(id: Long): FakeCall?

    // Get upcoming calls (scheduled time is in the future)
    @Query("SELECT * FROM fake_calls WHERE scheduledTime > :currentTime AND isActive = 1 ORDER BY scheduledTime ASC")
    fun getUpcomingCalls(currentTime: Long = System.currentTimeMillis()): Flow<List<FakeCall>>

    // Delete all fake calls
    @Query("DELETE FROM fake_calls")
    suspend fun deleteAll()

    // Deactivate a fake call
    @Query("UPDATE fake_calls SET isActive = 0 WHERE id = :id")
    suspend fun deactivateFakeCall(id: Long)

    // Get count of active calls
    @Query("SELECT COUNT(*) FROM fake_calls WHERE isActive = 1")
    fun getActiveCallsCount(): Flow<Int>

    // Get calls that should trigger within next minute (for scheduling)
    @Query("SELECT * FROM fake_calls WHERE scheduledTime <= :targetTime AND scheduledTime > :currentTime AND isActive = 1")
    suspend fun getCallsToSchedule(
        currentTime: Long = System.currentTimeMillis(),
        targetTime: Long = System.currentTimeMillis() + 60000 // Next 1 minute
    ): List<FakeCall>
}