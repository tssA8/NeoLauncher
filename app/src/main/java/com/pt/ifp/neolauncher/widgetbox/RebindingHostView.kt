package com.pt.ifp.neolauncher.widgetbox

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet

class RebindingHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null // 允許從 XML 建，但不傳給 super，因為 super 不支援 attrs
) : AppWidgetHostView(
    // 只能用這兩種其一：super(context) 或 super(context, animIn, animOut)
    context
    // 或想用動畫就改成：
    // context, android.R.anim.fade_in, android.R.anim.fade_out
) {

    private fun safeRebind() {
        val id = appWidgetId
        val info: AppWidgetProviderInfo? = appWidgetInfo
        if (id != AppWidgetManager.INVALID_APPWIDGET_ID && info != null) {
            try { setAppWidget(id, info) } catch (_: Throwable) {}
            val w = width; val h = height
            if (w > 0 && h > 0) {
                val dp = resources.displayMetrics.density
                val opts = Bundle().apply {
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,  (w / dp).toInt())
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, (h / dp).toInt())
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,  (w / dp).toInt())
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, (h / dp).toInt())
                }
                try { AppWidgetManager.getInstance(context).updateAppWidgetOptions(id, opts) } catch (_: Throwable) {}
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        safeRebind()
    }

    @SuppressLint("NewApi") // onVisibilityAggregated 在 API 24+，低版本會自動忽略
    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) safeRebind()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        safeRebind()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) safeRebind()
    }
}
