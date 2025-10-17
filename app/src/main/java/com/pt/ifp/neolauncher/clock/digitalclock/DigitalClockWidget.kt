package com.pt.ifp.neolauncher.clock.digitalclock

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DigitalClockWidget(
    modifier: Modifier = Modifier,
    cityLabel: String? = null,
    zoneId: String = ZoneId.systemDefault().id,
    timeTextSizeSp: Int = 88,
    dayTextSizeSp: Int = 18,
    gapDp: Dp = 6.dp,                 // 中間固定距離
    onClickClock: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val is24h = remember { DateFormat.is24HourFormat(ctx) }
    val locale = remember { Locale.getDefault() }

    val timeFmt = remember(is24h, locale) {
        DateTimeFormatter.ofPattern(if (is24h) "HH:mm" else "hh:mm", locale)
    }
    val dateFmt = remember(locale) {
        DateTimeFormatter.ofPattern("MM/dd  EEEE", locale)
    }

    var now by remember { mutableStateOf(ZonedDateTime.now(ZoneId.of(zoneId))) }
    LaunchedEffect(zoneId) {
        while (true) {
            now = ZonedDateTime.now(ZoneId.of(zoneId))
            delay(1000L - (System.currentTimeMillis() % 1000L))
        }
    }

    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(interactionSource = interaction, indication = null, onClick = onClickClock),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 上方留白（不影響時間/日期間距）
        Spacer(Modifier.weight(0.10f))

        // 時間區塊：內容置底
        Box(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter   // ★ 置底
        ) {
            Text(
                text = timeFmt.format(now),
                color = Color.White,
                fontSize = timeTextSizeSp.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                style = MaterialTheme.typography.displayLarge.copy(
                    shadow = Shadow(Color.Black, Offset(0f, 2f), 2f)
                )
            )
        }

        // 固定小距離（防止因容器高度不同而被拉開）
        Spacer(Modifier.height(gapDp))

        // 日期區塊：內容置頂
        Box(
            modifier = Modifier
                .weight(0.18f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter        // ★ 置頂
        ) {
            val base = dateFmt.format(now)
            val text = if (cityLabel.isNullOrBlank()) base else "$base, $cityLabel"
            Text(
                text = text,
                color = Color.White,
                fontSize = dayTextSizeSp.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyLarge.copy(
                    shadow = Shadow(Color.Black, Offset(0f, 1f), 2f)
                )
            )
        }

        // 下方留白
        Spacer(Modifier.weight(0.10f))
    }
}

/* ---------- Previews ---------- */

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
private fun DigitalClockWidget_Preview() {
    MaterialTheme {
        DigitalClockWidget(
            cityLabel = "Taipei",
            zoneId = ZoneId.systemDefault().id,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
private fun DigitalClockWidget_24h_Preview() {
    MaterialTheme {
        DigitalClockWidget(
            cityLabel = "Amsterdam",
            zoneId = "Europe/Amsterdam",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            timeTextSizeSp = 80,
            dayTextSizeSp = 16
        )
    }
}
