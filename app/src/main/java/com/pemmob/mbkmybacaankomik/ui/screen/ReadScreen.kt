package com.pemmob.mbkmybacaankomik.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.pemmob.mbkmybacaankomik.ui.theme.*
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

    // Fungsi transisi ke chapter baru
    val navigateToChapter: (String) -> Unit = { targetSlug ->
        viewModel.loadChapter(komikSlug, targetSlug)
        coroutineScope.launch {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        containerColor = MbkBackground
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
                                color = MbkPrimary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Menyiapkan lembar komik...",
                                color = MbkTextSecondary,
                                fontSize = 14.sp
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
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MbkSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MbkBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                    tint = MbkAccent,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Gagal Membaca Chapter",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MbkTextPrimary
                                )
                                Text(
                                    text = state.message,
                                    color = MbkTextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadChapter(komikSlug, chapterSlug) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MbkPrimary)
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

                    // ─── AREA BACA VERTICAL (WEBTOON CONTINUOUS SCROLL) ─────
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
                            ReaderChapterInfoBanner(
                                chapterTitle = detail.chapterTitle,
                                chapterNumber = detail.chapterNumber,
                                komikTitle = detail.komikTitle
                            )
                        }

                        // Lembar-lembar gambar komik
                        itemsIndexed(detail.pages) { pageIndex, pageUrl ->
                            ReaderPageItem(
                                pageUrl = pageUrl,
                                pageNumber = pageIndex + 1,
                                totalPages = detail.pages.size
                            )
                        }

                        // Kartu Selesai & Tombol Navigasi Chapter di Bawah
                        item {
                            ChapterCompletionSection(
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

                    // ─── FLOATING OVERLAY: TOP BAR ──────────────────────────
                    AnimatedVisibility(
                        visible = state.areControlsVisible,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        ReaderTopBar(
                            komikTitle = detail.komikTitle,
                            chapterTitle = "Bab ${detail.chapterNumber}: ${detail.chapterTitle}",
                            onBackClick = { navController.popBackStack() },
                            onOpenChapterList = { showChapterSheet = true }
                        )
                    }

                    // ─── FLOATING OVERLAY: BOTTOM BAR (PREV / LIST / NEXT) ──
                    AnimatedVisibility(
                        visible = state.areControlsVisible,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        ReaderBottomBar(
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

                    // ─── MODAL BOTTOM SHEET: PILIH CHAPTER CEPAT ─────────────
                    if (showChapterSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showChapterSheet = false },
                            sheetState = sheetState,
                            containerColor = MbkSurface,
                            contentColor = MbkTextPrimary
                        ) {
                            ChapterSelectorSheetContent(
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

// ─── ITEM GAMBAR HALAMAN BACA KOMIK ───────────────────────────────────────────
@Composable
private fun ReaderPageItem(
    pageUrl: String,
    pageNumber: Int,
    totalPages: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MbkBackground)
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
                        .background(MbkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MbkPrimary,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = "Memuat hlm. $pageNumber/$totalPages",
                            color = MbkTextHint,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(MbkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = MbkTextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Gagal memuat gambar hlm. $pageNumber",
                            color = MbkTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        )

        // Penanda nomor halaman kecil di pojok kanan bawah tiap lembar
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = "$pageNumber / $totalPages",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── BANNER INFO DI AWAL CHAPTER ──────────────────────────────────────────────
@Composable
private fun ReaderChapterInfoBanner(
    chapterTitle: String,
    chapterNumber: String,
    komikTitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MbkSurface, MbkBackground)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = komikTitle,
                color = MbkPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Bab $chapterNumber: $chapterTitle",
                color = MbkTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Scroll ke bawah untuk membaca • Ketuk layar untuk kontrol",
                color = MbkTextHint,
                fontSize = 11.sp
            )
        }
    }
}

// ─── SEKSI AKHIR CHAPTER (TOMBOL NEXT/PREV BESAR & KARTU SELESAI) ─────────────
@Composable
private fun ChapterCompletionSection(
    chapterDetail: ChapterDetail,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onBackToDetail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MbkSurface),
        border = BorderStroke(1.dp, MbkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                tint = MbkPrimary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Bab ${chapterDetail.chapterNumber} Selesai!",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MbkTextPrimary
            )
            Text(
                text = "Kamu telah membaca seluruh lembaran ${chapterDetail.chapterTitle}.",
                color = MbkTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Tombol Chapter Selanjutnya (Utama)
            if (chapterDetail.nextChapterSlug != null) {
                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MbkPrimary)
                ) {
                    Text(
                        text = "Lanjut Chapter Selanjutnya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MbkSurfaceVariant)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ini adalah chapter terbaru saat ini ✨",
                        color = MbkTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // Baris Tombol Prev & Kembali ke Detail
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
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MbkBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MbkTextPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Bab Sebelumnya", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onBackToDetail,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MbkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MbkTextSecondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Detail Komik", fontSize = 12.sp)
                }
            }
        }
    }
}

