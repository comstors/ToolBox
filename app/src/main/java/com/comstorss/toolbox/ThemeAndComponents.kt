package com.comstorss.toolbox

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
    val dark = when (theme) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    ComstTheme(dark = dark) { AppRoot(vm, theme) }
}

@Composable
fun ComstTheme(dark: Boolean, content: @Composable () -> Unit) {
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
            accent = Color(0xFF38BDF8),
            accentStrong = Color(0xFF0284C7),
            accentSoft = Color(0x1F38BDF8),
            text = Color(0xFFF1F5F9),
            textMuted = Color(0xFFB6C6D8),
            success = Color(0xFF71E3B2),
            error = Color(0xFFFF8A8A),
            info = Color(0xFF8CD7FF),
            glow = Color(0x336FD0FF)
        )
    } else {
        ToolboxPalette(
            backgroundTop = Color(0xFFEDF3F8),
            backgroundBottom = Color(0xFFEDF3F8),
            card = Color(0xFFFFFFFF),
            cardStrong = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFDFEBF6),
            cardMuted = Color(0xFFE0EFFA),
            accent = Color(0xFF0284C7),
            accentStrong = Color(0xFF0284C7),
            accentSoft = Color(0xFFE0EFFA),
            text = Color(0xFF1E293B),
            textMuted = Color(0xFF64748B),
            success = Color(0xFF1B9C6A),
            error = Color(0xFFD84E67),
            info = Color(0xFF157DFF),
            glow = Color(0x26157DFF)
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
                        Brush.verticalGradient(
                            listOf(palette.backgroundTop, palette.backgroundBottom)
                        )
                    )
                ) {
                    content()
                }
            }
        }
    }
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
    loading: Boolean = false
) {
    val palette = toolboxPalette()
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.958f else 1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), label = "primaryScale")
    Button(
        onClick = click,
        enabled = !loading,
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
        Text(if (loading) "\u5904\u7406\u4e2d" else text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    icon: ImageVector,
    click: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val palette = toolboxPalette()
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.958f else 1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), label = "secondaryScale")
    OutlinedButton(
        onClick = click,
        enabled = enabled,
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
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Medium)
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