package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.features.stock.domain.model.StockCheckRow

/** The `.stk-row` grid: name over its previous quantity, a 92dp numeric box, then the unit. */
@Composable
internal fun StockCheckInputRow(
    row: StockCheckRow,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.x8),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${row.emoji} ${row.name}",
                    style = AppTheme.typography.rowTitle,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // The estimate is shown but never pre-filled. This sheet is where the app finds
                // out what is actually on the shelf; a line that arrives already holding a guess
                // gets confirmed rather than checked, and the estimator would then be learning
                // from its own output.
                Text(
                    text = when (row.estimatedQty) {
                        null -> stringResource(
                            R.string.stock_check_previous,
                            row.previousQty.toQtyLabel(),
                            row.unit,
                        )
                        else -> stringResource(
                            R.string.stock_check_previous_estimate,
                            row.previousQty.toQtyLabel(),
                            row.unit,
                            row.estimatedQty.toQtyLabel(),
                        )
                    },
                    style = AppTheme.typography.tiny,
                    color = colors.inkTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            AppTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(92.dp),
                enabled = enabled,
                textStyle = AppTheme.typography.rowTitle.copy(textAlign = TextAlign.End),
                contentPadding = PaddingValues(Spacing.x10),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Text(
                text = row.unit,
                modifier = Modifier.width(54.dp),
                style = AppTheme.typography.fieldLabel,
                color = colors.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (!showDivider) return@Column

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(colors.line),
        )
    }
}
