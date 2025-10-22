// FavoritesPickerHost.kt
package com.pt.ifp.neolauncher.appgrid

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

/* ---------- Model ---------- */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable
)

/* ---------- Load installed, launchable apps ---------- */
private fun loadAllApps(ctx: Context): List<AppInfo> {
    val pm = ctx.packageManager
    val q = pm.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
    )
    return q.map {
        AppInfo(
            it.activityInfo.packageName,
            it.loadLabel(pm)?.toString() ?: it.activityInfo.packageName,
            it.loadIcon(pm)
        )
    }.sortedBy { it.label.lowercase() }
}

@Composable
private fun rememberInstalledApps(): List<AppInfo> {
    val ctx = LocalContext.current
    return remember(ctx) { loadAllApps(ctx) }
}

// 放在同一檔或單獨檔案
private class FavoritesStore(ctx: Context) {
    private val sp = ctx.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun get(): Set<String> {
        val raw = sp.getStringSet("pkgs", emptySet()) ?: emptySet()
        return HashSet(raw)
    }

    fun set(newSet: Set<String>) {
        sp.edit().putStringSet("pkgs", HashSet(newSet)).apply()
    }
}


/* ---------- Public host ---------- */
@Composable
fun FavoritesPickerHost(
    modifier: Modifier = Modifier,
    columns: Int = 5,
    appsOverride: List<AppInfo>? = null,
) {
    val allApps = appsOverride ?: rememberInstalledApps()
    val ctx = LocalContext.current
    val store = remember(ctx) { FavoritesStore(ctx) }

    // favorites shown on the grid
//    var favorites by rememberSaveable { mutableStateOf(setOf<String>()) }
    var favorites by rememberSaveable { mutableStateOf(store.get()) }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.background(Color(0xFF3A4064))) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favorites",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { showDialog = true }) {
                Text("Select")
            }
        }

        // Favorites grid (only chosen apps)
        val favApps = remember(allApps, favorites) {
            if (favorites.isEmpty()) emptyList()
            else allApps.filter { it.packageName in favorites }
        }
        FavoritesGrid(
            apps = favApps,
            columns = columns,
            onOpen = { pkg ->
                val intent = ctx.packageManager
                    .getLaunchIntentForPackage(pkg)
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intent != null) ctx.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
    }

    if (showDialog) {
        AllAppsPickerDialog(
            allApps = allApps,
            initialSelection = favorites,
            onCancel = { showDialog = false },
            onDone = { newSelection ->
                favorites = newSelection
                store.set(newSelection)       // ✅ 寫入 SharedPreferences
                showDialog = false
            }
        )
    }
}

