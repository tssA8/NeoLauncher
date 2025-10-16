package com.pt.ifp.neolauncher.clock.settingpage

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.pt.ifp.neolauncher.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val CITY1 = 1
const val CITY2 = 2

/* ===================== 主畫面 ===================== */

@Composable
fun ClockSettingsScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier,
    onSelectTimezone: (which: Int) -> Unit,
    onSync: suspend () -> Boolean = { false },
) {
    val ctx = LocalContext.current
    val bgDrawable = remember {
        AppCompatResources.getDrawable(ctx, R.drawable.theme_widget_settings_background)
    }

    // VM 狀態
    val setting = viewModel.settingClock.collectAsStateWithLifecycle().value
    val isFloating = setting.isFloating
    val transparency = setting.transparency
    val pageIndex = setting.pageIndex

    val showFloating = false

    // 城市名稱雙向綁定 + 防抖
    var city1Text by remember(setting.city1DisplayName) { mutableStateOf(TextFieldValue(setting.city1DisplayName)) }
    var city2Text by remember(setting.city2DisplayName) { mutableStateOf(TextFieldValue(setting.city2DisplayName)) }
    var debounceJob1 by remember { mutableStateOf<Job?>(null) }
    var debounceJob2 by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val debounceMs = 200L

    // 浮動/透明度本地狀態
    var localIsFloating by remember(isFloating) { mutableStateOf(isFloating) }
    var localTransparency by remember(transparency) { mutableStateOf(transparency) }

    // 預覽縮圖（取代 ClockPreviewPager）
    val previews = remember(localIsFloating) {
        loadPreviewImages(ctx, isFloating = localIsFloating)
    }
    val pagerState = rememberPagerState(
        initialPage = pageIndex.coerceIn(0, (previews.size - 1).coerceAtLeast(0)),
        pageCount = { previews.size.coerceAtLeast(1) }
    )
    LaunchedEffect(pagerState.currentPage) {
        viewModel.addClockSetting(pageIndex = pagerState.currentPage)
    }

    // isShow City1/City2
    val isShowCity1 by viewModel.isShowCity1.collectAsStateWithLifecycle(false)
    val isShowCity2 by viewModel.isShowCity2.collectAsStateWithLifecycle(false)

    // 城市時區副標
    val city1Tz by viewModel.city1Timezone.collectAsStateWithLifecycle("")
    val city2Tz by viewModel.city2Timezone.collectAsStateWithLifecycle("")

    Box(
        modifier = modifier
            .width(320.dp)
            .height(540.dp)
    ) {
        // 背景（支援 shape/selector/vector/bitmap）
        if (bgDrawable != null) {
            Image(
                painter = rememberDrawablePainter(bgDrawable),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 0.dp, vertical = 0.dp)
        ) {
            // Title（對齊 XML 高 40dp 的區域）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ctx.getString(R.string.set_clock_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }

            // Scroll 的內容：用 Column 模擬即可（內容高度不大）
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp)
            ) {
                // switcher：sensor_switch_view → SwitchCell
                AnimatedVisibility(visible = showFloating) {
                    SwitchCell(
                        title = ctx.getString(R.string.floating_mode),
                        checked = localIsFloating,
                        onCheckedChange = {
                            localIsFloating = it
                            viewModel.addClockSetting(isFloating = it)
                        }
                    )
                }

                DividerThin()

                // slider：sensor_seekbar_view → SliderCellStyled（僅在浮動時顯示）
                AnimatedVisibility(visible = localIsFloating) {
                    SliderCellStyled(
                        title = ctx.getString(R.string.opacity),
                        value = localTransparency,
                        onValueCommit = { viewModel.addClockSetting(transparency = it) },
                        valueRange = 0..100
                    )
                }

                DividerThin()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                ) {

                    // ViewPager 預覽 + 指示點（用 drawBackground 套 focus_frame）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .drawBackground(R.drawable.focus_frame)
                            .padding(top = 10.dp, bottom = 5.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                        ) { page ->
                            val resId = previews.getOrNull(page)
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (resId != null) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                    val count = previews.size.coerceAtLeast(1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top  = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(count) { i ->
                            val selected = pagerState.currentPage == i
                            Box(
                                modifier = Modifier
                                    .size(if (selected) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .drawBackground(
                                        if (selected) R.drawable.indicator_selected
                                        else R.drawable.indicator_no_selected
                                    )
                            )
                        }
                    }
                }


                DividerThin()

                // type0：目前時區（title + subtitle）
                InfoCellType0(
                    title = ctx.getString(R.string.current_timezone),
                    subTitle = viewModel.getCurrentGMTTime(),
                    backgroundRes = R.drawable.focus_frame
                )

                DividerThin()

                // type1：City1（可見性）
                TimezoneCellType1(
                    title = ctx.getString(R.string.select_timezone_title) +
                            " " + ctx.getString(R.string.timezone3_city1),
                    subTitle = city1Tz,
                    isShow = isShowCity1,
                    onClick = { onSelectTimezone(CITY1) }
                )

                AnimatedVisibility(visible = isShowCity1) { DividerThin() }

                // type2：City1 Name
                TimezoneCellType2(
                    title = ctx.getString(R.string.city_display),
                    name = city1Text.text,
                    isShow = isShowCity1,
                    onNameChange = { new ->
                        city1Text = city1Text.copy(text = new)
                        debounceJob1?.cancel()
                        debounceJob1 = scope.launch {
                            delay(debounceMs)
                            viewModel.addClockSetting(city1DisplayName = new)
                        }
                    },
                    innerBackgroundRes = R.drawable.focus_frame
                )

                AnimatedVisibility(visible = isShowCity1) { DividerThin() }

                // type1：City2
                TimezoneCellType1(
                    title = ctx.getString(R.string.select_timezone_title) +
                            " " + ctx.getString(R.string.timezone3_city2),
                    subTitle = city2Tz,
                    isShow = isShowCity2,
                    onClick = { onSelectTimezone(CITY2) }
                )

                AnimatedVisibility(visible = isShowCity2) { DividerThin() }

                // type2：City2 Name
                TimezoneCellType2(
                    title = ctx.getString(R.string.city_display),
                    name = city2Text.text,
                    isShow = isShowCity2,
                    onNameChange = { new ->
                        city2Text = city2Text.copy(text = new)
                        debounceJob2?.cancel()
                        debounceJob2 = scope.launch {
                            delay(debounceMs)
                            viewModel.addClockSetting(city2DisplayName = new)
                        }
                    },
                    innerBackgroundRes = R.drawable.focus_frame
                )
            }
        }
    }
}

