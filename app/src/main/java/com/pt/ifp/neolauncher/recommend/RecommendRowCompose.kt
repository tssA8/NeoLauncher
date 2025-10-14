package com.pt.ifp.neolauncher.recommend

import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.pt.ifp.neolauncher.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RecommendRowCompose(
    modifier: Modifier = Modifier,
    onLeftClick: () -> Unit = { Log.d("RecommendRow", "rcLeft") },
    onMiddle1Click: () -> Unit = { Log.d("RecommendRow", "rcMiddle 1") },
    onMiddle2Click: () -> Unit = { Log.d("RecommendRow", "rcMiddle 2") },
    onRightClick: () -> Unit = { Log.d("RecommendRow", "rcRight") },
) {
    val itemW = dimensionResource(id = R.dimen.recommend_row_compose_item_width)
    val itemH = dimensionResource(id = R.dimen.recommend_row_compose_item_height)
    val leftFR   = remember { FocusRequester() }
    val secondFR = remember { FocusRequester() }   // ← 新增，給第二個 item 用
    val middleFR = remember { FocusRequester() }
    val rightFR  = remember { FocusRequester() }

    Row(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(Modifier.width(55.dp))

        // 1) Summery AI
        RecommendRowItem(
            text = "Summery AI",
            imageRes = R.drawable.summeryai,
            imageResFocused = R.drawable.summeryai,
            modifier = Modifier
                .width(itemW).height(itemH)
                .focusRequester(leftFR)
                .focusProperties { left = leftFR }, // 左鍵回到自己
            onClick = onLeftClick
        )

        // 2) Wireless Projection
        RecommendRowItem(
            text = "Instashare",
            imageRes = R.drawable.wireless,
            imageResFocused = R.drawable.wireless,
            modifier = Modifier
                .width(itemW).height(itemH)
                .focusRequester(secondFR)
                .focusProperties { left = leftFR }, // 左鍵移到第一個（可選，預設也多半會到前一個）
            onClick = onMiddle1Click      // ← 不要再用 onLeftClick
        )

        // 3) AMS
        RecommendRowItem(
            text = "Conference",
            imageRes = R.drawable.meeting,
            imageResFocused = R.drawable.meeting,
            modifier = Modifier
                .width(itemW).height(itemH)
                .focusRequester(middleFR),
            onClick = onMiddle2Click
        )

        // 4) Whiteboard
        RecommendRowItem(
            text = "Whiteboard",
            imageRes = R.drawable.write,
            imageResFocused = R.drawable.write,
            modifier = Modifier
                .width(itemW).height(itemH)
                .focusRequester(rightFR)
                .focusProperties { right = rightFR }, // 右鍵回到自己
            onClick = onRightClick
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RecommendRowItem(
    text: String,
    @DrawableRes imageRes: Int,
    @DrawableRes imageResFocused: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    var focused by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(false) }      // ← 新增
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val colorFocused = colorResource(R.color.recommend_row_focus_color)
    val colorNormal  = colorResource(R.color.recommend_row_normal_color)
    val textColor by animateColorAsState(if (focused) colorFocused else colorNormal, tween(160))
    val textAlpha by animateFloatAsState(if (focused) 1f else 0.6f, tween(160))

    val itemW = dimensionResource(R.dimen.recommend_row_compose_item_width)
    val itemH = dimensionResource(R.dimen.recommend_row_compose_item_height)
    val labelTop = 1.dp
    val labelSize = 12.sp
    val density = LocalDensity.current
    val labelH = with(density) { labelSize.toDp() * 1.2f }  // 粗估 1 行字高度
    val cellH = itemH + labelTop + labelH

    Column(
        modifier = modifier
            .width(itemW)
            .height(cellH),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 圖片盒：可聚焦＋可點擊
        Box(
            modifier = Modifier
                .requiredSize(136.dp, 138.dp)
                .onFocusChanged { focused = it.isFocused }
                .zIndex(if (focused) 1f else 0f)
                .focusable()
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.matchParentSize()
                    .scale(0.9f),
                factory = { ctx ->
                    AppCompatImageView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.FIT_XY
                        isClickable = false
                        isFocusable = false
                    }
                },
                update = { iv ->
                    val resIdToUse = imageResFocused?.let { if (focused) it else imageRes } ?: imageRes
                    val d = AppCompatResources.getDrawable(context, resIdToUse)
                    if (imageResFocused == null) {
                        val state = buildList {
                            if (focused) add(android.R.attr.state_focused)
                            if (pressed) add(android.R.attr.state_pressed)
                        }.toIntArray()
                        d?.state = state
                    }
                    iv.setImageDrawable(d)
                }
            )
        }

        Spacer(Modifier.height(labelTop))

        // 文字在圖片下方置中
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(textAlpha),
            maxLines = 1
        )
    }
}

@Preview(
    name = "RecommendRow",
    showBackground = true,
    backgroundColor = 0xFF121212,
    widthDp = 3840,
    heightDp = 1080
)
@Composable
fun RecommendRowPreview() {
    RecommendRowCompose(modifier = Modifier)
}
