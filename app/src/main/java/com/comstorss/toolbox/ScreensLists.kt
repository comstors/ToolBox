package com.comstorss.toolbox

import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.material.icons.rounded.ExpandMore

import androidx.compose.foundation.clickable

import androidx.compose.runtime.setValue

import androidx.compose.runtime.remember

import androidx.compose.runtime.mutableStateOf

import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.animation.animateContentSize

import androidx.compose.foundation.border

import androidx.compose.material.icons.rounded.RestartAlt

import androidx.compose.material.icons.rounded.Image

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.horizontalScroll

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

import androidx.activity.compose.rememberLauncherForActivityResult

import android.net.Uri

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
fun SettingsScreen(vm: ToolboxViewModel, theme: ThemeMode, setTheme: (ThemeMode) -> Unit, clear: () -> Unit) {
    val personalization by vm.personalization.collectAsState()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        vm.setBackgroundImage(uri)
    }

    androidx.compose.foundation.lazy.LazyColumn(
        contentPadding = PaddingValues(bottom = 28.dp),
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
            CollapsibleSettingsCard(
                title = "\u5916\u89c2\u4e2a\u6027\u5316",
                subtitle = "${personalization.accentPreset.label} \u00b7 ${personalization.backgroundStyle.label}",
                initiallyExpanded = false
            ) {
                AppearanceSettingsCard(
                    personalization = personalization,
                    setAccent = vm::setAccentPreset,
                    setBackgroundStyle = vm::setBackgroundStyle,
                    setTone = vm::setBackgroundImageTone,
                    chooseImage = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    clearImage = { vm.setBackgroundImage(null) },
                    reset = vm::resetPersonalization
                )
            }
        }
        item {
            CollapsibleSettingsCard(
                title = "\u7f13\u5b58",
                subtitle = "\u6e05\u7406\u4e34\u65f6\u6587\u4ef6\uff0c\u4e0d\u5f71\u54cd\u5df2\u4fdd\u5b58\u7684\u8f93\u51fa\u3002",
                initiallyExpanded = false
            ) {
                Feature(
                    icon = Icons.Rounded.Settings,
                    title = "\u7f13\u5b58",
                    body = "\u6e05\u7406\u4e34\u65f6\u6587\u4ef6\uff0c\u4e0d\u5f71\u54cd\u5df2\u7ecf\u4fdd\u5b58\u7684\u8f93\u51fa\u6587\u4ef6\u3002",
                    button = "\u6e05\u7406\u7f13\u5b58",
                    click = clear
                )
            }
        }
        item { AboutFooter() }
    }
}

@Composable
private fun CollapsibleSettingsCard(
    title: String,
    subtitle: String,
    initiallyExpanded: Boolean,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, tween(220), label = "settingsExpand")
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
        Column(modifier = Modifier.animateContentSize(tween(220)), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = !expanded }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = toolboxPalette().textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = toolboxPalette().textMuted,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }
            AnimatedVisibility(visible = expanded, enter = fadeIn(tween(160)), exit = fadeOut(tween(120))) {
                content()
            }
        }
    }
}
@Composable
private fun AppearanceSettingsCard(
    personalization: PersonalizationState,
    setAccent: (AccentPreset) -> Unit,
    setBackgroundStyle: (BackgroundStyle) -> Unit,
    setTone: (BackgroundImageTone) -> Unit,
    chooseImage: () -> Unit,
    clearImage: () -> Unit,
    reset: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionHeader(
                title = "\u5916\u89c2\u4e2a\u6027\u5316",
                subtitle = "\u8c03\u6574\u4e3b\u8272\u3001\u80cc\u666f\u6837\u5f0f\u548c\u81ea\u5b9a\u4e49\u80cc\u666f\u56fe\u3002"
            )
            AppearancePreview(personalization)
            SettingChoiceRow("\u914d\u8272", AccentPreset.entries, personalization.accentPreset, setAccent) { preset ->
                ColorDot(color = Color(preset.color), selected = preset == personalization.accentPreset)
                Text(preset.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            }
            SettingChoiceRow("\u80cc\u666f", BackgroundStyle.entries, personalization.backgroundStyle, setBackgroundStyle) { style ->
                Text(style.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            }
            SettingChoiceRow("\u56fe\u7247\u906e\u7f69", BackgroundImageTone.entries, personalization.backgroundImageTone, setTone) { tone ->
                Text(tone.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryActionButton(
                    text = "\u9009\u62e9\u80cc\u666f\u56fe",
                    icon = Icons.Rounded.Image,
                    click = chooseImage,
                    modifier = Modifier.weight(1f)
                )
                SecondaryActionButton(
                    text = "\u79fb\u9664\u56fe\u7247",
                    icon = Icons.Rounded.Delete,
                    click = clearImage,
                    modifier = Modifier.weight(1f),
                    enabled = personalization.backgroundImageUri != null
                )
            }
            PrimaryActionButton(
                text = "\u6062\u590d\u9ed8\u8ba4\u5916\u89c2",
                icon = Icons.Rounded.RestartAlt,
                click = reset,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AppearancePreview(personalization: PersonalizationState) {
    val palette = toolboxPalette()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(MaterialTheme.shapes.large)
            .background(
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(Color(personalization.accentPreset.color).copy(alpha = 0.24f), palette.cardMuted)
                )
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("\u9884\u89c8", style = MaterialTheme.typography.labelLarge, color = palette.textMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ColorDot(color = Color(personalization.accentPreset.color), selected = true)
                Text(personalization.accentPreset.label, fontWeight = FontWeight.Black)
                Text("\u00b7 ${personalization.backgroundStyle.label}", color = palette.textMuted)
            }
        }
    }
}

@Composable
private fun <T> SettingChoiceRow(
    title: String,
    values: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    item: @Composable (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = toolboxPalette().textMuted)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { value ->
                Pressable(selected = value == selected, onClick = { onSelected(value) }) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) { item(value) }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean) {
    Box(
        modifier = Modifier
            .size(if (selected) 24.dp else 20.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, if (selected) toolboxPalette().text else toolboxPalette().cardBorder, CircleShape)
    )
}
@Composable
private fun AboutFooter() {
    val palette = toolboxPalette()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Mingyu Toolbox \u00b7 comstorss", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
        Text("\u5f00\u6e90\u8f6f\u4ef6\uff0c\u4ec5\u4f9b\u81ea\u5df1\u4f7f\u7528", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
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