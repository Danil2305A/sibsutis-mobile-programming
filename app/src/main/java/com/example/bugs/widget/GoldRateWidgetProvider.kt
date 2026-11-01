package com.example.bugs.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.bugs.R
import com.example.bugs.client.GoldRateProvider
import java.util.Locale

class GoldRateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Только рисуем. НИКАКОГО WorkManager здесь.
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentText = prefs.getString(KEY_PRICE_TEXT, "") ?: ""

        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context, appWidgetIds, currentText))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Клик по виджету обрабатываем отдельно
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
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<GoldRateWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE, // новый клик = новый запрос
            request
        )
    }

    companion object {
        private const val PREFS_NAME = "gold_widget"
        private const val KEY_PRICE_TEXT = "price_text"
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
            // Сохраняем в кэш, чтобы пережить пересоздание виджета
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PRICE_TEXT, priceText)
                .apply()

            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, GoldRateWidgetProvider::class.java)
            )
            ids.forEach { id ->
                manager.updateAppWidget(id, buildViews(context, ids, priceText))
            }
        }
    }
}

class GoldRateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val provider = GoldRateProvider()
            val price = provider.getGoldRateWithFallback()

            if (price == null) {
                GoldRateWidgetProvider.updateAll(applicationContext, "нет данных")
                return Result.success()
            }

            val priceText = String.format(Locale("ru"), "%.2f ₽/г", price)
            GoldRateWidgetProvider.updateAll(applicationContext, priceText)
            Result.success()
        } catch (e: Exception) {
            GoldRateWidgetProvider.updateAll(applicationContext, "ошибка")
            Result.success()
        }
    }
}