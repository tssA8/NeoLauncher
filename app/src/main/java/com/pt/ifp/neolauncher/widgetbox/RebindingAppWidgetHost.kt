package com.pt.ifp.neolauncher.widgetbox

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

class RebindingAppWidgetHost(
    context: Context,
    hostId: Int
) : AppWidgetHost(context, hostId) {

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return RebindingHostView(context).apply {
            try { setAppWidget(appWidgetId, appWidget) } catch (_: Throwable) {}
        }
    }
}
