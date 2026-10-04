package com.pemmob.mbkmybacaankomik.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.pemmob.mbkmybacaankomik.data.model.Chapter
import com.pemmob.mbkmybacaankomik.data.model.Komik
import com.pemmob.mbkmybacaankomik.ui.theme.*
import com.pemmob.mbkmybacaankomik.viewmodel.DetailUiState
import com.pemmob.mbkmybacaankomik.viewmodel.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    komikSlug: String = "legenda-garuda-putih",
    viewModel: DetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(komikSlug) {
        viewModel.loadDetail(komikSlug)
    }

    Scaffold(
        containerColor = MbkBackground,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MbkTopBarColor).statusBarsPadding()) {
                TopAppBar(
                    title = {
                        Text(
                            "Detail Komik",
                            color = MbkTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = MbkTextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MbkTopBarColor)
                )
                HorizontalDivider(color = MbkBorder, thickness = 1.dp)
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = MbkPrimary, strokeWidth = 3.dp)
                        Text("Memuat data komik...", color = MbkTextSecondary, fontSize = 13.sp)
                    }
                }
            }
            is DetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MbkSurface),
                        border = BorderStroke(1.dp, MbkBorder),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.WarningAmber, null, tint = MbkAccent, modifier = Modifier.size(44.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Gagal Memuat Komik", color = MbkTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(state.message, color = MbkTextSecondary, fontSize = 12.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadDetail(komikSlug) },
                                colors  = ButtonDefaults.buttonColors(containerColor = MbkPrimary),
                                shape   = RoundedCornerShape(10.dp)
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
            is DetailUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // 1. Profil Komik
                    item {
                        KomikProfileSection(komik = state.komik)
                        Spacer(Modifier.height(16.dp))
                    }

                    // 2. Genre Tags
                    item {
                        if (state.komik.genre.isNotEmpty()) {
                            GenreTagRow(
                                genres   = state.komik.genre,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(14.dp))
                        }
                    }

                    // 3. Sinopsis Card
                    item {
                        SynopsisSection(
                            synopsis = state.komik.synopsis,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // 4. Tombol Utama Baca Sekarang
                    item {
                        val firstChapterSlug = state.chapters.firstOrNull()?.slug ?: "bab-1"
                        Button(
                            onClick  = { navController.navigate("read/${state.komik.slug}/$firstChapterSlug") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(50.dp)
                                .shadow(3.dp, RoundedCornerShape(12.dp)),
                            shape  = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MbkPrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = Color.White, modifier = Modifier.size(19.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Mulai Membaca Bab Pertama", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // 5. Header Daftar Chapter
                    item {
                        ChapterListHeader(
                            totalChapters = state.chapters.size,
                            isAsc         = state.isChapterSortAsc,
                            onToggleSort  = { viewModel.toggleChapterSort() },
                            modifier      = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // 6. List Chapter dalam Card Kontainer yang Rapi
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MbkSurface),
                            border = BorderStroke(1.dp, MbkBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column {
                                state.chapters.forEachIndexed { index, chapter ->
                                    ChapterItem(
                                        chapter  = chapter,
                                        onClick  = { navController.navigate("read/${state.komik.slug}/${chapter.slug}") },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    if (index < state.chapters.size - 1) {
                                        HorizontalDivider(color = MbkDivider, thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── PROFIL KOMIK ─────────────────────────────────────────────────────────────
@Composable
private fun KomikProfileSection(komik: Komik) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Background banner blur halus
        AsyncImage(
            model              = komik.thumbnail,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        // Gradien transisi ke latar Light Mode
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MbkBackground.copy(alpha = 0.5f),
                            MbkBackground.copy(alpha = 0.95f),
                            MbkBackground
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Thumbnail dengan border dan shadow
            Card(
                modifier = Modifier
                    .size(width = 100.dp, height = 145.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model              = komik.thumbnail,
                        contentDescription = komik.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                    // Badge status di atas thumbnail
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(5.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MbkOngoing)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(komik.status, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            // Info Kanan
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    komik.title,
                    color      = MbkTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 17.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(4.dp))
                Text("Penulis: ${komik.author}", color = MbkTextSecondary, fontSize = 12.sp, maxLines = 1)
                Spacer(Modifier.height(8.dp))
                // Rating Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = MbkRating, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("${komik.rating}", color = MbkTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("  •  ${komik.totalReaders} pembaca", color = MbkTextSecondary, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                // Chips tipe dan total bab
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(komik.type)
                    InfoChip("${komik.totalChapters} Bab")
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MbkSurface)
            .border(1.dp, MbkBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.5.dp)
    ) {
        Text(text, color = MbkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─── GENRE TAG ────────────────────────────────────────────────────────────────
@Composable
private fun GenreTagRow(genres: List<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.take(4).forEach { genre ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MbkSurface)
                    .border(1.dp, MbkBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(genre, color = MbkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─── SINOPSIS CARD ─────────────────────────────────────────────────────────────
@Composable
private fun SynopsisSection(synopsis: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MbkSurface),
        border = BorderStroke(1.dp, MbkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text     = "Sinopsis",
                color    = MbkTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                synopsis,
                color    = MbkTextSecondary,
                fontSize = 13.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            Text(
                text       = if (expanded) "Tutup Sinopsis" else "Baca Selengkapnya",
                color      = MbkPrimary,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 8.dp)
            )
        }
    }
}

// ─── CHAPTER LIST HEADER ──────────────────────────────────────────────────────
@Composable
private fun ChapterListHeader(
    totalChapters: Int,
    isAsc: Boolean,
    onToggleSort: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Daftar Chapter ($totalChapters)",
            color      = MbkTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleSort)
                .background(MbkSurface)
                .border(1.dp, MbkBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isAsc) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                null,
                tint     = MbkPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (isAsc) "Terlama" else "Terbaru",
                color      = MbkPrimary,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── CHAPTER ITEM ─────────────────────────────────────────────────────────────
@Composable
private fun ChapterItem(
    chapter: Chapter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Bab ${chapter.chapterNumber}: ${chapter.title}",
                    color      = MbkTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                if (chapter.isNew) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MbkNew)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("Baru", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                "${chapter.releaseDate}  •  ${chapter.totalPages} hlm.",
                color    = MbkTextSecondary,
                fontSize = 11.sp
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            null,
            tint     = MbkTextHint,
            modifier = Modifier.size(18.dp)
        )
    }
}