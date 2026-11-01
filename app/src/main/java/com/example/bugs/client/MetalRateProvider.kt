package com.example.bugs.client

import com.example.bugs.client.dto.MetalRateResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MetalRateProvider {
    @GET("scripts/xml_metall.asp")
    suspend fun getMetalRate(
        @Query("date_req1") dateReq1: String,
        @Query("date_req2") dateReq2: String
    ): MetalRateResponse
}