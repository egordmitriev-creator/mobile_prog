package com.example.bugs.data.api

import com.example.bugs.data.models.ValCurs
import retrofit2.Response
import retrofit2.http.GET

interface CbrApiService {
    @GET("scripts/XML_daily.asp")
    suspend fun getDailyRates(): Response<ValCurs>
}