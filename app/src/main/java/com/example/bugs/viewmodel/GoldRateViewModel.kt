package com.example.bugs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bugs.client.GoldRateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface GoldRateState {
    data object Idle : GoldRateState
    data object Loading : GoldRateState
    data class Success(val price: Double, val date: String) : GoldRateState
    data class Error(val message: String) : GoldRateState
}

class GoldRateViewModel(
    private val provider: GoldRateProvider = GoldRateProvider()
) : ViewModel() {

    private val _state = MutableStateFlow<GoldRateState>(GoldRateState.Idle)
    val state: StateFlow<GoldRateState> = _state.asStateFlow()

    private var cachedDate: String? = null
    private var cachedPrice: Double? = null

    fun loadGoldRate() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        if (cachedDate == today && cachedPrice != null) {
            _state.value = GoldRateState.Success(cachedPrice!!, today)
            return
        }
        if (_state.value is GoldRateState.Loading) return

        viewModelScope.launch {
            _state.value = GoldRateState.Loading
            val price = provider.getGoldRateWithFallback()
            _state.value = if (price != null) {
                cachedDate = today
                cachedPrice = price
                GoldRateState.Success(price, today)
            } else {
                GoldRateState.Error("Не удалось получить курс. Попробуйте позже.")
            }
        }
    }

    fun forceRefresh() {
        cachedDate = null
        cachedPrice = null
        loadGoldRate()
    }
}