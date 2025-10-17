package com.pt.ifp.neolauncher.clock.twoclocks

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pt.ifp.neolauncher.clock.settingpage.ClockViewModel
import com.pt.ifp.neolauncher.clock.settingpage.SettingsManager
import com.pt.ifp.neolauncher.clock.threeclocks.CityClock
import com.pt.ifp.neolauncher.clock.threeclocks.WorldClocksRow
import java.util.TimeZone
import java.time.ZoneId

/* ————— keep your CityClock and WorldClocksRow from your current file ————— */

/** Two clocks: LEFT = Now (Local), RIGHT = City1 */
@Composable
fun WorldClocksTwoFromSettings(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier,
    clockSize: Dp = 96.dp,
    onClickClock: () -> Unit = {},
) {
    val ctx = LocalContext.current

    val setting     = viewModel.settingClock.collectAsStateWithLifecycle().value
    val city1Name   = setting.city1DisplayName.ifBlank { "City 1" }
    val city1ZoneId = (setting.city1Id ?: "").ifBlank { "Asia/Taipei" }

    // 已在 VM 算好的副標（例如 "GMT+08:00 Taipei Standard Time"）
    val city1Tz by viewModel.city1Timezone.collectAsStateWithLifecycle("")

    // ★ 本地時區顯示名：SettingsManager → 取不到就用 id 的尾段當 fallback
    val localTzId = TimeZone.getDefault().id
    val localCityName = SettingsManager
        .getInstance(ctx)
        .getTimeZoneLabel(localTzId)
        ?.takeIf { it.isNotBlank() }
        ?: localTzId.substringAfterLast('/').replace('_', ' ')

    WorldClocksRow(
        cities = listOf(
            CityClock(localCityName, localTzId, isCenterStyle = true), // 左：Now（顯示本地城市名）
            CityClock(city1Name, city1ZoneId)                           // 右：City1
        ),
        centerIndex = 0,                     // 左邊是基準（Now）
        subtitles  = listOf(null, city1Tz),  // 左顯示 "Now"（內部處理），右顯示 city1Tz
        onClickClock = onClickClock,
        modifier   = modifier,
        clockSize  = clockSize
    )
}

/* -------------------- Previews -------------------- */
@Preview(name = "Two Clocks – Light", showBackground = true, widthDp = 320, heightDp = 160)
@Composable
private fun Preview_WorldClocksTwo_Light() {
    WorldClocksRow(
        cities = listOf(
            CityClock("Local", ZoneId.systemDefault().id, isCenterStyle = true),
            CityClock("Taipei", "Asia/Taipei")
        ),
        centerIndex = 0,
        subtitles = listOf(null, "GMT+08:00"),
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(
    name = "Two Clocks – Dark",
    showBackground = true,
    widthDp = 320, heightDp = 160,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun Preview_WorldClocksTwo_Dark() {
    Preview_WorldClocksTwo_Light()
}
