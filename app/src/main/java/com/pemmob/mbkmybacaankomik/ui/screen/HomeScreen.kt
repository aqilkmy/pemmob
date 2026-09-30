package com.pemmob.mbkmybacaankomik.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
        containerColor = MbkBgDark,
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
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = MbkPrimary) }
            }
            is HomeUiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MbkTextSecondary)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadHomePage() },
                            colors  = ButtonDefaults.buttonColors(MbkPrimary)
                        ) { Text("Coba Lagi") }
                    }
                }
            }
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Search bar
                    item {
                        SearchBar(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    // Banner utama
                    item {
                        FeaturedBanner(
                            komik  = state.featuredKomik,
                            onKlik = { navController.navigate("detail/${state.featuredKomik.slug}") },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                    // Filter kategori
                    item {
                        SectionHeader("Kategori", modifier = Modifier.padding(horizontal = 16.dp))
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
                        SectionHeader("Katalog Komik", modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(10.dp))
                    }
                    // Grid 2 kolom
                    val chunked = state.komikList.chunked(2)
                    items(chunked) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// ─── TOP BAR ──────────────────────────────────────────────────────────────────
@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MbkTopBarColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Beranda",
            color      = MbkTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp
        )
        IconButton(onClick = {}) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = "Notifikasi",
                tint = MbkTextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─── SEARCH BAR ───────────────────────────────────────────────────────────────
@Composable
private fun SearchBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MbkSurface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, null, tint = MbkTextHint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text("Cari judul, author, genre...", color = MbkTextHint, fontSize = 14.sp)
    }
}

// ─── FEATURED BANNER ──────────────────────────────────────────────────────────
@Composable
private fun FeaturedBanner(
    komik: Komik,
    onKlik: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onKlik)
    ) {
        AsyncImage(
            model              = komik.thumbnail,
            contentDescription = komik.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            // Badge status
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MbkOngoing)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(komik.status, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(4.dp))
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
                color    = MbkTextSecondary,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onKlik,
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = MbkPrimary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Baca Sekarang", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                if (isSelected) Color.White else MbkTextSecondary,
                animationSpec = tween(200), label = ""
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onGenreSelected(item.genre) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(item.label, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            AsyncImage(
                model              = komik.thumbnail,
                contentDescription = komik.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            // Badge tipe komik
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MbkBgDark.copy(alpha = 0.75f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(komik.type, color = MbkTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            // Chapter terbaru
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 5.dp, vertical = 3.dp)
            ) {
                Text(komik.latestChapter, color = Color.White, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            komik.title,
            color      = MbkTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 12.sp,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis
        )
        Text(
            komik.author,
            color    = MbkTextHint,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── SECTION HEADER ───────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        color      = MbkTextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize   = 15.sp,
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
                    indicatorColor      = MbkPrimary.copy(alpha = 0.15f),
                    selectedIconColor   = MbkPrimary,
                    unselectedIconColor = MbkTextHint,
                    selectedTextColor   = MbkPrimary,
                    unselectedTextColor = MbkTextHint
                )
            )
        }
    }
}