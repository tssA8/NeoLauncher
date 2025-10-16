package com.pt.ifp.neolauncher.clock.settingpage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxWithConstraints


@Composable
fun SliderCellStyled(
    title: String,
    value: Int,
    onValueCommit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 30..100,
    step: Int = 1,
    showLabel: Boolean = true,
    primaryHex: Color = Color(0xFF19BABA),   // active 軌道色
    labelBg: Color = Color(0x80000000),      // label 圓底色(50%黑)
) {
    var local by remember(value) { mutableStateOf(value.toFloat()) }
    val steps = ((valueRange.last - valueRange.first) / step) - 1
        .coerceAtLeast(0)

    // 共用參數（軌道與 thumb 需一致）
    val trackHeight = 2.dp
    val sidePadding = 16.dp
    val thumbSize = 18.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 疊放：自畫軌道(2dp) + Slider(隱藏內建軌道/拇指) + 自訂圓形thumb + (可選)圓形label
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {

            // 0) 自畫 2dp 軌道（底灰＋主色前景）
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val fraction = ((local - valueRange.first) /
                        (valueRange.last - valueRange.first).toFloat()).coerceIn(0f, 1f)

                // 軌道總寬（扣掉左右 padding）
                val trackWidth = maxWidth - sidePadding * 2

                // 依進度的前景寬度
                val activeWidth = trackWidth * fraction

                // 底軌（灰）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = sidePadding)
                        .height(trackHeight)
                        .background(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )

                // 前景軌（主色）
                Box(
                    modifier = Modifier
                        .padding(start = sidePadding)   // 只從左側開始畫
                        .width(activeWidth)             // ★ 用實際寬度，不用 fillMaxWidth(fraction)
                        .height(trackHeight)
                        .background(primaryHex, CircleShape)
                )
            }


            // 1) 真正的 Slider（軌道/拇指都設透明，只負責手勢與數值）
            Slider(
                value = local,
                onValueChange = {
                    local = it.coerceIn(
                        valueRange.first.toFloat(),
                        valueRange.last.toFloat()
                    )
                },
                onValueChangeFinished = { onValueCommit(local.roundToInt()) },
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                steps = steps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp), // 外框高度，與點擊手勢相關
                colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )

            // 2) 自訂「圓形 thumb」：精準跟隨進度
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val density = LocalDensity.current
                val fraction = ((local - valueRange.first) /
                        (valueRange.last - valueRange.first).toFloat()).coerceIn(0f, 1f)

                val widthPx = with(density) { maxWidth.toPx() }
                val sidePx = with(density) { sidePadding.toPx() }
                val thumbPx = with(density) { thumbSize.toPx() }
                val trackWidthPx = (widthPx - sidePx * 2).coerceAtLeast(0f)
                val xPx = (sidePx + trackWidthPx * fraction - thumbPx / 2).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xPx, 0) }     // 垂直置中不需偏移
                        .size(thumbSize)
                        .background(Color.White, CircleShape)
                )
            }

            // 3) 可選：拇指上方圓形數字 label
            if (showLabel) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = sidePadding - 8.dp) // 與自畫軌道略對齊
                ) {
                    val bubbleSize = 28.dp
                    val topGap = 8.dp
                    val density = LocalDensity.current

                    val fraction = ((local - valueRange.first) /
                            (valueRange.last - valueRange.first).toFloat()).coerceIn(0f, 1f)

                    val widthPx = with(density) { maxWidth.toPx() }
                    val sidePx = with(density) { (sidePadding).toPx() }
                    val bubblePx = with(density) { bubbleSize.toPx() }
                    val topGapPx = with(density) { topGap.toPx() }
                    val trackWidthPx = (widthPx - sidePx * 2).coerceAtLeast(0f)
                    val xPx = (sidePx + trackWidthPx * fraction - bubblePx / 2).roundToInt()
                    val yPx = (-bubblePx - topGapPx).roundToInt()

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(xPx, yPx) }
                            .size(bubbleSize)
                            .background(labelBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = local.roundToInt().toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Compose Slider Styled", showBackground = true, widthDp = 360)
@Preview(
    name = "Compose Slider Styled – Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 360
)
@Composable
private fun Preview_SliderCellStyled() {
    MaterialTheme {
        var v by remember { mutableStateOf(60) }
        Column(Modifier.padding(16.dp)) {
            SliderCellStyled(
                title = "Opacity",
                value = v,
                onValueCommit = { v = it },
                valueRange = 30..100,
                primaryHex = Color(0xFF19BABA),
                labelBg = Color(0x80000000)
            )
        }
    }
}
