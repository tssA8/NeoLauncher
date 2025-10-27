// SearchWidgetSlot.kt
package com.pt.ifp.neolauncher.searchbarcomponentView

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun SearchWidgetSlot(
    host: AppWidgetHost,
    mgr: AppWidgetManager,
    widgetId: Int,
    modifier: Modifier = Modifier
) = SearchWidgetSlotInternal(host, mgr, widgetId, modifier)

/**
 * 方案 A：在 factory 直接 new RebindingHostView（不經 host.createView）
 * - 無效 id 顯示占位
 * - startListening/stopListening 綁生命周期
 * - key(widgetId) 穩定 identity
 * - factory 立即 setAppWidget；update 再保險重綁並同步 options
 */
@Composable
fun SearchWidgetSlotInternal(
    host: AppWidgetHost,           // 仍需要，為了 startListening/stopListening
    mgr: AppWidgetManager,
    widgetId: Int,
    modifier: Modifier = Modifier,
    onInvalidId: ((Int) -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
        Box(modifier = modifier.height(80.dp)) {} // 占位
        return
    }

    // 讓 Host 隨可見生命周期啟停
    DisposableEffect(host, lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_START -> host.startListening()
                Lifecycle.Event.ON_STOP  -> host.stopListening()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    // 嘗試取 provider info；必要時在組成後再補拉一次
    var info: AppWidgetProviderInfo? by remember(widgetId, mgr) {
        mutableStateOf(mgr.getAppWidgetInfo(widgetId))
    }
    LaunchedEffect(widgetId) {
        if (info == null) {
            info = mgr.getAppWidgetInfo(widgetId)
            if (info == null) onInvalidId?.invoke(widgetId)
        }
    }
    if (info == null) {
        Box(modifier = modifier.height(80.dp)) {}
        return
    }
    val i = info!!

    // 固定 identity，確保回收後會重新跑 factory/update
    key(widgetId) {
// 節錄：factory 部分
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                // 這裡仍然呼叫 host.createView（現在會回傳 RebindingHostView，且已被 Host 註冊）
                host.createView(ctx, widgetId, i).apply {
                    try { setAppWidget(widgetId, i) } catch (_: Throwable) {}
                    val w = width; val h = height
                    if (w > 0 && h > 0) {
                        val dp = resources.displayMetrics.density
                        val opts = Bundle().apply {
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,  (w / dp).toInt())
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, (h / dp).toInt())
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,  (w / dp).toInt())
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, (h / dp).toInt())
                        }
                        try { mgr.updateAppWidgetOptions(widgetId, opts) } catch (_: Throwable) {}
                    }
                }
            },
            update = { view ->
                if (view.appWidgetId != widgetId || view.appWidgetInfo == null) {
                    try { view.setAppWidget(widgetId, i) } catch (_: Throwable) {}
                }
                val w = view.width; val h = view.height
                if (w > 0 && h > 0) {
                    val dp = view.resources.displayMetrics.density
                    val opts = Bundle().apply {
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,  (w / dp).toInt())
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, (h / dp).toInt())
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,  (w / dp).toInt())
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, (h / dp).toInt())
                    }
                    try { mgr.updateAppWidgetOptions(widgetId, opts) } catch (_: Throwable) {}
                }
            }
        )
    }
}
