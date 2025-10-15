package com.pt.ifp.neolauncher.searchbarcomponentView

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.ifp.neolauncher.R

@Composable
fun GoogleSearchBarWithHistory(
    modifier: Modifier = Modifier,
    showHistory: Boolean,
    onDismissHistory: () -> Unit,
    onShowHistory: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { com.pt.ifp.neolauncher.preference.Preferences(context) }

    var query by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(prefs.searchHistory) }

    fun launchGoogleSearch(q: String) {
        if (q.isBlank()) return
        val url = "https://www.google.com/search?q=${Uri.encode(q)}"
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        history = (listOf(q) + history).distinct().take(10)
        prefs.saveSearchHistory(history) // ← 存到 SharedPreferences
        onDismissHistory()
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = modifier.fillMaxWidth()) {
            // 🔍 Search Bar
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(2.dp, RoundedCornerShape(50))
                        .background(Color.White, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .heightIn(min = 40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.google_logo_icon),
                        contentDescription = "Google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    // 輸入框
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text("Search", color = Color(0xFF9AA0A6), fontSize = 16.sp)
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                onShowHistory()
                            },
                            textStyle = TextStyle(color = Color(0xFF202124), fontSize = 16.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { launchGoogleSearch(query) }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    IconButton(onClick = { launchGoogleSearch(query) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 📜 歷史紀錄 ListView
            if (showHistory && history.isNotEmpty()) {
                Box {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                    ) {
                        items(history) { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        query = item
                                        onDismissHistory()
                                    }
                                    .padding(horizontal = 12.dp)
                            ) {
                                // ⬅️ 左邊的小時鐘 icon
                                Icon(
                                    painter = painterResource(R.drawable.history),
                                    contentDescription = "History",
                                    tint = Color(0xFF9AA0A6),
                                    modifier = Modifier
                                        .size(26.dp)
                                        .padding(end = 8.dp)
                                )

                                // ➡️ 右邊的文字
                                Text(
                                    text = item,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )

                                // ➡️ 右邊 Clear icon
                                IconButton(onClick = {
                                    history = history.filterNot { it == item }
                                    prefs.saveSearchHistory(history)
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.clear_ic_icon),
                                        contentDescription = "Clear",
                                        tint = Color(0xFF9AA0A6),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ⚡ 點擊清單外面收起，但不會蓋掉 SearchBar
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onDismissHistory()
                            }
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GoogleSearchBarWithHistory(
        showHistory = true,                 // 預覽時顯示歷史紀錄
        onDismissHistory = {},              // 預覽時不需要實作
        onShowHistory = {}                  // 預覽時不需要實作
    )
}
