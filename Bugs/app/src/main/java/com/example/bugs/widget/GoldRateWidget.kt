package com.example.bugs.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.bugs.R
import com.example.bugs.data.repository.GoldRateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoldRateWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_gold_rate)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = GoldRateRepository(context)
                val goldRate = repository.getCachedGoldRate()
                val rateText = String.format("%.2f", goldRate)

                views.setTextViewText(R.id.gold_rate_text, rateText)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                views.setTextViewText(R.id.gold_rate_text, "Ошибка")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}