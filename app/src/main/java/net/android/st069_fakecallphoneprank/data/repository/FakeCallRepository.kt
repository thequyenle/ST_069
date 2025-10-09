package net.android.st069_fakecallphoneprank.data.repository

import kotlinx.coroutines.flow.Flow
import net.android.st069_fakecallphoneprank.data.dao.FakeCallDao
import net.android.st069_fakecallphoneprank.data.entity.FakeCall

class FakeCallRepository(private val fakeCallDao: FakeCallDao) {

    // Get all fake calls as Flow
    val allFakeCalls: Flow<List<FakeCall>> = fakeCallDao.getAllFakeCalls()

    // Get active fake calls
    val activeFakeCalls: Flow<List<FakeCall>> = fakeCallDao.getActiveFakeCalls()

    // Get active calls count
    val activeCallsCount: Flow<Int> = fakeCallDao.getActiveCallsCount()

    // Insert new fake call
    suspend fun insert(fakeCall: FakeCall): Long {
        return fakeCallDao.insert(fakeCall)
    }

    // Update existing fake call
    suspend fun update(fakeCall: FakeCall) {
        fakeCallDao.update(fakeCall)
    }

    // Delete fake call
    suspend fun delete(fakeCall: FakeCall) {
        fakeCallDao.delete(fakeCall)
    }

    // Delete by ID
    suspend fun deleteById(id: Long) {
        fakeCallDao.deleteById(id)
    }

    // Get fake call by ID
    suspend fun getFakeCallById(id: Long): FakeCall? {
        return fakeCallDao.getFakeCallById(id)
    }

    // Get upcoming calls (not yet called)
    fun getUpcomingCalls(): Flow<List<FakeCall>> {
        return fakeCallDao.getUpcomingCalls()
    }

    // Get past calls (already called/triggered)
    fun getPastCalls(): Flow<List<FakeCall>> {
        return fakeCallDao.getPastCalls()
    }

    // Delete all fake calls
    suspend fun deleteAll() {
        fakeCallDao.deleteAll()
    }

    // Deactivate fake call
    suspend fun deactivateFakeCall(id: Long) {
        fakeCallDao.deactivateFakeCall(id)
    }
}