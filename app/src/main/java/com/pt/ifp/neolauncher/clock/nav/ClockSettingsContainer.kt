package com.pt.ifp.neolauncher.clock.nav

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import com.pt.ifp.neolauncher.clock.clocklacation.ClockLocationScreen

private enum class ClockPage { Settings, City1, City2 }

/** 單檔案容器：用 state 切換畫面，完全不用 NavHost */
@Composable
fun ClockSettingsContainer(
    viewModel: com.pt.ifp.neolauncher.clock.settingpage.ClockViewModel
) {
    var page by remember { mutableStateOf(ClockPage.Settings) }

    when (page) {
        ClockPage.Settings -> {
            _root_ide_package_.com.pt.ifp.neolauncher.clock.settingpage.ClockSettingsScreen(
                viewModel = viewModel,
                onSelectTimezone = { which ->
                    page =
                        if (which == _root_ide_package_.com.pt.ifp.neolauncher.clock.settingpage.CITY1) ClockPage.City1 else ClockPage.City2
                },
                onSync = { false }
            )
        }

        ClockPage.City1 -> {
            // 返回鍵 -> 回到設定頁
            BackHandler { page = ClockPage.Settings }

            ClockLocationScreen(
                titleWhich = _root_ide_package_.com.pt.ifp.neolauncher.clock.settingpage.CITY1,
                onBack = { page = ClockPage.Settings },
                onSelectTimezone = { zoneId, label ->
                    // 更新 VM 後回到設定頁
                    viewModel.addClockSetting(
                        city1Id = zoneId,
                        city1DisplayName = label
                    )
                    page = ClockPage.Settings
                }
            )
        }

        ClockPage.City2 -> {
            BackHandler { page = ClockPage.Settings }

            ClockLocationScreen(
                titleWhich = _root_ide_package_.com.pt.ifp.neolauncher.clock.settingpage.CITY2,
                onBack = { page = ClockPage.Settings },
                onSelectTimezone = { zoneId, label ->
                    viewModel.addClockSetting(
                        city2Id = zoneId,
                        city2DisplayName = label
                    )
                    page = ClockPage.Settings
                }
            )
        }
    }
}
