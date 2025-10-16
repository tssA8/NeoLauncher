import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.ifp.neolauncher.R
import kotlin.math.roundToInt

/**
 * 等價於 seekbar_cell.xml
 *
 * @param title     對應 seekbar_title
 * @param subtitle  對應 seekbar_content（可為 null -> GONE）
 * @param value     目前值（Int）
 * @param onValueCommit 放開滑桿後回傳（等同你的 onValueChangeFinished 裡更新 DB）
 * @param valueRange 範圍，預設 30..100（對齊 XML 的 valueFrom / valueTo）
 * @param step      步進，預設 1（對齊 android:stepSize="1"）
 */
@Composable
fun SliderCell(
    title: String,
    subtitle: String? = null,
    value: Int,
    onValueCommit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 30..100,
    step: Int = 1,
    enabled: Boolean = true,
) {
    // 內部暫存值：拖曳時即時變化，放手才 commit
    var local by remember(value) { mutableStateOf(value.toFloat()) }
    val steps = ((valueRange.last - valueRange.first) / step) - 1
        .coerceAtLeast(0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp) // dp_54
            .padding(start = 30.dp, end = 20.dp), // start=30, end=20 對齊 XML
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左側：兩行文字
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 12.dp), // 與 slider 間距
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = colorResource(R.color.white),
                fontSize = 12.sp,       // sp_12
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    color = colorResource(R.color.bq_grey_4),
                    fontSize = 9.sp,     // sp_9
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 右側：Slider（寬 140dp，高 2dp 由 trackHeight 近似）
        Slider(
            value = local,
            onValueChange = { v -> local = v.coerceIn(valueRange.first.toFloat(), valueRange.last.toFloat()) },
            onValueChangeFinished = { onValueCommit(local.roundToInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = steps,
            enabled = enabled,
            modifier = Modifier
                .width(140.dp) // dp_140
                .height(24.dp) // 給觸控高度，實際軌跡用顏色模擬 2dp
                .align(Alignment.CenterVertically),
            colors = SliderDefaults.colors(
                thumbColor = colorResource(R.color.white),                 // app:thumbColor
                activeTrackColor = colorResource(R.color.white),           // 依需求調整
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),       // #4DFFFFFF
                activeTickColor = Color.Transparent,                       // app:tickColor
                inactiveTickColor = Color.Transparent
            )
        )
    }
}


// 互動預覽：可以拖拉看看 onValueCommit 的效果（這裡直接寫回本地 state）
@Preview(name = "SliderCell – Light", showBackground = true, widthDp = 360)
@Preview(name = "SliderCell – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 360)
@Composable
private fun Preview_SliderCell_Interactive() {
    MaterialTheme {
        var value by remember { mutableStateOf(60) } // 初始值 ≈ xml 的 android:value="60"
        Column(Modifier.padding(16.dp)) {
            SliderCell(
                title = "Opacity",
                subtitle = "Adjust transparency (30–100)",
                value = value,
                onValueCommit = { v -> value = v },  // 放手後回寫
                valueRange = 30..100,
                step = 1
            )
        }
    }
}

// 靜態展示：兩個實例，顯示/隱藏副標題、不同行為
@Preview(name = "SliderCell – Variants (Light)", showBackground = true, widthDp = 360)
@Preview(name = "SliderCell – Variants (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 360)
@Composable
private fun Preview_SliderCell_Variants() {
    MaterialTheme {
        var v1 by remember { mutableStateOf(30) }
        var v2 by remember { mutableStateOf(85) }

        Column(Modifier.padding(16.dp)) {
            SliderCell(
                title = "Opacity",
                subtitle = null,            // 對應 xml 的 GONE
                value = v1,
                onValueCommit = { v1 = it },
                valueRange = 30..100,
                step = 1
            )

            Spacer(Modifier.height(12.dp))

            SliderCell(
                title = "Opacity",
                subtitle = "Fine tune transparency",
                value = v2,
                onValueCommit = { v2 = it },
                valueRange = 30..100,
                step = 1
            )
        }
    }
}