package com.example.bugs.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.bugs.data.api.CbrApiService
import com.example.bugs.data.models.Valute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class GoldRateRepository(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("gold_rate_prefs", Context.MODE_PRIVATE)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.cbr.ru/")
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .build()

    private val apiService: CbrApiService = retrofit.create(CbrApiService::class.java)

    suspend fun getCurrentGoldRate(): Float {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDailyRates()
                if (response.isSuccessful) {
                    val valCurs = response.body()
                    val goldRate = findGoldRate(valCurs?.valutes)

                    // Сохраняем курс для оффлайн использования
                    sharedPreferences.edit().putFloat("current_gold_rate", goldRate).apply()
                    goldRate
                } else {
                    // Если ошибка сети, используем сохраненное значение
                    sharedPreferences.getFloat("current_gold_rate", 5000f)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // При ошибке используем сохраненное значение
                sharedPreferences.getFloat("current_gold_rate", 5000f)
            }
        }
    }

    private fun findGoldRate(valutes: List<Valute>?): Float {
        // Золото имеет ID R01235 в системе ЦБ
        val goldValute = valutes?.find { it.id == "R01235" }
        return goldValute?.getValueAsFloat() ?: 5000f // Значение по умолчанию
    }

    fun getCachedGoldRate(): Float {
        return sharedPreferences.getFloat("current_gold_rate", 5000f)
    }
}