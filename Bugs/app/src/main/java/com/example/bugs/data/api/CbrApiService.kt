package com.example.bugs.data.api

import com.example.bugs.data.models.MetallResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CbrApiService {

    // Для ежедневных курсов драгоценных металлов
    @GET("scripts/xml_metall.asp")
    suspend fun getMetallRates(
        @Query("date_req1") dateReq1: String? = null,
        @Query("date_req2") dateReq2: String? = null
    ): Response<MetallResponse>

    // Или использовать текущий курс (без дат)
    @GET("scripts/xml_metall.asp")
    suspend fun getCurrentMetallRates(): Response<MetallResponse>
}