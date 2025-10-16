package com.pt.ifp.neolauncher.clock.twoclocks

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pt.ifp.neolauncher.clock.settingpage.ClockViewModel
import com.pt.ifp.neolauncher.clock.threeclocks.CityClock
import com.pt.ifp.neolauncher.clock.threeclocks.WorldClocksRow
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
    val setting = viewModel.settingClock.collectAsStateWithLifecycle().value
    val city1Name   = setting.city1DisplayName.ifBlank { "City 1" }
    val city1ZoneId = (setting.city1Id ?: "").ifBlank { "Asia/Taipei" }

    // Already computed in VM (e.g., "GMT+08:00 Taipei Standard Time")
    val city1Tz by viewModel.city1Timezone.collectAsStateWithLifecycle("")

    WorldClocksRow(
        cities = listOf(
            CityClock("Local", ZoneId.systemDefault().id, isCenterStyle = true), // LEFT: Now
            CityClock(city1Name, city1ZoneId)                                    // RIGHT: City1
        ),
        centerIndex = 0,                                // ★ make LEFT the “Now” reference
        subtitles = listOf(null, city1Tz),             // left uses "Now" internally; right shows city1Tz
        onClickClock = onClickClock,
        modifier = modifier,
        clockSize = clockSize
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
