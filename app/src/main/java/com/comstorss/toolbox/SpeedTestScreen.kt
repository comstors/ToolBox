package com.comstorss.toolbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun SpeedTestScreen(vm: ToolboxViewModel) {
    val state by vm.speedTest.collectAsState()
    val testing = state.phase == SpeedTestPhase.Testing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "下载测速 Lite",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "使用公开测试源估算当前下载速度，结果仅供参考。",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = toolboxPalette().textMuted
                        )
                    }
                    SpeedGauge(state = state)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        if (testing) {
                            SecondaryActionButton(
                                text = "\u505c\u6b62\u6d4b\u901f",
                                icon = Icons.Rounded.Stop,
                                click = vm::stopSpeedTest,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            PrimaryActionButton(
                                text = if (state.phase == SpeedTestPhase.Idle) "\u5f00\u59cb\u6d4b\u901f" else "\u91cd\u65b0\u6d4b\u901f",
                                icon = if (state.phase == SpeedTestPhase.Idle) Icons.Rounded.Speed else Icons.Rounded.Replay,
                                click = vm::startSpeedTest,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            SpeedResultCard(state)
        }
    }
}

@Composable
private fun SpeedGauge(state: SpeedTestUiState) {
    val palette = toolboxPalette()
    val animatedSpeed by animateFloatAsState(
        targetValue = state.currentMbps.toFloat().coerceAtLeast(0f),
        animationSpec = tween(260),
        label = "speedGaugeValue"
    )
    val progress = (animatedSpeed / 300f).coerceIn(0f, 1f)
    val arcColor by animateColorAsState(
        targetValue = when {
            animatedSpeed >= 100f -> palette.success
            animatedSpeed >= 30f -> palette.accent
            else -> palette.info
        },
        animationSpec = tween(220),
        label = "speedGaugeColor"
    )

    Box(modifier = Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(210.dp)) {
            val stroke = 16.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = palette.cardMuted,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = arcColor,
                startAngle = 135f,
                sweepAngle = 270f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(formatSpeed(animatedSpeed.toDouble()), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
            Text("Mbps", style = MaterialTheme.typography.titleMedium, color = palette.accent, fontWeight = FontWeight.Bold)
            Text("${formatMbPerSecond(animatedSpeed.toDouble())} MB/s", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
        }
    }
}

@Composable
private fun SpeedResultCard(state: SpeedTestUiState) {
    val palette = toolboxPalette()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(palette.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.NetworkCheck, null, tint = palette.accent)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(resultTitle(state), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(resultSubtitle(state), color = palette.textMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SpeedMetric("\u6700\u5927", formatSpeed(state.maxMbps), "Mbps", Modifier.weight(1f))
                SpeedMetric("\u5e73\u5747", formatSpeed(state.averageMbps), "Mbps", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SpeedMetric("\u8017\u65f6", formatElapsed(state.elapsedMillis), "", Modifier.weight(1f))
                SpeedMetric("\u7f51\u7edc", state.network.label, "", Modifier.weight(1f))
            }
            SpeedMetric("\u8bc4\u7ea7", state.rating.label, "", Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SpeedMetric(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    val palette = toolboxPalette()
    Box(modifier = modifier.clip(MaterialTheme.shapes.large).background(palette.cardMuted).padding(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = palette.textMuted, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1)
            if (unit.isNotBlank()) Text(unit, color = palette.textMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun resultTitle(state: SpeedTestUiState): String = when (state.phase) {
    SpeedTestPhase.Testing -> "\u6b63\u5728\u6d4b\u901f"
    SpeedTestPhase.Finished -> "\u6d4b\u901f\u5b8c\u6210"
    SpeedTestPhase.Failed -> "\u6d4b\u901f\u5931\u8d25"
    SpeedTestPhase.Stopped -> "\u5df2\u505c\u6b62\u6d4b\u901f"
    SpeedTestPhase.Idle -> "\u7b49\u5f85\u5f00\u59cb"
}

private fun resultSubtitle(state: SpeedTestUiState): String {
    state.errorMessage?.let { return it }
    return when (state.phase) {
        SpeedTestPhase.Testing -> "\u6b63\u5728\u8bfb\u53d6\u6d4b\u8bd5\u6570\u636e\uff0c\u53ef\u968f\u65f6\u505c\u6b62\u3002"
        SpeedTestPhase.Finished -> "\u5df2\u7edf\u8ba1\u672c\u6b21\u4e0b\u8f7d\u6d4b\u901f\u7ed3\u679c\u3002"
        SpeedTestPhase.Stopped -> "\u6d4b\u901f\u5df2\u505c\u6b62\uff0c\u663e\u793a\u5df2\u6536\u96c6\u7684\u7ed3\u679c\u3002"
        SpeedTestPhase.Failed -> "\u8bf7\u68c0\u67e5\u7f51\u7edc\u540e\u91cd\u8bd5\u3002"
        SpeedTestPhase.Idle -> "\u70b9\u51fb\u5f00\u59cb\u540e\u4f1a\u8fdb\u884c\u7ea6 10 \u79d2\u4e0b\u8f7d\u6d4b\u8bd5\u3002"
    }
}

private fun formatSpeed(value: Double): String = String.format(Locale.US, when { value >= 100 -> "%.0f"; value >= 1 -> "%.1f"; value > 0 -> "%.2f"; else -> "%.1f" }, value)
private fun formatMbPerSecond(mbps: Double): String = String.format(Locale.US, "%.2f", mbps / 8.0)
private fun formatElapsed(millis: Long): String = String.format(Locale.US, "%.1fs", millis / 1000.0)