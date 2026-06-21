package com.comstorss.toolbox

import kotlinx.coroutines.withContext

import kotlinx.coroutines.Dispatchers

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.graphics.asImageBitmap

import androidx.compose.runtime.setValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.Image

import android.graphics.BitmapFactory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class NoticeTone { Info, Success, Error }

data class ToolboxPalette(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val card: Color,
    val cardStrong: Color,
    val cardBorder: Color,
    val cardMuted: Color,
    val accent: Color,
    val accentStrong: Color,
    val accentSoft: Color,
        val text: Color,
val textMuted: Color,
    val success: Color,
    val error: Color,
    val info: Color,
    val glow: Color
)

private val CardRadius = RoundedCornerShape(28.dp)
private val InnerRadius = RoundedCornerShape(18.dp)
private val PillRadius = RoundedCornerShape(999.dp)

private val LocalToolboxPalette = staticCompositionLocalOf<ToolboxPalette> {
    error("ToolboxPalette not provided")
}

@Composable
fun toolboxPalette(): ToolboxPalette = LocalToolboxPalette.current

@Composable
fun ToolboxApp(vm: ToolboxViewModel) {
    val theme by vm.theme.collectAsState()
    val personalization by vm.personalization.collectAsState()
    val dark = when (theme) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    ComstTheme(dark = dark, personalization = personalization) { AppRoot(vm, theme) }
}

@Composable
fun ComstTheme(dark: Boolean, personalization: PersonalizationState = PersonalizationState(), content: @Composable () -> Unit) {
    val lightScheme = lightColorScheme(
        primary = Color(0xFF0284C7),
        onPrimary = Color.White,
        background = Color(0xFFEDF3F8),
        surface = Color.White,
        surfaceVariant = Color(0xFFE0EFFA),
        secondary = Color(0xFF64748B)
    )
    val darkScheme = darkColorScheme(
        primary = Color(0xFF38BDF8),
        onPrimary = Color(0xFF06223D),
        background = Color(0xFF0A0D12),
        surface = Color(0xFF121624),
        surfaceVariant = Color(0xFF1D243A),
        secondary = Color(0xFF94A3B8)
    )
    val targetPalette = if (dark) {
        ToolboxPalette(
            backgroundTop = Color(0xFF0A0D12),
            backgroundBottom = Color(0xFF0A0C12),
            card = Color(0xF2121624),
            cardStrong = Color(0xFF121624),
            cardBorder = Color(0xFF242E42),
            cardMuted = Color(0xFF1D243A),
            accent = presetAccent(personalization, dark = true, default = Color(0xFF38BDF8)),
            accentStrong = presetAccent(personalization, dark = false, default = Color(0xFF0284C7)),
            accentSoft = presetAccent(personalization, dark = true, default = Color(0xFF38BDF8)).copy(alpha = 0.14f),
            text = Color(0xFFF1F5F9),
            textMuted = Color(0xFFB6C6D8),
            success = Color(0xFF71E3B2),
            error = Color(0xFFFF8A8A),
            info = presetAccent(personalization, dark = true, default = Color(0xFF8CD7FF)),
            glow = presetAccent(personalization, dark = true, default = Color(0xFF6FD0FF)).copy(alpha = 0.20f)
        )
    } else {
        ToolboxPalette(
            backgroundTop = Color(0xFFEDF3F8),
            backgroundBottom = Color(0xFFEDF3F8),
            card = Color(0xFFFFFFFF),
            cardStrong = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFDFEBF6),
            cardMuted = Color(0xFFE0EFFA),
            accent = presetAccent(personalization, dark = false, default = Color(0xFF0284C7)),
            accentStrong = presetAccent(personalization, dark = false, default = Color(0xFF0284C7)),
            accentSoft = presetAccent(personalization, dark = false, default = Color(0xFF0284C7)).copy(alpha = 0.12f),
            text = Color(0xFF1E293B),
            textMuted = Color(0xFF64748B),
            success = Color(0xFF1B9C6A),
            error = Color(0xFFD84E67),
            info = presetAccent(personalization, dark = false, default = Color(0xFF157DFF)),
            glow = presetAccent(personalization, dark = false, default = Color(0xFF157DFF)).copy(alpha = 0.16f)
        )
    }
    val themeMotion = tween<Color>(durationMillis = 360, easing = FastOutSlowInEasing)
    val palette = ToolboxPalette(
        backgroundTop = animateColorAsState(targetPalette.backgroundTop, themeMotion, label = "backgroundTop").value,
        backgroundBottom = animateColorAsState(targetPalette.backgroundBottom, themeMotion, label = "backgroundBottom").value,
        card = animateColorAsState(targetPalette.card, themeMotion, label = "card").value,
        cardStrong = animateColorAsState(targetPalette.cardStrong, themeMotion, label = "cardStrong").value,
        cardBorder = animateColorAsState(targetPalette.cardBorder, themeMotion, label = "cardBorder").value,
        cardMuted = animateColorAsState(targetPalette.cardMuted, themeMotion, label = "cardMuted").value,
        accent = animateColorAsState(targetPalette.accent, themeMotion, label = "accent").value,
        accentStrong = animateColorAsState(targetPalette.accentStrong, themeMotion, label = "accentStrong").value,
        accentSoft = animateColorAsState(targetPalette.accentSoft, themeMotion, label = "accentSoft").value,
        text = animateColorAsState(targetPalette.text, themeMotion, label = "text").value,
        textMuted = animateColorAsState(targetPalette.textMuted, themeMotion, label = "textMuted").value,
        success = animateColorAsState(targetPalette.success, themeMotion, label = "success").value,
        error = animateColorAsState(targetPalette.error, themeMotion, label = "error").value,
        info = animateColorAsState(targetPalette.info, themeMotion, label = "info").value,
        glow = animateColorAsState(targetPalette.glow, themeMotion, label = "glow").value
    )

    CompositionLocalProvider(LocalToolboxPalette provides palette) {
        MaterialTheme(
            colorScheme = if (dark) darkScheme else lightScheme,
            typography = Typography()
        ) {
            Surface(color = Color.Transparent, contentColor = palette.text) {
                Box(
                    modifier = Modifier.background(
                        Brush.verticalGradient(backgroundColors(personalization, palette, dark))
                    )
                ) {
                    BackgroundImageLayer(personalization = personalization, dark = dark)
                    content()
                }
            }
        }
    }
}



