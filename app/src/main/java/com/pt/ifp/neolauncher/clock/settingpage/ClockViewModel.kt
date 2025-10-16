package com.pt.ifp.neolauncher.clock.settingpage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone


const val CLOCK_DIGITAL: Int = 0
const val CLOCK_ANALOG2: Int = 1
const val CLOCK_ANALOG3: Int = 2

enum class ClockSkin { DIGITAL, ANALOG2, ANALOG3 }

/** 不用 DB 的設定資料模型 */
data class ClockSetting(
    val id: String = "in_memory",
    val pageIndex: Int = CLOCK_DIGITAL,
    val city1Id: String = "Europe/London",
    val city1DisplayName: String = "London",
    val city2Id: String = "America/New_York",
    val city2DisplayName: String = "New York",
    val language: String = Locale.getDefault().language,
    val isFloating: Boolean = false,
    val transparency: Int = 100,
    val windowX: Int = 0,
    val windowY: Int = 0
)

/* -------------------- ViewModel -------------------- */
class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val context = getApplication<Application>()
    private val sm by lazy { SettingsManager.getInstance(context) }

    /** 單一真實來源：純記憶體設定 */
    private val _setting = MutableStateFlow(ClockSetting())
    val settingClock: StateFlow<ClockSetting> = _setting.asStateFlow()

    /** 由 pageIndex 對應出目前皮膚（提供給 UI 收集使用） */
    val skin: StateFlow<ClockSkin> = settingClock
        .map { cfg ->
            when (cfg.pageIndex) {
                CLOCK_DIGITAL -> ClockSkin.DIGITAL
                CLOCK_ANALOG2 -> ClockSkin.ANALOG2
                CLOCK_ANALOG3 -> ClockSkin.ANALOG3
                else -> ClockSkin.DIGITAL
            }
        }
        .stateIn(appScope, SharingStarted.Eagerly, ClockSkin.DIGITAL)

    /** 顯示用的衍生資料（時區字串） */
    val city1Timezone: StateFlow<String> =
        settingClock.map { getGMTTimeAndDisplayName(it.city1Id) }
            .stateIn(appScope, SharingStarted.Eagerly, getGMTTimeAndDisplayName(_setting.value.city1Id))

    val city2Timezone: StateFlow<String> =
        settingClock.map { getGMTTimeAndDisplayName(it.city2Id) }
            .stateIn(appScope, SharingStarted.Eagerly, getGMTTimeAndDisplayName(_setting.value.city2Id))

    val isShowCity1: StateFlow<Boolean> =
        settingClock.map { cfg ->
            ((cfg.pageIndex == CLOCK_ANALOG2) || (cfg.pageIndex == CLOCK_ANALOG3)) && !cfg.isFloating
        }.stateIn(appScope, SharingStarted.Eagerly, false)

    val isShowCity2: StateFlow<Boolean> =
        settingClock.map { cfg ->
            (cfg.pageIndex == CLOCK_ANALOG3) && !cfg.isFloating
        }.stateIn(appScope, SharingStarted.Eagerly, false)

    /** 多語系變更時，從 SettingsManager 回填顯示名稱（僅改本地狀態） */
    fun refreshNamesForLocaleIfNeeded() {
        val cfg = _setting.value
        val langNow = Locale.getDefault().language
        if (cfg.language != langNow) {
            val c1 = sm.getTimeZoneLabel(cfg.city1Id)
            val c2 = sm.getTimeZoneLabel(cfg.city2Id)
            _setting.value = cfg.copy(
                language = langNow,
                city1DisplayName = if (c1.isNotEmpty()) c1 else cfg.city1DisplayName,
                city2DisplayName = if (c2.isNotEmpty()) c2 else cfg.city2DisplayName
            )
        }
    }

    fun getCurrentGMTTime(): String {
        val offsetToday = OffsetDateTime.now().offset
        return "GMT$offsetToday " + TimeZone.getDefault().displayName
    }

    /** Compose 畫面更新狀態（不寫 DB） */
    fun addClockSetting(
        isFloating: Boolean? = null,
        transparency: Int? = null,
        pageIndex: Int? = null,
        city1DisplayName: String? = null,
        city2DisplayName: String? = null,
        city1Id: String? = null,
        city2Id: String? = null,
        language: String? = null,
        windowX: Int? = null,
        windowY: Int? = null
    ) {
        val cur = _setting.value
        _setting.value = cur.copy(
            isFloating = isFloating ?: cur.isFloating,
            transparency = transparency ?: cur.transparency,
            pageIndex = pageIndex ?: cur.pageIndex,
            city1DisplayName = city1DisplayName ?: cur.city1DisplayName,
            city2DisplayName = city2DisplayName ?: cur.city2DisplayName,
            city1Id = city1Id ?: cur.city1Id,
            city2Id = city2Id ?: cur.city2Id,
            language = language ?: cur.language,
            windowX = windowX ?: cur.windowX,
            windowY = windowY ?: cur.windowY
        )
    }

    /** 從「選時區頁」回來更新對應城市 ZoneId */
    fun setCityTimezone(which: Int, zoneId: String) {
        when (which) {
            CITY1 -> addClockSetting(city1Id = zoneId)
            CITY2 -> addClockSetting(city2Id = zoneId)
        }
    }

    fun isFloatingMode(): Boolean = _setting.value.isFloating

    private fun getGMTTimeAndDisplayName(id: String): String {
        val zone = ZoneId.of(id)
        val offsetToday = OffsetDateTime.now(zone).offset
        return "GMT$offsetToday " + TimeZone.getTimeZone(id).displayName
    }
}
