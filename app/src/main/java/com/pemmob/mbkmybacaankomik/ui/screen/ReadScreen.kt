package com.pemmob.mbkmybacaankomik.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import com.pemmob.mbkmybacaankomik.data.model.Chapter
import com.pemmob.mbkmybacaankomik.data.model.ChapterDetail
import com.pemmob.mbkmybacaankomik.viewmodel.ReadUiState
import com.pemmob.mbkmybacaankomik.viewmodel.ReadViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadScreen(
    navController: NavController,
    komikSlug: String = "legenda-garuda-putih",
    chapterSlug: String = "bab-1",
    viewModel: ReadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showChapterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Muat data chapter ketika slug berubah
    LaunchedEffect(komikSlug, chapterSlug) {
        viewModel.loadChapter(komikSlug, chapterSlug)
        listState.scrollToItem(0)
    }

    // Fungsi navigasi antar-chapter
    val navigateToChapter: (String) -> Unit = { targetSlug ->
        viewModel.loadChapter(komikSlug, targetSlug)
        coroutineScope.launch {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ReadUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Menyiapkan lembaran komik dari MangaDex...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is ReadUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Gagal Membaca Chapter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadChapter(komikSlug, chapterSlug) },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Coba Lagi")
                                }
                            }
                        }
                    }
                }

                is ReadUiState.Success -> {
                    val detail = state.chapterDetail

                    // ─── AREA BACA VERTICAL (CONTINUOUS WEBTOON SCROLL) ─────
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.toggleControls()
                            },
                        contentPadding = PaddingValues(top = 64.dp, bottom = 84.dp)
                    ) {
                        // Header info chapter di awal halaman
                        item {
                            M3ReaderChapterInfoBanner(
                                chapterTitle = detail.chapterTitle,
                                chapterNumber = detail.chapterNumber,
                                komikTitle = detail.komikTitle
                            )
                        }

                        // Lembar-lembar gambar komik
                        itemsIndexed(detail.pages) { pageIndex, pageUrl ->
                            M3ReaderPageItem(
                                pageUrl = pageUrl,
                                pageNumber = pageIndex + 1,
                                totalPages = detail.pages.size
                            )
                        }

                        // Seksi Selesai & Navigasi Chapter di Bawah
                        item {
                            M3ChapterCompletionSection(
                                chapterDetail = detail,
                                onPrevClick = {
                                    detail.prevChapterSlug?.let { navigateToChapter(it) }
                                },
                                onNextClick = {
                                    detail.nextChapterSlug?.let { navigateToChapter(it) }
                                },
                                onBackToDetail = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    // ─── FLOATING OVERLAY: TOP BAR (MATERIAL 3) ─────────────
                    AnimatedVisibility(
                        visible = state.areControlsVisible,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        M3ReaderTopBar(
                            komikTitle = detail.komikTitle,
                            chapterTitle = "Bab ${detail.chapterNumber}: ${detail.chapterTitle}",
                            onBackClick = { navController.popBackStack() },
                            onOpenChapterList = { showChapterSheet = true }
                        )
                    }

                    // ─── FLOATING OVERLAY: BOTTOM BAR (MATERIAL 3) ──────────
                    AnimatedVisibility(
                        visible = state.areControlsVisible,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        M3ReaderBottomBar(
                            chapterDetail = detail,
                            currentChapterIndex = state.currentChapterIndex,
                            totalChapters = state.allChapters.size,
                            onPrevClick = {
                                detail.prevChapterSlug?.let { navigateToChapter(it) }
                            },
                            onNextClick = {
                                detail.nextChapterSlug?.let { navigateToChapter(it) }
                            },
                            onOpenChapterList = { showChapterSheet = true }
                        )
                    }

                    // ─── MODAL BOTTOM SHEET: PILIH CHAPTER CEPAT (M3) ────────
                    if (showChapterSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showChapterSheet = false },
                            sheetState = sheetState,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            dragHandle = { BottomSheetDefaults.DragHandle() }
                        ) {
                            M3ChapterSelectorSheetContent(
                                chapters = state.allChapters,
                                currentChapterSlug = detail.chapterSlug,
                                onChapterSelected = { selectedSlug ->
                                    showChapterSheet = false
                                    navigateToChapter(selectedSlug)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── ITEM GAMBAR HALAMAN BACA KOMIK (MATERIAL 3) ──────────────────────────────
@Composable
private fun M3ReaderPageItem(
    pageUrl: String,
    pageNumber: Int,
    totalPages: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SubcomposeAsyncImage(
            model = pageUrl,
            contentDescription = "Halaman $pageNumber",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = "Memuat hlm. $pageNumber/$totalPages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Gagal memuat gambar hlm. $pageNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        // Penanda nomor halaman kecil di pojok kanan bawah
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            shape = MaterialTheme.shapes.extraSmall,
            color = Color.Black.copy(alpha = 0.65f)
        ) {
            Text(
                text = "$pageNumber / $totalPages",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
    }
}

// ─── BANNER INFO DI AWAL CHAPTER (MATERIAL 3) ─────────────────────────────────
@Composable
private fun M3ReaderChapterInfoBanner(
    chapterTitle: String,
    chapterNumber: String,
    komikTitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = komikTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Bab $chapterNumber: $chapterTitle",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Scroll ke bawah untuk membaca • Ketuk layar untuk kontrol navigasi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── SEKSI AKHIR CHAPTER (MATERIAL 3) ─────────────────────────────────────────
@Composable
private fun M3ChapterCompletionSection(
    chapterDetail: ChapterDetail,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onBackToDetail: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(46.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Bab ${chapterDetail.chapterNumber} Selesai!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Kamu telah menyelesaikan pembacaan ${chapterDetail.chapterTitle}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Tombol Chapter Selanjutnya (Primary M3 Button)
            if (chapterDetail.nextChapterSlug != null) {
                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Lanjut Bab Selanjutnya",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Ini adalah bab terbaru saat ini ✨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // Baris Tombol Prev & Detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (chapterDetail.prevChapterSlug != null) {
                    OutlinedButton(
                        onClick = onPrevClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Bab Sebelumnya", style = MaterialTheme.typography.labelMedium)
                    }
                }

                OutlinedButton(
                    onClick = onBackToDetail,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Detail Komik", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ─── FLOATING TOP BAR (MATERIAL 3) ───────────────────────────────────────────
@Composable
private fun M3ReaderTopBar(
    komikTitle: String,
    chapterTitle: String,
    onBackClick: () -> Unit,
    onOpenChapterList: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = komikTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onOpenChapterList) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Daftar Chapter",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ─── FLOATING BOTTOM BAR (MATERIAL 3) ─────────────────────────────────────────
@Composable
private fun M3ReaderBottomBar(
    chapterDetail: ChapterDetail,
    currentChapterIndex: Int,
    totalChapters: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onOpenChapterList: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Prev
            val hasPrev = chapterDetail.prevChapterSlug != null
            FilledTonalButton(
                onClick = onPrevClick,
                enabled = hasPrev,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = "Sebelumnya",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text("Prev", style = MaterialTheme.typography.labelMedium)
            }

            // Tombol Tengah: Selector Chapter
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .clickable(onClick = onOpenChapterList)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Bab ${chapterDetail.chapterNumber} (${currentChapterIndex + 1}/$totalChapters)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Tombol Next
            val hasNext = chapterDetail.nextChapterSlug != null
            Button(
                onClick = onNextClick,
                enabled = hasNext,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text("Next", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = "Selanjutnya",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── BOTTOM SHEET DAFTAR SEMUA CHAPTER (MATERIAL 3) ───────────────────────────
@Composable
private fun M3ChapterSelectorSheetContent(
    chapters: List<Chapter>,
    currentChapterSlug: String,
    onChapterSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pilih Chapter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Total ${chapters.size} Bab",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 440.dp)
        ) {
            itemsIndexed(chapters) { _, chapter ->
                val isSelected = chapter.slug == currentChapterSlug
                val itemBg = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                    Color.Transparent
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChapterSelected(chapter.slug) }
                        .background(itemBg)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bab ${chapter.chapterNumber}: ${chapter.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${chapter.releaseDate} • ${chapter.totalPages} hlm.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sedang dibaca",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}