private fun presetAccent(personalization: PersonalizationState, dark: Boolean, default: Color): Color {
    if (personalization.accentPreset == AccentPreset.Ocean) return default
    val selected = Color(personalization.accentPreset.color)
    return if (dark) mix(selected, Color.White, 0.24f) else selected
}
private fun backgroundColors(personalization: PersonalizationState, palette: ToolboxPalette, dark: Boolean): List<Color> {
    return when (personalization.backgroundStyle) {
        BackgroundStyle.Solid -> listOf(palette.backgroundBottom, palette.backgroundBottom)
        BackgroundStyle.Gradient -> if (personalization.accentPreset == AccentPreset.Ocean) {
            listOf(palette.backgroundTop, palette.backgroundBottom)
        } else if (dark) {
            listOf(mix(palette.backgroundTop, palette.accent, 0.08f), palette.backgroundBottom)
        } else {
            listOf(mix(palette.backgroundTop, palette.accent, 0.08f), mix(palette.backgroundBottom, palette.accent, 0.04f))
        }
        BackgroundStyle.Image -> listOf(palette.backgroundTop, palette.backgroundBottom)
    }
}

@Composable
private fun BackgroundImageLayer(personalization: PersonalizationState, dark: Boolean) {
    if (personalization.backgroundStyle != BackgroundStyle.Image || personalization.backgroundImageUri.isNullOrBlank()) return
    val context = LocalContext.current
    var image by remember(personalization.backgroundImageUri) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(personalization.backgroundImageUri) {
        image = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(android.net.Uri.parse(personalization.backgroundImageUri))?.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    image?.let { bitmap ->
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.82f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = (personalization.backgroundImageTone.overlayAlpha + if (dark) 0.12f else 0f).coerceIn(0f, 0.72f)))
        )
    }
}

