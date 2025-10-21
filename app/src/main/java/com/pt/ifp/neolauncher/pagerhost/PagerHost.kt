package com.pt.ifp.neolauncher.pagerhost

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerHost(
    pages: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    indicatorActiveColor: Color = Color.White,
    indicatorInactiveColor: Color = Color.White.copy(alpha = 0.4f),
    indicatorSize: Int = 6,        // dot diameter (dp)
    indicatorSpacing: Int = 6,     // dot spacing (dp)
) {
    // Keep pageCount in the state (newer API).
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { pages.size.coerceAtLeast(1) }
    )

    Box(modifier = modifier) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> index } // stable keys help recycling
        ) { page ->
            pages.getOrNull(page)?.invoke()
        }

        // Vertical dots at the right (適合直向翻頁的視覺)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(indicatorSpacing.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val count = pagerState.pageCount
            repeat(count) { i ->
                val selected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .size(indicatorSize.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) indicatorActiveColor
                            else indicatorInactiveColor
                        )
                )
            }
        }
    }
}

/* ---------------- Demo Page ---------------- */

@Composable
private fun DemoPage(label: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.text.BasicText(
            text = label,
            style = androidx.compose.ui.text.TextStyle(
                color = Color.White,
                fontSize = 22.sp
            )
        )
    }
}

/* ---------------- Previews ---------------- */

@Preview(name = "PagerHost – Light (Vertical)", showBackground = true, widthDp = 360, heightDp = 480)
@Preview(
    name = "PagerHost – Dark (Vertical)",
    showBackground = true,
    widthDp = 360, heightDp = 480,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun Preview_PagerHost_Vertical() {
    val pages = listOf<@Composable () -> Unit>(
        { DemoPage("Page 1", Color(0xFF3F51B5)) },
        { DemoPage("Page 2", Color(0xFF009688)) },
        { DemoPage("Page 3", Color(0xFF795548)) },
    )

    PagerHost(
        pages = pages,
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        indicatorActiveColor = Color.White,
        indicatorInactiveColor = Color.White.copy(alpha = 0.4f),
        indicatorSize = 8,
        indicatorSpacing = 8
    )
}
