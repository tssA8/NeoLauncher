// ComposeHostHelpers.kt
package com.pt.ifp.neolauncher.helper

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.pt.ifp.neolauncher.pagerhost.PagerWithSearchDemo

object ComposeHostHelpers {
    // ComposeHostHelpers.kt（示意）
    fun setPagerHostContent(host: ComposeView, onOpenEditor: () -> Unit) {
        host.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        host.setContent {
            MaterialTheme {
                PagerWithSearchDemo(onOpenEditor = onOpenEditor)
            }
        }
    }
}
