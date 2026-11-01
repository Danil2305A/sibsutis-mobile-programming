package com.example.bugs.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bugs.data.repository.GoldRateCache
import com.example.bugs.data.repository.GoldRateProvider

val GameViewModelFactory = viewModelFactory {
    initializer {
        val app = this[APPLICATION_KEY] as Application
        val cache = GoldRateCache(app)
        val provider = GoldRateProvider(cache = cache)
        GameViewModel(goldRateProvider = provider)
    }
}