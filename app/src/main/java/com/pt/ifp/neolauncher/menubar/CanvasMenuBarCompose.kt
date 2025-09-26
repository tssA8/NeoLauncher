// filename: CanvasMenuBarCompose.kt
package com.pt.ifp.neolauncher.menubar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.ifp.neolauncher.R

private const val SLOT_COUNT = 4

@Immutable
data class MenuBarIcons(
    @DrawableRes val casting: Int = R.drawable.source_52,
    @DrawableRes val settings: Int = R.drawable.setting_52,
    @DrawableRes val allApps: Int = R.drawable.all_apps_52,
    @DrawableRes val help: Int = R.drawable.help_52,
    @DrawableRes val user: Int = R.drawable.svg_icon_login_user
)

@Composable
fun CanvasMenuBarCompose(
    modifier: Modifier = Modifier,
    userName: String = "admin",
    icons: MenuBarIcons = MenuBarIcons(),
    barHeight: Dp = dimensionResource(id = R.dimen.menu_bar_container_height),
    slotIconSize: Dp = 30.dp,
    userIconSize: Dp = 24.dp,
    userTextSizeSp: Int = 12,
    onCasting: () -> Unit = {},
    onSettings: () -> Unit = {},
    onAllApps: () -> Unit = {},
    onHelp: () -> Unit = {}
) {
    val barBg = Color.Black.copy(alpha = 0.03f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(barBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左：User info
        val barBg2 = Color.Black.copy(alpha = 0.63f)
        val userPainter = painterResource(id = icons.user)
        MenubarUserInfo()

        Spacer(modifier = Modifier.fillMaxHeight().width(250.dp))

        MenubarSlotContainer(
            barHeight = barHeight,
            slotIconSize = slotIconSize,
            icons = icons,
            onCasting = onCasting,
            onSettings = onSettings,
            onAllApps = onAllApps,
            onHelp = onHelp
        )
    }
}


@Composable
fun MenubarUserInfo(modifier: Modifier = Modifier,
                    barBg2 : Color = Color.Black.copy(alpha = 0.63f),
                    userPainter : Painter = painterResource(id = R.drawable.svg_icon_login_user),
                    userIconSize : Dp = 24.dp,
                    userName : String = "admin",
                    userTextSizeSp : Int = 12) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxHeight()
            .background(barBg2)
            .padding(start = 20.dp)
            .width(300.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = modifier.fillMaxHeight()) {
            Icon(
                painter = userPainter,
                contentDescription = "User",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(userIconSize)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(8.dp).background(barBg2))
            Text(
                text = userName,
                color = Color.White,
                fontSize = userTextSizeSp.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MenubarSlotContainer(modifier: Modifier = Modifier,
                         barHeight: Dp = dimensionResource(id = R.dimen.menu_bar_container_height),
                         barBg2 : Color = Color.Black.copy(alpha = 0.63f),
                         slotIconSize: Dp = 30.dp,
                         icons: MenuBarIcons = MenuBarIcons(),
                         onCasting: () -> Unit = {},
                         onSettings: () -> Unit = {},
                         onAllApps: () -> Unit = {},
                         onHelp: () -> Unit = {}) {
    val slotSize = barHeight
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 63.dp)
                .background(barBg2)
        ) {
            MenuSlot(
                size = slotSize,
                iconSize = slotIconSize,
                resId = icons.casting,
                contentDescription = "Casting",
                onClick = onCasting
            )
            MenuSlot(
                size = slotSize,
                iconSize = slotIconSize,
                resId = icons.settings,
                contentDescription = "Settings",
                onClick = onSettings
            )
            MenuSlot(
                size = slotSize,
                iconSize = slotIconSize,
                resId = icons.allApps,
                contentDescription = "All Apps",
                onClick = onAllApps
            )
            MenuSlot(
                size = slotSize,
                iconSize = slotIconSize,
                resId = icons.help,
                contentDescription = "Help",
                onClick = onHelp
            )
        }
    }
}

@Composable
private fun MenuSlot(
    size: Dp,
    iconSize: Dp,
    @DrawableRes resId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size) // 正方形 slot（等於原本 slotW = slotH = barHeight）
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            tint = Color.Unspecified,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CanvasMenuBarCompose()
}
