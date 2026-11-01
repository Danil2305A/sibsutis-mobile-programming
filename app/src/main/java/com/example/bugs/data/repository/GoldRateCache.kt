package com.example.bugs.data.repository

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit

class GoldRateCache(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun save(price: Double, date: String = today()) {
        prefs.edit {
            putString(KEY_PRICE, price.toString())
                .putString(KEY_DATE, date)
        }
    }

    fun getTodayPrice(): Double? {
        val savedDate = prefs.getString(KEY_DATE, null) ?: return null
        if (savedDate != today()) return null
        return prefs.getString(KEY_PRICE, null)?.toDoubleOrNull()
    }

    fun getLastPrice(): Double? =
        prefs.getString(KEY_PRICE, null)?.toDoubleOrNull()

    fun getLastDate(): String? =
        prefs.getString(KEY_DATE, null)

    fun clear() {
        prefs.edit { remove(KEY_PRICE).remove(KEY_DATE) }
    }

    fun today(): String = dateFormat.format(Date())

    companion object {
        private const val PREFS_NAME = "gold_rate_cache"
        private const val KEY_PRICE = "price"
        private const val KEY_DATE = "date"
    }
}
