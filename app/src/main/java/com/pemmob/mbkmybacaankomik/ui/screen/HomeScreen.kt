package com.pemmob.mbkmybacaankomik.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.pemmob.mbkmybacaankomik.data.model.Komik
import com.pemmob.mbkmybacaankomik.viewmodel.HomeTab
import com.pemmob.mbkmybacaankomik.viewmodel.HomeUiState
import com.pemmob.mbkmybacaankomik.viewmodel.HomeViewModel

// ─── Data Kategori ───────────────────────────────────────────────────────────
private data class KategoriItem(val label: String, val icon: ImageVector, val genre: String)

private val kategoriList = listOf(
    KategoriItem("Semua",    Icons.Filled.GridView,      "semua"),
    KategoriItem("Fantasy",  Icons.Filled.AutoAwesome,   "fantasy"),
    KategoriItem("Aksi",     Icons.Filled.Bolt,          "action"),
    KategoriItem("Romantis", Icons.Filled.Favorite,      "romance"),
    KategoriItem("Komedi",   Icons.Filled.EmojiEmotions, "comedy"),
    KategoriItem("Horror",   Icons.Filled.Nightlight,    "horror"),
    KategoriItem("Sci-Fi",   Icons.Filled.RocketLaunch,  "sci-fi"),
    KategoriItem("Misteri",  Icons.Filled.Search,        "mystery"),
)

