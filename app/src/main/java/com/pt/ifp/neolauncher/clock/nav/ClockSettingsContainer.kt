package com.pt.ifp.neolauncher.clock.nav

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import com.pt.ifp.neolauncher.clock.clocklacation.ClockLocationScreen
import com.pt.ifp.neolauncher.clock.settingpage.CITY1
import com.pt.ifp.neolauncher.clock.settingpage.CITY2
import com.pt.ifp.neolauncher.clock.settingpage.ClockViewModel
import com.pt.ifp.neolauncher.clock.settingpage.ClockSettingsScreen

private enum class ClockPage { Settings, City1, City2 }

/** 用 state 切畫面，不用 NavHost */
@Composable
fun ClockSettingsContainer(
    viewModel: ClockViewModel,
    onDismiss: () -> Unit = {},
) {
    var page by remember { mutableStateOf(ClockPage.Settings) }

    when (page) {
        ClockPage.Settings -> {
            ClockSettingsScreen(
                viewModel = viewModel,
                onSelectTimezone = { which ->
                    page = if (which == CITY1) ClockPage.City1 else ClockPage.City2
                },
                onSync = { false },
                onClose = { onDismiss() }   // 只呼叫外部關閉，不要自己設 show=false
            )
        }
        ClockPage.City1 -> {
            BackHandler { page = ClockPage.Settings }
            ClockLocationScreen(
                titleWhich = CITY1,
                onBack = { page = ClockPage.Settings },
                onSelectTimezone = { zoneId, label ->
                    viewModel.addClockSetting(city1Id = zoneId, city1DisplayName = label)
                    page = ClockPage.Settings
                }
            )
        }
        ClockPage.City2 -> {
            BackHandler { page = ClockPage.Settings }
            ClockLocationScreen(
                titleWhich = CITY2,
                onBack = { page = ClockPage.Settings },
                onSelectTimezone = { zoneId, label ->
                    viewModel.addClockSetting(city2Id = zoneId, city2DisplayName = label)
                    page = ClockPage.Settings
                }
            )
        }
    }
}

