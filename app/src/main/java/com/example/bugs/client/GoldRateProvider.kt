package com.example.bugs.client

import com.example.bugs.client.dto.MetalRateResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoldRateProvider(
    private val provider: MetalRateProvider = CbrClient.metalRateProvider
) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Курс золота на конкретную дату.
     * @return цена за 1 грамм в рублях или null, если данных нет.
     */
    suspend fun getGoldRate(date: Date = Date()): Double? {
        val dateStr = dateFormat.format(date)
        return try {
            val response = provider.getMetalRate(dateStr, dateStr)
            extractGoldPrice(response)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Последний доступный курс золота за последние [daysBack] дней.
     */
    suspend fun getGoldRateWithFallback(daysBack: Int = 5): Double? {
        return try {
            val today = Date()
            val from = Date(today.time - daysBack * 24L * 60 * 60 * 1000)
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
            e.printStackTrace()
            null
        }
    }

    private fun extractGoldPrice(response: MetalRateResponse): Double? =
        response.records
            .firstOrNull { it.code == GOLD_CODE }
            ?.parsePrice()

    private fun com.example.bugs.client.dto.MetalRateRecord.parsePrice(): Double? =
        buy?.replace(",", ".")?.toDoubleOrNull()
            ?: sell?.replace(",", ".")?.toDoubleOrNull()

    companion object {
        private const val GOLD_CODE = "1"
    }
}