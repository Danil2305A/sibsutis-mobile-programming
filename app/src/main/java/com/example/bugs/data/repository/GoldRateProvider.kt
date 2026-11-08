package com.example.bugs.data.repository

import com.example.bugs.client.CbrClient
import com.example.bugs.client.MetalRateProvider
import com.example.bugs.client.dto.MetalRateRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoldRateProvider(
    private val provider: MetalRateProvider = CbrClient.metalRateProvider,
    private val cache: GoldRateCache,
    private val clock: () -> Date = { Date() }
) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    suspend fun getTodayGoldRate(forceRefresh: Boolean = false): Double? {
        if (forceRefresh) cache.clear()

        cache.getTodayPrice()?.let { return it }

        val price = fetchFromNetwork() ?: return null
        cache.save(price, cache.today())
        return price
    }

    fun getLastKnownRate(): Double? = cache.getLastPrice()

    suspend fun getGoldRateForDate(date: Date = Date()): Double? {
        val dateStr = dateFormat.format(date)
        return try {
            val response = provider.getMetalRate(dateStr, dateStr)
            response.records.firstOrNull { it.code == GOLD_CODE }?.parsePrice()
        } catch (e: Exception) {
            null
        }
    }

    private fun daysToMillis(daysCount: Int): Long = daysCount * 24L * 60 * 60 * 1000

    private suspend fun fetchFromNetwork(): Double? {
        return try {
            val today = clock()
            val from = Date(today.time - daysToMillis(FALLBACK_DAYS))
            val response = provider.getMetalRate(
                dateFormat.format(from),
                dateFormat.format(today)
            )
            response.records
                .asSequence()
                .filter { it.code == GOLD_CODE }
                .mapNotNull { it.parsePrice() }
                .lastOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun MetalRateRecord.parsePrice(): Double? =
        buy?.replace(",", ".")?.toDoubleOrNull()
            ?: sell?.replace(",", ".")?.toDoubleOrNull()

    companion object {
        private const val GOLD_CODE = "1"
        private const val FALLBACK_DAYS = 5
    }
}