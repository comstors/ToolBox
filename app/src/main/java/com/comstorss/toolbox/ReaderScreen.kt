package com.comstorss.toolbox

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import kotlin.math.absoluteValue

private enum class ReaderOverlay { None, Controls, Toc, Settings }

private data class TocEntry(val title: String, val index: Int)

@Composable
fun ReaderScreen(vm: ToolboxViewModel) {
    val state by vm.reader.collectAsState()
    val txtPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let(vm::importReaderTxt)
    }

    AnimatedContent(
        targetState = state.activeBook?.id,
        transitionSpec = {
            (fadeIn(tween(180, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.992f, animationSpec = tween(220, easing = FastOutSlowInEasing)))
                .togetherWith(fadeOut(tween(120, easing = LinearOutSlowInEasing)) + scaleOut(targetScale = 0.998f, animationSpec = tween(120, easing = LinearOutSlowInEasing)))
                .using(SizeTransform(clip = false))
        },
        label = "readerMode"
    ) { activeId ->
        if (activeId == null) {
            ReaderShelf(
                state = state,
                importTxt = { txtPicker.launch(arrayOf("text/plain", "text/*", "application/octet-stream")) },
                openBook = vm::openReaderBook,
                deleteBook = vm::deleteReaderBook
            )
        } else {
            ReaderContent(
                state = state,
                close = vm::closeReaderBook,
                saveProgress = vm::saveReaderProgress,
                changeFont = vm::adjustReaderFont,
                changeLine = vm::adjustReaderLineSpacing,
                setBackground = vm::setReaderBackground,
                setPageMode = vm::setReaderPageMode,
                savePageProgress = vm::saveReaderPageProgress
            )
        }
    }
}

