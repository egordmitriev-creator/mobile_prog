package com.example.bugs.di

import android.content.Context
import com.example.bugs.data.AppDatabase
import com.example.bugs.data.repository.GameRepository
import com.example.bugs.data.repository.GoldRateRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // База данных
    single { AppDatabase.getInstance(androidContext()) }

    // Репозитории
    single { GameRepository(get()) }
    single { GoldRateRepository(androidContext()) }
}