// ─── HomeScreen (Material Design 3) ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            M3HomeTopBar(
                onRefresh = { viewModel.loadHomePage() }
            )
        },
        bottomBar = {
            val currentTab = when (val s = uiState) {
                is HomeUiState.Success -> s.currentTab
                else -> HomeTab.BERANDA
            }
            M3BottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.onTabSelected(it) }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
                            "Menghubungkan ke MangaDex...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ElevatedCard(
                        modifier = Modifier.padding(24.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Gagal Memuat MangaDex",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(18.dp))
                            Button(
                                onClick = { viewModel.loadHomePage() },
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }

            is HomeUiState.Success -> {
                val isSearching = state.searchQuery.isNotBlank()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // 1. Material 3 Search Bar
                    item {
                        M3SearchBar(
                            query = state.searchQuery,
                            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            onClear = { viewModel.onClearSearch() },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    // 2. Banner Utama (Hanya tampil di tab Beranda & tidak sedang mencari)
                    if (state.currentTab == HomeTab.BERANDA && !isSearching) {
                        item {
                            M3FeaturedBanner(
                                komik = state.featuredKomik,
                                onKlik = { navController.navigate("detail/${state.featuredKomik.slug}") },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(18.dp))
                        }
                    }

                    // 3. Filter Kategori (Material 3 FilterChip)
                    item {
                        Text(
                            text = "Filter Genre",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        M3KategoriFilterRow(
                            items = kategoriList,
                            selectedGenre = state.selectedGenre,
                            onGenreSelected = { viewModel.onGenreSelected(it) }
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // 4. Header Seksi Sesuai Tab / Status Pencarian
                    item {
                        val headerTitle = when {
                            isSearching -> "Hasil Pencarian: \"${state.searchQuery}\""
                            state.currentTab == HomeTab.POPULER -> "Komik Terpopuler (${state.selectedGenre})"
                            state.currentTab == HomeTab.TERBARU -> "Update Chapter Terbaru (${state.selectedGenre})"
                            else -> "Rekomendasi Komik (${state.selectedGenre})"
                        }

                        val headerSubtitle = when {
                            isSearching -> "Ditemukan ${state.komikList.size} judul dari MangaDex"
                            state.currentTab == HomeTab.POPULER -> "Berdasarkan jumlah pembaca terbanyak"
                            state.currentTab == HomeTab.TERBARU -> "Rilis chapter paling gres hari ini"
                            else -> "Katalog pilihan terfavorit minggu ini"
                        }

                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = headerTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )

                                if (state.isListLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = headerSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // 5. Empty State atau Grid 2 Kolom
                    if (state.komikList.isEmpty() && !state.isListLoading) {
                        item {
                            M3EmptyState(
                                query = state.searchQuery,
                                onReset = {
                                    viewModel.onClearSearch()
                                    viewModel.onGenreSelected("Semua")
                                }
                            )
                        }
                    } else {
                        val chunked = state.komikList.chunked(2)
                        itemsIndexed(chunked) { rowIndex, rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                rowItems.forEachIndexed { colIndex, komik ->
                                    val itemIndex = rowIndex * 2 + colIndex
                                    val rankNumber = if (state.currentTab == HomeTab.POPULER && !isSearching) {
                                        itemIndex + 1
                                    } else null

                                    M3KomikCard(
                                        komik = komik,
                                        rankNumber = rankNumber,
                                        isLatestTab = state.currentTab == HomeTab.TERBARU && !isSearching,
                                        modifier = Modifier.weight(1f),
                                        onClick = { navController.navigate("detail/${komik.slug}") }
                                    )
                                }
                                if (rowItems.size < 2) Spacer(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── MATERIAL 3 TOP APP BAR ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun M3HomeTopBar(
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "MBK Baca Komik",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "MangaDex API",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Muat Ulang",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ─── MATERIAL 3 SEARCH BAR ───────────────────────────────────────────────────
@Composable
private fun M3SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        placeholder = {
            Text(
                "Cari judul manga, manhwa, author...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Cari",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Hapus Pencarian",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

// ─── MATERIAL 3 FEATURED BANNER ──────────────────────────────────────────────
@Composable
private fun M3FeaturedBanner(
    komik: Komik,
    onKlik: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onKlik)
        ) {
            AsyncImage(
                model = komik.thumbnail,
                contentDescription = komik.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Scrim gradient gelap agar teks selalu tajam dan kontras
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.40f),
                                Color.Black.copy(alpha = 0.90f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "UNGGULAN",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = komik.type,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = komik.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Karya: ${komik.author}",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onKlik,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Baca Sekarang",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

// ─── MATERIAL 3 FILTER CHIP ROW (KATEGORI) ───────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun M3KategoriFilterRow(
    items: List<KategoriItem>,
    selectedGenre: String,
    onGenreSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            val isSelected = selectedGenre.equals(item.genre, ignoreCase = true)

            FilterChip(
                selected = isSelected,
                onClick = { onGenreSelected(item.genre) },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

// ─── MATERIAL 3 KOMIK CARD ───────────────────────────────────────────────────
@Composable
private fun M3KomikCard(
    komik: Komik,
    rankNumber: Int?,
    isLatestTab: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = komik.thumbnail,
                    contentDescription = komik.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Badge Peringkat (Populer #1, #2, dst)
                if (rankNumber != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = when (rankNumber) {
                            1 -> MaterialTheme.colorScheme.primary
                            2 -> MaterialTheme.colorScheme.secondary
                            3 -> MaterialTheme.colorScheme.tertiary
                            else -> Color.Black.copy(alpha = 0.75f)
                        }
                    ) {
                        Text(
                            text = "#$rankNumber",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    // Badge Tipe Komik
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = Color.Black.copy(alpha = 0.70f)
                    ) {
                        Text(
                            text = komik.type,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Badge Status Baru Update
                if (isLatestTab) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = "UPDATE",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Overlay chapter di bawah cover
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = komik.latestChapter,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = komik.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = komik.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${komik.rating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─── MATERIAL 3 EMPTY STATE ───────────────────────────────────────────────────
@Composable
private fun M3EmptyState(
    query: String,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (query.isNotBlank()) "Tidak ada komik untuk \"$query\"" else "Tidak ada komik di kategori ini",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Coba gunakan kata kunci lain atau pilih kategori berbeda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onReset) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reset Filter & Pencarian")
            }
        }
    }
}

// ─── MATERIAL 3 BOTTOM NAVIGATION BAR ─────────────────────────────────────────
@Composable
private fun M3BottomNavigationBar(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        HomeTab.values().forEach { tab ->
            val isSelected = currentTab == tab

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.iconSelected else tab.iconUnselected,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}