/* ===================== 子元件：type0 / divider ===================== */

@Composable
private fun InfoCellType0(
    title: String,
    subTitle: String,
    backgroundRes: Int? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .drawBackground(backgroundRes)
            .padding(horizontal = 30.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 1
        )
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun DividerThin() {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.White.copy(alpha = 0.08f),
        thickness = 1.dp
    )
}

/* ===================== helper：載入預覽縮圖 ===================== */

private fun loadPreviewImages(
    context: Context,
    isFloating: Boolean
): List<Int> {
    @ArrayRes val arrayRes = if (isFloating)
        R.array.previewImages_floating
    else
        R.array.previewImages

    val ta = context.resources.obtainTypedArray(arrayRes)
    return try {
        val list = ArrayList<Int>(ta.length())
        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id != 0) list.add(id)
        }
        list
    } finally {
        ta.recycle()
    }
}

/* ============== 可畫任意 Drawable 的背景（shape/selector/vector/bitmap） ============== */

@Composable
fun Modifier.drawBackground(@DrawableRes resId: Int?): Modifier {
    if (resId == null) return this
    val ctx = LocalContext.current
    val drawable = remember(resId) { AppCompatResources.getDrawable(ctx, resId) }
    return if (drawable != null) {
        this.paint(
            painter = rememberDrawablePainter(drawable),
            contentScale = ContentScale.FillBounds
        )
    } else this
}

/* ===================== Preview ===================== */

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockSettingsScreen_Preview() {
    val app = LocalContext.current.applicationContext as Application
    val vm = remember { ClockViewModel(app) }
    MaterialTheme {
        ClockSettingsScreen(
            viewModel = vm,
            onSelectTimezone = {}
        )
    }
}
