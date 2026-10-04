package com.pemmob.mbkmybacaankomik.ui.theme

import androidx.compose.ui.graphics.Color

// === Brand Colors (MBK - My Bacaan Komik) ===
val MbkPrimary        = Color(0xFF059669)   // Emerald Green 600 - Segar, modern & kontras tinggi di Light Mode
val MbkPrimaryLight   = Color(0xFF10B981)   // Emerald 500
val MbkPrimaryDark    = Color(0xFF047857)   // Emerald 700
val MbkSecondary      = Color(0xFF2563EB)   // Royal Blue 600
val MbkAccent         = Color(0xFFEA580C)   // Oranye untuk badge Hot / trending

// === Light Theme Palette (DEFAULT) ===
val MbkBackground     = Color(0xFFF8FAFC)   // Slate 50: Bersih, lembut di mata
val MbkSurface        = Color(0xFFFFFFFF)   // Pure White untuk kartu & kontainer
val MbkSurfaceVariant = Color(0xFFF1F5F9)   // Slate 100 untuk background input & chip
val MbkTopBarColor    = Color(0xFFFFFFFF)   // Header & bar warna putih bersih
val MbkBorder         = Color(0xFFE2E8F0)   // Slate 200: Garis tepi halus & rapi
val MbkDivider        = Color(0xFFEDF2F7)   // Garis pemisah konten

// === Text Colors (Light Mode) ===
val MbkOnPrimary      = Color(0xFFFFFFFF)   // Teks di atas warna primary
val MbkTextPrimary    = Color(0xFF0F172A)   // Slate 900: Hitam pekat tajam & sangat mudah dibaca
val MbkTextSecondary  = Color(0xFF475569)   // Slate 600: Teks sekunder yang nyaman
val MbkTextHint       = Color(0xFF94A3B8)   // Slate 400: Placeholder & keterangan kecil

// === Dark Theme Palette (Tersedia untuk opsi Dark Mode) ===
val MbkDarkBackground    = Color(0xFF0D0F14)
val MbkDarkSurface       = Color(0xFF161B26)
val MbkDarkSurfaceVariant= Color(0xFF1E2530)
val MbkDarkTopBar        = Color(0xFF121318)
val MbkDarkTextPrimary   = Color(0xFFF0F2F5)
val MbkDarkTextSecondary = Color(0xFF9BA3AF)
val MbkDarkTextHint      = Color(0xFF5A6370)

// === Backward Compatibility Aliases ===
// Mengarahkan alias lama ke palet Light Mode agar default app langsung tampil dalam Light Mode
val MbkBgDark         = MbkBackground

// === Semantic Badges & Ratings ===
val MbkRating         = Color(0xFFF59E0B)   // Kuning emas bintang rating
val MbkOngoing        = Color(0xFF059669)   // Hijau status Ongoing
val MbkCompleted      = Color(0xFF2563EB)   // Biru status Selesai
val MbkNew            = Color(0xFFE11D48)   // Merah muda cerah untuk chapter Baru

// Legacy
val Purple80          = Color(0xFFD0BCFF)
val PurpleGrey80      = Color(0xFFCCC2DC)
val Pink80            = Color(0xFFEFB8C8)
val Purple40          = Color(0xFF6650a4)
val PurpleGrey40      = Color(0xFF625b71)
val Pink40            = Color(0xFF7D5260)