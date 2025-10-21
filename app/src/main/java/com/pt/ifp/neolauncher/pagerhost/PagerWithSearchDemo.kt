package com.pt.ifp.neolauncher.pagerhost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pt.ifp.neolauncher.R
import com.pt.ifp.neolauncher.note.NoteSharedViewModel
import com.pt.ifp.neolauncher.note.NoteWidget
import com.pt.ifp.neolauncher.searchbarcomponentView.GoogleSearchBarWithHistory

// PagerWithSearchDemo.kt
@Composable
fun PagerWithSearchDemo(
    onOpenEditor: () -> Unit = {}   // ← 新增參數
) {
    var showHistory by rememberSaveable { mutableStateOf(false) }

    val pages = listOf<@Composable () -> Unit>(
        {
            GoogleSearchBarWithHistory(
                showHistory = showHistory,
                onDismissHistory = { showHistory = false },
                onShowHistory = { showHistory = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        },
        {
            // 第 2 頁：NoteWidget
            Box(Modifier.fillMaxSize()) {
                val vm: NoteSharedViewModel = viewModel()
                val defaultSizeSp = dimensionResource(id = R.dimen.note_text_size).value
                LaunchedEffect(Unit) {
                    if (vm.sizeSp <= 0f) vm.update(sizeSp = defaultSizeSp)
                }
                NoteWidget(
                    text = vm.text,
                    fontSizeSp = vm.sizeSp,
                    onClick = { onOpenEditor() } // ← 直接呼叫外部 callback
                )
            }
        },
        {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Third Page")
            }
        }
    )

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


@Preview(showBackground = true, widthDp = 360, heightDp = 260)
@Composable
private fun Preview_PagerWithSearchDemo() {
    MaterialTheme {
        Surface {
            PagerWithSearchDemo()
        }
    }
}
