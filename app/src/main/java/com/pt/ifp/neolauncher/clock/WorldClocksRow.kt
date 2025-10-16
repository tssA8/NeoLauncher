package com.pt.ifp.neolauncher.clock

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pt.ifp.neolauncher.R
import com.pt.ifp.neolauncher.clock.settingpage.ClockViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs


data class CityClock(
    val city: String,
    val zoneId: String,
    val isCenterStyle: Boolean = false    // 預設不是中間樣式
)


@Composable
fun WorldClocksRow(
    cities: List<CityClock>,                 // 0=City1, 1=Local, 2=City2
    modifier: Modifier = Modifier,
    clockSize: Dp = 96.dp,
    horizontalPadding: Dp = 1.dp,
    verticalSpacing: Dp = 8.dp,
    centerIndex: Int = 1,                    // 中間固定為 Local/Now
    subtitles: List<String?>? = null,        // ★ 新增：每顆時鐘的副標（外部傳入）
    onClickClock: () -> Unit = {}
) {
    val colorNormal  = colorResource(R.color.recommend_row_normal_color)
    val tCityColor by animateColorAsState(colorNormal)
    val tTimeColor by animateColorAsState(colorResource(R.color.bq_grey_4))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        cities.forEachIndexed { idx, item ->
            val interaction = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = verticalSpacing)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClickClock),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(clockSize)) {
                    AnalogClockQ(
                        modifier = Modifier.fillMaxSize(),
                        isCenter = (idx == centerIndex) || item.isCenterStyle,
                        timeZoneId = item.zoneId
                    )
                }

                Text(
                    text = item.city,
                    color = tCityColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 6.dp)
                )

                // ★ 副標：中間固定 "Now"，其他用外部傳進來的字串
                val subtitle = if (idx == centerIndex) "Now"
                else subtitles?.getOrNull(idx).orEmpty()

                val boldNow = idx == centerIndex
                Text(
                    text = subtitle,
                    style = if (boldNow)
                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.bodyMedium,
                    color = tTimeColor,
                    fontSize = 10.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}


@Composable
fun WorldClocksFromSettings(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier,
    onClickClock: () -> Unit = {},
) {
    // 取設定
    val setting = viewModel.settingClock.collectAsStateWithLifecycle().value
    val city1Name = setting.city1DisplayName.ifBlank { "City 1" }
    val city2Name = setting.city2DisplayName.ifBlank { "City 2" }
    val city1ZoneId = (setting.city1Id ?: "").ifBlank { "Asia/Taipei" }
    val city2ZoneId = (setting.city2Id ?: "").ifBlank { "America/New_York" }

    // 已在 VM 算好的副標字串（例如 "GMT+08:00 台北標準時間"）
    val city1Tz by viewModel.city1Timezone.collectAsStateWithLifecycle("")
    val city2Tz by viewModel.city2Timezone.collectAsStateWithLifecycle("")

    WorldClocksRow(
        cities = listOf(
            CityClock(city1Name, city1ZoneId),                                  // 左：City 1
            CityClock("Local", ZoneId.systemDefault().id, isCenterStyle = true), // 中：本地 Now
            CityClock(city2Name, city2ZoneId)                                    // 右：City 2
        ),
        centerIndex = 1,                                                         // 中間固定 Now
        subtitles = listOf(city1Tz, null, city2Tz),                              // 左/右用 VM 字串
        onClickClock = onClickClock,
        modifier = modifier
    )
}



/** 範例：把中間那顆設成 Local（Now） */
@Composable
fun WorldClocksTripleDefault(modifier: Modifier = Modifier) {
    WorldClocksRow(
        modifier = modifier,
        centerIndex = 1, // ★ 中間為基準
        cities = listOf(
            CityClock("London",   "Europe/London"),
            CityClock("Local",    ZoneId.systemDefault().id, isCenterStyle = true), // ★ 中間：Now
            CityClock("Brasilia", "America/Sao_Paulo")
        )
    )
}


/* -------------------- Previews -------------------- */

@Preview(name = "World Clocks – Light", showBackground = true, widthDp = 380, heightDp = 180)
@Composable
private fun Preview_WorldClocks_Light() {
    WorldClocksTripleDefault(
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(
    name = "World Clocks – Dark",
    showBackground = true,
    widthDp = 380, heightDp = 180,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun Preview_WorldClocks_Dark() {
    WorldClocksTripleDefault(
        modifier = Modifier.fillMaxWidth()
    )
}
