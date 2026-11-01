package com.example.bugs.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.bugs.R
import com.example.bugs.data.repository.GoldRateProvider
import com.example.bugs.data.repository.GoldRateCache
import java.util.Locale

class GoldRateWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val cache = GoldRateCache(context)
        val price = cache.getLastPrice()
        val text = price?.let { formatPrice(it) } ?: "—"

        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context, appWidgetIds, text))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_REFRESH_CLICK) {
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                ?: AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(context, GoldRateWidgetProvider::class.java))

            if (ids.isNotEmpty()) {
                ids.forEach { id ->
                    AppWidgetManager.getInstance(context)
                        .updateAppWidget(id, buildViews(context, ids, "загрузка…"))
                }
                enqueueUpdate(context)
            }
        }
    }

    private fun enqueueUpdate(context: Context) {
        val request = OneTimeWorkRequestBuilder<GoldRateWorker>()
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        private const val WORK_NAME = "gold_rate_update"
        const val ACTION_REFRESH_CLICK = "com.example.bugs.ACTION_REFRESH_CLICK"

        fun buildViews(
            context: Context,
            appWidgetIds: IntArray,
            priceText: String
        ): RemoteViews {
            return RemoteViews(context.packageName, R.layout.widget_gold_ball).apply {
                setTextViewText(R.id.tvGoldPrice, priceText)

                val intent = Intent(context, GoldRateWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_CLICK
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.ivBall, pendingIntent)
                setOnClickPendingIntent(R.id.tvGoldPrice, pendingIntent)
            }
        }

        fun updateAll(context: Context, priceText: String) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, GoldRateWidgetProvider::class.java)
            )
            ids.forEach { id ->
                manager.updateAppWidget(id, buildViews(context, ids, priceText))
            }
        }

        fun formatPrice(price: Double): String =
            String.format(Locale("ru"), "%.2f ₽/г", price)
    }
}

class GoldRateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val cache = GoldRateCache(applicationContext)
        val provider = GoldRateProvider(cache = cache)

        if (!isNetworkAvailable()) {
            val lastKnown = provider.getLastKnownRate()
            val text = lastKnown?.let { GoldRateWidgetProvider.formatPrice(it) } ?: "нет сети"
            GoldRateWidgetProvider.updateAll(applicationContext, text)
            return Result.success()
        }

        return try {
            val price = provider.getTodayGoldRate()
            if (price == null) {
                val lastKnown = provider.getLastKnownRate()
                val text = lastKnown?.let { GoldRateWidgetProvider.formatPrice(it) } ?: "нет данных"
                GoldRateWidgetProvider.updateAll(applicationContext, text)
                return Result.success()
            }
            GoldRateWidgetProvider.updateAll(
                applicationContext,
                GoldRateWidgetProvider.formatPrice(price)
            )
            Result.success()
        } catch (e: Exception) {
            val lastKnown = provider.getLastKnownRate()
            val text = lastKnown?.let { GoldRateWidgetProvider.formatPrice(it) } ?: "ошибка"
            GoldRateWidgetProvider.updateAll(applicationContext, text)
            Result.success()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}