// FavoritesPickerHost.kt
package com.pt.ifp.neolauncher.appgrid

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.*
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

/* ---------- Model ---------- */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable
)

/* ---------- Load installed, launchable apps ---------- */
private fun loadAllApps(ctx: Context): List<AppInfo> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .map { ri ->
            AppInfo(
                packageName = ri.activityInfo.packageName,
                label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.packageName,
                icon = ri.loadIcon(pm)
            )
        }
        .sortedBy { it.label.lowercase() }
}

@Composable
private fun rememberInstalledApps(): List<AppInfo> {
    val ctx = LocalContext.current
    return remember(ctx) { loadAllApps(ctx) }
}

/* ---------- Public host ---------- */
@Composable
fun FavoritesPickerHost(
    modifier: Modifier = Modifier,
    columns: Int = 6,
    // optional: provide your own app list in previews/tests
    appsOverride: List<AppInfo>? = null,
) {
    val allApps = appsOverride ?: rememberInstalledApps()

    // The favorites set shown on the home grid (package names)
    var favorites by rememberSaveable { mutableStateOf(setOf<String>()) }

    var showDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
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
            columns = columns
        )
    }

    if (showDialog) {
        AllAppsPickerDialog(
            allApps = allApps,
            initialSelection = favorites,
            onCancel = { showDialog = false },
            onDone = { newSelection ->
                favorites = newSelection
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
    capsule: Boolean = true,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            AppTileStatic(app = app, capsule = capsule)
        }
    }
}

@Composable
private fun AppTileStatic(
    app: AppInfo,
    capsule: Boolean,
    modifier: Modifier = Modifier
) {
    val tileShape = RoundedCornerShape(18.dp)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .then(
                    if (capsule)
                        Modifier.clip(tileShape).background(Color.White.copy(alpha = 0.08f))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            val bmp = remember(app.icon) { app.icon.toBitmap(128, 128) }
            Image(
                painter = BitmapPainter(bmp.asImageBitmap()),
                contentDescription = app.label,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
        }

        BasicText(
            text = app.label,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            style = TextStyle(color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ---------- Dialog: lists ALL apps, supports multi-select, applies on Done ---------- */
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

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = { onDone(selected) }) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        },
        title = { Text("Select apps") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search apps") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(96.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(display, key = { it.packageName }) { app ->
                        AppTileSelectable(
                            app = app,
                            isSelected = app.packageName in selected,
                            onToggle = {
                                selected = if (app.packageName in selected)
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

            // top-right check badge
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                modifier = Modifier.align(Alignment.TopEnd),
                enter = fadeIn(),
                exit = fadeOut()
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
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            style = TextStyle(color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center),
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