/* ---------- Favorites grid (non-selectable, just shows chosen apps) ---------- */
@Composable
private fun FavoritesGrid(
    apps: List<AppInfo>,
    columns: Int,
    onOpen: (pkg: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tileSize = 72.dp
    val iconSize = 40.dp
    val tileShape = RoundedCornerShape(18.dp)

    if (apps.isEmpty()) {
        Box(
            modifier.fillMaxWidth().height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No favorites selected",
                color = Color(0x80FFFFFF),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(tileSize)
                        .clip(tileShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onOpen(app.packageName) },
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = remember(app.icon) { app.icon.toBitmap(128, 128) }
                    Image(
                        painter = BitmapPainter(bmp.asImageBitmap()),
                        contentDescription = app.label,
                        modifier = Modifier.size(iconSize)
                    )
                }
                Spacer(Modifier.height(6.dp))
                BasicText(
                    text = app.label,
                    modifier = Modifier
                        .width(tileSize)
                        .padding(horizontal = 2.dp),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AllAppsPickerDialog(
    allApps: List<AppInfo>,
    initialSelection: Set<String>,
    onCancel: () -> Unit,
    onDone: (Set<String>) -> Unit
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var selected by rememberSaveable { mutableStateOf(initialSelection) }

    val display = remember(allApps, query) {
        if (query.text.isBlank()) allApps
        else allApps.filter {
            it.label.contains(query.text, ignoreCase = true) ||
                    it.packageName.contains(query.text, ignoreCase = true)
        }
    }
    val displayPkgs = remember(display) { display.map { it.packageName }.toSet() }

    val panel  = Color(0xFF2F3456)
    val onPanel = Color(0xFFEFEFF5)
    val chipBg = Color(0xFF394068)

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor    = panel,
        titleContentColor = onPanel,
        textContentColor  = onPanel,
        confirmButton = { TextButton(onClick = { onDone(selected) }) { Text("Done", color = onPanel) } },
        dismissButton  = { TextButton(onClick = onCancel) { Text("Cancel", color = onPanel) } },
        title = { Text("Select apps") },
        text = {
            Column {
                // 搜尋
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search apps", color = onPanel.copy(alpha = 0.6f)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(panel),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = chipBg,
                        unfocusedContainerColor = chipBg,
                        disabledContainerColor  = chipBg,
                        cursorColor             = onPanel,
                        focusedIndicatorColor   = onPanel.copy(alpha = 0.75f),
                        unfocusedIndicatorColor = onPanel.copy(alpha = 0.35f),
                        focusedTextColor        = onPanel,
                        unfocusedTextColor      = onPanel
                    )
                )

                Spacer(Modifier.height(8.dp))

                // ⬇️ 新增：Select All / Unselect All（作用於目前顯示清單）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { selected = selected + displayPkgs }
                    ) { Text("Select All", color = onPanel) }

                    TextButton(
                        onClick = { selected = selected - displayPkgs }
                    ) { Text("Unselect All", color = onPanel) }
                }

                Spacer(Modifier.height(8.dp))

                // 清單
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(96.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .background(panel),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding        = PaddingValues(4.dp)
                ) {
                    items(display, key = { it.packageName }) { app ->
                        AppTileSelectable(
                            app = app,
                            isSelected = app.packageName in selected,
                            onToggle = {
                                selected =
                                    if (app.packageName in selected)
                                        selected - app.packageName
                                    else
                                        selected + app.packageName
                            }
                        )
                    }
                }
            }
        }
    )
}


/* ---------- Selectable tile used inside dialog ---------- */
@Composable
private fun AppTileSelectable(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.06f))
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            val bmp = remember(app.icon) { app.icon.toBitmap(128, 128) }
            Image(
                painter = BitmapPainter(bmp.asImageBitmap()),
                contentDescription = app.label,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )

            // ★ 使用完整名稱，避免 AnimatedVisibility 衝突
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                modifier = Modifier.align(Alignment.TopEnd),
                enter = fadeIn(),
                exit  = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(18.dp)
                        .background(Color(0xFF2BB39B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        BasicText(
            text = app.label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            style = TextStyle(
                color = Color.White,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ---------- Preview (fake data) ---------- */
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FavoritesPickerHost_Preview() {
    val fake = listOf(
        AppInfo("com.example.alpha", "Alpha", ColorDrawable(0xFFE91E63.toInt())),
        AppInfo("com.example.beta", "Beta", ColorDrawable(0xFF9C27B0.toInt())),
        AppInfo("com.example.gamma", "Gamma", ColorDrawable(0xFF3F51B5.toInt())),
        AppInfo("com.example.delta", "Delta", ColorDrawable(0xFF03A9F4.toInt())),
        AppInfo("com.example.epsilon", "Epsilon", ColorDrawable(0xFF009688.toInt())),
        AppInfo("com.example.zeta", "Zeta", ColorDrawable(0xFF8BC34A.toInt())),
        AppInfo("com.example.eta", "Eta", ColorDrawable(0xFFFFC107.toInt())),
        AppInfo("com.example.theta", "Theta", ColorDrawable(0xFFFF5722.toInt())),
    )

    MaterialTheme {
        Surface(color = Color(0xFF202124)) {
            FavoritesPickerHost(
                modifier = Modifier.fillMaxSize(),
                columns = 5,
                appsOverride = fake
            )
        }
    }
}
