package com.pt.ifp.neolauncher.clock

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.pt.ifp.neolauncher.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs


data class CityClock(
    val city: String,
    val zoneId: String,
    val isCenterStyle: Boolean = true   // 是否用中間版背景（可依你的資源調整）
)

/**
 * 三個世界時鐘一排排版（也可傳任意數量，預設三個）
 */
@Composable
fun WorldClocksRow(
    cities: List<CityClock>,
    modifier: Modifier = Modifier,
    clockSize: Dp = 96.dp,
    horizontalPadding: Dp = 1.dp,
    verticalSpacing: Dp = 8.dp,
) {

    val colorNormal  = colorResource(R.color.recommend_row_normal_color)
    val tCityColor by animateColorAsState(colorNormal)
    val tTimeColor by animateColorAsState(colorResource(R.color.bq_grey_4))

    // 1) 共用一個「對齊秒」的現在時間（用 Instant 對齊所有時區運算）
    var nowUtc by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            nowUtc = Instant.now()
            val delayMs = 1000L - (System.currentTimeMillis() % 1000L)
            delay(delayMs)
        }
    }

    // 系統時區的當下時間
    val systemZone = remember { ZoneId.systemDefault() }
    val systemZdt = remember(nowUtc) { ZonedDateTime.ofInstant(nowUtc, systemZone) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        cities.forEach { item ->
            val zone = remember(item.zoneId) { ZoneId.of(item.zoneId) }
            val cityZdt = remember(nowUtc, zone) { ZonedDateTime.ofInstant(nowUtc, zone) }

            // 2) 計算相對時差（以當下 offset 計）
            val sysOffset = systemZdt.offset.totalSeconds
            val cityOffset = cityZdt.offset.totalSeconds
            val deltaSec = cityOffset - sysOffset

            // 3) 產生副標：Now / Today, +5 hours / Yesterday, +6 hours / Tomorrow, -3 hours / …
            val subtitle = remember(systemZdt, cityZdt, deltaSec) {
                val dayDelta = cityZdt.toLocalDate().toEpochDay() - systemZdt.toLocalDate().toEpochDay()
                val dayWord = when {
                    dayDelta < 0 -> "Yesterday"
                    dayDelta > 0 -> "Tomorrow"
                    else -> "Today"
                }

                if (deltaSec == 0) {
                    "Now"
                } else {
                    val sign = if (deltaSec > 0) "+" else "-"
                    val absSec = abs(deltaSec)
                    val h = absSec / 3600
                    val m = (absSec % 3600) / 60
                    val offsetText = if (m == 0) "$sign${h} hours" else "$sign${h}h${m}m"
                    "$dayWord, $offsetText"
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = verticalSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 4) 時鐘
                Box(Modifier.size(clockSize)) {
                    AnalogClockQ(
                        modifier = Modifier.fillMaxSize(),
                        isCenter = item.isCenterStyle,
                        timeZoneId = item.zoneId
                    )
                }
                // 5) 城市名稱
                Text(
                    text = item.city,
                    color = tCityColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 6.dp)
                )

                // 6) 副標：0 小時顯示粗體「Now」，其餘一般字
                val boldNow = deltaSec == 0
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

/** 方便呼叫：預設 London / New York / Amsterdam */
@Composable
fun WorldClocksTripleDefault(
    modifier: Modifier = Modifier,
) {
    WorldClocksRow(
        modifier = modifier,
        cities = listOf(
            CityClock("London", "Europe/London"),
            CityClock("New York", "America/New_York"),
            CityClock("Amsterdam", "Europe/Amsterdam")
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
