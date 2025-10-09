package net.android.st069_fakecallphoneprank.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import net.android.st069_fakecallphoneprank.api.ApiClient
import net.android.st069_fakecallphoneprank.data.Resource
import net.android.st069_fakecallphoneprank.data.model.FakeCallApi

class ApiRepository {

    private val apiService = ApiClient.fakeCallApiService

    // Get all fake calls (combines kid + general)
    fun getAllFakeCalls(): Flow<Resource<List<FakeCallApi>>> = flow {
        try {
            emit(Resource.Loading())

            // Fetch both kid and general categories
            val kidResponse = apiService.getKidFakeCalls()
            val generalResponse = apiService.getGeneralFakeCalls()

            if (kidResponse.isSuccessful && generalResponse.isSuccessful) {
                val kidData = kidResponse.body() ?: emptyList()
                val generalData = generalResponse.body() ?: emptyList()

                // Combine both lists
                val allData = kidData + generalData

                if (allData.isNotEmpty()) {
                    emit(Resource.Success(allData))
                } else {
                    emit(Resource.Error("No data available"))
                }
            } else {
                val errorMessage = when {
                    !kidResponse.isSuccessful -> "Error loading kid calls: ${kidResponse.code()}"
                    !generalResponse.isSuccessful -> "Error loading general calls: ${generalResponse.code()}"
                    else -> "Error loading data"
                }
                emit(Resource.Error(errorMessage))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    // Get fake calls by category
    fun getFakeCallsByCategory(category: String): Flow<Resource<List<FakeCallApi>>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getFakeCallsByCategory(category)

            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    emit(Resource.Success(data))
                } else {
                    emit(Resource.Error("No data available"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)
}