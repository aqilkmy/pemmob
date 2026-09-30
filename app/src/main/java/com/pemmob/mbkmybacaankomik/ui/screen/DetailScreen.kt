package com.pemmob.mbkmybacaankomik.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
        containerColor = MbkBgDark,
        topBar = {
            TopAppBar(
                title = { Text("Detail Komik", color = MbkTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = MbkTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MbkTopBarColor)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                    CircularProgressIndicator(color = MbkPrimary)
                }
            }
            is DetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MbkTextSecondary)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadDetail(komikSlug) },
                            colors  = ButtonDefaults.buttonColors(MbkPrimary)
                        ) { Text("Coba Lagi") }
                    }
                }
            }
            is DetailUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // 1. Profil komik
                    item { KomikProfileSection(komik = state.komik) }

                    // 2. Genre tags
                    item {
                        if (state.komik.genre.isNotEmpty()) {
                            GenreTagRow(
                                genres   = state.komik.genre,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // 3. Sinopsis
                    item {
                        SynopsisSection(
                            synopsis = state.komik.synopsis,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // 4. Tombol Baca
                    item {
                        Button(
                            onClick  = { navController.navigate("read") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(46.dp),
                            shape  = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MbkPrimary)
                        ) {
                            Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Baca Sekarang", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // 5. Header daftar chapter
                    item {
                        ChapterListHeader(
                            totalChapters = state.chapters.size,
                            isAsc         = state.isChapterSortAsc,
                            onToggleSort  = { viewModel.toggleChapterSort() },
                            modifier      = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Divider(color = MbkSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    // 6. List chapter
                    items(state.chapters) { chapter ->
                        ChapterItem(
                            chapter  = chapter,
                            onClick  = { navController.navigate("read") },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Divider(
                            color    = MbkSurfaceVariant,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
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
            .height(200.dp)
    ) {
        // Background blur
        AsyncImage(
            model              = komik.thumbnail,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(MbkBgDark.copy(0.5f), MbkBgDark)
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 95.dp, height = 135.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model              = komik.thumbnail,
                    contentDescription = komik.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                // Badge status
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MbkOngoing)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(komik.status, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(12.dp))
            // Info kanan
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    komik.title,
                    color      = MbkTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text("Karya: ${komik.author}", color = MbkTextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = MbkRating, modifier = Modifier.size(13.dp))
                    Text(" ${komik.rating}", color = MbkTextSecondary, fontSize = 12.sp)
                    Text("  •  ${komik.totalReaders} pembaca", color = MbkTextHint, fontSize = 12.sp)
                }
                Spacer(Modifier.height(6.dp))
                // Chip tipe
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoChip(komik.type)
                    InfoChip("${komik.totalChapters} Chapter")
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
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = MbkTextSecondary, fontSize = 11.sp)
    }
}

// ─── GENRE TAG ────────────────────────────────────────────────────────────────
@Composable
private fun GenreTagRow(genres: List<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        genres.take(4).forEach { genre ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MbkSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(genre, color = MbkTextSecondary, fontSize = 11.sp)
            }
        }
    }
}

// ─── SINOPSIS ─────────────────────────────────────────────────────────────────
@Composable
private fun SynopsisSection(synopsis: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text     = "Sinopsis",
            color    = MbkTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            synopsis,
            color    = MbkTextSecondary,
            fontSize = 13.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis,
            lineHeight = 19.sp
        )
        Text(
            text       = if (expanded) "Lebih sedikit" else "Selengkapnya",
            color      = MbkPrimary,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier
                .clickable { expanded = !expanded }
                .padding(top = 4.dp)
        )
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
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Daftar Chapter ($totalChapters)",
            color      = MbkTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize   = 14.sp
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onToggleSort)
                .background(MbkSurface)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isAsc) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                null,
                tint     = MbkPrimary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (isAsc) "Terlama" else "Terbaru",
                color      = MbkPrimary,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Bab ${chapter.chapterNumber}: ${chapter.title}",
                    color      = MbkTextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 13.sp,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                if (chapter.isNew) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MbkPrimary)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("Baru", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "${chapter.releaseDate}  •  ${chapter.totalPages} hlm.",
                color    = MbkTextHint,
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