@Composable
private fun ReaderShelf(
    state: ReaderUiState,
    importTxt: () -> Unit,
    openBook: (ReaderBook) -> Unit,
    deleteBook: (ReaderBook) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(
                        title = "\u672c\u5730 TXT \u9605\u8bfb",
                        subtitle = "\u5bfc\u5165\u5c0f\u8bf4\u540e\u4f1a\u81ea\u52a8\u4fdd\u5b58\u9605\u8bfb\u4f4d\u7f6e\uff0c\u4e0b\u6b21\u6253\u5f00\u7ee7\u7eed\u770b\u3002"
                    )
                    PrimaryActionButton(
                        text = "\u5bfc\u5165 TXT \u5c0f\u8bf4",
                        icon = Icons.Rounded.Add,
                        click = importTxt,
                        modifier = Modifier.fillMaxWidth(),
                        loading = state.loading,
                        loadingText = "\u8bfb\u53d6\u4e2d"
                    )
                    state.errorMessage?.let { message ->
                        Text(message, color = toolboxPalette().error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        if (state.books.isEmpty()) {
            item { ReaderEmptyCard() }
        } else {
            items(state.books, key = { it.id }) { book ->
                ReaderBookCard(book = book, open = { openBook(book) }, delete = { deleteBook(book) })
            }
        }
    }
}

@Composable
private fun ReaderEmptyCard() {
    val palette = toolboxPalette()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(palette.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.MenuBook, null, tint = palette.accent)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("\u4e66\u67b6\u8fd8\u662f\u7a7a\u7684", fontWeight = FontWeight.SemiBold)
                Text("\u5148\u5bfc\u5165\u4e00\u672c TXT\uff0c\u672c\u5730\u9605\u8bfb\u5668\u4f1a\u8bb0\u4f4f\u4f60\u770b\u5230\u54ea\u91cc\u3002", color = palette.textMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ReaderBookCard(book: ReaderBook, open: () -> Unit, delete: () -> Unit) {
    val palette = toolboxPalette()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(46.dp).clip(MaterialTheme.shapes.large).background(palette.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MenuBook, null, tint = palette.accent)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("\u8fdb\u5ea6 ${book.progressPercent()} \u00b7 ${book.lastReadText()}", color = palette.textMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            LinearProgressIndicator(
                progress = { book.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = palette.accent,
                trackColor = palette.cardMuted
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryActionButton(text = "\u7ee7\u7eed\u9605\u8bfb", icon = Icons.Rounded.MenuBook, click = open, modifier = Modifier.weight(1f))
                SecondaryActionButton(text = "\u79fb\u9664", icon = Icons.Rounded.Delete, click = delete, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ReaderContent(
    state: ReaderUiState,
    close: () -> Unit,
    saveProgress: (Int, Int) -> Unit,
    changeFont: (Float) -> Unit,
    changeLine: (Float) -> Unit,
    setBackground: (ReaderBackground) -> Unit,
    setPageMode: (ReaderPageMode) -> Unit,
    savePageProgress: (Int, Int, Float) -> Unit
) {
    val book = state.activeBook ?: return
    val listState = rememberLazyListState()
    var overlay by remember(book.id) { mutableStateOf(ReaderOverlay.None) }
    var jumpToParagraph by remember(book.id) { mutableStateOf<Int?>(null) }
    val colors = readerColors(state.background)
    val toc = remember(state.paragraphs) { buildToc(state.paragraphs) }

    LaunchedEffect(book.id) {
        listState.scrollToItem(book.currentParagraph.coerceAtLeast(0), book.scrollOffset.coerceAtLeast(0))
    }
    LaunchedEffect(book.id, state.paragraphs.size, state.pageMode) {
        if (state.pageMode == ReaderPageMode.Scroll) {
            while (true) {
                delay(900)
                saveProgress(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
            }
        }
    }
    LaunchedEffect(jumpToParagraph, state.pageMode) {
        val target = jumpToParagraph ?: return@LaunchedEffect
        if (state.pageMode == ReaderPageMode.Scroll) {
            listState.animateScrollToItem(target.coerceAtLeast(0))
            jumpToParagraph = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colors.background)
    ) {
        if (state.loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("\u6b63\u5728\u8bfb\u53d6 TXT...", color = colors.text.copy(alpha = 0.72f))
            }
        } else {
            if (state.pageMode == ReaderPageMode.Horizontal) {
                ReaderPagedContent(
                    state = state,
                    book = book,
                    colors = colors,
                    toggleControls = { overlay = if (overlay == ReaderOverlay.None) ReaderOverlay.Controls else ReaderOverlay.None },
                    saveProgress = saveProgress,
                    savePageProgress = savePageProgress,
                    jumpToParagraph = jumpToParagraph,
                    onJumpConsumed = { jumpToParagraph = null }
                )
            } else {
                ReaderScrollContent(
                    state = state,
                    colors = colors,
                    listState = listState,
                    toggleControls = { overlay = if (overlay == ReaderOverlay.None) ReaderOverlay.Controls else ReaderOverlay.None }
                )
            }
        }

        ReaderSideGear(
            visible = overlay != ReaderOverlay.Settings,
            open = { overlay = ReaderOverlay.Settings },
            modifier = Modifier.align(Alignment.CenterEnd).offset(x = 7.dp)
        )

        ReaderBottomControls(
            visible = overlay == ReaderOverlay.Controls,
            book = book,
            close = close,
            openToc = { overlay = ReaderOverlay.Toc },
            toggleNight = { setBackground(if (state.background == ReaderBackground.Dark) ReaderBackground.Warm else ReaderBackground.Dark) },
            openSettings = { overlay = ReaderOverlay.Settings },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        ReaderTocPanel(
            visible = overlay == ReaderOverlay.Toc,
            toc = toc,
            close = { overlay = ReaderOverlay.Controls },
            jumpTo = { index ->
                overlay = ReaderOverlay.None
                saveProgress(index, 0)
            },
            listJump = { index ->
                overlay = ReaderOverlay.None
                jumpToParagraph = index
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        ReaderSettingsPanel(
            visible = overlay == ReaderOverlay.Settings,
            state = state,
            close = { overlay = ReaderOverlay.None },
            changeFont = changeFont,
            changeLine = changeLine,
            setBackground = setBackground,
            setPageMode = setPageMode,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
        )
    }
}


@Composable
private fun ReaderScrollContent(
    state: ReaderUiState,
    colors: ReaderColors,
    listState: androidx.compose.foundation.lazy.LazyListState,
    toggleControls: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(state.activeBook?.id, state.pageMode) { detectTapGestures { toggleControls() } },
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        items(state.paragraphs.size) { index ->
            Text(
                text = state.paragraphs[index],
                color = colors.text,
                fontSize = state.fontSizeSp.sp,
                lineHeight = (state.fontSizeSp * state.lineSpacing).sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReaderPagedContent(
    state: ReaderUiState,
    book: ReaderBook,
    colors: ReaderColors,
    toggleControls: () -> Unit,
    saveProgress: (Int, Int) -> Unit,
    savePageProgress: (Int, Int, Float) -> Unit,
    jumpToParagraph: Int?,
    onJumpConsumed: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val pages = remember(state.paragraphs, state.fontSizeSp, state.lineSpacing, maxWidth, maxHeight) {
            paginateParagraphs(
                paragraphs = state.paragraphs,
                fontSizeSp = state.fontSizeSp,
                lineSpacing = state.lineSpacing,
                pageWidthDp = maxWidth.value,
                pageHeightDp = maxHeight.value
            )
        }
        val initialPage = remember(book.id, pages.size) {
            pages.indexOfLast { it.paragraphIndex <= book.currentParagraph }.coerceAtLeast(0)
        }
        val pagerState = rememberPagerState(initialPage = initialPage) { pages.size.coerceAtLeast(1) }

        LaunchedEffect(pagerState.currentPage, pages) {
            pages.getOrNull(pagerState.currentPage)?.let {
                val pageProgress = ((pagerState.currentPage + 1).toFloat() / pages.size.coerceAtLeast(1)).coerceIn(0f, 1f)
                savePageProgress(it.paragraphIndex, 0, pageProgress)
            }
        }
        LaunchedEffect(jumpToParagraph, pages) {
            val target = jumpToParagraph ?: return@LaunchedEffect
            val page = pages.indexOfLast { it.paragraphIndex <= target }.coerceAtLeast(0)
            pagerState.animateScrollToPage(page)
            onJumpConsumed()
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 0.dp
        ) { page ->
            val current = pages.getOrNull(page)
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - pageOffset * 0.18f
                        translationX = -pageOffset * 12f
                    }
                    .pointerInput(book.id, page) { detectTapGestures { toggleControls() } }
                    .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 92.dp)
            ) {
                Text(
                    text = current?.text.orEmpty(),
                    color = colors.text,
                    fontSize = state.fontSizeSp.sp,
                    lineHeight = (state.fontSizeSp * state.lineSpacing).sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private data class ReaderPage(val paragraphIndex: Int, val text: String)

private fun paginateParagraphs(
    paragraphs: List<String>,
    fontSizeSp: Float,
    lineSpacing: Float,
    pageWidthDp: Float,
    pageHeightDp: Float
): List<ReaderPage> {
    if (paragraphs.isEmpty()) return emptyList()
    val usableWidth = (pageWidthDp - 48f).coerceAtLeast(220f)
    val usableHeight = (pageHeightDp - 126f).coerceAtLeast(260f)
    val estimatedCharWidth = (fontSizeSp * 1.04f).coerceAtLeast(12f)
    val estimatedLineHeight = (fontSizeSp * lineSpacing).coerceAtLeast(fontSizeSp + 6f)
    val charsPerLine = (usableWidth / estimatedCharWidth).toInt().coerceIn(8, 44)
    val linesPerPage = (usableHeight / estimatedLineHeight).toInt().coerceIn(5, 42)
    val targetChars = (charsPerLine * linesPerPage * 0.78f).toInt().coerceIn(120, 1700)
    val pages = mutableListOf<ReaderPage>()
    var startIndex = 0
    val buffer = StringBuilder()

    fun flushPage() {
        if (buffer.isNotBlank()) {
            pages += ReaderPage(startIndex, buffer.toString())
            buffer.clear()
        }
    }

    paragraphs.forEachIndexed { index, paragraph ->
        val chunks = paragraph.chunked(targetChars.coerceAtLeast(1)).ifEmpty { listOf("") }
        chunks.forEach { chunk ->
            val nextLength = chunk.length + if (buffer.isEmpty()) 0 else 2
            if (buffer.isNotEmpty() && buffer.length + nextLength > targetChars) {
                flushPage()
            }
            if (buffer.isEmpty()) {
                startIndex = index
            } else {
                buffer.append("\n\n")
            }
            buffer.append(chunk)
        }
    }
    flushPage()
    return pages
}
@Composable
private fun ReaderSideGear(visible: Boolean, open: () -> Unit, modifier: Modifier = Modifier) {
    val palette = toolboxPalette()
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.94f, animationSpec = tween(120)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(width = 42.dp, height = 56.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .background(palette.cardStrong.copy(alpha = 0.72f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = open),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(Icons.Rounded.Settings, null, tint = palette.accent, modifier = Modifier.padding(start = 10.dp).size(20.dp))
        }
    }
}

@Composable
private fun ReaderBottomControls(
    visible: Boolean,
    book: ReaderBook,
    close: () -> Unit,
    openToc: () -> Unit,
    toggleNight: () -> Unit,
    openSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(190, easing = FastOutSlowInEasing)) + slideInVertically(tween(260, easing = FastOutSlowInEasing)) { it / 2 } + scaleIn(initialScale = 0.985f, animationSpec = tween(260, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(120, easing = LinearOutSlowInEasing)) + slideOutVertically(tween(150, easing = LinearOutSlowInEasing)) { it / 3 },
        modifier = modifier.padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(14.dp), elevated = true) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(book.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                        Text("\u5df2\u8bfb ${book.progressPercent()}", color = toolboxPalette().textMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("\u9605\u8bfb\u4e2d", color = toolboxPalette().accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
                LinearProgressIndicator(
                    progress = { book.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                    color = toolboxPalette().accent,
                    trackColor = toolboxPalette().cardMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReaderMiniAction("\u4e66\u67b6", Icons.Rounded.ArrowBack, close, Modifier.weight(1f))
                    ReaderMiniAction("\u76ee\u5f55", Icons.Rounded.List, openToc, Modifier.weight(1f))
                    ReaderMiniAction("\u591c\u95f4", Icons.Rounded.DarkMode, toggleNight, Modifier.weight(1f))
                    ReaderMiniAction("\u8bbe\u7f6e", Icons.Rounded.Settings, openSettings, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ReaderMiniAction(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: () -> Unit, modifier: Modifier = Modifier) {
    val palette = toolboxPalette()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(palette.cardMuted.copy(alpha = 0.82f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = click)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, null, tint = palette.accent, modifier = Modifier.size(19.dp))
        Text(text, color = palette.textMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ReaderTocPanel(
    visible: Boolean,
    toc: List<TocEntry>,
    close: () -> Unit,
    jumpTo: (Int) -> Unit,
    listJump: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(190, easing = FastOutSlowInEasing)) + slideInVertically(tween(260, easing = FastOutSlowInEasing)) { it / 2 },
        exit = fadeOut(tween(120)) + slideOutVertically(tween(150)) { it / 3 },
        modifier = modifier.padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp), elevated = true) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader(title = "\u76ee\u5f55", subtitle = "\u4ece TXT \u7ae0\u8282\u6807\u9898\u4e2d\u81ea\u52a8\u8bc6\u522b", modifier = Modifier.weight(1f))
                    Text("\u6536\u8d77", color = toolboxPalette().accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = close))
                }
                if (toc.isEmpty()) {
                    Text("\u6682\u672a\u8bc6\u522b\u5230\u76ee\u5f55\uff0c\u53ef\u4ee5\u5148\u7ee7\u7eed\u6eda\u52a8\u9605\u8bfb\u3002", color = toolboxPalette().textMuted)
                } else {
                    LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(toc) { entry ->
                            Text(
                                text = entry.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(toolboxPalette().cardMuted.copy(alpha = 0.72f))
                                    .clickable {
                                        jumpTo(entry.index)
                                        listJump(entry.index)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                color = toolboxPalette().text
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderSettingsPanel(
    visible: Boolean,
    state: ReaderUiState,
    close: () -> Unit,
    changeFont: (Float) -> Unit,
    changeLine: (Float) -> Unit,
    setBackground: (ReaderBackground) -> Unit,
    setPageMode: (ReaderPageMode) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180, easing = FastOutSlowInEasing)) + slideInVertically(tween(240, easing = FastOutSlowInEasing)) { it / 8 } + scaleIn(initialScale = 0.96f, animationSpec = tween(240, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.96f, animationSpec = tween(140)),
        modifier = modifier
    ) {
        GlassCard(modifier = Modifier.width(260.dp), padding = PaddingValues(16.dp), elevated = true) {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader(title = "\u9605\u8bfb\u8bbe\u7f6e", subtitle = "\u4e0d\u5f71\u54cd\u5168\u5c40\u4e3b\u9898", modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.Settings, null, tint = toolboxPalette().accent, modifier = Modifier.size(22.dp))
                }
                ReaderSettingStepper("\u5b57\u53f7", "${state.fontSizeSp.toInt()}sp", { changeFont(-1f) }, { changeFont(1f) })
                ReaderSettingStepper("\u884c\u8ddd", "${String.format("%.1f", state.lineSpacing)}x", { changeLine(-0.1f) }, { changeLine(0.1f) })
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("\u7ffb\u9875\u65b9\u5f0f", color = toolboxPalette().textMuted, style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        ReaderPageMode.entries.forEach { item ->
                            OptionChip(text = item.label, selected = state.pageMode == item, onClick = { setPageMode(item) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("\u80cc\u666f", color = toolboxPalette().textMuted, style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        ReaderBackground.entries.forEach { item ->
                            OptionChip(text = item.label, selected = state.background == item, onClick = { setBackground(item) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                SecondaryActionButton(
                    text = if (state.background == ReaderBackground.Dark) "\u5173\u95ed\u591c\u95f4\u6a21\u5f0f" else "\u5f00\u542f\u591c\u95f4\u6a21\u5f0f",
                    icon = Icons.Rounded.DarkMode,
                    click = { setBackground(if (state.background == ReaderBackground.Dark) ReaderBackground.Warm else ReaderBackground.Dark) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("\u5b8c\u6210", color = toolboxPalette().accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.End).clickable(onClick = close))
            }
        }
    }
}

@Composable
private fun ReaderSettingStepper(label: String, value: String, minus: () -> Unit, plus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = toolboxPalette().textMuted, style = MaterialTheme.typography.labelMedium)
        ReaderSmallRound("-", minus)
        Text(value, modifier = Modifier.width(48.dp), color = toolboxPalette().text, fontWeight = FontWeight.SemiBold)
        ReaderSmallRound("+", plus)
    }
}

@Composable
private fun ReaderSmallRound(text: String, click: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(toolboxPalette().accentSoft)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = click),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = toolboxPalette().accent, fontWeight = FontWeight.Black)
    }
}

private data class ReaderColors(val background: Color, val text: Color)

private fun readerColors(background: ReaderBackground): ReaderColors = when (background) {
    ReaderBackground.Paper -> ReaderColors(background = Color(0xFFF3DFBD), text = Color(0xFF2B2114))
    ReaderBackground.Warm -> ReaderColors(background = Color(0xFFE4BC82), text = Color(0xFF2A2114))
    ReaderBackground.Dark -> ReaderColors(background = Color(0xFF090B10), text = Color(0xFFD8DEE9))
}

private fun buildToc(paragraphs: List<String>): List<TocEntry> {
    val pattern = Regex("^(第\\s*[0-9零一二三四五六七八九十百千万]+\\s*[章节回卷].*|Chapter\\s+\\d+.*)", RegexOption.IGNORE_CASE)
    return paragraphs.mapIndexedNotNull { index, text ->
        val title = text.trim().take(42)
        if (pattern.containsMatchIn(title)) TocEntry(title, index) else null
    }.take(120)
}

private fun ReaderBook.progressPercent(): String = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%"

private fun ReaderBook.lastReadText(): String {
    if (lastReadAt <= 0L) return "\u672a\u9605\u8bfb"
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lastReadAt))
}
