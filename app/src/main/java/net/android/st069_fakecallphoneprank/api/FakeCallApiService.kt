package net.android.st069_fakecallphoneprank.api

import net.android.st069_fakecallphoneprank.data.model.FakeCallApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FakeCallApiService {

    // Get all available fake calls
    // Your API should return array: [{...}, {...}]
    @GET("fake-calls")
    suspend fun getAllFakeCalls(): Response<List<FakeCallApi>>

    // Get fake calls by category
    @GET("fake-calls")
    suspend fun getFakeCallsByCategory(
        @Query("category") category: String
    ): Response<List<FakeCallApi>>
}