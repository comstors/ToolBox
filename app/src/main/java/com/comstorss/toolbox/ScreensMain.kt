package com.comstorss.toolbox

import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private data class HeaderContent(
    val eyebrow: String,
    val title: String,
    val subtitle: String,
    val showBack: Boolean = false,
    val compactTitle: Boolean = false
)

@Composable
fun AppRoot(vm: ToolboxViewModel, theme: ThemeMode) {
    var splash by remember { mutableStateOf(true) }
    AnimatedContent(
        targetState = splash,
        transitionSpec = {
            (fadeIn(tween(420)) + slideInVertically(tween(420)) { it / 12 })
                .togetherWith(fadeOut(tween(280))).using(SizeTransform(false))
        },
        label = "splash"
    ) { show ->
        if (show) SplashScreen { splash = false } else MainShell(vm, theme)
    }
}

@Composable
fun SplashScreen(done: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1350)
        done()
    }

    val infinite = rememberInfiniteTransition(label = "splashMotion")
    val breathe by infinite.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val orbit by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1800, easing = FastOutSlowInEasing)),
        label = "orbit"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF07111F),
                        Color(0xFF0B2742),
                        Color(0xFF06101B)
                    )
                )
            )
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        SplashAuroraField(orbit = orbit)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(156.dp)
                        .graphicsLayer {
                            scaleX = breathe
                            scaleY = breathe
                        }
                        .clip(CircleShape)
                        .background(Color(0x3338BDF8))
                        .blur(22.dp)
                )

                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .graphicsLayer {
                            scaleX = breathe
                            scaleY = breathe
                        }
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color(0xFF38BDF8).copy(alpha = 0.10f)
                                )
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.splash_avatar_rounded),
                        contentDescription = "Mingyu Toolbox avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF7DD3FC),
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            translationX = 46f
                            translationY = -44f + orbit * 8f
                            alpha = 0.78f
                        }
                )
            }

            Spacer(Modifier.height(30.dp))

            Text(
                text = "Mingyu Toolbox",
                color = Color(0xFFF8FBFF),
                fontSize = 29.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "\u4f5c\u8005\u5fae\u4fe1 comstorss",
                color = Color(0xFFB8D7F2),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "\u5f00\u6e90\u8f6f\u4ef6\uff0c\u4ec5\u4f9b\u81ea\u5df1\u4f7f\u7528",
                color = Color(0xFF7F9FBA),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(34.dp))

            SplashLoadingRail(progress = orbit)
        }
    }
}

@Composable
private fun SplashAuroraField(orbit: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val firstCenter = Offset(size.width * (0.25f + orbit * 0.08f), size.height * 0.26f)
        val secondCenter = Offset(size.width * 0.82f, size.height * (0.72f - orbit * 0.04f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x5540C4FF), Color.Transparent),
                center = firstCenter,
                radius = size.minDimension * 0.55f
            ),
            radius = size.minDimension * 0.55f,
            center = firstCenter
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x3338BDF8), Color.Transparent),
                center = secondCenter,
                radius = size.minDimension * 0.62f
            ),
            radius = size.minDimension * 0.62f,
            center = secondCenter
        )
        drawOval(
            color = Color.White.copy(alpha = 0.08f),
            topLeft = Offset(size.width * 0.18f, size.height * 0.38f),
            size = Size(size.width * 0.64f, size.height * 0.18f),
            style = Stroke(width = 1.2.dp.toPx())
        )
    }
}

