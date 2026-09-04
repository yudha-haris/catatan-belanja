package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.component.input.AppUnitDropdown
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.domain.model.PriceBasis
import com.yudha.catatanbelanja.features.dashboard.domain.model.PriceTrendData

/**
 * The switch this whole page exists for: measure the trip price, or the unit price.
 *
 * The note under the chips is not decoration. "Harga belanja" quietly lies whenever the amount
 * bought changes, and the only place the user can find that out is here, before they read a rise
 * that is really just a bigger bag.
 */
@Composable
internal fun TrendBasisCard(
    data: PriceTrendData,
    onSelectBasis: (PriceBasis) -> Unit,
    onSelectBaseUnit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    AppCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.trend_basis_title),
            style = AppTheme.typography.sectionTitle,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Spacing.x10))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
        ) {
            AppChip(
                text = stringResource(R.string.trend_basis_raw),
                onClick = { onSelectBasis(PriceBasis.RAW) },
                selected = data.basis == PriceBasis.RAW,
            )
            AppChip(
                text = stringResource(R.string.trend_basis_per_unit),
                onClick = { onSelectBasis(PriceBasis.PER_UNIT) },
                selected = data.basis == PriceBasis.PER_UNIT,
                enabled = data.canUsePerUnit,
            )
        }
        Spacer(Modifier.height(Spacing.x10))
        Text(
            text = when (data.basis) {
                PriceBasis.RAW -> stringResource(R.string.trend_basis_raw_note)
                PriceBasis.PER_UNIT -> stringResource(R.string.trend_basis_per_unit_note)
            },
            style = AppTheme.typography.muted,
            color = colors.inkSecondary,
        )

        if (!data.canUsePerUnit) {
            Spacer(Modifier.height(Spacing.x10))
            Text(
                text = stringResource(R.string.trend_per_unit_blocked),
                style = AppTheme.typography.tiny,
                color = colors.coral,
            )
            return@AppCard
        }

        if (data.basis == PriceBasis.RAW) return@AppCard

        Spacer(Modifier.height(Spacing.x14))
        AppUnitDropdown(
            value = data.baseUnit,
            onValueChange = onSelectBaseUnit,
            units = data.baseUnitOptions,
            label = stringResource(R.string.trend_base_unit_label),
        )
    }
}
