package com.pt.ifp.neolauncher.clock.settingpage

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter

/**
 * 時區設定 row（type_2）
 * - 外層 54dp，內層 48dp（top 3dp）
 * - 左右 30dp padding
 * - 上：title（白字），下：可編輯單行 name（白字）
 * - 無箭頭
 */
@Composable
fun TimezoneCellType2(
    title: String,
    name: String,
    isShow: Boolean,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes outerBackgroundRes: Int? = null,   // 外層背景（如 focus_frame）
    @DrawableRes innerBackgroundRes: Int? = null,   // 內層背景（如 focus_frame）
) {
    AnimatedVisibility(visible = isShow) {
        val focusManager = LocalFocusManager.current

        // 外層容器（54dp）
        val outerBase = modifier
            .fillMaxWidth()
            .height(54.dp)
            .semantics { role = Role.Button } // 輔助工具可讀為可互動列

        val outerWithBg = if (outerBackgroundRes != null) {
            outerBase.paint(
                painter = painterResource(id = outerBackgroundRes),
                contentScale = ContentScale.FillBounds
            )
        } else outerBase

        Box(
            modifier = outerWithBg
                .clickable {
                    // 點整列可把焦點移給 TextField（實務上會自動處理，這裡保留語意）
                    // focusManager.moveFocus(FocusDirection.Next)
                }
        ) {
            // 內層（48dp，top 3dp）
            val innerBase = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.TopStart)
                .padding(top = 3.dp)

            val innerWithBg = if (innerBackgroundRes != null) {
                innerBase.backgroundDrawable(innerBackgroundRes)
            } else {
                innerBase
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            }


            Column(
                modifier = innerWithBg
                    .padding(start = 30.dp, end = 30.dp),
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

                // 單行可編輯 name（白字、無底線/框線）
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

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
    var name by remember { mutableStateOf("Taipei") }
    MaterialTheme {
        Column(Modifier.padding(16.dp)) {
            TimezoneCellType2(
                title = "City display – City 1",
                name = name,
                isShow = true,
                onNameChange = { name = it },
                // 如果要沿用 XML 的焦點框背景，可帶入：
                // outerBackgroundRes = R.drawable.focus_frame,
                // innerBackgroundRes = R.drawable.focus_frame
            )
            Spacer(Modifier.height(8.dp))
            TimezoneCellType2(
                title = "City display – City 2",
                name = "New York",
                isShow = true,
                onNameChange = {},
            )
        }
    }
}
