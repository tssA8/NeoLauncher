// PagerWithSearchDemo.kt（整段可直接貼上取代）
package com.pt.ifp.neolauncher.pagerhost

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pt.ifp.neolauncher.R
import com.pt.ifp.neolauncher.appgrid.FavoritesPickerHost
import com.pt.ifp.neolauncher.note.NoteSharedViewModel
import com.pt.ifp.neolauncher.note.NoteWidget
import com.pt.ifp.neolauncher.searchbarcomponentView.GoogleSearchBarWithHistory
import com.pt.ifp.neolauncher.searchbarcomponentView.SearchWidgetSlot

private const val GSA_PKG = "com.google.android.googlequicksearchbox"
private const val GSA_PROVIDER = "com.google.android.googlequicksearchbox.SearchWidgetProvider"

@Composable
fun PagerWithSearchDemo(
    showHistoryState: MutableState<Boolean>,
    onOpenEditor: () -> Unit,
    noteVm: NoteSharedViewModel,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    searchWidgetId: Int
) {
    val providerKey =
        "com.google.android.googlequicksearchbox/com.google.android.googlequicksearchbox.SearchWidgetProvider"

    var savedWidgetId by rememberSaveable(providerKey) { mutableStateOf(searchWidgetId) }

    val isWidgetReady = remember(appWidgetManager, savedWidgetId) {
        savedWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID &&
                appWidgetManager.getAppWidgetInfo(savedWidgetId) != null
    }

    val pages = remember(appWidgetHost, appWidgetManager, savedWidgetId, noteVm, showHistoryState) {
        listOf<@Composable () -> Unit>(
            {
                // 第一頁：Google 搜尋 Widget（或 fallback）
                Column(Modifier.fillMaxWidth()) {
                    val ctx = LocalContext.current

                    val hasGsaProvider = remember(ctx, appWidgetManager) {
                        try {
                            // 套件存在？
                            ctx.packageManager.getApplicationInfo(GSA_PKG, 0)
                            // Provider 已安裝？
                            appWidgetManager.installedProviders.any { p ->
                                p.provider.packageName == GSA_PKG &&
                                        p.provider.className == GSA_PROVIDER
                            }
                        } catch (_: Exception) {
                            false
                        }
                    }

                    Log.d("PagerWithSearchDemo", "isWidgetReady=$isWidgetReady, hasGsaProvider=$hasGsaProvider")

                    if (hasGsaProvider) {
                        Log.d("PagerWithSearchDemo", " using google search widget providers ")
                        SearchWidgetSlot(
                            host = appWidgetHost,
                            mgr = appWidgetManager,
                            widgetId = savedWidgetId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .heightIn(min = 80.dp)
                        )
                    } else {
                        Log.d("PagerWithSearchDemo", " using custom compose search -GoogleSearchBarWithHistory")
                        GoogleSearchBarWithHistory(
                            showHistory = showHistoryState.value,
                            onDismissHistory = { showHistoryState.value = false },
                            onShowHistory = { showHistoryState.value = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            },
            {
                // 第二頁：Note
                Box(Modifier.fillMaxSize()) {
                    val defaultSizeSp = dimensionResource(id = R.dimen.note_text_size).value
                    LaunchedEffect(Unit) {
                        if (noteVm.sizeSp <= 0f) noteVm.update(sizeSp = defaultSizeSp)
                    }
                    NoteWidget(
                        text = noteVm.text,
                        fontSizeSp = noteVm.sizeSp,
                        onClick = { onOpenEditor() }
                    )
                }
            },
            {
                // 第三頁：收藏 App
                FavoritesPickerHost(
                    modifier = Modifier.fillMaxSize(),
                    columns = 6
                )
            }
        )
    }

    PagerHost(
        pages = pages,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        indicatorActiveColor = Color.White,
        indicatorInactiveColor = Color.White.copy(alpha = 0.4f),
        indicatorSize = 8,
        indicatorSpacing = 8
    )
}

@Preview(
    name = "PagerWithSearchDemo (Light)",
    showBackground = true,
    widthDp = 900,
    heightDp = 260
)
@Composable
fun PagerWithSearchDemoPreview_Light() {
    PagerWithSearchDemoPreviewCore()
}

@Preview(
    name = "PagerWithSearchDemo (Dark)",
    showBackground = true,
    widthDp = 900,
    heightDp = 260,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PagerWithSearchDemoPreview_Dark() {
    PagerWithSearchDemoPreviewCore()
}

@Composable
private fun PagerWithSearchDemoPreviewCore() {
    val ctx = LocalContext.current

    // AppWidgetHost / AppWidgetManager：預覽環境下也能建立
    val host = remember { AppWidgetHost(ctx, /*HOST_ID*/ 1024) }
    val mgr = remember { AppWidgetManager.getInstance(ctx) }

    // Note VM（若你的 VM 需要參數，請改成預設假資料的 factory 或 Fake VM）
    val noteVm = remember {
        // 替代作法（若無無參數建構子）：
        // object : NoteSharedViewModel(/*...*/){ ... }  // 或建立一個 FakeNoteSharedViewModel
        NoteSharedViewModel().apply {
            // 預覽用假資料
            update(text = "Tap to edit note…", sizeSp = 18f)
        }
    }

    val showHistory = remember { mutableStateOf(false) }

    // 使用 INVALID_APPWIDGET_ID 讓第一頁走 fallback（GoogleSearchBarWithHistory），
    // 也可以改成實際綁定的 id 來預覽 widget 版面。
    PagerWithSearchDemo(
        showHistoryState = showHistory,
        onOpenEditor = { /* no-op for preview */ },
        noteVm = noteVm,
        appWidgetHost = host,
        appWidgetManager = mgr,
        searchWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    )
}
