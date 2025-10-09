package net.android.st069_fakecallphoneprank.api

import net.android.st069_fakecallphoneprank.data.model.FakeCallApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FakeCallApiService {

    // Get kid category fake calls
    // Endpoint: https://lvtglobal.site/api/fakephonecall/kid
    @GET("api/fakephonecall/kid")
    suspend fun getKidFakeCalls(): Response<List<FakeCallApi>>

    // Get general category fake calls
    // Endpoint: https://lvtglobal.site/api/fakephonecall/general
    @GET("api/fakephonecall/general")
    suspend fun getGeneralFakeCalls(): Response<List<FakeCallApi>>

    // Get fake calls by category (dynamic)
    // Endpoint: https://lvtglobal.site/api/fakephonecall/{category}
    @GET("api/fakephonecall/{category}")
    suspend fun getFakeCallsByCategory(
        @Path("category") category: String
    ): Response<List<FakeCallApi>>
}