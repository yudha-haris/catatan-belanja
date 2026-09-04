package com.yudha.catatanbelanja.android.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.yudha.catatanbelanja.R

/** Plus Jakarta Sans, the single family the whole app draws with. */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

/** Lining tabular figures, so money columns never jitter as digits change. */
private const val TABULAR = "tnum"

@Immutable
data class AppTypography(
    val receiptTotal: TextStyle,
    val receiptTotalSmall: TextStyle,
    val heroTitle: TextStyle,
    val screenTitle: TextStyle,
    val sheetTitle: TextStyle,
    val sectionTitle: TextStyle,
    val rowTitle: TextStyle,
    val price: TextStyle,
    val bodyLarge: TextStyle,
    val body: TextStyle,
    val muted: TextStyle,
    val label: TextStyle,
    val fieldLabel: TextStyle,
    val tiny: TextStyle,
    val statValue: TextStyle,
    val moneyInput: TextStyle,
    val searchInput: TextStyle,
    val subtitle: TextStyle,
    val priceDelta: TextStyle,
    val tabLabel: TextStyle,
    val barLabel: TextStyle,
    val emoji: TextStyle,
    val emojiLarge: TextStyle,
    val receiptBrand: TextStyle,
    val receiptStamp: TextStyle,
)

/**
 * Builds the type scale bound to [colors] — every token already carries its resting colour
 * (ink / inkSecondary / inkTertiary), so screens rarely pass one.
 */
fun appTypographyFor(colors: AppColors): AppTypography {
    val base = TextStyle(fontFamily = PlusJakartaSans, color = colors.ink)
    return AppTypography(
        receiptTotal = base.copy(
            fontSize = 40.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.03).em,
            fontFeatureSettings = TABULAR,
        ),
        receiptTotalSmall = base.copy(
            fontSize = 30.sp,
            lineHeight = 33.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.03).em,
            fontFeatureSettings = TABULAR,
        ),
        heroTitle = base.copy(
            fontSize = 26.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.02).em,
        ),
        screenTitle = base.copy(
            fontSize = 24.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.02).em,
        ),
        sheetTitle = base.copy(
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.ExtraBold,
        ),
        sectionTitle = base.copy(
            fontSize = 17.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Bold,
        ),
        rowTitle = base.copy(
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        ),
        price = base.copy(
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFeatureSettings = TABULAR,
        ),
        bodyLarge = base.copy(
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
        ),
        body = base.copy(
            fontSize = 15.sp,
            lineHeight = 21.75.sp,
            fontWeight = FontWeight.Normal,
        ),
        muted = base.copy(
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Normal,
            color = colors.inkSecondary,
        ),
        label = base.copy(
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        fieldLabel = base.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.inkSecondary,
        ),
        tiny = base.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            color = colors.inkTertiary,
        ),
        statValue = base.copy(
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.02).em,
            fontFeatureSettings = TABULAR,
        ),
        moneyInput = base.copy(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFeatureSettings = TABULAR,
        ),
        searchInput = base.copy(
            fontSize = 17.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Medium,
        ),
        subtitle = base.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            color = colors.inkSecondary,
        ),
        priceDelta = base.copy(
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFeatureSettings = TABULAR,
        ),
        tabLabel = base.copy(
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        barLabel = base.copy(
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.inkTertiary,
        ),
        emoji = base.copy(fontSize = 20.sp, lineHeight = 24.sp),
        emojiLarge = base.copy(fontSize = 44.sp, lineHeight = 50.sp),
        // The two tracked-caps tokens the printed receipt needs. Wide letter spacing is what makes
        // a line read as till-roll small print rather than as another label, and it is a property
        // of the token rather than something a screen is allowed to add to one.
        receiptBrand = base.copy(
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.22.em,
            color = colors.inkTertiary,
        ),
        receiptStamp = base.copy(
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.16.em,
        ),
    )
}
