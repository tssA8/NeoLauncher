// AppWidgetBinder.java
package com.pt.ifp.neolauncher.widgetbox

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle

object AppWidgetBinder {
    // ====== 常數 ======
    const val REQ_BIND_APPWIDGET: Int = 0xA11C

    private const val SP_NAME = "widget_prefs"
    private const val KEY_ID_PREFIX = "bound_id_"
    private const val KEY_PENDING_PREFIX = "pending_id_"

    // ====== 對外主要 API ======
    /**
     * 確保 provider 已綁定：
     * 1) 若已有保存 id → 驗證仍有效且 provider 一致 → 回傳
     * 2) 否則 allocate → 若允許則立即 bind → 回傳新 id
     * 3) 若不允許 → 啟動 ACTION_APPWIDGET_BIND（需在 onActivityResult 處理）
     *
     * @return 已綁定的 appWidgetId；若需要授權或錯誤則回傳 INVALID_APPWIDGET_ID
     */
    fun ensureBoundOrRequest(
        activity: Activity,
        host: AppWidgetHost,
        mgr: AppWidgetManager,
        providerFlat: String?,
        options: Bundle?
    ): Int {
        val provider = parseProvider(providerFlat)
        if (provider == null) return AppWidgetManager.INVALID_APPWIDGET_ID

        // 1) 讀取已保存 id
        val saved = getSavedBoundId(activity, providerFlat)
        if (saved != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val info = mgr.getAppWidgetInfo(saved)
            if (info != null && provider == info.provider) {
                // 仍有效且 provider 一致 → 直接沿用
                return saved
            } else {
                // 失效或 provider 不一致 → 刪除並清除保存，走重綁
                try {
                    host.deleteAppWidgetId(saved)
                } catch (ignore: Throwable) {
                }
                saveId(activity, providerFlat, AppWidgetManager.INVALID_APPWIDGET_ID)
            }
        }

        // 2) 分配新 id
        val newId = host.allocateAppWidgetId()

        // 3) 嘗試靜默綁定
        var bound = false
        try {
            if (options != null) {
                bound = mgr.bindAppWidgetIdIfAllowed(newId, provider, options)
            } else {
                bound = mgr.bindAppWidgetIdIfAllowed(newId, provider)
            }
        } catch (ignore: Throwable) {
            // 某些 ROM 可能在 provider 不存在時丟例外
        }

        if (bound) {
            saveId(activity, providerFlat, newId)
            clearPendingId(activity, providerFlat)
            return newId
        }

        // 4) 需要授權 → 啟動授權流程（舊式 onActivityResult）
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        if (options != null) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options)
        }
        savePendingId(activity, providerFlat, newId)
        activity.startActivityForResult(intent, REQ_BIND_APPWIDGET)

        return AppWidgetManager.INVALID_APPWIDGET_ID
    }

    /**
     * 取回已保存的綁定 id；若無則回傳 INVALID_APPWIDGET_ID
     */
    fun getSavedBoundId(ctx: Context, providerFlat: String?): Int {
        return ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_ID_PREFIX + providerFlat, AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    // ====== 內部保存 helper（你要的 saveId 也在這裡） ======
    /** 僅供內部保存  */
    fun saveId(ctx: Context, providerFlat: String?, id: Int) {
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_ID_PREFIX + providerFlat, id).apply()
    }

    /** 授權流程中的「暫存 id」  */
    fun savePendingId(ctx: Context, providerFlat: String?, id: Int) {
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_PENDING_PREFIX + providerFlat, id).apply()
    }

    /** 讀取授權流程中的「暫存 id」；沒有則 INVALID  */
    fun getPendingId(ctx: Context, providerFlat: String?): Int {
        return ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_PENDING_PREFIX + providerFlat, AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    /** 清除授權流程中的「暫存 id」  */
    fun clearPendingId(ctx: Context, providerFlat: String?) {
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_PENDING_PREFIX + providerFlat).apply()
    }

    // ====== 雜項 ======
    fun parseProvider(flat: String?): ComponentName? {
        var flat = flat
        try {
            // 允許 "pkg/cls" 或 "ComponentInfo{pkg/cls}" 兩種格式
            if (flat == null || flat.isEmpty()) return null
            if (flat.startsWith("ComponentInfo{") && flat.endsWith("}")) {
                flat = flat.substring("ComponentInfo{".length, flat.length - 1)
            }
            val slash = flat.indexOf('/')
            if (slash <= 0 || slash >= flat.length - 1) return null
            val pkg = flat.substring(0, slash)
            var cls = flat.substring(slash + 1)
            if (cls.startsWith(".")) cls = pkg + cls // 相對類名補全

            return ComponentName(pkg, cls)
        } catch (ignore: Throwable) {
            return null
        }
    }
}