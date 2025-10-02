package com.benq.uikit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
import com.benq.uikit.R

@Composable
fun NoteWidget(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    backgroundRes: Int = R.drawable.ic_widget_note_bg,
    textColorRes: Int = R.color.bq_grey_10,
    fontSizeSp: Float? = null,                 // ★ 新增：可選外部字體大小
    outerPadding: Dp = 10.dp,
    innerPadding: Dp = 10.dp
) {
    val context = LocalContext.current


    val scroll = rememberScrollState()
    val noteColor = colorResource(id = textColorRes)
    val bgPainter = painterResource(id = backgroundRes)

    // 若外部沒給，就用資源的預設大小
    val effectiveFontSizeSp =
        fontSizeSp ?: dimensionResource(id = R.dimen.note_text_size).value

    val baseMod = modifier
        .fillMaxSize()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = androidx.compose.material3.ripple(),
            onClick = onClick
        )

    Column(modifier = baseMod) {
        Spacer(Modifier.fillMaxWidth().weight(0.01f))
        Row(
            modifier = Modifier.fillMaxWidth().weight(0.98f),
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
                    fontSize = effectiveFontSizeSp.sp,   // ★ 用外部/預設大小
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
