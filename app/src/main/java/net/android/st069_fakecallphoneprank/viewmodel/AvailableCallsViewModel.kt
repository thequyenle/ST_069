package net.android.st069_fakecallphoneprank.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.android.st069_fakecallphoneprank.data.Resource
import net.android.st069_fakecallphoneprank.data.model.CallCategory
import net.android.st069_fakecallphoneprank.data.model.FakeCallApi
import net.android.st069_fakecallphoneprank.data.repository.ApiRepository

class AvailableCallsViewModel : ViewModel() {

    private val repository = ApiRepository()

    // LiveData for fake calls from API
    private val _fakeCalls = MutableLiveData<Resource<List<FakeCallApi>>>()
    val fakeCalls: LiveData<Resource<List<FakeCallApi>>> = _fakeCalls

    // Selected category
    private val _selectedCategory = MutableLiveData(CallCategory.KID)
    val selectedCategory: LiveData<CallCategory> = _selectedCategory

    // Filtered calls based on category
    private val _filteredCalls = MutableLiveData<List<FakeCallApi>>()
    val filteredCalls: LiveData<List<FakeCallApi>> = _filteredCalls

    init {
        // Load all fake calls on initialization
        loadAllFakeCalls()
    }

    // Load all fake calls from API
    fun loadAllFakeCalls() {
        viewModelScope.launch {
            repository.getAllFakeCalls().collect { resource ->
                _fakeCalls.value = resource

                // Filter based on selected category when data is loaded
                if (resource is Resource.Success) {
                    filterByCategory(_selectedCategory.value ?: CallCategory.KID)
                }
            }
        }
    }

    // Load fake calls by category
    fun loadFakeCallsByCategory(category: CallCategory) {
        _selectedCategory.value = category

        viewModelScope.launch {
            repository.getFakeCallsByCategory(category.value).collect { resource ->
                _fakeCalls.value = resource

                if (resource is Resource.Success) {
                    _filteredCalls.value = resource.data ?: emptyList()
                }
            }
        }
    }

    // Filter locally loaded calls by category
    private fun filterByCategory(category: CallCategory) {
        val allCalls = (_fakeCalls.value as? Resource.Success)?.data ?: emptyList()

        _filteredCalls.value = when (category) {
            CallCategory.KID -> allCalls.filter { it.category == "kid" }
            CallCategory.GENERAL -> allCalls.filter { it.category == "general" }
            CallCategory.ALL -> allCalls
        }
    }

    // Change category
    fun setCategory(category: CallCategory) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category

            // You can either:
            // 1. Filter locally (faster)
            filterByCategory(category)

            // 2. Or fetch from API (more accurate if data changes frequently)
            // loadFakeCallsByCategory(category)
        }
    }

    // Refresh data
    fun refresh() {
        loadAllFakeCalls()
    }
}