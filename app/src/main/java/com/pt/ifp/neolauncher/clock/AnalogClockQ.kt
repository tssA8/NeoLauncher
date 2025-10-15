package com.pt.ifp.neolauncher.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pt.ifp.neolauncher.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Compose 版類比時鐘。
 * - 每秒更新
 * - 支援自訂時區（timeZoneId），預設跟系統
 * - isCenter 會切不同的背景
 * - 縮放由外層 Modifier 大小決定
 */
@Composable
fun AnalogClockQ(
    modifier: Modifier = Modifier,
    isCenter: Boolean = false,
    timeZoneId: String? = null, // e.g. "Asia/Taipei"，null 代表跟著系統
    @DrawableRes dialRes: Int = R.drawable.widget_timezone3_clock_mark,
    @DrawableRes hourRes: Int = R.drawable.widget_timezone3_hour,
    @DrawableRes minuteRes: Int = R.drawable.widget_timezone3_minute,
    @DrawableRes bgCenterRes: Int = R.drawable.svg_widget_timezone3_clock_center_bg,
    @DrawableRes bgNormalRes: Int = R.drawable.svg_widget_timezone3_clock_bg,
    autoObserveSystemTimeZoneChange: Boolean = true, // 是否偵測系統時區異動
) {
    val context = LocalContext.current

    // 追蹤目前時區
    var currentZone by remember(timeZoneId) {
        mutableStateOf(
            timeZoneId?.let { ZoneId.of(it) } ?: ZoneId.systemDefault()
        )
    }

    // （可選）偵聽系統時區改變
    if (timeZoneId == null && autoObserveSystemTimeZoneChange) {
        SystemTimeZoneObserver(onChanged = {
            currentZone = ZoneId.systemDefault()
        })
    }

    // 時間狀態：每秒對齊更新（與原 View 的 postDelayed 相同語意）
    var now by remember { mutableStateOf(ZonedDateTime.now(currentZone)) }
    LaunchedEffect(currentZone) {
        while (isActive) {
            val n = ZonedDateTime.now(currentZone)
            now = n
            // 對齊下一秒，避免漂移
            val delayMs = 1000L - (System.currentTimeMillis() % 1000L)
            delay(delayMs)
        }
    }

    // 角度：分針/時針
    val minute = now.minute + now.second / 60f
    val hour = (now.hour % 12) + minute / 60f
    val minuteDeg = minute / 60f * 360f
    val hourDeg = hour / 12f * 360f

    // 無障礙 contentDescription（跟原本 updateContentDescription 類似）
    val is24 = DateFormat.is24HourFormat(context)
    val fmt = remember(is24) {
        if (is24) DateTimeFormatter.ofPattern("HH:mm")
        else DateTimeFormatter.ofPattern("hh:mm a")
    }
    val a11y = remember(now, fmt) { now.format(fmt) }

    val dialPainter = painterResource(id = dialRes)
    val hourPainter = painterResource(id = hourRes)
    val minutePainter = painterResource(id = minuteRes)

    val bgPainter = painterResource(id = if (isCenter) bgCenterRes else bgNormalRes)

    Box(
        modifier = modifier
            .paint(
                painter = bgPainter,
                contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
            )
            .semantics { contentDescription = a11y },
        contentAlignment = Alignment.Center
    ) {
        // 錶盤（隨容器縮放）
        Image(
            painter = dialPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        // 時針：以圖片中心為旋轉樞紐
        Image(
            painter = hourPainter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = hourDeg
                }
        )

        // 分針
        Image(
            painter = minutePainter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = minuteDeg
                }
        )
    }
}

/**
 * 用 BroadcastReceiver 監聽 ACTION_TIMEZONE_CHANGED。
 * 僅在 timeZoneId == null（也就是跟系統）時才有意義。
 */
@Composable
private fun SystemTimeZoneObserver(onChanged: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                onChanged()
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    widthDp = 220, heightDp = 220,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AnalogClockQPreview_Dark() {
    Box(Modifier.size(200.dp)) {
        AnalogClockQ(
            isCenter = true,
            timeZoneId = "Asia/Taipei"
        )
    }
}