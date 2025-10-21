// ComposeHostHelpers.kt
package com.pt.ifp.neolauncher.helper

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.pt.ifp.neolauncher.pagerhost.PagerWithSearchDemo

object ComposeHostHelpers {
    // ComposeHostHelpers.kt（示意）
    fun setPagerHostContent(
        view: ComposeView,
        showHistoryState: MutableState<Boolean>,
        onOpenEditor: () -> Unit
    ) {
        view.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        view.setContent {
            MaterialTheme {
                PagerWithSearchDemo(
                    showHistoryState = showHistoryState,
                    onOpenEditor = onOpenEditor
                )
            }
        }
    }

}
