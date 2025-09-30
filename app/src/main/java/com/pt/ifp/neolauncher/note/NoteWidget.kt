package com.pt.ifp.neolauncher.note

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.ifp.neolauncher.R

@Composable
fun NoteWidget(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,         // 可選：由外部接管點擊
    backgroundRes: Int = R.drawable.ic_widget_note_bg,
    textColorRes: Int = R.color.bq_grey_10,
    // ⚠️ 不要在預設參數呼叫 composable；改在函式內取用
    outerPadding: Dp = 10.dp,
    innerPadding: Dp = 10.dp
) {
    val context = LocalContext.current

    // 預設開啟 NoteEditActivity 的行為
    val defaultOpen: () -> Unit = {
        val intent = Intent(context, NoteEditActivity::class.java)
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    val clickHandler = onClick ?: defaultOpen

    val scroll = rememberScrollState()
    val noteColor = colorResource(id = textColorRes)
    val bgPainter = painterResource(id = backgroundRes)
    val textSizePx = dimensionResource(id = R.dimen.note_text_size).value // 取出資源值

    val baseMod = modifier
        .fillMaxSize() // 讓整塊佔滿父層
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = clickHandler
        )

    Column(modifier = baseMod) {
        Spacer(Modifier.fillMaxWidth().weight(0.01f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.98f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.fillMaxHeight().weight(0.1f))

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.8f)
                    .padding(outerPadding)
            ) {
                Image(
                    painter = bgPainter,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize()
                )

                Text(
                    text = text,
                    color = noteColor,
                    fontSize = textSizePx.sp,           // 以 sp 顯示
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scroll)
                )
            }

            Spacer(Modifier.fillMaxHeight().weight(0.1f))
        }

        Spacer(Modifier.fillMaxWidth().weight(0.01f))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
fun NoteWidgetPreview() {
    NoteWidget(
        text = "This is a note.\n多行文字測試，多到需要垂直捲動時會自動可以捲。"
    )
}
