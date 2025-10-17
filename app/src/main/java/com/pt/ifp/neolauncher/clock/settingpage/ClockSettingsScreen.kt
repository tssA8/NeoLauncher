package com.pt.ifp.neolauncher.clock.settingpage

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun ClockSettingsScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier,
    onSelectTimezone: (which: Int) -> Unit,
    onSync: suspend () -> Boolean = { false },
    onClose: () -> Unit
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

    var localIsFloating by remember(isFloating) { mutableStateOf(isFloating) }
    var localTransparency by remember(transparency) { mutableStateOf(transparency) }

    // 預覽縮圖
    val previews = remember(localIsFloating) {
        loadPreviewImages(ctx, isFloating = localIsFloating)
    }
    val pagerState = rememberPagerState(
        initialPage = pageIndex.coerceIn(0, (previews.size - 1).coerceAtLeast(0)),
        pageCount = { previews.size.coerceAtLeast(1) }
    )
    LaunchedEffect(pagerState.currentPage) {
        android.util.Log.d("ClockPager", "currentPage = ${pagerState.currentPage}")
        viewModel.addClockSetting(pageIndex = pagerState.currentPage)
    }

    // 顯示控制 & 文字
    val isShowCity1 by viewModel.isShowCity1.collectAsStateWithLifecycle(false)
    val isShowCity2 by viewModel.isShowCity2.collectAsStateWithLifecycle(false)
    val city1Tz by viewModel.city1Timezone.collectAsStateWithLifecycle("")
    val city2Tz by viewModel.city2Timezone.collectAsStateWithLifecycle("")

    val scope = rememberCoroutineScope()
    var debounce by remember { mutableStateOf<Job?>(null) }

    Box(
        modifier = modifier
            .width(320.dp)
            .height(540.dp)
    ) {
        // 背景
        bgDrawable?.let {
            Image(
                painter = rememberDrawablePainter(it),
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
            // Header：中間標題 + 右上角 X
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                // Title 置中
                Text(
                    text = ctx.getString(R.string.set_clock_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                // 右上角關閉（X）
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            // 內容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp)
            ) {
                // 浮動模式（目前關閉）
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

                AnimatedVisibility(visible = localIsFloating) {
                    SliderCellStyled(
                        title = ctx.getString(R.string.opacity),
                        value = localTransparency,
                        onValueCommit = { viewModel.addClockSetting(transparency = it) },
                        valueRange = 0..100
                    )
                }

                DividerThin()

                // 預覽 + 指示點
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                ) {
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
                            .padding(top = 6.dp),
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

                // 目前時區
                InfoCellType0(
                    title = ctx.getString(R.string.current_timezone),
                    subTitle = viewModel.getCurrentGMTTime(),
                    backgroundRes = R.drawable.focus_frame
                )

                DividerThin()

                // City 1
                TimezoneCellType1(
                    title = ctx.getString(R.string.select_timezone_title) +
                            " " + ctx.getString(R.string.timezone3_city1),
                    subTitle = city1Tz,
                    isShow = isShowCity1,
                    onClick = { onSelectTimezone(CITY1) }
                )

                AnimatedVisibility(visible = isShowCity1) { DividerThin() }

                TimezoneCellType2(
                    title = ctx.getString(R.string.city_display),
                    name = setting.city1DisplayName,
                    isShow = isShowCity1,
                    onNameChange = { new ->
                        debounce?.cancel()
                        debounce = scope.launch {
                            delay(200)
                            viewModel.addClockSetting(city1DisplayName = new)
                        }
                    },
                    innerBackgroundRes = R.drawable.focus_frame
                )

                AnimatedVisibility(visible = isShowCity1) { DividerThin() }

                // City 2
                TimezoneCellType1(
                    title = ctx.getString(R.string.select_timezone_title) +
                            " " + ctx.getString(R.string.timezone3_city2),
                    subTitle = city2Tz,
                    isShow = isShowCity2,
                    onClick = { onSelectTimezone(CITY2) }
                )

                AnimatedVisibility(visible = isShowCity2) { DividerThin() }

                TimezoneCellType2(
                    title = ctx.getString(R.string.city_display),
                    name = setting.city2DisplayName,
                    isShow = isShowCity2,
                    onNameChange = { new ->
                        debounce?.cancel()
                        debounce = scope.launch {
                            delay(200)
                            viewModel.addClockSetting(city2DisplayName = new)
                        }
                    },
                    innerBackgroundRes = R.drawable.focus_frame
                )
            }
        }
    }
}

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
@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockSettingsScreen_Preview() {
    val app = LocalContext.current.applicationContext as Application
    val vm = remember { ClockViewModel(app) }
    MaterialTheme {
        ClockSettingsScreen(
            viewModel = vm,
            onSelectTimezone = {},
            onClose = {}
        )
    }
}
