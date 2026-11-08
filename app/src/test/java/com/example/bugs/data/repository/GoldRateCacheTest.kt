package com.example.bugs.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GoldRateCacheTest {

    private lateinit var cache: GoldRateCache

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.getSharedPreferences("gold_rate_cache", Context.MODE_PRIVATE)
            .edit().clear().commit()
        cache = GoldRateCache(ctx)
    }

    @Test
    fun clearRemovesBothKeys() {
        cache.save(100.0)
        cache.clear()
        assertNull(cache.getLastPrice())
        assertNull(cache.getLastDate())
        assertNull(cache.getTodayPrice())
    }
}