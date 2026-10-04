package com.pemmob.mbkmybacaankomik.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.pemmob.mbkmybacaankomik.data.model.Komik
import com.pemmob.mbkmybacaankomik.ui.theme.*
import com.pemmob.mbkmybacaankomik.viewmodel.HomeUiState
import com.pemmob.mbkmybacaankomik.viewmodel.HomeViewModel

// ─── Kategori Cepat ───────────────────────────────────────────────────────────
private data class KategoriCepat(val label: String, val icon: ImageVector, val genre: String)

private val kategoriList = listOf(
    KategoriCepat("Semua",    Icons.Filled.GridView,      "semua"),
    KategoriCepat("Fantasy",  Icons.Filled.AutoAwesome,   "fantasy"),
    KategoriCepat("Aksi",     Icons.Filled.Bolt,          "action"),
    KategoriCepat("Romantis", Icons.Filled.Favorite,      "romance"),
    KategoriCepat("Komedi",   Icons.Filled.EmojiEmotions, "comedy"),
    KategoriCepat("Horror",   Icons.Filled.Nightlight,    "horror"),
    KategoriCepat("Sci-Fi",   Icons.Filled.RocketLaunch,  "sci-fi"),
    KategoriCepat("Misteri",  Icons.Filled.Search,        "mystery"),
)

// ─── Bottom Nav ───────────────────────────────────────────────────────────────
private data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

private val bottomNavItems = listOf(
    BottomNavItem("Beranda",  Icons.Filled.Home,        Icons.Outlined.Home,        "home"),
    BottomNavItem("Populer",  Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard, "ranking"),
    BottomNavItem("Terbaru",  Icons.Filled.NewReleases, Icons.Outlined.NewReleases, "terbaru"),
)

// ─── HomeScreen ───────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedBottomNav by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MbkBackground,
        topBar     = { HomeTopBar() },
        bottomBar  = {
            MbkBottomNavigation(
                items       = bottomNavItems,
                selectedIdx = selectedBottomNav,
                onItemClick = { idx, _ -> selectedBottomNav = idx }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = MbkPrimary, strokeWidth = 3.dp)
                        Text(
                            "Memuat katalog komik...",
                            color = MbkTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            is HomeUiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
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
                            Icon(
                                Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = MbkTextHint,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Gagal Memuat Data",
                                color = MbkTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                state.message,
                                color = MbkTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 3
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadHomePage() },
                                colors  = ButtonDefaults.buttonColors(containerColor = MbkPrimary),
                                shape   = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Coba Lagi", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Search bar interaktif
                    item {
                        SearchBar(
                            query = state.searchQuery,
                            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    // Banner utama (hanya tampil jika tidak sedang searching)
                    if (state.searchQuery.isBlank()) {
                        item {
                            FeaturedBanner(
                                komik  = state.featuredKomik,
                                onKlik = { navController.navigate("detail/${state.featuredKomik.slug}") },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                    }

                    // Filter kategori
                    item {
                        SectionHeader("Kategori Genre", modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(10.dp))
                        KategoriRow(
                            items           = kategoriList,
                            selectedGenre   = state.selectedGenre,
                            onGenreSelected = { viewModel.onGenreSelected(it) }
                        )
                        Spacer(Modifier.height(20.dp))
                    }

                    // Header katalog
                    item {
                        val headerTitle = if (state.searchQuery.isNotBlank()) {
                            "Hasil Pencarian: \"${state.searchQuery}\""
                        } else {
                            "Katalog Komik (${state.selectedGenre})"
                        }
                        SectionHeader(headerTitle, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(12.dp))
                    }

                    if (state.komikList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.SearchOff,
                                        contentDescription = null,
                                        tint = MbkTextHint,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Tidak ada komik yang ditemukan.",
                                        color = MbkTextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Grid 2 kolom yang rapi
                        val chunked = state.komikList.chunked(2)
                        items(chunked) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                rowItems.forEach { komik ->
                                    KomikCard(
                                        komik   = komik,
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

// ─── TOP BAR ──────────────────────────────────────────────────────────────────
@Composable
private fun HomeTopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MbkTopBarColor)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "MBK Baca Komik",
                    color      = MbkTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 19.sp,
                    letterSpacing = (-0.3).sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MbkPrimary)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "MangaDex Online",
                        color      = MbkPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 11.sp
                    )
                }
            }
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MbkSurfaceVariant)
                    .size(38.dp)
            ) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    tint = MbkTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        HorizontalDivider(color = MbkBorder, thickness = 1.dp)
    }
}

