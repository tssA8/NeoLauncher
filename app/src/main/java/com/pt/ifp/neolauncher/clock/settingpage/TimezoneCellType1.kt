package com.pt.ifp.neolauncher.clock.settingpage

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.pt.ifp.neolauncher.R

/**
 * 時區設定 row（type_1）
 * - 外層 54dp，高度內縮 3dp 的 48dp 內層
 * - 左右 30dp padding
 * - 兩行：title / subTitle
 * - 右側 16dp 箭頭
 */
@Composable
fun TimezoneCellType1(
    title: String,
    subTitle: String,
    isShow: Boolean,
    modifier: Modifier = Modifier,
    @DrawableRes outerBackgroundRes: Int? = null,   // 對應外層 focus_frame（可選）
    @DrawableRes innerBackgroundRes: Int? = null,   // 對應內層 focus_frame（可選）
    @DrawableRes arrowRes: Int = R.drawable.svg_btn_nextpage,
    titleColor: Color = Color.White,           // 或 Color.White
    subTitleColor: Color = Color.White, // 或 Color.White
    onClick: (() -> Unit)? = null,
) {
    AnimatedVisibility(visible = isShow) {
        // 外層：RelativeLayout → Box
        val outerBase = modifier
            .fillMaxWidth()
            .height(54.dp)
            .semantics { if (onClick != null) role = Role.Button }

        val outerWithBg = if (outerBackgroundRes != null) {
            outerBase.paint(
                painter = painterResource(id = outerBackgroundRes),
                contentScale = ContentScale.FillBounds
            )
        } else outerBase

        Box(
            modifier = outerWithBg
                .clickable(enabled = onClick != null) { onClick?.invoke() }
        ) {
            // 內層：LinearLayout（高 48dp，top 3dp）
            val innerBase = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.TopStart)
                .padding(top = 3.dp)

            val innerWithBg = if (innerBackgroundRes != null) {
                innerBase.paint(
                    painter = painterResource(id = innerBackgroundRes),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                innerBase
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            }

            Row(
                modifier = innerWithBg
                    .padding(start = 30.dp, end = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 文字區
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subTitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 右側箭頭
                Image(
                    painter = painterResource(id = arrowRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/* -------------------- Previews -------------------- */

@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_TimezoneCellType1() {
    MaterialTheme {
        Column(Modifier.padding(16.dp)) {
            TimezoneCellType1(
                title = "Select timezone – City 1",
                subTitle = "GMT+08:00 Taipei Standard Time",
                isShow = true,
                // 填你自己的焦點背景（若需要雙層都要背景可兩個都傳）
                // outerBackgroundRes = R.drawable.focus_frame,
                // innerBackgroundRes = R.drawable.focus_frame,
                titleColor = Color.White,
                subTitleColor = Color.White.copy(alpha = 0.7f),
                onClick = {}
            )
            Spacer(Modifier.height(8.dp))
            TimezoneCellType1(
                title = "Select timezone – City 2",
                subTitle = "GMT+01:00 Amsterdam",
                isShow = true,
                titleColor = Color.White,
                subTitleColor = Color.White.copy(alpha = 0.7f),
                onClick = {}
            )
        }
    }
}
