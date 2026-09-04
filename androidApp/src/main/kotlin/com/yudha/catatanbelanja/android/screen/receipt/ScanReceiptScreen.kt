package com.yudha.catatanbelanja.android.screen.receipt

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.feedback.PhotoSourceBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.format.toInputDateLabel
import com.yudha.catatanbelanja.android.photo.rememberReceiptPhotoPicker
import com.yudha.catatanbelanja.android.screen.receipt.components.ScanReceiptIntro
import com.yudha.catatanbelanja.android.screen.receipt.components.ScanReceiptItemSheet
import com.yudha.catatanbelanja.android.screen.receipt.components.ScanReceiptReview
import com.yudha.catatanbelanja.android.screen.receipt.components.ScanReceiptSaveBar
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.service.ReceiptScanException
import com.yudha.catatanbelanja.features.receipt.domain.model.ScannedItemRow
import com.yudha.catatanbelanja.features.receipt.presentation.ScanReceiptEffect
import com.yudha.catatanbelanja.features.receipt.presentation.ScanReceiptViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Riwayat > Scan struk: photograph a receipt from a trip that was never logged, check what the
 * model read off it, and save it as a finished session dated to the paper rather than to today.
 *
 * Pushed route, no tab bar. Nothing reaches the database until the save bar is pressed, so leaving
 * the screen throws the draft away and costs only the scan.
 */
@Composable
fun ScanReceiptScreen(
    onBack: () -> Unit,
    onOpenSessionDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanReceiptViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val context = LocalContext.current
    val savedMessage = stringResource(R.string.scan_toast_saved)
    val invalidDateMessage = stringResource(R.string.scan_toast_invalid_date)
    val itemDeletedMessage = stringResource(R.string.common_item_toast_deleted)
    val unreadablePhotoMessage = stringResource(R.string.photo_toast_failed)

    var showSourceSheet by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<ScannedItemRow?>(null) }

    val photoPicker = rememberReceiptPhotoPicker(
        onPhoto = viewModel::scan,
        onFailed = { appUi.showToast(unreadablePhotoMessage) },
    )

    // Keyed on the scan, not on the values: a second scan of the same shop on the same date must
    // still reseed these, and a keystroke must never be overwritten by a state emission.
    var tripName by remember(state.scanId) { mutableStateOf(state.store) }
    var store by remember(state.scanId) { mutableStateOf(state.store) }
    var dateText by remember(state.scanId) {
        mutableStateOf(
            when (state.hasScan) {
                true -> state.purchasedAt.toInputDateLabel()
                false -> ""
            },
        )
    }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.effects.collect { effect ->
            when (effect) {
                ScanReceiptEffect.ScanReady -> showSourceSheet = false
                ScanReceiptEffect.InvalidDate -> appUi.showToast(invalidDateMessage)
                ScanReceiptEffect.ItemDeleted -> appUi.showToast(itemDeletedMessage)
                is ScanReceiptEffect.Saved -> {
                    appUi.showToast(savedMessage)
                    // Straight to the trip it became: the receipt is the confirmation.
                    onOpenSessionDetail(effect.sessionId)
                }
            }
        }
    }

    LaunchedEffect(state.scanState) {
        val scan = state.scanState
        if (scan is UiState.Loading) {
            appUi.showLoading()
            return@LaunchedEffect
        }
        appUi.dismissLoading()
        if (scan !is UiState.Error) return@LaunchedEffect
        showSourceSheet = false
        appUi.showToast(context.scanErrorMessage(scan.failure.code))
    }

    LaunchedEffect(state.actionState) {
        val action = state.actionState
        if (action is UiState.Loading) {
            appUi.showLoading()
            return@LaunchedEffect
        }
        appUi.dismissLoading()
        if (action !is UiState.Error) return@LaunchedEffect
        appUi.showError(action.failure)
    }

    val saveBar: (@Composable () -> Unit)? = when (state.hasScan) {
        true -> ({
            ScanReceiptSaveBar(
                total = state.total,
                itemCount = state.itemCount,
                canSave = state.canSave,
                onSave = { viewModel.save(tripName, store, dateText) },
            )
        })
        false -> null
    }

    AppScaffold(
        modifier = modifier,
        bottomBar = saveBar,
        header = {
            AppScreenHeader(
                title = stringResource(R.string.scan_title),
                subtitle = when (state.hasScan) {
                    true -> pluralStringResource(
                        R.plurals.scan_review_count,
                        state.itemCount,
                        state.itemCount,
                    )

                    false -> stringResource(R.string.scan_subtitle)
                },
                onBack = onBack,
            )
        },
    ) {
        if (!state.hasScan) {
            ScanReceiptIntro(
                available = state.available,
                onChoosePhoto = { showSourceSheet = true },
                modifier = Modifier.fillMaxWidth(),
            )
            return@AppScaffold
        }

        ScanReceiptReview(
            state = state,
            tripName = tripName,
            onTripNameChange = { tripName = it },
            store = store,
            onStoreChange = { store = it },
            dateText = dateText,
            onDateChange = { dateText = it },
            onItemClicked = { row -> editingRow = row },
            onRetake = {
                viewModel.discard()
                showSourceSheet = true
            },
        )
    }

    if (showSourceSheet) {
        PhotoSourceBottomSheet(
            title = stringResource(R.string.photo_source_title),
            message = stringResource(R.string.photo_source_message),
            cameraText = stringResource(R.string.photo_source_camera),
            galleryText = stringResource(R.string.photo_source_gallery),
            cancelText = stringResource(R.string.common_cancel),
            canUseCamera = photoPicker.canTakePhoto,
            onCamera = photoPicker::takePhoto,
            onGallery = photoPicker::pickFromGallery,
            onDismiss = { showSourceSheet = false },
        )
    }

    val row = editingRow ?: return

    ScanReceiptItemSheet(
        item = row.item,
        emoji = row.emoji,
        onSave = { name, qtyText, unit, note, priceText ->
            editingRow = null
            viewModel.updateItem(
                itemId = row.item.id,
                name = name,
                qtyText = qtyText,
                unit = unit,
                note = note,
                priceText = priceText,
            )
        },
        onDelete = {
            editingRow = null
            viewModel.deleteItem(row.item.id)
        },
        onDismiss = { editingRow = null },
    )
}

/**
 * A failed scan gets a sentence the user can act on, chosen off [Failure.code]. The `Failure`
 * message itself is developer-facing and stays out of the UI — "paste a key into
 * local.properties" and "photograph it again" are different instructions and must not share one.
 */
private fun Context.scanErrorMessage(code: String?): String = when (code) {
    ReceiptScanException.MISSING_KEY -> getString(R.string.scan_error_missing_key)
    ReceiptScanException.REQUEST_FAILED -> getString(R.string.scan_error_request)
    ReceiptScanException.UNREADABLE_REPLY -> getString(R.string.scan_error_unreadable)
    ReceiptScanException.NO_ITEMS -> getString(R.string.scan_error_no_items)
    else -> getString(R.string.scan_error_generic)
}
