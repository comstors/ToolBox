package com.comstorss.toolbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TaskScreen(vm: ToolboxViewModel) {
    val tasks by vm.tasks.collectAsState()
    ListOrEmpty(
        entries = tasks,
        title = "\u6682\u65e0\u4efb\u52a1",
        body = "\u8f6c\u6362\u548c\u4e0b\u8f7d\u4efb\u52a1\u4f1a\u5728\u8fd9\u91cc\u51fa\u73b0\u3002"
    ) { TaskCard(it) }
}

@Composable
fun HistoryScreen(vm: ToolboxViewModel) {
    val history by vm.history.collectAsState()
    ListOrEmpty(
        entries = history,
        title = "\u6682\u65e0\u5386\u53f2",
        body = "PDF \u548c\u89c6\u9891\u4e0b\u8f7d\u8bb0\u5f55\u4f1a\u4fdd\u5b58\u5728\u8fd9\u91cc\u3002"
    ) { HistoryCard(it, vm) }
}

@Composable
fun TaskCard(t: ToolboxTask) {
    val progress by animateFloatAsState(t.progress.coerceIn(0f, 1f), spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow), label = "progress")
    val palette = toolboxPalette()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(palette.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(statusIcon(t.status), null, tint = palette.accent)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(t.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(t.detail, color = palette.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(t.status.label, color = palette.textMuted, style = MaterialTheme.typography.labelMedium)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = palette.accent,
                trackColor = palette.cardMuted
            )
        }
    }
}

@Composable
fun HistoryCard(r: HistoryRecord, vm: ToolboxViewModel) {
    val palette = toolboxPalette()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(palette.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(statusIcon(r.status), null, tint = palette.accent)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(r.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(r.detail, color = palette.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(r.status.label, color = palette.textMuted, style = MaterialTheme.typography.labelMedium)
            }
            r.path?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(palette.cardMuted)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(it, color = palette.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (r.status == TaskStatus.Success && r.uri != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryActionButton(
                        text = "\u6253\u5f00",
                        icon = Icons.Rounded.OpenInNew,
                        click = { vm.openHistory(r) },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryActionButton(
                        text = "\u5206\u4eab",
                        icon = Icons.Rounded.Share,
                        click = { vm.shareHistory(r) },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryActionButton(
                        text = "\u5220\u9664",
                        icon = Icons.Rounded.Delete,
                        click = { vm.deleteHistory(r) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                PrimaryActionButton(
                    text = "\u5220\u9664",
                    icon = Icons.Rounded.Delete,
                    click = { vm.deleteHistory(r) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(theme: ThemeMode, setTheme: (ThemeMode) -> Unit, clear: () -> Unit) = androidx.compose.foundation.lazy.LazyColumn(
    contentPadding = PaddingValues(bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
) {
    item {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionHeader(
                    title = "\u4e3b\u9898",
                    subtitle = "\u53ef\u4ee5\u9009\u62e9\u6d45\u8272\u3001\u6df1\u8272\u6216\u8ddf\u968f\u7cfb\u7edf\u3002"
                )
                ThemeMode.entries.forEach { mode ->
                    ThemeRow(mode = mode, selected = theme == mode) { setTheme(mode) }
                }
            }
        }
    }
    item {
        Feature(
            icon = Icons.Rounded.Settings,
            title = "\u7f13\u5b58",
            body = "\u6e05\u7406\u4e34\u65f6\u6587\u4ef6\uff0c\u4e0d\u5f71\u54cd\u5df2\u7ecf\u4fdd\u5b58\u7684\u8f93\u51fa\u6587\u4ef6\u3002",
            button = "\u6e05\u7406\u7f13\u5b58",
            click = clear
        )
    }
    item {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = "\u5173\u4e8e",
                    subtitle = "\u5e94\u7528\u4fe1\u606f\u548c\u4f5c\u8005\u8054\u7cfb\u65b9\u5f0f\u3002"
                )
                SettingLine("\u5e94\u7528", "Mingyu Toolbox")
                SettingLine("\u4f5c\u8005\u5fae\u4fe1", "comstorss")
                SettingLine("\u8bf4\u660e", "\u5f00\u6e90\u8f6f\u4ef6\uff0c\u4ec5\u4f9b\u81ea\u5df1\u4f7f\u7528")
            }
        }
    }
}

@Composable
fun ThemeRow(mode: ThemeMode, selected: Boolean, click: () -> Unit) {
    val palette = toolboxPalette()
    val bg by animateColorAsState(
        targetValue = if (selected) palette.accentSoft else Color.Transparent,
        animationSpec = tween(220),
        label = "themeBg"
    )
    val icon = when (mode) {
        ThemeMode.Dark -> Icons.Rounded.DarkMode
        ThemeMode.Light -> Icons.Rounded.LightMode
        ThemeMode.System -> Icons.Rounded.PhoneAndroid
    }
    Pressable(selected = selected, onClick = click) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = if (selected) palette.accent else palette.textMuted)
            Text(mode.label, modifier = Modifier.weight(1f))
            AnimatedVisibility(selected, enter = fadeIn(tween(180)), exit = fadeOut(tween(140))) {
                Icon(Icons.Rounded.CheckCircle, null, tint = palette.accent)
            }
        }
    }
}

@Composable
fun <T> ListOrEmpty(entries: List<T>, title: String, body: String, row: @Composable (T) -> Unit) = androidx.compose.foundation.lazy.LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    if (entries.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 86.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(toolboxPalette().accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Pending, null, modifier = Modifier.size(28.dp), tint = toolboxPalette().textMuted)
                    }
                    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = toolboxPalette().textMuted)
                    Text(body, color = toolboxPalette().textMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    } else {
        items(entries) { item ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(180)) + slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                ) { it / 4 }
            ) {
                row(item)
            }
        }
    }
}
@Composable
fun Feature(icon: ImageVector, title: String, body: String, button: String, click: () -> Unit) {
    val palette = toolboxPalette()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(palette.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = palette.accent)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(body, color = palette.textMuted)
                }
            }
            PrimaryActionButton(text = button, icon = icon, click = click, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SettingLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = toolboxPalette().textMuted, modifier = Modifier.weight(0.32f))
        Text(value, modifier = Modifier.weight(0.68f))
    }
}