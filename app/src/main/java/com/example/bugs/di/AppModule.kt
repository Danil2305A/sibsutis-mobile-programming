package com.example.bugs.di

import com.example.bugs.data.db.AppDatabase
import com.example.bugs.data.repository.GoldRateCache
import com.example.bugs.data.repository.GoldRateProvider
import com.example.bugs.data.repository.PlayerRepository
import com.example.bugs.viewmodel.GameViewModel
import com.example.bugs.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.get(androidContext()) }
    single { get<AppDatabase>().playerDao() }
    single { get<AppDatabase>().scoreDao() }
    single { PlayerRepository(get(), get()) }

    single { GoldRateCache(androidContext()) }
    single { GoldRateProvider(cache = get()) }

    viewModel { GameViewModel(get()) }
    viewModel { MainViewModel() }
}