package net.android.st069_fakecallphoneprank.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.android.st069_fakecallphoneprank.data.database.FakeCallDatabase
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.data.repository.FakeCallRepository

class FakeCallViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FakeCallRepository

    // LiveData for observing in UI
    val allFakeCalls: LiveData<List<FakeCall>>
    val activeFakeCalls: LiveData<List<FakeCall>>
    val upcomingCalls: LiveData<List<FakeCall>>
    val activeCallsCount: LiveData<Int>

    init {
        val fakeCallDao = FakeCallDatabase.getDatabase(application).fakeCallDao()
        repository = FakeCallRepository(fakeCallDao)

        // Convert Flow to LiveData
        allFakeCalls = repository.allFakeCalls.asLiveData()
        activeFakeCalls = repository.activeFakeCalls.asLiveData()
        upcomingCalls = repository.getUpcomingCalls().asLiveData()
        activeCallsCount = repository.activeCallsCount.asLiveData()
    }

    // Insert new fake call and schedule it
    fun insert(fakeCall: FakeCall) = viewModelScope.launch {
        val id = repository.insert(fakeCall)

        // Schedule the fake call using AlarmManager
        val scheduler = net.android.st069_fakecallphoneprank.services.FakeCallScheduler(
            getApplication()
        )

        // Get the inserted fake call with ID
        val insertedCall = fakeCall.copy(id = id)
        scheduler.scheduleFakeCall(insertedCall)
    }

    // Update fake call
    fun update(fakeCall: FakeCall) = viewModelScope.launch {
        repository.update(fakeCall)
    }

    // Delete fake call
    fun delete(fakeCall: FakeCall) = viewModelScope.launch {
        repository.delete(fakeCall)
    }

    // Delete by ID
    fun deleteById(id: Long) = viewModelScope.launch {
        repository.deleteById(id)
    }

    // Get fake call by ID (suspend function)
    suspend fun getFakeCallById(id: Long): FakeCall? {
        return repository.getFakeCallById(id)
    }

    // Delete all
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    // Deactivate fake call
    fun deactivateFakeCall(id: Long) = viewModelScope.launch {
        repository.deactivateFakeCall(id)
    }
}