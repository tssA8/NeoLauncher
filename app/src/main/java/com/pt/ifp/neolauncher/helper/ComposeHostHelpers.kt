package com.pt.ifp.neolauncher.helper

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pt.ifp.neolauncher.R
import com.pt.ifp.neolauncher.clock.nav.ClockSettingsContainer
import com.pt.ifp.neolauncher.clock.settingpage.ClockViewModel
import com.pt.ifp.neolauncher.clock.threeclocks.WorldClocksAutoFromSettings
import com.pt.ifp.neolauncher.note.NoteEditorDialog
import com.pt.ifp.neolauncher.note.NoteSharedViewModel
import com.pt.ifp.neolauncher.pagerhost.PagerWithSearchDemo
import com.pt.ifp.neolauncher.widgetbox.RebindingAppWidgetHost


object ComposeHostHelpers {

    /** 把 AppWidgetHostView 包成 Compose 可用的 Composable */
    @Composable
    private fun SearchAppWidget(
        appWidgetHost: AppWidgetHost,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        modifier: Modifier = Modifier
    ) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return
        AndroidView(
            modifier = modifier,
            factory = { context ->
                appWidgetHost.createView(context, appWidgetId, info).apply {
                    setAppWidget(appWidgetId, info)
                    isClickable = true
                    isFocusable = false
                }
            }
        )
    }

    // ComposeHostHelpers.kt
    @JvmStatic
    fun setPagerHostContent(
        view: ComposeView,
        noteVm: NoteSharedViewModel,
        initialShowHistory: Boolean,
        onOpenEditor: Runnable,
        appWidgetHost: RebindingAppWidgetHost,
        appWidgetManager: AppWidgetManager,
        searchWidgetId: Int
    ) {
        view.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        view.setContent {
            MaterialTheme {
                val showHistoryState = rememberSaveable { mutableStateOf(initialShowHistory) }
                PagerWithSearchDemo(
                    showHistoryState = showHistoryState,
                    onOpenEditor = { onOpenEditor.run() },
                    noteVm = noteVm,
                    appWidgetHost = appWidgetHost,
                    appWidgetManager = appWidgetManager,
                    searchWidgetId = searchWidgetId
                )
            }
        }
    }


    @JvmStatic
    fun setWorldClocksContent(
        composeView: ComposeView,
        viewModel: ClockViewModel,
        onClickClock: () -> Unit
    ) {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindow
        )
        composeView.setContent {
            // 不包 MaterialTheme
            WorldClocksAutoFromSettings(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                onClickClock = onClickClock
            )
        }
    }

    @JvmStatic
    fun setClockSettingsContent(
        composeView: ComposeView,
        viewModel: ClockViewModel,
        onDismiss: () -> Unit
    ) {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindow
        )
        composeView.setContent {
            // 不包 MaterialTheme
            ClockSettingsContainer(
                viewModel = viewModel,
                onDismiss = onDismiss
            )
        }
    }


    @JvmStatic
    fun setNoteEditorContent(
        view: ComposeView,
        vm: NoteSharedViewModel,
        onDismiss: Runnable    // Java 這邊傳進來：關閉視窗要做的事
    ) {
        view.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        view.setContent {
            MaterialTheme {
                val defaultSizeSp = dimensionResource(id = R.dimen.note_text_size).value
                val presetText = vm.text
                val presetSize = if (vm.sizeSp > 0f) vm.sizeSp else defaultSizeSp

                NoteEditorDialog(
                    initialText = presetText,
                    initialSizeSp = presetSize,
                    onSave = { text, sizeSp ->
                        vm.update(text = text, sizeSp = sizeSp)
                        onDismiss.run() // 關閉
                    },
                    onCancel = {
                        onDismiss.run() // 關閉
                    },
                    size = DpSize(426.dp, 526.dp),
                    useDialogWindow = false,
                    scale = 1.4f
                )
            }
        }
    }
}
