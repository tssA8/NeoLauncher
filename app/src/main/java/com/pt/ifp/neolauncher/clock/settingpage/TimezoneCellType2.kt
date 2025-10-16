package com.pt.ifp.neolauncher.clock.settingpage

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter

/**
 * 時區設定 row（type_2）— 僅顯示文字（不再可編輯）
 */
@Composable
fun TimezoneCellType2(
    title: String,
    name: String = "Taipei",
    isShow: Boolean,
    onNameChange: (String) -> Unit = {},        // 保留但不使用
    modifier: Modifier = Modifier,
    @DrawableRes outerBackgroundRes: Int? = null,   // 外層背景（如 focus_frame）
    @DrawableRes innerBackgroundRes: Int? = null,   // 內層背景（如 focus_frame）
) {
    AnimatedVisibility(visible = isShow) {

        // 外層容器（54dp）
        val outer = modifier
            .fillMaxWidth()
            .height(54.dp)
            .backgroundDrawable(outerBackgroundRes)

        Box(outer) {
            // 內層（48dp，top 3dp）
            val inner = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.TopStart)
                .padding(top = 3.dp)
                .backgroundDrawable(innerBackgroundRes)
                .let {
                    if (innerBackgroundRes == null)
                        it.clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                    else it
                }

            Column(
                modifier = inner.padding(start = 30.dp, end = 30.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title（白字）
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 顯示名稱（白字 7sp；若為空顯示淡色 placeholder）
                val isEmpty = name.isBlank()
                Text(
                    text = if (isEmpty) "City name" else name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 10.sp,
                        color = if (isEmpty) Color.White.copy(0.6f) else Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp) // 與上方 title 保持一點距離
                )
            }
        }
    }
}

/** 支援 shape/selector/vector/bitmap 的背景繪製 */
@Composable
fun Modifier.backgroundDrawable(@DrawableRes resId: Int?): Modifier {
    if (resId == null) return this
    val ctx = LocalContext.current
    val drawable = remember(resId) { AppCompatResources.getDrawable(ctx, resId) }
    return if (drawable != null) {
        paint(
            painter = rememberDrawablePainter(drawable),
            contentScale = ContentScale.FillBounds
        )
    } else this
}

/* -------------------- Previews -------------------- */

@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_TimezoneCellType2() {
    MaterialTheme {
        Column(Modifier.padding(16.dp)) {
            TimezoneCellType2(
                title = "City display – City 1",
                name = "London",
                isShow = true
            )
            Spacer(Modifier.height(8.dp))
            TimezoneCellType2(
                title = "City display – City 2",
                name = "Taipei",                // 空字串 → 會顯示淡色 placeholder「City name」
                isShow = true
            )
        }
    }
}
