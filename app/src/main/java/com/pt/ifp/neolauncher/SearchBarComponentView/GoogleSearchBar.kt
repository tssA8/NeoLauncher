// file: SearchBarComponentView/GoogleSearchBar.kt
package com.pt.ifp.neolauncher.SearchBarComponentView

import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.ifp.neolauncher.R

@Composable
fun GoogleSearchBar(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }

    // ---- 語音輸入 launcher ----
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) query = text
        }
    }

    // ---- 照片挑選（Android 官方 Photo Picker，不需權限）----
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        // 先嘗試呼叫 Google Lens（若裝置支援）
        val lens = Intent("com.google.android.gms.actions.VIEW_IN_LENS").apply {
            data = uri
            setPackage("com.google.android.googlequicksearchbox")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val pm = context.packageManager
        if (lens.resolveActivity(pm) != null) {
            context.startActivity(lens)
        } else {
            // 後備：至少把圖片打開；或你也可以改成上傳到瀏覽器 google 圖片（需要使用者手動）
            val view = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (view.resolveActivity(pm) != null) {
                context.startActivity(view)
            }
        }
    }

    fun launchWebSearch(q: String) {
        if (q.isBlank()) return
        val url = "https://www.google.com/search?q=${Uri.encode(q)}"
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }


    fun launchGoogleSearch(q: String) {
        try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, q)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // fallback
            val url = "https://www.google.com/search?q=" + Uri.encode(q)
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }


    Surface(
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(2.dp, RoundedCornerShape(50))
                .background(Color.White, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                // 點整條也可送出（跟 Google App 行為略像）
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { launchGoogleSearch(query) }
                .fillMaxWidth()
                .heightIn(min = 40.dp)
        ) {
            // 左：彩色 G
            Icon(
                painter = painterResource(R.drawable.google_logo_icon),
                contentDescription = "Google",
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(8.dp))

            // 中：純自訂輸入框（BasicTextField）
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        "Search",
                        color = Color(0xFF9AA0A6),
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle = TextStyle(
                        color = Color(0xFF202124),
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { launchGoogleSearch(query) }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 右一：語音
            IconButton(onClick = {
                // 使用系統的語音辨識 Intent（不需 RECORD_AUDIO 權限）
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
                }
                val pm = context.packageManager
                if (intent.resolveActivity(pm) != null) {
                    voiceLauncher.launch(intent)
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.google_mic_icon),
                    contentDescription = "Voice Search",
                    tint = Color(0xFF4285F4),
                    modifier = Modifier.size(22.dp)
                )
            }

            // 右二：Lens（照片）
            IconButton(onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Icon(
                    painter = painterResource(R.drawable.lens_google_icon),
                    contentDescription = "Google Lens",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 右三：搜尋按鈕
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
    
}
