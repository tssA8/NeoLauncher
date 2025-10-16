package com.pt.ifp.neolauncher.clock.settingpage

import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.pt.ifp.neolauncher.R

@Composable
fun SwitchCell(
    title: String,
    subtitle: String? = null,                  // 對應 switch_content（可為 GONE）
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    // 焦點框（@drawable/focus_frame）若你已有，也可改成 painterResource
    showFocusFrame: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val bgColor = Color.Transparent
    val focusStroke = if (showFocusFrame) 1.dp else 0.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp) // @dimen/dp_54
            .padding(horizontal = 0.dp)
            .clip(RoundedCornerShape(8.dp)) // 讓 focus 邊框有圓角，接近原先背景
            .border(focusStroke, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .background(bgColor)
            // descendantFocusability="blocksDescendants" → 點整列就切換（Switch 自己也能點）
            .clickable(
                interactionSource = interaction,
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(start = 30.dp, end = 30.dp), // @dimen/dp_30
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左側：兩行文字（權重 1）
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                // android:textColor="@color/white" & textSize="@dimen/sp_12"
                color = colorResource(id = R.color.white),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp) // 讓視覺更貼近「置中」
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    color = colorResource(id = R.color.login_view_hint_color),
                    fontSize = 9.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
            }
        }

        // 右側：Switch（對齊父容器上下置中、靠右）
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.wrapContentSize(),
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(R.color.uikit_thumb_track_on),
                checkedTrackColor = colorResource(R.color.white),   // 依你專案顏色調
                uncheckedThumbColor = colorResource(R.color.uikit_thumb_track_off),
                uncheckedTrackColor = colorResource(R.color.white)        // 依你專案顏色調
            )
        )
    }
}


// ------- Legacy：用 SwitchCompat + style（需要 appcompat & 你的 style） -------
@Composable
fun LegacyStyledSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val themed = ContextThemeWrapper(context, R.style.uni_style_switch)
            SwitchCompat(themed, /* attrs = */ null, /* defStyleAttr = */ 0).apply {
                isChecked = checked
                setOnCheckedChangeListener { _, v -> onCheckedChange(v) }
                isClickable = true
                isFocusable = false
            }
        },
        update = { view ->
            if (view.isChecked != checked) view.isChecked = checked
        }
    )
}

@Composable
fun SwitchCellLegacy(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            .clickable { onCheckedChange(!checked) }
            .padding(start = 30.dp, end = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            if (!subtitle.isNullOrEmpty()) {
                Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        LegacyStyledSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/* -------------------- Previews -------------------- */

@Preview(name = "Compose Switch – Light", showBackground = true, widthDp = 360)
@Preview(name = "Compose Switch – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 360)
@Composable
private fun Preview_SwitchCell_Compose() {
    MaterialTheme {
        var on by remember { mutableStateOf(true) }
        Column(Modifier.padding(16.dp)) {
            SwitchCell(
                title = "Floating mode",
                subtitle = "Enable floating clock",
                checked = on,
                onCheckedChange = { on = it }
            )
        }
    }
}

@Preview(name = "Legacy SwitchCompat + Style – Light", showBackground = true, widthDp = 360)
@Preview(name = "Legacy SwitchCompat + Style – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 360)
@Composable
private fun Preview_SwitchCell_Legacy() {
    MaterialTheme {
        var on by remember { mutableStateOf(false) }
        Column(Modifier.padding(16.dp)) {
            SwitchCellLegacy(
                title = "Floating mode",
                subtitle = "Enable floating clock",
                checked = on,
                onCheckedChange = { on = it }
            )
        }
    }
}