// ─── FLOATING TOP BAR ─────────────────────────────────────────────────────────
@Composable
private fun ReaderTopBar(
    komikTitle: String,
    chapterTitle: String,
    onBackClick: () -> Unit,
    onOpenChapterList: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.98f),
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MbkBorder),
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
                    tint = MbkTextPrimary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = komikTitle,
                    color = MbkTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chapterTitle,
                    color = MbkTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onOpenChapterList) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Daftar Chapter",
                    tint = MbkPrimary
                )
            }
        }
    }
}

// ─── FLOATING BOTTOM BAR DENGAN TOMBOL PREV / NEXT CHAPTER ─────────────────────
@Composable
private fun ReaderBottomBar(
    chapterDetail: ChapterDetail,
    currentChapterIndex: Int,
    totalChapters: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onOpenChapterList: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.98f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MbkBorder),
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
            // Tombol Chapter Sebelumnya (Prev)
            val hasPrev = chapterDetail.prevChapterSlug != null
            Button(
                onClick = onPrevClick,
                enabled = hasPrev,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MbkSurfaceVariant,
                    contentColor = MbkTextPrimary,
                    disabledContainerColor = MbkSurface.copy(alpha = 0.5f),
                    disabledContentColor = MbkTextHint
                ),
                border = BorderStroke(1.dp, MbkBorder),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = "Sebelumnya",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text("Prev", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            // Tombol Tengah: Selector Chapter Aktif
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenChapterList)
                    .background(MbkSurfaceVariant)
                    .border(1.dp, MbkBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MbkPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Bab ${chapterDetail.chapterNumber} (${currentChapterIndex + 1}/$totalChapters)",
                        color = MbkTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Tombol Chapter Selanjutnya (Next)
            val hasNext = chapterDetail.nextChapterSlug != null
            Button(
                onClick = onNextClick,
                enabled = hasNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MbkPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = MbkSurface.copy(alpha = 0.5f),
                    disabledContentColor = MbkTextHint
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = "Selanjutnya",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── BOTTOM SHEET DAFTAR SEMUA CHAPTER UNTUK LOMPAT LANGSUNG ──────────────────
@Composable
private fun ChapterSelectorSheetContent(
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pilih Chapter",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MbkTextPrimary
            )
            Text(
                text = "Total ${chapters.size} Bab",
                fontSize = 12.sp,
                color = MbkTextSecondary
            )
        }

        HorizontalDivider(color = MbkBorder)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
        ) {
            itemsIndexed(chapters) { _, chapter ->
                val isSelected = chapter.slug == currentChapterSlug
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChapterSelected(chapter.slug) }
                        .background(if (isSelected) MbkPrimary.copy(alpha = 0.12f) else Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bab ${chapter.chapterNumber}: ${chapter.title}",
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MbkPrimary else MbkTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${chapter.releaseDate} • ${chapter.totalPages} hlm.",
                            fontSize = 11.sp,
                            color = MbkTextHint
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MbkPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sedang dibaca",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = MbkDivider, thickness = 0.5.dp)
            }
        }
    }
}