@Composable
private fun SplashLoadingRail(progress: Float) {
    val trackShape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.52f)
            .height(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(trackShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(trackShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0x2238BDF8),
                            Color.Transparent
                        )
                    )
                )
                .blur(2.dp)
        )

        repeat(3) { index ->
            val phase = (progress + index * 0.24f) % 1f
            val alphaValue = when {
                phase < 0.16f -> phase / 0.16f
                phase > 0.82f -> (1f - phase) / 0.18f
                else -> 1f
            }.coerceIn(0.18f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .graphicsLayer {
                        translationX = (phase - 0.5f) * 150f
                    }
                    .alpha(alphaValue),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = if (index == 1) 46.dp else 30.dp, height = 5.dp)
                        .clip(trackShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF7DD3FC),
                                    Color(0xFF38BDF8),
                                    Color.Transparent
                                )
                            )
                        )
                        .blur(if (index == 1) 0.dp else 0.4.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFFBAE6FD))
                .graphicsLayer {
                    translationX = (progress - 0.5f) * 150f
                    scaleX = 0.88f + 0.12f * kotlin.math.sin(progress * Math.PI.toFloat() * 2f)
                    scaleY = scaleX
                    alpha = 0.82f
                }
        )
    }
}
@Composable
fun MainShell(vm: ToolboxViewModel, theme: ThemeMode) {
    var tab by remember { mutableStateOf(MainTab.Home) }
    var route by remember { mutableStateOf(Route.Home) }
    val notice by vm.notice.collectAsState()
    val reader by vm.reader.collectAsState()
    BackHandler(enabled = route != Route.Home || tab != MainTab.Home) {
        if (route != Route.Home) route = Route.Home else tab = MainTab.Home
    }
    val header = headerFor(route, tab, reader)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(visible = route == Route.Home, enter = fadeIn(tween(220)), exit = fadeOut(tween(180))) {
                BottomTabs(current = tab, change = { tab = it })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp)
            ) {
                ShellHeader(header = header, onBack = { route = Route.Home })
                AnimatedContent(
                    targetState = route to tab,
                                        transitionSpec = {
                        val direction = if (screenMotionIndex(targetState) >= screenMotionIndex(initialState)) 1 else -1
                        (fadeIn(tween(durationMillis = 170, delayMillis = 25, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(tween(durationMillis = 220, easing = FastOutSlowInEasing)) { direction * it / 18 } +
                            scaleIn(initialScale = 0.992f, animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                fadeOut(tween(durationMillis = 90, easing = LinearOutSlowInEasing)) +
                                    slideOutHorizontally(tween(durationMillis = 110, easing = LinearOutSlowInEasing)) { -direction * it / 28 } +
                                    scaleOut(targetScale = 0.998f, animationSpec = tween(durationMillis = 90, easing = LinearOutSlowInEasing))
                            )
                            .using(SizeTransform(clip = false))
                    },                    label = "content"
                ) { state ->
                    when (state.first) {
                        Route.Convert -> ConvertScreen(vm)
                        Route.Video -> VideoScreen(vm)
                        Route.SpeedTest -> SpeedTestScreen(vm)
                        Route.Reader -> ReaderScreen(vm)
                        Route.Home -> when (state.second) {
                            MainTab.Home -> HomeScreen(vm.modules) { route = it }
                            MainTab.Tasks -> TaskScreen(vm)
                            MainTab.History -> HistoryScreen(vm)
                            MainTab.Settings -> SettingsScreen(vm, theme, vm::setTheme, vm::clearCache)
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = notice != null,
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { -it / 2 },
                exit = fadeOut(tween(180)),
                modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Notice(notice.orEmpty(), vm::clearNotice)
            }
        }
    }
}

@Composable
private fun ShellHeader(header: HeaderContent, onBack: () -> Unit) {
    val palette = toolboxPalette()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedContent(
            targetState = header,
            transitionSpec = {
                fadeIn(tween(durationMillis = 120, delayMillis = 30, easing = LinearOutSlowInEasing))
                    .togetherWith(fadeOut(tween(durationMillis = 90, easing = LinearOutSlowInEasing)))
            },
            label = "headerTransition"
        ) { data ->
            val titleStyle = if (data.compactTitle) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium
            Column(
                modifier = Modifier.fillMaxWidth().offset(y = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (data.showBack) {
                    Text(
                        text = "\u8fd4\u56de\u9996\u9875\u83dc\u5355",
                        color = palette.textMuted,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = onBack
                            )
                            .padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = data.title,
                    style = titleStyle,
                    fontSize = if (data.compactTitle) compactReaderTitleSize(data.title) else titleStyle.fontSize,
                    fontWeight = FontWeight.Black,
                    maxLines = if (data.compactTitle) 1 else Int.MAX_VALUE,
                    overflow = if (data.compactTitle) TextOverflow.Ellipsis else TextOverflow.Clip,
                    modifier = Modifier.fillMaxWidth()
                )
                if (data.subtitle.isNotBlank()) {
                    Text(
                        text = data.subtitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.accent,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun compactReaderTitleSize(title: String) = when {
    title.length <= 10 -> 24.sp
    title.length <= 16 -> 21.sp
    title.length <= 24 -> 18.sp
    title.length <= 34 -> 16.sp
    else -> 15.sp
}

@Composable
fun BottomTabs(current: MainTab, change: (MainTab) -> Unit) {
    val palette = toolboxPalette()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(palette.cardStrong)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(36.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val tabWidth = maxWidth / MainTab.entries.size
            val pillOffset by animateDpAsState(
                targetValue = tabWidth * current.ordinal,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
                label = "gliderOffset"
            )
            Box(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(tabWidth)
                    .fillMaxSize()
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(palette.accentSoft)
            )
            Row(modifier = Modifier.fillMaxSize()) {
                MainTab.entries.forEach { item ->
                    val selected = current == item
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.08f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                        label = "tabScale"
                    )
                    val color by animateColorAsState(
                        targetValue = if (selected) palette.accent else palette.textMuted,
                        animationSpec = tween(180),
                        label = "tabColor"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { change(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(item.icon, item.label, modifier = Modifier.size(22.dp).graphicsLayer { scaleX = scale; scaleY = scale }, tint = color)
                            Text(item.label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = if (selected) FontWeight.Black else FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun HomeScreen(modules: List<ToolModule>, open: (Route) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionHeader(
                        title = "\u672c\u5730\u5de5\u5177\u7bb1",
                        subtitle = "\u683c\u5f0f\u8f6c\u6362\u548c\u89c6\u9891\u4e0b\u8f7d\u90fd\u5728\u672c\u5730\u5b8c\u6210\uff0c\u754c\u9762\u66f4\u987a\u624b\u3002"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HeroStat("\u6a21\u5757", modules.size.toString(), Modifier.weight(1f))
                        HeroStat("\u8054\u7f51\u529f\u80fd", modules.count { it.network }.toString(), Modifier.weight(1f))
                    }
                }
            }
        }
        items(modules, key = { it.id }) { module ->
            ModuleCard(m = module, click = { open(module.route) })
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String, modifier: Modifier = Modifier) {
    val palette = toolboxPalette()
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(palette.accentSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
        }
    }
}

@Composable
fun ModuleCard(m: ToolModule, click: () -> Unit) = Pressable(onClick = click) {
    val palette = toolboxPalette()
    Row(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(58.dp).clip(MaterialTheme.shapes.large).background(palette.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(m.icon, null, modifier = Modifier.size(28.dp), tint = palette.accent)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(m.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(m.desc, color = palette.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (m.network) {
            Box(modifier = Modifier.clip(CircleShape).background(palette.cardMuted).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("\u9700\u8054\u7f51", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
            }
        }
    }
}

@Composable
fun ConvertScreen(vm: ToolboxViewModel) {
    var formatTab by remember { mutableStateOf("image") }
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var options by remember { mutableStateOf(PdfOptions()) }
    var wordUri by remember { mutableStateOf<Uri?>(null) }
    var wordName by remember { mutableStateOf("\u672a\u9009\u62e9\u6587\u4ef6") }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        if (it.isNotEmpty()) images = it
    }
    val wordPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            wordUri = it
            wordName = it.lastPathSegment ?: "DOCX"
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(8.dp), elevated = true) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionChip(
                        text = "\u56fe\u7247",
                        selected = formatTab == "image",
                        onClick = { formatTab = "image" },
                        modifier = Modifier.weight(1f)
                    )
                    OptionChip(
                        text = "Word",
                        selected = formatTab == "word",
                        onClick = { formatTab = "word" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            AnimatedContent(
                targetState = formatTab,
                                transitionSpec = {
                    val direction = if (formatMotionIndex(targetState) >= formatMotionIndex(initialState)) 1 else -1
                    (fadeIn(tween(durationMillis = 150, delayMillis = 20, easing = FastOutSlowInEasing)) +
                        slideInHorizontally(tween(durationMillis = 200, easing = FastOutSlowInEasing)) { direction * it / 22 } +
                        scaleIn(initialScale = 0.994f, animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            fadeOut(tween(durationMillis = 80, easing = LinearOutSlowInEasing)) +
                                slideOutHorizontally(tween(durationMillis = 95, easing = LinearOutSlowInEasing)) { -direction * it / 32 }
                        )
                        .using(SizeTransform(clip = false))
                },                label = "formatTab"
            ) { tab ->
                if (tab == "image") {
                    ConvertPanel(
                        icon = Icons.Rounded.Image,
                        title = "\u56fe\u7247\u8f6c PDF",
                        detail = if (images.isEmpty()) "\u8f93\u51fa\u76ee\u5f55\uff1a Download/MingyuToolBox/PDF" else "\u5df2\u9009\u62e9 ${images.size} \u5f20\u56fe\u7247"
                    ) {
                        SecondaryActionButton(
                            text = "\u9009\u62e9\u56fe\u7247",
                            icon = Icons.Rounded.Image,
                            click = { imagePicker.launch(arrayOf("image/jpeg", "image/png", "image/webp")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OptionRow("\u9875\u9762", PdfPageMode.entries.map { it.label }, options.pageMode.label) { selected ->
                            options = options.copy(pageMode = PdfPageMode.entries.first { it.label == selected })
                        }
                        OptionRow("\u65b9\u5411", PdfOrientation.entries.map { it.label }, options.orientation.label) { selected ->
                            options = options.copy(orientation = PdfOrientation.entries.first { it.label == selected })
                        }
                        OptionRow("\u9002\u914d", PdfImageFit.entries.map { it.label }, options.imageFit.label) { selected ->
                            options = options.copy(imageFit = PdfImageFit.entries.first { it.label == selected })
                        }
                        PrimaryActionButton(
                            text = "\u751f\u6210 PDF",
                            icon = Icons.Rounded.Download,
                            click = { vm.imagesToPdf(images, options) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    ConvertPanel(
                        icon = Icons.Rounded.Article,
                        title = "Word \u8f6c PDF",
                        detail = wordName
                    ) {
                        SecondaryActionButton(
                            text = "\u9009\u62e9 Word",
                            icon = Icons.Rounded.Article,
                            click = { wordPicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        SoftNote("\u652f\u6301 .docx \u7b80\u5316\u8f6c\u6362\uff0c\u590d\u6742\u6392\u7248\u53ef\u80fd\u4e0e\u539f\u6587\u6863\u4e0d\u540c\u3002")
                        PrimaryActionButton(
                            text = "\u751f\u6210 PDF",
                            icon = Icons.Rounded.Download,
                            click = { wordUri?.let(vm::wordToPdf) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ConvertPanel(icon: ImageVector, title: String, detail: String, content: @Composable ColumnScope.() -> Unit) {
    val palette = toolboxPalette()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).clip(MaterialTheme.shapes.large).background(palette.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = palette.accent, modifier = Modifier.size(26.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(detail, color = palette.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            content()
        }
    }
}

@Composable
fun OptionRow(title: String, values: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = toolboxPalette().textMuted)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { value ->
                OptionChip(text = value, selected = value == selected, onClick = { onSelected(value) })
            }
        }
    }
}

@Composable
private fun SoftNote(text: String) {
    val palette = toolboxPalette()
    Box(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(palette.cardMuted).padding(14.dp)
    ) {
        Text(text, color = palette.textMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun VideoScreen(vm: ToolboxViewModel) {
    var url by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val info by vm.video.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val parsing = tasks.any { it.status == TaskStatus.Running && it.title.contains("\u89e3\u6790") }
    val downloading = tasks.any { it.status == TaskStatus.Running && it.title.contains("\u4e0b\u8f7d") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(
                        title = "\u7c98\u8d34\u89c6\u9891\u94fe\u63a5",
                        subtitle = "\u652f\u6301\u6296\u97f3\u548c Bilibili \u5206\u4eab\u94fe\u63a5\uff0c\u5148\u89e3\u6790\uff0c\u518d\u4e0b\u8f7d\u3002"
                    )
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("\u6296\u97f3\u6216 Bilibili \u5206\u4eab\u94fe\u63a5") },
                        minLines = 3,
                        shape = MaterialTheme.shapes.large,
                        colors = tintedFieldColors()
                    )
                    SecondaryActionButton(
                        text = "\u7c98\u8d34\u526a\u8d34\u677f\u94fe\u63a5",
                        icon = Icons.Rounded.ContentPaste,
                        click = {
                            val pasted = clipboard.getText()?.text?.trim().orEmpty()
                            if (pasted.isNotEmpty()) {
                                url = pasted
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !parsing && !downloading
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryActionButton(
                            text = "\u89e3\u6790\u94fe\u63a5",
                            icon = Icons.Rounded.Pending,
                            click = { vm.parseVideo(url) },
                            modifier = Modifier.weight(1f),
                            enabled = !downloading,
                            loading = parsing,
                            loadingText = "\u89e3\u6790\u4e2d"
                        )
                        PrimaryActionButton(
                            text = "\u4e0b\u8f7d",
                            icon = Icons.Rounded.Download,
                            click = vm::downloadVideo,
                            modifier = Modifier.weight(1f),
                            loading = downloading,
                            loadingText = "\u4e0b\u8f7d\u4e2d",
                            enabled = !parsing
                        )
                    }
                }
            }
        }
        item {
            AnimatedVisibility(visible = info != null, enter = fadeIn(tween(160, delayMillis = 30, easing = FastOutSlowInEasing)) + slideInVertically(tween(220, easing = FastOutSlowInEasing)) { it / 24 } + scaleIn(initialScale = 0.99f, animationSpec = tween(220, easing = FastOutSlowInEasing)), exit = fadeOut(tween(90, easing = LinearOutSlowInEasing)) + scaleOut(targetScale = 0.995f, animationSpec = tween(90, easing = LinearOutSlowInEasing))) {
                info?.let { VideoCard(it) }
            }
        }
    }
}

@Composable
fun VideoCard(info: VideoInfo) {
    val palette = toolboxPalette()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.clip(CircleShape).background(palette.accentSoft).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(info.platform, style = MaterialTheme.typography.labelMedium, color = palette.accent)
            }
            Text(info.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("\u4f5c\u8005\uff1a${info.author}", color = palette.textMuted)
            Text("\u65f6\u957f\uff1a${info.duration}", color = palette.textMuted)
            info.coverUrl?.let { cover ->
                Text("\u5c01\u9762\uff1a$cover", color = palette.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                if (info.directUrl.isNullOrBlank()) "\u72b6\u6001\uff1a\u6682\u65e0\u53ef\u4e0b\u8f7d\u5730\u5740" else "\u72b6\u6001\uff1a\u53ef\u4e0b\u8f7d\u5230 Download/MingyuToolBox/Video",
                color = if (info.directUrl.isNullOrBlank()) palette.error else palette.success
            )
            info.warning?.let { Text(it, color = palette.textMuted) }
        }
    }
}


private fun screenMotionIndex(state: Pair<Route, MainTab>): Int {
    val (route, tab) = state
    return when (route) {
        Route.Home -> tab.ordinal
        Route.Convert -> MainTab.entries.size
        Route.Video -> MainTab.entries.size + 1
        Route.SpeedTest -> MainTab.entries.size + 2
        Route.Reader -> MainTab.entries.size + 3
    }
}

private fun formatMotionIndex(tab: String): Int = when (tab) {
    "word" -> 1
    else -> 0
}
private fun headerFor(route: Route, tab: MainTab, reader: ReaderUiState): HeaderContent {
    return when (route) {
        Route.Convert -> HeaderContent(
            eyebrow = "\u683c\u5f0f\u8f6c\u6362",
            title = "\u751f\u6210\u672c\u5730 PDF",
            subtitle = "\u56fe\u7247\u548c Word \u6587\u4ef6\u90fd\u8d70\u672c\u5730\u5904\u7406\u6d41\u7a0b\u3002",
            showBack = true
        )
        Route.Video -> HeaderContent(
            eyebrow = "\u89c6\u9891\u4e0b\u8f7d",
            title = "\u89e3\u6790\u540e\u518d\u4fdd\u5b58",
            subtitle = "\u5148\u8bc6\u522b\u5e73\u53f0\u548c\u72b6\u6001\uff0c\u518d\u51b3\u5b9a\u662f\u5426\u80fd\u4e0b\u8f7d\u3002",
            showBack = true
        )
        Route.SpeedTest -> HeaderContent(
            eyebrow = "\u7f51\u7edc\u6d4b\u901f",
            title = "\u4e0b\u8f7d\u901f\u5ea6\u4f30\u7b97",
            subtitle = "\u8f7b\u91cf\u6d4b\u8bd5\u5f53\u524d\u7f51\u7edc\u4e0b\u8f7d\u8868\u73b0\u3002",
            showBack = true
        )
        Route.Reader -> HeaderContent(
            eyebrow = "",
            title = reader.activeBook?.title ?: "\u5c0f\u8bf4\u9605\u8bfb\u5668",
            subtitle = reader.currentChapterTitle(),
            showBack = true,
            compactTitle = true
        )
        Route.Home -> when (tab) {
            MainTab.Home -> HeaderContent("Mingyu Toolbox", "\u9996\u9875", "\u672c\u5730\u5de5\u5177\u548c\u5e38\u7528\u6d41\u7a0b\u90fd\u4ece\u8fd9\u91cc\u8fdb\u5165\u3002")
            MainTab.Tasks -> HeaderContent("\u5f53\u524d\u4efb\u52a1", "\u4efb\u52a1", "\u8fdb\u884c\u4e2d\u7684\u8f6c\u6362\u548c\u4e0b\u8f7d\u4f1a\u663e\u793a\u5728\u8fd9\u91cc\u3002")
            MainTab.History -> HeaderContent("\u672c\u5730\u8bb0\u5f55", "\u5386\u53f2", "\u5df2\u7ecf\u751f\u6210\u6216\u4e0b\u8f7d\u8fc7\u7684\u6587\u4ef6\u4f1a\u4fdd\u7559\u5728\u8fd9\u91cc\u3002")
            MainTab.Settings -> HeaderContent("\u504f\u597d\u8bbe\u7f6e", "\u8bbe\u7f6e", "\u4e3b\u9898\u3001\u7f13\u5b58\u548c\u5e94\u7528\u4fe1\u606f\u90fd\u96c6\u4e2d\u5728\u8fd9\u91cc\u3002")
        }
    }
}

private fun ReaderUiState.currentChapterTitle(): String {
    val book = activeBook ?: return "\u672c\u5730\u4e66\u67b6"
    if (paragraphs.isEmpty()) return "\u6b63\u6587\u9605\u8bfb\u4e2d"
    val safeIndex = book.currentParagraph.coerceIn(0, (paragraphs.size - 1).coerceAtLeast(0))
    for (index in safeIndex downTo 0) {
        val title = paragraphs[index].trim().take(42)
        if (isReaderChapterTitle(title)) return title
    }
    return "\u6b63\u6587\u9605\u8bfb\u4e2d"
}

private fun isReaderChapterTitle(text: String): Boolean {
    if (text.isBlank()) return false
    val pattern = Regex("^(第\\s*[0-9零一二三四五六七八九十百千万]+\\s*[章节回卷].*|Chapter\\s+\\d+.*)", RegexOption.IGNORE_CASE)
    return pattern.containsMatchIn(text.trim())
}