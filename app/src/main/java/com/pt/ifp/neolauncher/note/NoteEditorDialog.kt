// NoteEditorDialog.kt
package com.pt.ifp.neolauncher.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pt.ifp.neolauncher.R

@Composable
fun NoteEditorDialog(
    initialText: String,
    initialSizeSp: Float,
    onSave: (text: String, sizeSp: Float) -> Unit,
    onCancel: () -> Unit,
    size: DpSize = DpSize(426.dp, 526.dp),   // ⬅️ 指定大小
    useDialogWindow: Boolean = false,         // true=用 Dialog 視窗；false=overlay 浮層
    scale: Float = 1.4f                       // 整體 UI 放大倍率
) {
    var text by rememberSaveable { mutableStateOf(initialText) }
    var sizeSp by rememberSaveable { mutableStateOf(initialSizeSp) }

    BackHandler { onCancel() }

    // 螢幕可用最大範圍（留 32dp 邊距，避免外溢）
    val cfg = LocalConfiguration.current
    val maxW = (cfg.screenWidthDp.dp - 4.dp).coerceAtLeast(0.dp)
    val maxH = (cfg.screenHeightDp.dp - 4.dp).coerceAtLeast(0.dp)
    // 實際使用大小：不超過螢幕
    val targetW = min(size.width, maxW)
    val targetH = min(size.height, maxH)

    // 縮放 helper（使用外層 scale）
    fun Dp.s() = this * scale
    fun TextUnit.s() = this * scale

    val content: @Composable () -> Unit = {
        // 主卡片底色（深藍灰），頂部條較淺
        val panelColor = Color(0xFF343A58)
        val headerColor = Color(0xFF49507A)
        val captionColor = Color(0xFFDFE3F2) // Font Size : 文字顏色

        Surface(
            color = panelColor,
            modifier = Modifier
                .sizeIn(maxWidth = maxW, maxHeight = maxH)
                .requiredWidth(targetW)
                .requiredHeight(targetH)
                .clip(RoundedCornerShape(12.dp.s()))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 頂部標題帶
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp.s())
                        .background(headerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bulletin Board",
                        color = Color.White,
                        fontSize = 18.sp.s()
                    )
                }

                // 內容區內邊距
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp.s(), vertical = 12.dp.s())
                ) {
                    // Font Size 選擇
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            "Font Size :",
                            color = captionColor,
                            fontSize = 12.sp.s(),
                            modifier = Modifier.padding(end = 8.dp.s())
                        )
                        SizeChip("L", 40f, sizeSp, 1.0f) { sizeSp = it }
                        Spacer(Modifier.width(6.dp.s()))
                        SizeChip("M", 30f, sizeSp, 1.0f) { sizeSp = it }
                        Spacer(Modifier.width(6.dp.s()))
                        SizeChip("S", 20f, sizeSp, 1.0f) { sizeSp = it }
                    }

                    Spacer(Modifier.height(12.dp.s()))

                    // 紙張背景的輸入區
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp.s()))
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_widget_note_bg),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = sizeSp.sp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp.s())
                                .verticalScroll(rememberScrollState()),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = false,
                            maxLines = Int.MAX_VALUE,
                            minLines = (6 * scale).toInt().coerceAtLeast(4),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp.s()))

                    // 底部按鈕（白色描邊、透明底）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp.s()),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 6.dp.s())
                    ) {
                        OutlinedButton(
                            onClick = { onSave(text.trimEnd(), sizeSp) },
                            modifier = Modifier
                                .height(34.dp.s())
                                .width(97.dp.s()),
                            shape = RoundedCornerShape(24.dp.s()),
                            border = BorderStroke(1.dp, Color.White),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Save", fontSize = 13.sp.s())
                        }

                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .height(34.dp.s())
                                .width(97.dp.s()),
                            shape = RoundedCornerShape(24.dp.s()),
                            border = BorderStroke(1.dp, Color.White),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Cancel", fontSize = 13.sp.s())
                        }
                    }
                }
            }
        }
    }


    if (useDialogWindow) {
        // 真的開一個 Dialog 視窗（會自帶 dim；大小由上面的 Surface 控制）
        Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,   // 允許自訂寬度
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false
            )
        ) { content() }
    } else {
        // 同頁透明浮層（想全透明把 alpha 改 0f）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* 點外不關閉；要可點外關閉就 onCancel() */ },
            contentAlignment = Alignment.Center
        ) { content() }
    }
}

@Composable
private fun SizeChip(
    label: String,
    valueSp: Float,
    currentSp: Float,
    scale: Float,
    onClick: (Float) -> Unit
) {
    fun Dp.s() = this * scale
    val selected = valueSp == currentSp
    val selectedBg = Color(0xFF2BB39B)
    val unselectedBg = Color(0xFFBFC3DA)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp.s()))
            .background(if (selected) selectedBg else unselectedBg)
            .clickable { onClick(valueSp) }
            .padding(horizontal = 10.dp.s(), vertical = 6.dp.s())
    ) {
        Text(
            text = label,
            fontSize = 12.sp * scale,
            color = if (selected) Color.White else Color(0xFF30324A)
        )
    }
}

@Preview(showBackground = true, widthDp = 426, heightDp = 526)
@Composable
fun NoteEditorDialogPreview() {
    MaterialTheme {
        NoteEditorDialog(
            initialText = "點我開啟編輯",
            initialSizeSp = 30f,                 // 測試用字體大小（sp）
            onSave = { _, _ -> },                // 預覽不需要真正儲存
            onCancel = { },                      // 預覽不需要關閉
            size = DpSize(426.dp, 526.dp),       // 與 Preview 尺寸一致
            useDialogWindow = false,             // 預覽不要開 Dialog 視窗
            scale = 1.4f
        )
    }
}
