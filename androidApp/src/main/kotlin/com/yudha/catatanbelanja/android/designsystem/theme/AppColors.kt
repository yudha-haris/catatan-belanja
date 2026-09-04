package com.yudha.catatanbelanja.android.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor

@Immutable
data class AppColors(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val tint: Color,
    val background: Color,
    val paper: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val ink: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val line: Color,
    val mint: Color,
    val mintBg: Color,
    val coral: Color,
    val coralBg: Color,
    val confetti: List<Color>,
)

private val Paper = Color(0xFFFFFFFF)
private val Mint = Color(0xFF16A34A)
private val MintBg = Color(0xFFDCFCE7)
private val Coral = Color(0xFFE11D48)
private val CoralBg = Color(0xFFFFE4E6)
private val InkTertiary = Color(0xFFA39BB8)

private val Confetti = listOf(
    Color(0xFF7C3AED),
    Color(0xFFC026D3),
    Color(0xFFF59E0B),
    Color(0xFF10B981),
    Color(0xFF3B82F6),
    Color(0xFFF43F5E),
)

val purpleColors = AppColors(
    primary = Color(0xFF7C3AED),
    primaryDark = Color(0xFF5B21B6),
    primaryLight = Color(0xFFA78BFA),
    tint = Color(0xFFEFE9FF),
    background = Color(0xFFF7F4FF),
    paper = Paper,
    heroStart = Color(0xFF7C3AED),
    heroEnd = Color(0xFFC026D3),
    ink = Color(0xFF2B1D4A),
    inkSecondary = Color(0xFF6B6383),
    inkTertiary = InkTertiary,
    line = Color(0xFFECE8F3),
    mint = Mint,
    mintBg = MintBg,
    coral = Coral,
    coralBg = CoralBg,
    confetti = Confetti,
)

val greenColors = AppColors(
    primary = Color(0xFF059669),
    primaryDark = Color(0xFF047857),
    primaryLight = Color(0xFF6EE7B7),
    tint = Color(0xFFE6F7EF),
    background = Color(0xFFF2FBF6),
    paper = Paper,
    heroStart = Color(0xFF059669),
    heroEnd = Color(0xFF65A30D),
    ink = Color(0xFF173A2D),
    inkSecondary = Color(0xFF5B6E66),
    inkTertiary = InkTertiary,
    line = Color(0xFFE2EFE8),
    mint = Mint,
    mintBg = MintBg,
    coral = Coral,
    coralBg = CoralBg,
    confetti = Confetti,
)

val blueColors = AppColors(
    primary = Color(0xFF2563EB),
    primaryDark = Color(0xFF1D4ED8),
    primaryLight = Color(0xFF93C5FD),
    tint = Color(0xFFE8EFFF),
    background = Color(0xFFF3F7FF),
    paper = Paper,
    heroStart = Color(0xFF2563EB),
    heroEnd = Color(0xFF0891B2),
    ink = Color(0xFF14224A),
    inkSecondary = Color(0xFF5F6A8A),
    inkTertiary = InkTertiary,
    line = Color(0xFFE3E9F7),
    mint = Mint,
    mintBg = MintBg,
    coral = Coral,
    coralBg = CoralBg,
    confetti = Confetti,
)

fun appColorsFor(flavor: ThemeFlavor): AppColors = when (flavor) {
    ThemeFlavor.PURPLE -> purpleColors
    ThemeFlavor.GREEN -> greenColors
    ThemeFlavor.BLUE -> blueColors
}
