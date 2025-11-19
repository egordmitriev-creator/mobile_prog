package com.example.bugs.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SimpleGoldRateRepository(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("gold_rate_prefs", Context.MODE_PRIVATE)

    // Реалистичные курсы золота (в рублях за унцию)
    private val realisticRates = listOf(
        328020.88f
    )

    suspend fun getCurrentGoldRate(): Float {
        return withContext(Dispatchers.IO) {
            try {
                // Имитируем задержку сети
                kotlinx.coroutines.delay(1000)

                // Возвращаем случайный реалистичный курс
                val randomRate = realisticRates.random()

                // Сохраняем для кэша
                sharedPreferences.edit().putFloat("current_gold_rate", randomRate).apply()
                randomRate

            } catch (e: Exception) {
                e.printStackTrace()
                getCachedGoldRate()
            }
        }
    }

    fun getCachedGoldRate(): Float {
        return sharedPreferences.getFloat("current_gold_rate", 233000f)
    }
}