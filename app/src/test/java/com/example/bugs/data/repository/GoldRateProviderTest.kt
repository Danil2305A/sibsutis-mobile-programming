package com.example.bugs.data.repository

import com.example.bugs.client.MetalRateProvider
import com.example.bugs.client.dto.MetalRateRecord
import com.example.bugs.client.dto.MetalRateResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class GoldRateProviderTest {

    private lateinit var metal: MetalRateProvider
    private lateinit var cache: GoldRateCache
    private lateinit var provider: GoldRateProvider

    @Before
    fun setUp() {
        metal = mockk()
        cache = mockk(relaxed = true)
        provider = GoldRateProvider(provider = metal, cache = cache)
    }

    private fun rec(code: String, buy: String?, sell: String? = null, date: String = "01/01/2025") =
        MetalRateRecord(buy = buy, sell = sell, code = code, date = date)

    @Test
    fun cacheHitAvoidsNetwork() = runTest {
        every { cache.getTodayPrice() } returns 5555.0
        assertEquals(5555.0, provider.getTodayGoldRate())
        coVerify(exactly = 0) { metal.getMetalRate(any(), any()) }
    }

    @Test
    fun cacheMissFetchesAndSaves() = runTest {
        every { cache.getTodayPrice() } returns null
        every { cache.today() } returns "01/01/2025"
        coEvery { metal.getMetalRate(any(), any()) } returns
                MetalRateResponse(listOf(rec("1", "1000,50")))

        assertEquals(1000.50, provider.getTodayGoldRate()!!, 1e-6)
        verify { cache.save(1000.50, "01/01/2025") }
    }

    @Test
    fun networkFailureReturnsNullAndDoesNotSave() = runTest {
        every { cache.getTodayPrice() } returns null
        coEvery { metal.getMetalRate(any(), any()) } throws RuntimeException("boom")
        assertNull(provider.getTodayGoldRate())
        verify(exactly = 0) { cache.save(any(), any()) }
    }

    @Test
    fun forceRefreshClearsCache() = runTest {
        var cacheValue: Double? = 1.0
        every { cache.getTodayPrice() } answers { cacheValue }
        every { cache.clear() } answers { cacheValue = null; Unit }
        every { cache.today() } returns "01/01/2025"
        every { cache.save(any(), any()) } answers { cacheValue = firstArg() }
        coEvery { metal.getMetalRate(any(), any()) } returns
                MetalRateResponse(listOf(rec("1", "2000")))

        val result = provider.getTodayGoldRate(forceRefresh = true)

        verify { cache.clear() }
        assertEquals(2000.0, result!!, 1e-6)
    }

    @Test
    fun fetchRequestsFallbackWindowEndingToday() = runTest {
        val fixedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("10/01/2025")!!
        provider = GoldRateProvider(provider = metal, cache = cache) { fixedDate }

        every { cache.getTodayPrice() } returns null
        coEvery { metal.getMetalRate(any(), any()) } returns
                MetalRateResponse(listOf(rec("1", "1")))

        provider.getTodayGoldRate()

        coVerify {
            metal.getMetalRate(
                match { it == "05/01/2025" },
                match { it == "10/01/2025" }
            )
        }
    }

    @Test
    fun fetchUsesOnlyCode1AndPicksLast() = runTest {
        every { cache.getTodayPrice() } returns null
        every { cache.today() } returns "01/01/2025"
        coEvery { metal.getMetalRate(any(), any()) } returns MetalRateResponse(
            listOf(
                rec("2", "9999"),
                rec("1", "100", date = "01/01/2025"),
                rec("1", "300", date = "03/01/2025"),
            )
        )
        assertEquals(300.0, provider.getTodayGoldRate()!!, 1e-6)
    }

    @Test
    fun parsePrefersBuyThenSellThenNull() = runTest {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("15/06/2025")!!

        coEvery { metal.getMetalRate(any(), any()) } returns
                MetalRateResponse(listOf(rec("1", "123,45", "100,00")))
        assertEquals(123.45, provider.getGoldRateForDate(date)!!, 1e-6)

        coEvery { metal.getMetalRate(any(), any()) } returns
                MetalRateResponse(listOf(rec("1", null, "100,00")))
        assertEquals(100.0, provider.getGoldRateForDate(date)!!, 1e-6)

        coEvery { metal.getMetalRate(any(), any()) } returns
                MetalRateResponse(listOf(rec("1", null, null)))
        assertNull(provider.getGoldRateForDate(date))
    }

    @Test
    fun getGoldRateForDateUsesFirstCode1() = runTest {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("15/06/2025")!!
        coEvery { metal.getMetalRate(any(), any()) } returns MetalRateResponse(
            listOf(rec("1", "100"), rec("1", "200"))
        )
        assertEquals(100.0, provider.getGoldRateForDate(date)!!, 1e-6)
    }

    @Test
    fun getGoldRateForDateSwallowsExceptions() = runTest {
        coEvery { metal.getMetalRate(any(), any()) } throws RuntimeException()
        assertNull(provider.getGoldRateForDate())
    }
}