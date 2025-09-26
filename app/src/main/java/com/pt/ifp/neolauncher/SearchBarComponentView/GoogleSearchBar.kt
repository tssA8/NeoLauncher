// package com.pt.ifp.neolauncher.SearchBarComponentView

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.ifp.neolauncher.R

@Composable
fun GoogleSearchBar(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }

    val pillShape = RoundedCornerShape(50)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, pillShape)
            .background(Color.White, pillShape)
            .height(48.dp)                       // 輕薄高度（可依需求 44~52dp）
            .padding(horizontal = 16.dp)
    ) {
        // 左：彩色 G
        Icon(
            painter = painterResource(R.drawable.google_logo_icon),
            contentDescription = "Google",
            tint = Color.Unspecified,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(12.dp))

        // 中：可輸入的搜尋欄（用 BasicTextField 完全控制樣式與內距）
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color(0xFF202124),
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (query.isNotBlank()) {
                            val url = "https://www.google.com/search?q=${Uri.encode(query)}"
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1A73E8)),
                modifier = Modifier.fillMaxWidth()
            )

            // Placeholder
            if (query.isEmpty()) {
                Text(
                    text = "Search Google",
                    color = Color(0xFF9AA0A6),
                    fontSize = 16.sp
                )
            }
        }

//        // 右：搜尋按鈕（點擊觸發搜尋）
//        IconButton(onClick = {
//            if (query.isNotBlank()) {
//                val url = "https://www.google.com/search?q=${Uri.encode(query)}"
//                context.startActivity(
//                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                )
//            }
//        }) {
//            Icon(
//                painter = painterResource(R.drawable.ic_search_outline), // 放一個藍色放大鏡或用你現有的
//                contentDescription = "Search",
//                tint = Color(0xFF4285F4),
//                modifier = Modifier.size(22.dp)
//            )
//        }

        // 右：語音
        IconButton(onClick = {}) {
            Icon(
                painter = painterResource(R.drawable.google_mic_icon),
                contentDescription = "Voice Search",
                tint = Color(0xFF4285F4),
                modifier = Modifier.size(22.dp)
            )
        }

        // 右：Lens
        IconButton(onClick = {  }) {
            Icon(
                painter = painterResource(R.drawable.lens_google_icon),
                contentDescription = "Google Lens",
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