private fun mix(from: Color, to: Color, amount: Float): Color {
    val t = amount.coerceIn(0f, 1f)
    return Color(
        red = from.red + (to.red - from.red) * t,
        green = from.green + (to.green - from.green) * t,
        blue = from.blue + (to.blue - from.blue) * t,
        alpha = from.alpha + (to.alpha - from.alpha) * t
    )
}
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(20.dp),
    elevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = toolboxPalette()
    val lightMode = palette.backgroundTop.luminance() > 0.45f
    val elevation = when {
        !lightMode -> 0.dp
        elevated -> 18.dp
        else -> 10.dp
    }
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = CardRadius,
                ambientColor = palette.accent.copy(alpha = 0.12f),
                spotColor = Color(0xFF6EA6C8).copy(alpha = 0.18f)
            ),
        shape = CardRadius,
        border = BorderStroke(1.dp, palette.cardBorder),
        colors = CardDefaults.cardColors(containerColor = if (elevated) palette.cardStrong else palette.card, contentColor = palette.text)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (lightMode) 0.32f else 0.08f),
                            palette.card.copy(alpha = 0.96f)
                        )
                    )
                )
                .padding(padding),
            content = content
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    val palette = toolboxPalette()
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = palette.textMuted)
        }
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    icon: ImageVector,
    click: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    loadingText: String = "\u5904\u7406\u4e2d",
    enabled: Boolean = true
) {
    val palette = toolboxPalette()
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.958f else 1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), label = "primaryScale")
    Button(
        onClick = click,
        enabled = enabled && !loading,
        modifier = modifier.scale(scale),
        interactionSource = source,
        shape = InnerRadius,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0284C7),
            contentColor = Color.White,
            disabledContainerColor = palette.accent.copy(alpha = 0.55f),
            disabledContentColor = Color.White.copy(alpha = 0.82f)
        )
    ) {
        AnimatedVisibility(loading, enter = fadeIn(tween(140)), exit = fadeOut(tween(120))) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        }
        AnimatedVisibility(!loading, enter = fadeIn(tween(140)), exit = fadeOut(tween(120))) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(if (loading) loadingText else text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    icon: ImageVector,
    click: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String = "\u5904\u7406\u4e2d"
) {
    val palette = toolboxPalette()
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.958f else 1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), label = "secondaryScale")
    OutlinedButton(
        onClick = click,
        enabled = enabled && !loading,
        modifier = modifier.scale(scale),
        interactionSource = source,
        shape = InnerRadius,
        border = BorderStroke(1.dp, palette.cardBorder),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.cardMuted,
            contentColor = palette.accent
        )
    ) {
        AnimatedVisibility(loading, enter = fadeIn(tween(140)), exit = fadeOut(tween(120))) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = palette.accent
            )
        }
        AnimatedVisibility(!loading, enter = fadeIn(tween(140)), exit = fadeOut(tween(120))) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(if (loading) loadingText else text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SmoothButton(
    text: String,
    icon: ImageVector,
    click: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false
) = PrimaryActionButton(text = text, icon = icon, click = click, modifier = modifier, loading = loading)

@Composable
fun Pressable(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val palette = toolboxPalette()
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.958f else 1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), label = "pressScale")
    val border by animateColorAsState(
        if (selected) palette.accent.copy(alpha = 0.35f) else palette.cardBorder,
        tween(220),
        label = "pressBorder"
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CardRadius)
            .border(1.dp, border, CardRadius)
            .background(if (selected) palette.accentSoft else palette.card, CardRadius)
            .clickable(
                interactionSource = source,
                indication = null,
                onClick = onClick
            )
            .animateContentSize(tween(220, easing = FastOutSlowInEasing))
    ) {
        content()
    }
}

@Composable
fun OptionChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = toolboxPalette()
    val bg by animateColorAsState(if (selected) palette.accent else palette.cardMuted, tween(220), label = "chipBg")
    val fg by animateColorAsState(
        if (selected && palette.accent.luminance() > 0.4f) Color.White else if (selected) Color.White else palette.text,
        tween(220),
        label = "chipFg"
    )
    val border by animateColorAsState(
        if (selected) palette.accent.copy(alpha = 0.55f) else palette.cardBorder,
        tween(220),
        label = "chipBorder"
    )
    Box(
        modifier = modifier
            .clip(PillRadius)
            .border(1.dp, border, PillRadius)
            .background(bg, PillRadius)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun InfoToast(message: String, tone: NoticeTone, dismiss: () -> Unit) {
    val palette = toolboxPalette()
    val accent = when (tone) {
        NoticeTone.Info -> palette.info
        NoticeTone.Success -> palette.success
        NoticeTone.Error -> palette.error
    }
    val icon = when (tone) {
        NoticeTone.Info -> Icons.Rounded.Info
        NoticeTone.Success -> Icons.Rounded.CheckCircle
        NoticeTone.Error -> Icons.Rounded.Error
    }
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        elevated = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "\u5173\u95ed",
                color = accent,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = dismiss)
            )
        }
    }
}

@Composable
fun Notice(message: String, dismiss: () -> Unit) {
    LaunchedEffect(message) {
        delay(3200)
        dismiss()
    }
    InfoToast(message = message, tone = guessNoticeTone(message), dismiss = dismiss)
}

@Composable
fun tintedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = toolboxPalette().accent,
    unfocusedBorderColor = toolboxPalette().cardBorder,
    cursorColor = toolboxPalette().accent,
    focusedContainerColor = toolboxPalette().cardMuted,
    unfocusedContainerColor = toolboxPalette().cardMuted,
    focusedLabelColor = toolboxPalette().accent,
    unfocusedLabelColor = toolboxPalette().textMuted
)

private fun guessNoticeTone(message: String): NoticeTone {
    return when {
        message.contains("\u5931\u8d25") || message.contains("\u6ca1\u6709") || message.contains("\u8bf7\u5148") -> NoticeTone.Error
        message.contains("\u6210\u529f") || message.contains("\u5df2") || message.contains("\u5b8c\u6210") -> NoticeTone.Success
        else -> NoticeTone.Info
    }
}

fun statusIcon(status: TaskStatus): ImageVector = when (status) {
    TaskStatus.Running -> Icons.Rounded.Pending
    TaskStatus.Success -> Icons.Rounded.CheckCircle
    TaskStatus.Failed -> Icons.Rounded.Error
}
