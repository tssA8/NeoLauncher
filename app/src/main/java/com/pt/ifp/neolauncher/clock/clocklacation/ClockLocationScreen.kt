package com.pt.ifp.neolauncher.clock.clocklacation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.ifp.neolauncher.R
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

/** 時區項目資料，沿用你之前的 TimeZoneRow 的語意 */
data class TimeZoneRowUi(
    val id: String,
    val label: String,
    val gmt: String
)

/** 由 tzId 算 GMT+/-HH:mm 的字串 */
private fun gmtDisplay(tzId: String, nowMillis: Long = System.currentTimeMillis()): String {
    val zone = try { ZoneId.of(tzId) } catch (_: Throwable) { ZoneId.systemDefault() }
    val offset = zone.rules.getOffset(Instant.ofEpochMilli(nowMillis)).totalSeconds
    val sign = if (offset >= 0) "+" else "-"
    val p = abs(offset)
    val hh = (p / 3600)
    val mm = (p % 3600) / 60
    return "GMT$sign%02d:%02d".format(hh, mm)
}

/** 將 resources 的 timezone_values / timezone_labels 組成並依 offset 排序 */
@Composable
private fun rememberTimezoneRows(): List<TimeZoneRowUi> {
    val ids = stringArrayResource(id = R.array.timezone_values)
    val labels = stringArrayResource(id = R.array.timezone_labels)
    val now = remember { System.currentTimeMillis() }
    val rows = remember(ids, labels) {
        val count = minOf(ids.size, labels.size)
        (0 until count).map { i ->
            TimeZoneRowUi(
                id = ids[i],
                label = labels[i],
                gmt = gmtDisplay(ids[i], now)
            )
        }.sortedBy { ZoneId.of(it.id).rules.getOffset(Instant.ofEpochMilli(now)).totalSeconds }
    }
    return rows
}

/** 單一列表項 */
@Composable
private fun TimezoneListItem(
    row: TimeZoneRowUi,
    onClick: (TimeZoneRowUi) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick(row) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = row.label,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = row.gmt,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/** 深色搜尋列：Hint 靠左、緊貼放大鏡 */
@Composable
fun DarkSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x80000000))
            .heightIn(min = 48.dp)
            .fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 20.sp
        ),
        // ← 不再 fillMaxWidth / 不置中，預設就是靠左，會緊貼 leadingIcon
        placeholder = {
            Text(
                text = stringResource(R.string.location_search_hint),
                fontSize = 12.sp,
                color = Color.White.copy(0.6f),
                maxLines = 1
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White.copy(0.9f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = Color.White.copy(0.9f),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { onClear() }
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            cursorColor = Color.White
        )
    )
}


/** 頁面主體 */
@Composable
fun ClockLocationScreen(
    titleWhich: Int,                               // CITY1 / CITY2
    onBack: () -> Unit,
    onSelectTimezone: (zoneId: String, label: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val titleText = when (titleWhich) {
        1 -> stringResource(R.string.select_timezone_title) + " " + stringResource(R.string.timezone3_city1)
        2 -> stringResource(R.string.select_timezone_title) + " " + stringResource(R.string.timezone3_city2)
        else -> stringResource(R.string.select_timezone_title)
    }

    val allRows = rememberTimezoneRows()
    var query by remember { mutableStateOf("") }
    val filtered = remember(allRows, query) {
        if (query.isBlank()) allRows
        else allRows.filter { it.label.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .width(320.dp)
            .height(540.dp)
            .background(Color(0xFF2F3456)) // theme_background 近似
    ) {
        // Title bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(41.dp)
                .background(Color(0xFF3A4064)) // theme_title_background 近似
                .padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onBack() }
                    .padding(2.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Clock Location",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )
        }

        // Search
        DarkSearchBar(
            query = query,
            onQueryChange = { query = it },
            onClear = { query = "" },
            onSearch = { /* no-op：即時過濾 */ }
        )

        // List
        Divider(color = Color.White.copy(alpha = 0.08f))
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(filtered) { row ->
                TimezoneListItem(row = row) {
                    onSelectTimezone(it.id, it.label)
                }
                Divider(color = Color.White.copy(alpha = 0.06f))
            }
        }
    }
}

/* ----------------------- 預覽 ----------------------- */

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun Preview_ClockLocationScreen() {
    MaterialTheme {
        ClockLocationScreen(
            titleWhich = 1,
            onBack = {},
            onSelectTimezone = { _, _ -> }
        )
    }
}