// ─── SEARCH BAR ───────────────────────────────────────────────────────────────
@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MbkSurface)
            .border(1.dp, MbkBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, null, tint = MbkPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = MbkTextPrimary,
                fontSize = 14.sp
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text("Cari judul manga, manhwa, author...", color = MbkTextHint, fontSize = 13.sp)
                }
                innerTextField()
            }
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChanged("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = "Hapus",
                    tint = MbkTextHint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── FEATURED BANNER ──────────────────────────────────────────────────────────
@Composable
private fun FeaturedBanner(
    komik: Komik,
    onKlik: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MbkBorder),
        colors = CardDefaults.cardColors(containerColor = MbkSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onKlik)
        ) {
            AsyncImage(
                model              = komik.thumbnail,
                contentDescription = komik.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            // Scrim gradien gelap agar teks selalu tajam dan kontras
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.90f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MbkPrimary)
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text("UNGGULAN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(komik.type, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    komik.title,
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    komik.author,
                    color    = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onKlik,
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MbkPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Baca Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ─── KATEGORI ROW ─────────────────────────────────────────────────────────────
@Composable
private fun KategoriRow(
    items: List<KategoriCepat>,
    selectedGenre: String,
    onGenreSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            val isSelected = selectedGenre.equals(item.genre, ignoreCase = true)
            val bgColor by animateColorAsState(
                if (isSelected) MbkPrimary else MbkSurface,
                animationSpec = tween(200), label = ""
            )
            val textColor by animateColorAsState(
                if (isSelected) Color.White else MbkTextPrimary,
                animationSpec = tween(200), label = ""
            )
            val borderColor = if (isSelected) MbkPrimary else MbkBorder

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onGenreSelected(item.genre) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    item.label,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ─── KOMIK CARD ───────────────────────────────────────────────────────────────
@Composable
private fun KomikCard(
    komik: Komik,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MbkBorder),
        colors = CardDefaults.cardColors(containerColor = MbkSurface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                AsyncImage(
                    model              = komik.thumbnail,
                    contentDescription = komik.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                // Badge tipe komik di pojok kiri atas
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.70f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(komik.type, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Text(komik.latestChapter, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    komik.title,
                    color      = MbkTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        komik.author,
                        color    = MbkTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = MbkRating, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "${komik.rating}",
                            color = MbkTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─── SECTION HEADER ───────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        color      = MbkTextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize   = 16.sp,
        letterSpacing = (-0.2).sp,
        modifier   = modifier
    )
}

// ─── BOTTOM NAVIGATION ────────────────────────────────────────────────────────
@Composable
private fun MbkBottomNavigation(
    items: List<BottomNavItem>,
    selectedIdx: Int,
    onItemClick: (Int, BottomNavItem) -> Unit
) {
    Column {
        HorizontalDivider(color = MbkBorder, thickness = 1.dp)
        NavigationBar(
            containerColor = MbkTopBarColor,
            tonalElevation = 0.dp
        ) {
            items.forEachIndexed { idx, item ->
                val isSelected = idx == selectedIdx
                val tint by animateColorAsState(
                    if (isSelected) MbkPrimary else MbkTextHint,
                    animationSpec = tween(200), label = ""
                )
                NavigationBarItem(
                    selected = isSelected,
                    onClick  = { onItemClick(idx, item) },
                    icon     = {
                        Icon(
                            if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label    = {
                        Text(
                            item.label,
                            fontSize   = 10.sp,
                            color      = tint,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors   = NavigationBarItemDefaults.colors(
                        indicatorColor      = MbkPrimary.copy(alpha = 0.12f),
                        selectedIconColor   = MbkPrimary,
                        unselectedIconColor = MbkTextHint,
                        selectedTextColor   = MbkPrimary,
                        unselectedTextColor = MbkTextHint
                    )
                )
            }
        }
    }
}