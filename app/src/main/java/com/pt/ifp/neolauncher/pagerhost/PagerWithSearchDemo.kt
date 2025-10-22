package com.pt.ifp.neolauncher.pagerhost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pt.ifp.neolauncher.R
import com.pt.ifp.neolauncher.appgrid.FavoritesPickerHost
import com.pt.ifp.neolauncher.note.NoteSharedViewModel
import com.pt.ifp.neolauncher.note.NoteWidget
import com.pt.ifp.neolauncher.searchbarcomponentView.GoogleSearchBarWithHistory

// PagerWithSearchDemo.kt
@Composable
fun PagerWithSearchDemo(
    showHistoryState: MutableState<Boolean>,   // ⬅️ 從外部帶進來
    onOpenEditor: () -> Unit                   // 第二頁 NoteWidget 要開編輯器的 callback
) {

    val pages = listOf<@Composable () -> Unit>(
        {
            GoogleSearchBarWithHistory(
                showHistory = showHistoryState.value,
                onDismissHistory = { showHistoryState.value = false },
                onShowHistory = { showHistoryState.value = true },
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
            FavoritesPickerHost(
                modifier = Modifier.fillMaxSize(),
                columns = 6
            )
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

@Composable
fun FavoritesGridHost(modifier: Modifier, columns: Int) {
    TODO("Not yet implemented")
}


@Preview(showBackground = true, widthDp = 360, heightDp = 260)
@Composable
private fun Preview_PagerWithSearchDemo() {
    val showHistory = rememberSaveable { mutableStateOf(true) } // 或 false
    MaterialTheme {
        Surface {
            PagerWithSearchDemo(
                showHistoryState = showHistory,
                onOpenEditor = {} // Preview 不需要真的開視窗
            )
        }
    }
}
