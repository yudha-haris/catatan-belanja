# Catatan Belanja — Architecture Contract (KMP / Android)

Authoritative contract for this codebase. Every package, class name and signature below is
binding: parallel implementers must match it exactly so the pieces compile together.

- **Product behaviour, copy, visuals**: `../../catatan-belanja.html` (the concept prototype).
- **Code style**: `../../code_rules.md`.

---

## 0. Stack (verified building — do not change versions)

| | |
|---|---|
| Gradle | 8.11.1 (wrapper) |
| AGP | 8.7.3 |
| Kotlin | 2.1.0 (multiplatform) |
| JDK / jvmTarget | 17 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 |
| UI | Jetpack Compose, BOM 2024.12.01, Material 3 |
| DI | Koin 4.0.0 (`koin-core` shared, `koin-android` + `koin-androidx-compose` app) |
| DB | SQLDelight 2.0.2 (`android-driver`, `native-driver`) |
| Async | kotlinx-coroutines 1.9.0, `Flow` / `StateFlow` |
| JSON | kotlinx-serialization-json 1.7.3 |
| Time | kotlinx-datetime 0.6.1 |
| ViewModel | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.8.4` (KMP), in `shared` |
| Navigation | `androidx.navigation:navigation-compose` 2.8.5 (Android only) |

Modules: `:shared` (KMP — domain, data, presentation) and `:androidApp` (Compose UI only).
Targets: `androidTarget`, `iosX64`, `iosArm64`, `iosSimulatorArm64`. Only the Android UI exists
today; the iOS targets exist so `shared` stays honestly multiplatform.

Commands:
```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:allTests
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew lint
```

---

## 1. How `code_rules.md` maps onto Kotlin/KMP

The rulebook is Flutter-flavoured but the architecture it describes is plain clean
architecture. Translation table — **this mapping is binding**:

| `code_rules.md` (Flutter) | Here (KMP) |
|---|---|
| Cubit + `emit(state.copyWith(...))` | `ViewModel` exposing `StateFlow<XxxState>`, updated via `_state.update { it.copy(...) }` |
| `Resource<T>` | `Resource<T>` sealed interface — same API, same name |
| `UiState<T>` (`UiInitial`/`UiLoading`/`UiSuccess`/`UiError`) | identical sealed interface |
| Freezed immutable models | Kotlin `data class` (immutable `val`s, default values) |
| `@injectable` / `@LazySingleton` / GetIt | Koin `module { }` — `single`, `factory`, `viewModel` |
| Constructor injection, no `locator<T>()` in classes | Constructor injection; `get()`/`koinInject()` only inside Koin modules and Compose entry points |
| sqflite + DAOs | SQLDelight `.sq` files + DAO classes wrapping the generated queries |
| courtwin_core widget catalog | `androidApp/.../designsystem/` composables with the same names (`AppButton`, `AppCard`, …) |
| `context.s.someKey` | `stringResource(R.string.some_key)` via Android string resources |
| `ScreenDialogs` / `ScreenBottomsheets` mixins | `AppDialogHost` / `rememberAppSheetController` — see §7 |
| `BlocListener` for one-shot effects | `Channel<XxxEffect>` → `Flow`, collected once in a `LaunchedEffect` |
| "No `setState`" | No `var` state outside the ViewModel except Compose-local `remember { mutableStateOf(...) }` for *pure UI* concerns (focus, sheet open/closed, text field buffers) |
| "No `Widget _buildX()` helper methods" | **No private `@Composable fun` that returns UI declared inside another file's composable scope as a helper** — every composable is a top-level `@Composable fun` in its own file (see §2) |
| "One file, one class" | One top-level composable (or class) per file |
| "Screen-specific widgets are private + `part of`" | Screen-specific composables are `internal` (or private to the file when tiny) and live in `screens/<screen>/components/`, package-private to that screen's package |
| Guard clauses, `when` over `if/else` ladders, no `else if` | Same, `when` with exhaustive branches on sealed types / enums |
| No derived getters on state classes | Same — the ViewModel computes and puts explicit fields in the state |
| `AppButton(disabled = ...)` not `onPressed: null` | Same: `AppButton(..., enabled = false)` renders the disabled style; never pass `onClick = {}` to fake it |

### Deviations (deliberate — do not "fix")

1. **`courtwin_core` is reimplemented locally** in `androidApp/.../designsystem/` under the same
   names. That package is private to another product.
2. **§5 "max text size `bodyMedium`, max weight `w500`"** is not applied. The concept's core
   visual is a 40sp / ExtraBold receipt total. The enforced form of the rule stands:
   **never hardcode a `TextStyle`, `FontWeight`, `sp` size or `Color` inside a screen or
   component** — always `AppTheme.typography.*` and `AppTheme.colors.*`.
3. **§2 AP-7 Form B ("duplicate the layer stack per feature")** is not applied. That rule exists
   for features owning separate API contracts. Here there is one local database, so shared
   domain models, repositories and the DB live in `com.yudha.catatanbelanja.core.*` — the role
   `courtwin_core` plays in the original. **Feature-to-feature imports remain strictly
   forbidden**: `features.stock` may not import from `features.shopping`.
4. **§8 timezone**: device local time only. Single-device offline app, no server, no venue.
   All timestamps are stored as epoch millis and rendered in the system time zone.
5. **§3 API serializers** are applied to the **backup/import JSON DTOs only** — that JSON comes
   from an external file, which is exactly the loose-typing problem the serializers exist for.
6. Light theme only (three colour flavours). The concept has no dark variant.

### Formatting clarification of §5 "no manual logic in UI"

A composable MAY call the shared formatters (`price.toRupiah()`, `instant.toDayLabel()`).
A composable MUST NOT contain arithmetic, sorting, filtering, grouping, or business
conditionals. Anything beyond a straight format call is computed in the ViewModel or a use
case and emitted as an explicit state field or view model.

---

## 2. Package layout

Root package: `com.yudha.catatanbelanja`

### `:shared` — `shared/src/commonMain/kotlin/com/yudha/catatanbelanja/`

```
core/
  common/
    Resource.kt                 sealed interface Resource<out T>
    Failure.kt                  data class Failure
    UiState.kt                  sealed interface UiState<out T>
    Clock.kt                    interface Clock / SystemClock
    IdGenerator.kt              interface IdGenerator / RandomIdGenerator
    Strings.kt                  String.normalized(), String.capitalizeWords()
  catalog/
    CatalogData.kt              categories, units, defaultUnits, fallbackEmoji
    UnitConversion.kt           unit families and factors — mass and volume convert, counts do not
  domain/
    model/
      ShoppingItem.kt
      ShoppingSession.kt
      ShoppingList.kt
      ShoppingListItem.kt
      NameChipView.kt
      StockItem.kt
      StockCheckEntry.kt
      StockCheckLog.kt
      ItemCategory.kt
      AppSettings.kt
      ThemeFlavor.kt            enum PURPLE, GREEN, BLUE
      SessionSummary.kt
      ImportSummary.kt
      LastPurchase.kt
      PriceBasis.kt              enum RAW, PER_UNIT
      TrendSetting.kt
      QtyOverride.kt
    repository/
      SessionRepository.kt
      StockRepository.kt
      ShoppingListRepository.kt
      SettingsRepository.kt
      BackupRepository.kt
      TrendRepository.kt
    service/
      FileSharer.kt             interface (expect/actual-free; Android impl in androidMain)
      ClipboardWriter.kt        interface
    usecase/
      FindItemCategory.kt
      BuildNameSuggestions.kt   shared by the live session and the list screen
      BuildNameChips.kt         name -> NameChipView, ditto
  data/
    database/
      DatabaseDriverFactory.kt  expect class
      DatabaseProvider.kt       builds CatatanBelanjaDatabase from the driver
      SessionDao.kt
      StockDao.kt
      ShoppingListDao.kt
      SettingsDao.kt
      TrendDao.kt
      Mappers.kt                row -> domain mapping helpers (internal)
    backup/
      BackupDto.kt              @Serializable DTOs (one file may hold the DTO family)
      BackupCodec.kt            encode/decode + lenient coercion
      DemoDataFactory.kt        the prototype's seedDemo(), deterministic
    repository/
      SessionRepositoryImpl.kt
      StockRepositoryImpl.kt
      ShoppingListRepositoryImpl.kt
      SettingsRepositoryImpl.kt
      BackupRepositoryImpl.kt
      TrendRepositoryImpl.kt
  di/
    CoreModule.kt               coreModule  — Clock, IdGenerator, IO dispatcher, shared use cases
    DataModule.kt               dataModule  — database, DAOs, repository bindings
    KoinInit.kt                 fun initKoin(platformModule: Module, appDeclaration: ...)
                                lists coreModule, dataModule and all seven feature modules
features/
  <feature>/
    di/
      <Feature>Module.kt        the feature's own Koin module: its ViewModels and use cases
    domain/
      model/                    feature-local view models
      usecase/
    presentation/
      <Name>ViewModel.kt
      <Name>State.kt
      <Name>Effect.kt           only when the feature has one-shot effects
```

`shared/src/androidMain/kotlin/.../` — `DatabaseDriverFactory.android.kt`,
`AndroidFileSharer.kt`, `AndroidClipboardWriter.kt`, `AndroidPlatformModule.kt`.
`shared/src/iosMain/kotlin/.../` — `DatabaseDriverFactory.ios.kt` plus stub service impls.

### `:androidApp` — `androidApp/src/main/kotlin/com/yudha/catatanbelanja/android/`

```
CatatanBelanjaApp.kt            Application, starts Koin
MainActivity.kt                 edge-to-edge (light system bars), AppTheme + AppNavHost
designsystem/
  theme/
    AppTheme.kt                 AppTheme object + CompositionLocals + AppTheme composable
    AppColors.kt                immutable colour holder + the three flavour palettes
    AppTypography.kt
    AppShapes.kt                radii, elevations/shadows, spacing tokens
  component/
    layout/      AppScaffold.kt, AppCard.kt, AppScreenHeader.kt, AppSectionHeader.kt
    button/      AppButton.kt, AppIconButton.kt, AppChip.kt
    input/       AppTextField.kt, AppMoneyField.kt, AppSearchField.kt,
                 AppUnitDropdown.kt, AppToggleTile.kt
    feedback/    AppBottomSheet.kt, ConfirmationBottomSheet.kt, AppDialog.kt,
                 ErrorDialog.kt, SuccessDialog.kt, LoadingDialog.kt,
                 AppToastHost.kt, SuccessBurst.kt, AppDialogHost.kt
    display/     ReceiptHeader.kt, AppListRow.kt, AppBadge.kt, AppStatCard.kt,
                 AppLevelBar.kt, AppEmptyState.kt, AppBarChart.kt, AppRankRow.kt,
                 AppLineChart.kt, AppCompareRow.kt
navigation/
  AppDestination.kt             sealed route definitions
  AppNavHost.kt
  MainShellScreen.kt            4 tabs + the floating pill tab bar
  ShellTabBar.kt
format/
  MoneyFormat.kt                Int.toRupiah(), Int.toRupiahShort(), Int.toRupiahSigned()
  QtyFormat.kt                  Double.toQtyLabel()
  DateFormat.kt                 Instant/Long -> day / long-date / time / short-date labels
screen/
  <feature>/
    <Name>Screen.kt             the route-level composable; collects state, wires effects
    components/                 screen-private composables, one per file
                                (`screen/list/` is the Daftar screen — see §11.6)
```

Formatters live in `:androidApp` because they are presentation concerns that use
`java.text`/`android.icu`. ViewModels must **not** format — they emit numbers, `Instant`s and
enums; the composable formats via these helpers.

---

## 3. Core primitives (`:shared`, `core/common`)

```kotlin
data class Failure(
    val message: String,
    val code: String? = null,
    val cause: Throwable? = null,
)

sealed interface Resource<out T> {
    data class Success<T>(val value: T) : Resource<T>
    data class Error(val failure: Failure) : Resource<Nothing>
}

val Resource<*>.isError: Boolean
fun <T> Resource<T>.dataOrNull(): T?
fun Resource<*>.failureOrNull(): Failure?

inline fun <T, R> Resource<T>.returnWhen(
    onSuccess: (T) -> R,
    onError: (Failure) -> R,
): R

/** Runs [block], wrapping any throw into Resource.Error. Used by every repository impl. */
suspend inline fun <T> resourceOf(
    message: String,
    crossinline block: suspend () -> T,
): Resource<T>
```

```kotlin
sealed interface UiState<out T> {
    data object Initial : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val failure: Failure) : UiState<Nothing>
}
```

Every ViewModel state carries `val loadState: UiState<Unit> = UiState.Initial` (and a separate
`actionState` when a screen has a distinct save/delete flow). Never a bespoke status enum.

```kotlin
interface Clock { fun nowMillis(): Long }
class SystemClock : Clock

interface IdGenerator { fun next(): String }
class RandomIdGenerator(private val clock: Clock) : IdGenerator
// base36 random (6 chars) + base36 millis — mirrors the prototype's uid()
```

```kotlin
fun String.normalized(): String     // trim + lowercase + collapse whitespace
fun String.capitalizeWords(): String
```

---

## 4. Domain models (`core/domain/model`)

Money is `Int` rupiah, no cents. Quantity is `Double?`. Timestamps are `Long` epoch millis.

```kotlin
data class ShoppingItem(
    val id: String,
    val name: String,
    val price: Int,
    val qty: Double? = null,
    val unit: String? = null,
    val note: String = "",          // brand / note
)

data class ShoppingSession(
    val id: String,
    val name: String = "",
    val store: String = "",
    val startedAt: Long,
    val endedAt: Long? = null,      // null => this is the active session
    val items: List<ShoppingItem> = emptyList(),
)

/**
 * The plan for the next trip — the note the user would otherwise keep in WhatsApp. Exactly one
 * list is "active" (`!isTemplate && archivedAt == null`); a template is the same shape kept for
 * reuse and is never active. Finishing a session archives the active list.
 */
data class ShoppingList(
    val id: String,
    val name: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val isTemplate: Boolean = false,
    val archivedAt: Long? = null,   // null => still in play
    val items: List<ShoppingListItem> = emptyList(),
)

/** Deliberately thinner than [ShoppingItem]: planning is a list of names, not of prices. */
data class ShoppingListItem(
    val id: String,
    val name: String,
    val note: String = "",
    val isChecked: Boolean = false,
)

/** A suggestion chip: the item name plus the category emoji drawn in front of it. */
data class NameChipView(val name: String, val emoji: String)

data class StockItem(
    val id: String,
    val name: String,
    val qty: Double = 0.0,
    val unit: String = "pcs",
    val minQty: Double? = null,     // remind when qty <= minQty
    val fullQty: Double = 0.0,      // high-water mark, drives the level bar
    val updatedAt: Long,
)

data class StockCheckEntry(val name: String, val qty: Double, val unit: String)

data class StockCheckLog(
    val id: String,
    val month: String,              // "YYYY-MM"
    val checkedAt: Long,
    val entries: List<StockCheckEntry> = emptyList(),
)

/**
 * One moment the app knew how much of a stock item was in the house — the only evidence behind an
 * automatic drain rate. Written whenever a quantity moves; a save that only changes the reminder
 * threshold writes none and leaves the item's `updatedAt` alone, so the estimate goes on counting
 * from when the amount was last actually known.
 */
data class StockReading(
    val itemId: String,
    val qty: Double,
    val unit: String,
    val at: Long,
    val source: ReadingSource,      // enum MANUAL | CHECK | PURCHASE
)

/** How fast one stock item is used up. No row for an item == these defaults. */
data class StockRate(
    val itemId: String,
    val mode: RateMode = RateMode.AUTO,               // enum AUTO | MANUAL | OFF
    val manualQty: Double? = null,                    // only meaningful under MANUAL
    val manualUnit: String? = null,
    val manualPeriod: RatePeriod = RatePeriod.WEEK,   // enum DAY(1) | WEEK(7) | MONTH(30), `days`
    val updatedAt: Long = 0L,
)

data class ItemCategory(val name: String, val emoji: String, val items: List<String>)

enum class ThemeFlavor { PURPLE, GREEN, BLUE }

data class AppSettings(val themeFlavor: ThemeFlavor = ThemeFlavor.PURPLE)

/** Shared view model: a session plus everything the UI shows about it. */
data class SessionSummary(
    val session: ShoppingSession,
    val total: Int,
    val itemCount: Int,
)

data class LastPurchase(
    val price: Int,
    val qty: Double?,
    val unit: String?,
    val note: String,
    val whenMillis: Long,
    val store: String,
)

enum class PriceBasis { RAW, PER_UNIT }

/**
 * How one item's price trend is measured. [nameKey] is `name.normalized()`; [baseUnit] only means
 * anything for PER_UNIT and is null until the user picks one.
 */
data class TrendSetting(
    val nameKey: String,
    val basis: PriceBasis = PriceBasis.RAW,
    val baseUnit: String? = null,
)

/** A quantity typed in after the trip. Read by the price trend and by nothing else. */
data class QtyOverride(
    val itemId: String,
    val nameKey: String,
    val qty: Double,
    val unit: String,
)

data class ImportSummary(
    val sessionsAdded: Int = 0,
    val stockAdded: Int = 0,
    val logsAdded: Int = 0,
)
```

Domain models are **not** `@Serializable` — persistence goes through SQLDelight rows and the
separate backup DTOs.

### `core/catalog/CatalogData.kt`

```kotlin
object CatalogData {
    val categories: List<ItemCategory>          // the 6 categories from the prototype
    val units: List<String> = listOf(
        "pcs", "kg", "gram", "liter", "ml", "bungkus", "ikat",
        "sisir", "buah", "botol", "kotak", "galon", "tabung",
    )
    val defaultUnits: Map<String, String>       // keys are normalized() names
    const val FALLBACK_EMOJI = "🛍️"
}
```

Copy the six categories, their emoji, their item lists, and the whole `UNIT_DEFAULT` map
verbatim from the prototype's `CATS` / `UNITS` / `UNIT_DEFAULT`.

`core/domain/usecase/FindItemCategory.kt`:
```kotlin
class FindItemCategory {
    operator fun invoke(name: String): ItemCategory?
    fun emojiFor(name: String): String
}
```

---

## 5. Database — SQLDelight

`.sq` files live in `shared/src/commonMain/sqldelight/com/yudha/catatanbelanja/db/`.
Database class `CatatanBelanjaDatabase`, package `com.yudha.catatanbelanja.db`.
Delete the placeholder `Smoke.sq` — it exists only to prove the build.

Files: `Session.sq`, `SessionItem.sq`, `ShoppingList.sq`, `ShoppingListItem.sq`,
`StockItem.sq`, `StockCheckLog.sq`, `StockCheckLogItem.sq`, `StockReading.sq`, `StockRate.sq`,
`Settings.sq`, `TrendSetting.sq`, `TrendQtyOverride.sq`.

**Schema version 4.** Every schema change ships a `<n>.sqm` migration next to the `.sq` files —
SQLDelight derives `Schema.version` from how many there are, runs the `.sq` files on a fresh
install and the migrations on an existing one. `1.sqm` adds the two shopping-list tables, `2.sqm` the two
price-trend tables and `3.sqm` the two smart-stock tables, so an install that predates any of them
upgrades instead of crashing on a missing table.

**A migration is analysed against the migrations before it, never against the `.sq` files.** There
are no generated `.db` schema files in this project, so SQLDelight replays `1.sqm`, `2.sqm`, … from
an empty database and checks each one against what its predecessors created. A `.sqm` therefore
cannot name a table that only a `.sq` file declares — which rules out a foreign key from a new
table to an existing one, because the key would compile on a fresh install and fail in the
migration, leaving upgraded databases without a cascade that fresh ones enforce. New tables that
hang off old ones carry **no** `REFERENCES` clause in either file (the two `CREATE TABLE` blocks
stay byte-identical) and are cleaned up explicitly by the owning DAO instead, inside the same
transaction that deletes the parent row.

The same rule sends **backfills into Kotlin rather than into the migration**: a `.sqm` cannot read
the old tables it would need. `StockRepositoryImpl.getReadings()` seeds any stock item that has no
readings from that item's past month-end checks, lazily and once per item. Being Kotlin it matches
names with `normalized()` the way the rest of the app does instead of approximating it in SQL, and
it also catches items that arrive long after the upgrade — from a restored backup, or the demo data.

```sql
-- Session.sq
CREATE TABLE session (
    id          TEXT NOT NULL PRIMARY KEY,
    name        TEXT NOT NULL DEFAULT '',
    store       TEXT NOT NULL DEFAULT '',
    started_at  INTEGER NOT NULL,
    ended_at    INTEGER            -- NULL => active session
);
CREATE INDEX session_ended_at ON session(ended_at);

-- SessionItem.sq
CREATE TABLE session_item (
    id         TEXT NOT NULL PRIMARY KEY,
    session_id TEXT NOT NULL REFERENCES session(id) ON DELETE CASCADE,
    name       TEXT NOT NULL,
    qty        REAL,
    unit       TEXT,
    price      INTEGER NOT NULL DEFAULT 0,
    note       TEXT NOT NULL DEFAULT '',
    position   INTEGER NOT NULL DEFAULT 0     -- ascending == newest first
);
CREATE INDEX session_item_session_id ON session_item(session_id);

-- ShoppingList.sq
CREATE TABLE shopping_list (
    id          TEXT NOT NULL PRIMARY KEY,
    name        TEXT NOT NULL DEFAULT '',
    created_at  INTEGER NOT NULL,
    updated_at  INTEGER NOT NULL,
    is_template INTEGER NOT NULL DEFAULT 0,   -- 1 => a reusable template, never the active list
    archived_at INTEGER                       -- NULL => still in play
);
CREATE INDEX shopping_list_state ON shopping_list(is_template, archived_at);

-- ShoppingListItem.sq
CREATE TABLE shopping_list_item (
    id       TEXT NOT NULL PRIMARY KEY,
    list_id  TEXT NOT NULL REFERENCES shopping_list(id) ON DELETE CASCADE,
    name     TEXT NOT NULL,
    note     TEXT NOT NULL DEFAULT '',
    checked  INTEGER NOT NULL DEFAULT 0,
    position INTEGER NOT NULL DEFAULT 0     -- ascending == the order they were written down
);
CREATE INDEX shopping_list_item_list_id ON shopping_list_item(list_id);

-- StockItem.sq
CREATE TABLE stock_item (
    id         TEXT NOT NULL PRIMARY KEY,
    name       TEXT NOT NULL,
    qty        REAL NOT NULL DEFAULT 0.0,
    unit       TEXT NOT NULL DEFAULT 'pcs',
    min_qty    REAL,
    full_qty   REAL NOT NULL DEFAULT 0.0,
    updated_at INTEGER NOT NULL
);

-- StockReading.sq -- no REFERENCES, per the rule above
CREATE TABLE stock_reading (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id TEXT NOT NULL,
    qty     REAL NOT NULL,
    unit    TEXT NOT NULL,
    at      INTEGER NOT NULL,
    source  TEXT NOT NULL      -- 'MANUAL' | 'CHECK' | 'PURCHASE'
);
CREATE INDEX stock_reading_item_at ON stock_reading(item_id, at);

-- StockRate.sq -- ditto
CREATE TABLE stock_rate (
    item_id       TEXT NOT NULL PRIMARY KEY,
    mode          TEXT NOT NULL DEFAULT 'AUTO',   -- 'AUTO' | 'MANUAL' | 'OFF'
    manual_qty    REAL,
    manual_unit   TEXT,
    manual_period TEXT,                           -- 'DAY' | 'WEEK' | 'MONTH'
    updated_at    INTEGER NOT NULL
);

-- StockCheckLog.sq
CREATE TABLE stock_check_log (
    id         TEXT NOT NULL PRIMARY KEY,
    month      TEXT NOT NULL UNIQUE,          -- 'YYYY-MM'
    checked_at INTEGER NOT NULL
);

-- StockCheckLogItem.sq
CREATE TABLE stock_check_log_item (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    log_id TEXT NOT NULL REFERENCES stock_check_log(id) ON DELETE CASCADE,
    name   TEXT NOT NULL,
    qty    REAL NOT NULL,
    unit   TEXT NOT NULL
);
CREATE INDEX stock_check_log_item_log_id ON stock_check_log_item(log_id);

-- Settings.sq
CREATE TABLE settings (
    key   TEXT NOT NULL PRIMARY KEY,
    value TEXT NOT NULL
);

-- TrendSetting.sq — how one item's price trend is measured. Keyed by name.normalized(), so the
-- setting survives the same item being written down with different capitalisation next trip.
-- A name with no row here reads as the default, RAW.
CREATE TABLE trend_setting (
    name_key   TEXT NOT NULL PRIMARY KEY,
    basis      TEXT NOT NULL DEFAULT 'RAW',   -- 'RAW' | 'PER_UNIT'
    base_unit  TEXT,                          -- the unit a PER_UNIT price is quoted in
    updated_at INTEGER NOT NULL
);

-- TrendQtyOverride.sq — a quantity typed in after the trip, for a purchase whose receipt recorded
-- none. No REFERENCES session_item(id): see the migration note above. SessionDao deletes these
-- rows itself, in the same transactions that drop the items.
CREATE TABLE trend_qty_override (
    item_id  TEXT NOT NULL PRIMARY KEY,
    name_key TEXT NOT NULL,
    qty      REAL NOT NULL,
    unit     TEXT NOT NULL
);
CREATE INDEX trend_qty_override_name_key ON trend_qty_override(name_key);
```

`SessionDao` stands in for the missing cascade: `deleteSession`, `deleteAllSessions`, `deleteItem`
and `updateItem` all drop the matching `trend_qty_override` rows inside their own transaction.
`updateItem` is included deliberately — an edited receipt makes the correction a stale second
opinion, and one the user cannot see from the edit sheet.

Each `.sq` also declares its named queries (`selectAll`, `selectById`, `insert`, `update`,
`deleteById`, …). Only `.sq` files contain SQL; no raw SQL strings anywhere in Kotlin.

```kotlin
// core/data/database/DatabaseDriverFactory.kt  (commonMain)
expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}
```
Android actual takes a `Context` constructor param and returns `AndroidSqliteDriver(
CatatanBelanjaDatabase.Schema, context, "catatan_belanja.db", callback = ...)` with
`PRAGMA foreign_keys=ON` enabled in `onOpen`. iOS actual uses `NativeSqliteDriver`.

DAOs are plain classes taking `CatatanBelanjaDatabase` in the constructor. They accept and
return **domain models**, mapping rows in `Mappers.kt`. Multi-table writes use
`database.transaction { }`. All DAO functions are `suspend` and run on
`Dispatchers.IO`-equivalent via `withContext(dispatcher)` where `dispatcher: CoroutineDispatcher`
is constructor-injected (`Dispatchers.Default` on iOS is fine; Koin provides `Dispatchers.IO`
on Android).

---

## 6. Repositories

Interfaces in `core/domain/repository`, impls in `core/data/repository`. Every function is
`suspend` and returns `Resource<T>`.

```kotlin
interface SessionRepository {
    suspend fun getFinishedSessions(): Resource<List<ShoppingSession>>
    suspend fun getActiveSession(): Resource<ShoppingSession?>
    suspend fun getSession(id: String): Resource<ShoppingSession?>
    suspend fun startSession(store: String): Resource<ShoppingSession>
    suspend fun updateStore(sessionId: String, store: String): Resource<Unit>
    suspend fun addItem(sessionId: String, item: ShoppingItem): Resource<Unit>
    suspend fun updateItem(sessionId: String, item: ShoppingItem): Resource<Unit>
    suspend fun deleteItem(sessionId: String, itemId: String): Resource<Unit>
    suspend fun finishSession(sessionId: String, name: String): Resource<Unit>
    suspend fun cancelActiveSession(): Resource<Unit>
    suspend fun deleteSession(sessionId: String): Resource<Unit>
}

interface StockRepository {
    suspend fun getStockItems(): Resource<List<StockItem>>
    suspend fun upsertStockItem(item: StockItem): Resource<Unit>
    suspend fun deleteStockItem(id: String): Resource<Unit>
    /** Adds every item of [session] that has a qty into stock. Returns how many were added. */
    suspend fun addSessionToStock(session: ShoppingSession): Resource<Int>
    suspend fun getCheckLogs(): Resource<List<StockCheckLog>>
    /** Upserts the log for the current month and updates every stock item's qty. */
    suspend fun saveStockCheck(entries: List<StockCheckEntry>): Resource<Unit>
    suspend fun deleteCheckLog(id: String): Resource<Unit>

    /** Readings per stock item id, oldest first. Backfills any item that has none — see §5. */
    suspend fun getReadings(): Resource<Map<String, List<StockReading>>>
    /** Saved rates by item id; an item with no entry is on the `StockRate` defaults. */
    suspend fun getRates(): Resource<Map<String, StockRate>>
    suspend fun saveRate(rate: StockRate): Resource<Unit>
}

interface SettingsRepository {
    suspend fun getSettings(): Resource<AppSettings>
    /**
     * Live settings, re-emitted on every write, so a theme picked in Settings repaints the app
     * immediately instead of waiting for the screen to close. A stream rather than the usual
     * Resource-returning suspend call: the only consumer is the theme, and a theme that cannot be
     * read degrades to the default rather than raising an error at the user.
     */
    fun observeSettings(): Flow<AppSettings>
    suspend fun saveThemeFlavor(flavor: ThemeFlavor): Resource<Unit>
}

/**
 * The manual corrections behind the price trend. Everything is keyed by `name.normalized()`.
 * [getSetting] never returns null: a name with nothing saved reads back as the default.
 */
interface TrendRepository {
    suspend fun getSetting(nameKey: String): Resource<TrendSetting>
    suspend fun saveSetting(setting: TrendSetting): Resource<Unit>
    suspend fun getOverrides(nameKey: String): Resource<List<QtyOverride>>
    suspend fun saveOverride(override: QtyOverride): Resource<Unit>
    suspend fun deleteOverride(itemId: String): Resource<Unit>
    suspend fun clearAll(): Resource<Unit>
}

interface BackupRepository {
    /** The full backup document, pretty-printed. Compatible with the prototype's "Salin data". */
    suspend fun buildBackupJson(): Resource<String>
    /**
     * Writes the backup to a cache file and opens the system share sheet. The file is named
     * `catatan-belanja-YYYY-MM-DD-HHmm.json` so successive exports do not overwrite each other.
     */
    suspend fun shareBackup(): Resource<Unit>
    suspend fun copyBackupToClipboard(): Resource<Unit>
    /** null value == the user cancelled the picker. */
    suspend fun importFromJson(rawJson: String): Resource<ImportSummary>
    suspend fun clearAllData(): Resource<Unit>
    suspend fun seedDemoData(): Resource<Unit>
}
```

File picking is an Android UI concern (`ActivityResultContracts.OpenDocument`), so the picker
itself lives in the settings screen; it hands the **text it read** to
`SettingsViewModel.importFromText(raw)`. `FileSharer` / `ClipboardWriter` are interfaces in
`core/domain/service`, implemented in `androidMain`, injected into `BackupRepositoryImpl`
(never into a ViewModel — §4 of the rulebook).

### Backup JSON format (byte-compatible with the prototype)

```json
{
  "version": 1,
  "exportedAt": 1735689600000,
  "theme": "purple",
  "sessions": [
    { "id": "abc", "name": "Superindo", "store": "Superindo",
      "startedAt": 1735600000000, "endedAt": 1735689600000,
      "items": [ { "id": "i1", "name": "Beras", "qty": 5, "unit": "kg", "price": 72000, "note": "" } ] }
  ],
  "stok": [
    { "id": "s1", "name": "Beras", "qty": 3, "unit": "kg", "min": 2, "full": 5, "updatedAt": 1735689600000 }
  ],
  "stokLog": [
    { "id": "l1", "month": "2026-08", "at": 1735689600000,
      "items": [ { "name": "Beras", "qty": 3, "unit": "kg" } ] }
  ]
}
```

Import must tolerate: a bare top-level array (treated as `sessions`), missing keys, `qty` as
string / number / null, `price` as string or number, `min` absent or null, unknown keys.
Use `Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }` plus
custom `KSerializer`s in `BackupDto.kt` for the number-or-string fields (the KMP equivalent of
the rulebook's `@IntSerializer` / `@DoubleSerializer`; name them `IntOrStringSerializer`,
`NullableDoubleOrStringSerializer`, …).

Merge semantics (never overwrite): sessions matched by `id`, stock by `name.normalized()`,
logs by `month`. `ImportSummary` reports how many were actually added.

### Demo data

`DemoDataFactory` reproduces the prototype's `seedDemo()` exactly: 6 sessions spread backwards
at 13-day intervals, a 2%-per-session inflation drift, and the deterministic LCG
`seed = (seed * 9301 + 49297) % 233280` starting at `seed = 7`. Same base item table, same shop
list, same brand notes, the 9 demo stock rows, and one stock check log for last month.

---

## 7. Design system — `androidApp/.../designsystem/`

### Theme

```kotlin
@Immutable
data class AppColors(
    val primary: Color, val primaryDark: Color, val primaryLight: Color,
    val tint: Color, val background: Color, val paper: Color,
    val heroStart: Color, val heroEnd: Color,
    val ink: Color, val inkSecondary: Color, val inkTertiary: Color, val line: Color,
    val mint: Color, val mintBg: Color, val coral: Color, val coralBg: Color,
    val confetti: List<Color>,
)

object AppTheme {
    val colors: AppColors @Composable @ReadOnlyComposable get() = LocalAppColors.current
    val typography: AppTypography @Composable @ReadOnlyComposable get() = LocalAppTypography.current
    val shapes: AppShapes @Composable @ReadOnlyComposable get() = LocalAppShapes.current
}

@Composable
fun AppTheme(flavor: ThemeFlavor, content: @Composable () -> Unit)
```

`AppTheme` also installs a Material 3 `MaterialTheme` derived from the flavour so Material
components (sheets, ripples) inherit the right colours.

**System bars are `MainActivity`'s job.** The app is light-theme only, so `enableEdgeToEdge` is
given `SystemBarStyle.light(...)` for both bars. Left on its default it reads the *system* dark
mode instead, painting white icons onto the app's white background.

**Keyboard insets are `AppScaffold`'s job.** `MainActivity` calls `enableEdgeToEdge()`, which sets
`decorFitsSystemWindows = false` and makes the manifest's `android:windowSoftInputMode`
inert — the window never resizes. Without `imePadding()` on the scaffold's content the scroll
viewport keeps its full height, so a field that takes focus has nowhere to scroll into and simply
sits under the keyboard. `BasicTextField` brings itself into view once there is room; do not add a
second `BringIntoViewRequester` around it, because the field's own request runs last and wins.

Palettes — from the prototype CSS, do not invent values:

```
purple: primary #7C3AED, primaryDark #5B21B6, primaryLight #A78BFA, tint #EFE9FF,
        background #F7F4FF, heroStart #7C3AED, heroEnd #C026D3,
        ink #2B1D4A, inkSecondary #6B6383, inkTertiary #A39BB8, line #ECE8F3
green:  primary #059669, primaryDark #047857, primaryLight #6EE7B7, tint #E6F7EF,
        background #F2FBF6, heroStart #059669, heroEnd #65A30D,
        ink #173A2D, inkSecondary #5B6E66, inkTertiary #A39BB8, line #E2EFE8
blue:   primary #2563EB, primaryDark #1D4ED8, primaryLight #93C5FD, tint #E8EFFF,
        background #F3F7FF, heroStart #2563EB, heroEnd #0891B2,
        ink #14224A, inkSecondary #5F6A8A, inkTertiary #A39BB8, line #E3E9F7
shared: paper #FFFFFF, mint #16A34A, mintBg #DCFCE7, coral #E11D48, coralBg #FFE4E6
confetti: [#7C3AED, #C026D3, #F59E0B, #10B981, #3B82F6, #F43F5E]
```

Typography (`AppTypography`, all Plus Jakarta Sans — bundle the TTFs in
`androidApp/src/main/res/font/` as `plus_jakarta_sans_regular.ttf` … `_extrabold.ttf` and build
a `FontFamily`; fall back to `FontFamily.SansSerif` only if a weight is missing):

| Token | Size / weight / letterSpacing | Used for |
|---|---|---|
| `receiptTotal` | 40sp / W800 / -0.03em | receipt hero total |
| `receiptTotalSmall` | 30sp / W800 / -0.03em | compact receipt |
| `heroTitle` | 26sp / W800 / -0.02em | hero heading |
| `screenTitle` | 24sp / W800 / -0.02em | screen h1 |
| `sheetTitle` | 20sp / W800 | bottom sheet title |
| `sectionTitle` | 17sp / W700 | section h2 |
| `rowTitle` | 15sp / W700 | list row title |
| `price` | 15sp / W800 / tabular | trailing price |
| `bodyLarge` | 16sp / W500 | input text |
| `body` | 15sp / W400 / line-height 1.45 | body |
| `muted` | 13sp / W400 / inkSecondary | `.muted` |
| `label` | 14sp / W600 | chips / buttons |
| `fieldLabel` | 12sp / W600 / inkSecondary | field labels |
| `tiny` | 12sp / W400 / inkTertiary | `.tiny` |

Shapes / spacing (`AppShapes`): `radius = 22.dp`, `radiusSmall = 14.dp`, `radiusItem = 18.dp`,
`radiusSheet = 28.dp`, `pill = 999.dp`. Card shadow: ambient `ink @ 18%`, blur 30, y-offset 10
(use `Modifier.shadow(elevation = 10.dp, shape, ambientColor, spotColor)` tuned to match).
Screen padding `PaddingValues(start = 16, top = 18, end = 16, bottom = 150)`; `listPadding` is
the same without the top gap (a list under a pinned header) and `headerPadding` is the header's
own `start = 16, top = 18, end = 16`.
Max content width 440.dp, centred. Icon badge 40×40, radius 14, `tint` background, 20sp emoji.

### Components

Signatures are binding for the names and the first parameters; add optional params as needed.

```kotlin
AppScaffold(header, bottomBar, backgroundColor, contentPadding, content)
    // header sits OUTSIDE the scrolling area — the back pill and the screen's actions stay
    // reachable however far the content has scrolled. It carries shapes.headerPadding, and
    // contentPadding's top gap is dropped whenever a header is present or the two stack.
    // Every screen passes its header here; none renders AppScreenHeader as a list item.
    // bottomBar is lifted by LocalShellBottomInset so it clears the shell's floating
    // tab bar; the inset is zero on pushed routes, which have no tab bar under them.
    // The content area carries imePadding(); bottomBar does not, so a pinned action bar
    // hides behind the keyboard instead of eating a third of what is left to type into.
AppCard(modifier, flat = false, borderColor, contentPadding, onClick, content)
AppScreenHeader(title, subtitle, onBack, actions)             // the `.top` row
AppSectionHeader(title, trailing)
Gaps: object Spacing { val x4..x32 } used as Spacer(Modifier.height(Spacing.x12))

AppButton(text, onClick, modifier, variant = AppButtonVariant.Primary, enabled = true,
          emoji = null, icon = null, big = false, fillWidth = true)
enum class AppButtonVariant { Primary, Ghost, Danger, Soft, OnHero }
AppIconButton(onClick, contentDescription, icon | emoji)      // the 40dp pill
AppChip(text, onClick, emoji = null, selected = false, variant = AppChipVariant.Tint)
enum class AppChipVariant { Tint, Plain, Dark, Danger, OnHero }

AppTextField(value, onValueChange, label, optionalLabel, placeholder, ...)
AppMoneyField(value, onValueChange, label)                    // "Rp" prefix, 22sp W700, digit grouping
AppSearchField(value, onValueChange, placeholder, onClear)
AppUnitDropdown(value, onValueChange, units, label)
AppToggleTile(title, subtitle, checked, onCheckedChange)

AppBottomSheet(onDismiss, content)                            // grip + 28dp top corners
ConfirmationBottomSheet(title, message, confirmText, cancelText, isDanger, onConfirm, onDismiss)
AppDialog / ErrorDialog(failure) / SuccessDialog(message) / LoadingDialog()
AppToastHost(message, toastId, onTimeout)                     // top pill, 1.8s
    // Keyed on toastId, never on the text: two identical toasts in a row are the same String,
    // so a timer keyed on the message would not restart — and if the second arrives in the frame
    // the first one's timeout clears, the pill is stranded with nothing left to take it down.
SuccessBurst(visible)                                         // check ring + confetti, 1.2s

ReceiptHeader(label, amount, footerLeft, footerRight, compact = false)
    // gradient heroStart -> heroEnd, zigzag bottom edge drawn with a Path in drawBehind
AppListRow(title, subtitle, trailing, trailingSub, trailingSubTone, emoji | leading,
           selected = false, dense = false, progress = null, onClick)
AppBadge(text, tone)          enum class AppBadgeTone { Tint, Up, Down, Neutral }
AppStatCard(label, value, hint, hintTone, onClick = null)
    // onClick is optional: a stat that stands for a single thing (the biggest trip of the window)
    // opens it; a stat that is only a number stays inert rather than pretending to be a button.
AppLevelBar(progress, isLow, estimate = null)
    // `estimate` splits the one bar into two tones instead of adding a second: solid up to what
    // the app reckons is still there, faded from there to what was last written down. The faded
    // stretch is what it believes is already used — a shadow of the fill, not a rival to it.
AppEmptyState(emoji, title, message, action)
AppBarChart(bars, onBarClick)     data class AppBarChartBar(label, valueLabel, ratio, highlighted)
AppRankRow(rank, emoji, title, valueLabel, ratio, hint)
AppLineChart(points)              data class AppLineChartPoint(valueLabel, dateLabel, ratio)
AppDonutChart(slices, centerValue, centerLabel)   data class AppDonutSlice(fraction, color)
    // The share ring. The caller owns the colours, because the ramp is a screen decision: the
    // ranking page steps one hue down in opacity rather than using six hues, which would read as
    // six categories that mean something when the arcs only run biggest to smallest.
AppRollingText(text, style, color)
    // A figure that rolls instead of cutting. Always upward — a shopping total only really
    // goes one way, and a direction guessed from the value would flip on the rare correction
    // and read as a glitch. Used for every number that moves while the user is watching it.
AppCompareRow(title, emoji, leftLabel, leftSub, rightLabel, rightSub, deltaLabel, deltaTone)
```

### Dialog / sheet / toast plumbing (replaces the `ScreenDialogs` mixin)

`AppDialogHost` is installed once, at the top of `MainActivity`'s content, and exposes a
controller through a `CompositionLocal`:

```kotlin
@Stable
class AppUiController {
    fun showToast(message: String)
    fun showLoading()
    fun dismissLoading()
    fun showError(failure: Failure)
    fun showSuccess(message: String)
    fun celebrate()                 // SuccessBurst
}
val LocalAppUi: ProvidableCompositionLocal<AppUiController>
```

Screens call `LocalAppUi.current` inside a `LaunchedEffect` that collects the ViewModel's
effect flow. **No `Snackbar` anywhere.**

---

## 8. Presentation rules

- One `ViewModel` per screen, in `:shared`, `features/<feature>/presentation/`.
- Constructor-injected dependencies only: repositories and use cases. Never a DAO, a driver,
  a `FileSharer`, or the database.
- State: a single `data class XxxState(...)` with `val`s and defaults, exposed as
  `val state: StateFlow<XxxState>`. Backing field `private val _state = MutableStateFlow(XxxState())`.
- **No computed properties on the state class.** The ViewModel computes and stores explicit fields.
- One-shot events (navigate, toast, celebrate) go through
  `private val _effects = Channel<XxxEffect>(Channel.BUFFERED)` /
  `val effects = _effects.receiveAsFlow()`, consumed in a single `LaunchedEffect(Unit)`.
- Every suspend call is launched in `viewModelScope`; results are handled with `returnWhen`.
- Guard clauses first (`if (state.value.loadState is UiState.Loading) return`), then the happy path.

Compose side:
- `val state by viewModel.state.collectAsStateWithLifecycle()`.
- Screens get their ViewModel via `koinViewModel()`.
- Screen-private composables are top-level functions in
  `screen/<feature>/components/`, marked `internal`, one per file.
- Text field buffers use `rememberTextFieldState`/`remember { mutableStateOf("") }` — that is
  local UI state, allowed. Anything the ViewModel must know about is pushed on submit or via
  an explicit `onXChanged` call.
- Lists use `LazyColumn` with stable `key = { it.id }`.

---

## 9. Navigation

```kotlin
sealed interface AppDestination {
    data object Shell : AppDestination                    // "shell"
    data object LiveSession : AppDestination              // "live"
    data class SessionDetail(val sessionId: String)       // "detail/{sessionId}"
    data class Compare(val aId: String, val bId: String)  // "compare/{aId}/{bId}"
    data object ShoppingList : AppDestination              // "list"
    data object Settings : AppDestination                 // "settings"
    data object SpendingReport : AppDestination           // "report/spending"
    data object SpendingRanking : AppDestination          // "report/ranking"
    data class PriceTrend(val name: String?)              // "report/trend?trendName={trendName}"
}
```

`PriceTrend` is the only route carrying user data rather than a generated id, so its argument is
`Uri.encode`d — not `URLEncoder`, which spells a space `+`; navigation decodes with `Uri.decode`
and would hand the screen a name with a literal plus in it.

`MainShellScreen` measures `ShellTabBar` and publishes its height through
`LocalShellBottomInset` (`designsystem/component/layout/`), so any tab screen's `AppScaffold`
bottomBar floats above the tabs instead of behind them. Measured rather than hardcoded so it
survives font scaling and taller navigation bars.

`AppNavHost` uses `androidx.navigation.compose` with those routes. `MainShellScreen` owns the
four tabs (Belanja / Riwayat / Stok / Ringkasan) — a `Scaffold` with the floating pill
`ShellTabBar` and the four tab composables kept alive via saved state; the tab index is
`rememberSaveable`. `LiveSession`, `SessionDetail`, `Compare`, `ShoppingList`, `Settings`,
`SpendingReport`, `SpendingRanking` and `PriceTrend` are pushed routes with no tab bar.

---

## 10. Strings

All user-visible text goes in `androidApp/src/main/res/values/strings.xml` (Indonesian, the
default locale) and `values-en/strings.xml` (English). Composables read them with
`stringResource(R.string.key)`.

- Key naming: `<area>_<thing>` snake_case — `home_greeting_morning`, `live_add_to_cart`,
  `history_compare_cta`, `stock_running_low`, `dashboard_top_spending`,
  `settings_export_title`, `common_save`, `common_cancel`, `common_delete`.
- Plurals / interpolation use `%1$s`, `%1$d` positional args, or `<plurals>` where a count
  genuinely inflects.
- Emoji may stay inline in composables — they are icons, not copy.
- Category names, unit names and the demo data stay in `CatalogData` / `DemoDataFactory`;
  they are data, not UI copy, and are Indonesian in both locales.
- ViewModels **never** touch strings. They emit enums / ids / numbers; the composable resolves
  the label. `Failure.message` is developer-facing; `ErrorDialog` shows a generic localized
  message and only surfaces `failure.message` as secondary detail.

---

## 11. Feature specs

### 11.1 `features/shopping`

**Start screen** (Belanja tab) — `StartViewModel` / `StartState`:
`loadState`, `greeting: Greeting` (enum MORNING/NOON/AFTERNOON/EVENING),
`activeSession: SessionSummary?`, `monthTotal: Int`, `monthCount: Int`, `monthAverage: Int`,
`recent: List<SessionSummary>` (3), `storeSuggestions: List<String>` (5 most recent distinct),
`hasAnySession: Boolean`, plus the plan summary the `StartListCard` renders —
`hasList`, `listTotalCount`, `listRemainingCount`, `listPreviewNames` (3),
`listExtraCount`. The card is a fixed slot above the hero whether or not a list exists, so the
screen does not reshuffle the first time the user makes one; it reads
`ShoppingListRepository` (a `core` repository, so no feature-to-feature import).
Low stock is deliberately NOT surfaced here — it is a count badge on
the Stok tab instead (see §11.7), which is why the prototype's "Stok menipis" card is absent.
Actions: `load()`, `startSession(store: String)`, `seedDemo()`.

**Live session screen** (pushed) — `LiveSessionViewModel` / `LiveSessionState`:
`session: ShoppingSession?`, `total: Int`, `itemViews: List<ShoppingItemView>`,
`query: String`, `nameSuggestions: List<NameChipView>`, `frequentNames: List<NameChipView>`,
`selectedCategory: String?`, `categoryItems: List<NameChipView>`, `lastPurchase: LastPurchase?`,
`brandSuggestions: List<String>`, `selectedUnit: String`, `isNamePicked: Boolean`,
`finishedAtMillis: Long`, `loadState`, `actionState`, plus the plan strip —
`hasList`, `listRemaining: List<NameChipView>`, `listPreview` (first 8), `listHiddenCount`,
`listTotalCount`, `listCheckedCount`, `listRemainingCount`, `listProgress: Float`,
`isListComplete`.
`LiveListStrip` sits **below** the add card, not above it, and shows `listPreview` until the
"+n lagi" chip is tapped: a twenty-item plan is a wall of chips, and a wall of chips must never
be what stands between the user and the field they came here to type in. The expanded flag is
`rememberSaveable` in the screen — the strip lives inside the `LazyColumn`, so a plain `remember`
would collapse it again every time it scrolled out of view.
Every strip chip is the same `pickName()` tap the suggestion chips already were, so following a
list costs the user no extra step; a successful `addItem` then calls
`ShoppingListRepository.checkItemByName`, which ticks the line off by itself. That call is
best-effort — the item is in the cart either way, so a failure leaves the line unticked rather
than failing the add. `deleteItem` mirrors it with `uncheckItemByName`, but only once the cart
holds no other item of that name: buying two bags of rice and removing one keeps the line crossed
off, while removing the only one un-crosses it. Without the mirror the plan drifts — a line stays
crossed off for something that was never bought.

Picking a name moves focus to Harga, which is usually below the fold; the field scrolls itself in
(see §7 on `imePadding`), and the keyboard's own Done adds the item and returns focus to the name
field, so a whole item is logged without the hands leaving the keyboard. `AppMoneyField` spells
out `ImeAction.Done` rather than leaving it to the platform default, so that `onDone` is
guaranteed to fire.

**The add card shows two inputs: what, and how much.** Jumlah, Satuan and Merk are all optional,
and five fields stacked under a receipt is the kind of form people stop filling in, so they fold
behind one chip (`live_detail_toggle`). The screen holds the open/closed flag in `rememberSaveable`
and never resets it between items: someone who wants quantities wants them for the whole trip.
Harga and the button are never folded away — both carry a `FocusRequester`, and unmounting a
focus target mid-flow is how a "focus next" quietly stops working.

Motion is the same vocabulary as the Daftar screen: the cart uses `Modifier.animateItem()` so a
bought item slides in at the head and a removed one collapses, the running total in
`LiveSessionBottomBar` is an `AppRollingText`, and `ReceiptHeader` keeps its scale bump on every
change.
`NameChipView(name, emoji)` — the prototype prints `icon(name)` on every suggestion chip, so the
emoji is resolved in the domain layer, never in the composable.
Effects: `ItemAdded(name, note, price)`, `Finished(sessionId, addedToStock, carriedOverToList)`,
`ListCompleted` (fires only on the transition, so a second bag of rice does not celebrate twice),
`ShowCancelSheet`, `Left`, `Cancelled`,
`NamePicked`, `ShowFinishSheet`, `NoteSuggested(note)`, `ShowMessage(kind)`, `ShowError(failure)`.
Actions: `load()`, `onNameChanged`, `pickName`, `pickCategory`, `pickBrand`, `pickUnit`,
`useLastPrice(note, qtyText)`, `addItem(name, qtyText, unit, note, priceText)`, `updateItem`,
`deleteItem` (which also un-ticks the plan — see below), `updateStore`, `openFinishSheet()`,
`finishSession(name, addToStock, carryOverList)`, `requestCancel()`, `cancelSession()`,
`leaveSession()`.

`leaveSession()` is what the header arrow and the system `BackHandler` both call. **A session
that bought nothing is deleted on the way out**: it was only ever an empty container, and leaving
it behind put a trip the user never took on the home screen as "Sedang belanja". Silent — backing
out is not an action that needs announcing. A cart with something in it is left running, which is
what the resume card is for.
`requestCancel()` is the "Batal" button: it opens the confirmation sheet only when there is a
cart to lose, and otherwise just cancels.
`openFinishSheet()` is what "Selesai ✓" calls: it refuses an empty cart the way the prototype's
`finishSheet()` does, and stamps `finishedAtMillis` so the receipt is dated at finish time.

Use cases (`features/shopping/domain/usecase/`):
- `BuildNameSuggestions` / `BuildNameChips` live in **`core/domain/usecase`**, not here: the list
  screen needs the same suggestions while planning, and `features.list` may not import
  `features.shopping`. `BuildNameSuggestions` returns known names (catalog ∪ history), filtered
  by query, prefix matches first, max 8, plus a "new item" affordance flag. With an empty query it
  returns the frequent list: top 8 by purchase count, prefixed by low-stock names and the repeat
  list, deduped, max 14.
- `FindLastPurchase` → `LastPurchase?`
- `FindBrandSuggestions` — distinct notes for a name, max 6.
- `FinishShoppingSession` — finishes, optionally calls `StockRepository.addSessionToStock`, then
  archives the shopping list, carrying the unbought lines into the next plan when asked.
  Returns `FinishResult(addedToStock, carriedOverToList)`. The list step runs last and is
  best-effort: the trip is already filed by then, so a list that will not close must not turn a
  finished session into an error — it simply stays open, visible on the Daftar screen.
- `BuildSessionItemViews` → `ShoppingItemView(item, emoji, qtyLabelParts, unitPrice, deltaFromPrevious)`.

### 11.2 `features/history`

**History screen** (Riwayat tab) — `HistoryViewModel`: sessions grouped by month →
`List<HistoryMonthGroup(monthKey, total, summaries)>` where `summaries` are
`HistorySessionRowView(summary, showStore)` — `BuildSessionRowView` decides `showStore`, so the
row composable only picks a string resource. `compareMode: Boolean`,
`pickedIds: List<String>` (max 2, FIFO), `hasAny: Boolean`.
The "2 terakhir" shortcut is offered only while nothing is picked: once there is a manual
selection it competes with the "Bandingkan" bar for the same job, so `canQuickCompare`
goes false and the shortcut steps aside.

**Session detail** (pushed) — `SessionDetailViewModel`: the `SessionSummary`, `totalDeltaAmount`
+ `isTotalUp` vs the previous session, per-item `priceDeltaAmount` + `isPriceUp`,
`otherSessions: List<HistorySessionRowView>` for the "compare with" sheet. Actions: repeat
session, delete session (confirmation), `updateItem(itemId, name, qtyText, unit, note, priceText)`
— the sheet hands over its raw buffers, the ViewModel parses them.

**Compare** (pushed) — `CompareViewModel` + `BuildCompareResult` use case →
`CompareResult(inBoth, onlyInA, onlyInB, totalA, totalB, delta, deltaPercent, upCount, downCount)`.
Items are aggregated by `name.normalized()` (sum price + qty, like the prototype's `buildMap`);
`inBoth` is sorted by absolute delta descending. `CompareRow` keeps each side's own unit
(`unitA` / `unitB`) and reports the gap as `deltaAmount` (unsigned) plus
`delta: CompareRow.Delta` (`NONE` / `SAME` / `UP` / `DOWN`).

### 11.3 `features/stock`

`StockViewModel`: `lowRows` / `okRows` (`StockRowView(item, emoji, ratio, isLow)`, sorted by
name), `logs: List<StockCheckLogView>`, counts. `isLow` = `minQty?.let { qty <= it } ?: (qty <= 0)`.
`ratio` = `qty / max(fullQty, qty, minQty ?: 0.0, 1.0)`.
Sheets: add/edit stock item, "cek sisa stok akhir bulan" (bulk qty entry → one log per month),
log detail.
`CalculateStockUsage` use case: per entry, `bought` = that month's purchases of the same name
and unit, `remaining` = the logged qty, `used ≈ max(0, previousRemaining + bought − remaining)`.

#### Smart stock — the estimate

The stored quantity answers "what did somebody last write down". Smart stock answers "what is
probably there now", and the two are kept apart everywhere: the estimate is a **shadow**, drawn
beside the stored number, and it never becomes the stored number without a deliberate tap.

**Where the rate comes from.** Two ways, exactly as the feature was asked for:
1. *Automatic*, the default nobody has to choose. Every quantity that moves writes a
   `StockReading`; `EstimateStockRate` reads consecutive pairs of them as consumption windows.
   Pairs where the quantity rose are restocks and are skipped — they only mark where the next
   window starts. Windows are pooled (`total used / total days`) rather than averaged one by one,
   so a six-week observation outweighs a one-week one, and only the newest 8 count so a changed
   habit surfaces within weeks. A pair in a different unit still counts when the two convert
   (`UnitConversion`) — 1 kg and 50 gram belong on one line, 1 botol and 1 liter do not.
2. *Manual*, via `StockRate.mode = MANUAL`. Quoted in the user's own framing ("1 kg per bulan"),
   because that is how people describe a household. A stated rate always beats the inferred one.

`EstimateCurrentStock` projects forward from `item.updatedAt` at whichever rate applies and returns
null wherever silence is the honest answer: mode `OFF`, an already-empty shelf, no rate, less than
a day elapsed, or under 5% of the shelf used — below which the estimate would only repeat the
number already on screen.

**Rules the UI is built to, in order of priority:**
1. **Nothing appears until there is something to say.** An item with no estimate renders exactly
   as it did before the feature existed.
2. **Sections and warnings are decided by the stored quantity, never by the estimate.** A guess may
   whisper `Alert.MAYBE_LOW` (grey, not coral); it may not move a row into "Perlu dibeli", because
   sending someone to the shop on a guess is how a guess stops being welcome.
3. **One tap, in a sheet the user already opened.** The row gains no button. `StockSmartCard` sits
   in the editor directly above the quantity field, and "Pakai" only *types* the estimate into that
   field — Simpan is still the user's own tap.
4. **All the configuration lives behind one quiet line.** `StockRateRow` is the only thing added to
   the editor; the three modes and the manual fields live in `StockRateSheet`, which the user has
   to ask for. A brand-new item shows no rate row at all: no history to learn from, no id to hang a
   rate on, and the add sheet stays as short as it has always been.
5. **The month-end check is never pre-filled with an estimate.** It is shown on the line as
   information, and that is all. This sheet is where the app finds out what is *actually* on the
   shelf; a line arriving with a guess in it gets confirmed rather than counted, and the estimator
   would then be learning from its own output.
6. **The estimate marks its own homework.** When the user saves a quantity that a prediction was
   standing next to, `ScoreStockEstimate` scores the prediction; within 15% it fires
   `StockEffect.EstimateHit` instead of `ItemSaved`, and the screen celebrates with the accuracy.
   Paired with `StockConfidenceDots` — which visibly fill in as windows accumulate — this is the
   only place the feature is allowed to claim it was right, and only ever against a number the
   user typed themselves.

Use cases: `EstimateStockRate` (readings → `StockRateEstimate`), `EstimateCurrentStock`
(item + rate → `StockShadow`), `CreateStockRate` (sheet fields → `StockRate`),
`ScoreStockEstimate` (prediction vs. what the user typed → accuracy % or null). The first three
are pure and covered by `shared/src/commonTest/.../features/stock/domain/usecase/`.
A fourth sheet joins the three above: the drain-rate sheet, opened from the editor.

### 11.4 `features/dashboard`

`DashboardViewModel` + `BuildDashboardData` use case →
`DashboardData(monthKey, monthTotal, previousMonthTotal, monthDeltaPercent, monthSessionCount,
monthAverage, recentBars (last 8, oldest first), topItems (top 5: name, total, count, ratio,
sharePercent))`.
Scope toggle `MONTH` / `ALL` affects `topItems` only.

**The trend card is not part of `DashboardData`.** `DashboardState` carries `trendCandidates` /
`trendNames` from `BuildTrendCandidates` and a `PriceTrendData` from `BuildPriceTrend` — the same
use case the trend page uses — because a trend depends on the item's saved `PriceBasis` and its
manual quantity corrections, which are read from the database rather than derived from the
sessions. A second, simpler copy of the maths living in `BuildDashboardData` is exactly how the
card and the page it links to would end up disagreeing about what an item's price did.
`drawTrend` swallows a repository failure on purpose: the summary tab is not the place to raise a
dialog about a chart, and an unadjusted line beats an error where a card used to be.

Each of the three cards ends in a `DashboardSeeAllRow` — one quiet, right-aligned chip that opens
the page behind it. The tab stays a summary; these three are where the summary is allowed to run
out of room.

**Shared derivation helpers** live in `domain/usecase/SessionMetrics.kt` as `internal` top-level
functions: `endedMillis()`, `total()`, `monthKeysIn`, `previousMonthKey`, `inRange`, `ratioOf`,
`percentOf`, `percentChange`, `averageOf`. Every derivation in this feature uses them, so the tab
and the three pages cannot drift into two different ideas of what "rata-rata" means. `inRange` is
calendar-aligned: "3 bulan" is three month columns, not ninety days.

**`ReportRange`** (`MONTH` / `THREE_MONTHS` / `SIX_MONTHS` / `ALL`) is shared by both report
pages. Each page publishes the windows it offers as `rangeOptions` in its state, so the chip row is
data rather than a list hardcoded in a composable.

#### Spending report (pushed, from the "8 belanja terakhir" card)

`SpendingReportViewModel` + `BuildSpendingReport` → `SpendingReportData(range, total, tripCount,
tripAverage, monthCount, monthAverage, highestTotal, highestSessionId, hasHighest, months,
monthBars, trips, hasAnyTrip)`.
`months` and `trips` run newest first; `monthBars` is the chart's own slice — the last 6 months,
oldest first, **re-scaled against each other** rather than against the whole window, because one
huge older month would flatten every bar in a six-bar chart. `MonthSpending` compares against the
previous month *in the same series*, so a gap month reads as no comparison rather than a 100% drop.
`TripSpending` carries `hasName` / `hasStore` so the row composable picks a string resource instead
of running a `name.ifBlank { store }` ladder of its own.
Actions: `load()`, `selectRange(range)`. Range chips re-derive; they never re-query.

#### Spending ranking (pushed, from the "Pengeluaran terbesar" card)

`SpendingRankingViewModel` + `BuildSpendingRanking` → `SpendingRankingData(range, mode, total,
entryCount, tripCount, entries, slices, leaderLabel, leaderPercent, hasEntries)`.
`RankingMode.ITEM` ranks individual items; `RankingMode.CATEGORY` rolls them up through
`FindItemCategory`. Anything the catalog does not know lands in one catch-all row flagged
`isOther` with a **blank label** — "lain-lain" is copy, so the composable resolves it. Dropping
those items instead would make every percentage on the page a lie.
`slices` is the donut: the five biggest plus one tail arc, with `fraction` taken off the totals
rather than off the rounded percentages, so the ring always closes exactly. An item bought twice or
more sets `canOpenTrend`, and tapping the row opens its price trend.
Actions: `load()`, `selectRange(range)`, `selectMode(mode)`.

#### Price trend (pushed, from the "Tren harga" card or a ranking row)

`PriceTrendViewModel` + `BuildTrendCandidates` + `BuildPriceTrend` → `PriceTrendData(name, emoji,
basis, baseUnit, baseUnitOptions, canUsePerUnit, points, purchases, hasTrend, usableCount,
skippedCount, firstValue, lastValue, deltaPercent, isUp, isDown, cheapest, dearest, average)`.

**The chart is dumb by default, per item, and that is the design.** `PriceBasis.RAW` plots what the
item cost each trip — the number the user recognises, and the right one while the same amount is
bought each time. It is also wrong the moment the amount changes: 0,5 kg one month against 2 kg the
next reads as a 300% rise. `PriceBasis.PER_UNIT` divides by the quantity and is **opt-in per item**,
saved to `trend_setting` the instant the chip is tapped, because it needs a quantity on every
purchase and the receipt does not always carry one.

A purchase the current basis cannot measure — no quantity anywhere, or one in a unit
`UnitConversion` will not convert — is `isUsable = false`. It stays in `purchases` and is left out
of `points`: it is exactly what this page exists to let the user fix, so it must not silently
distort the line, and must not silently vanish from it either. Deltas are drawn against the
previous *usable* purchase, never across a skipped one.

`TrendQtySheet` is the manual half. It writes a `QtyOverride` keyed by `session_item.id` and
**never rewrites the receipt** — the trip keeps exactly what was logged in the shop, and the sheet
says so. `TrendPurchase` carries both `recordedQty`/`recordedUnit` and `effectiveQty`/
`effectiveUnit` so the difference stays visible.

Actions: `load(initialName)`, `openPicker()`, `dismissPicker()`, `onQueryChanged(query)`,
`selectName(name)`, `selectBasis(basis)`, `selectBaseUnit(unit)`, `openQtySheet(itemId)`,
`dismissQtySheet()`, `saveQtyOverride(qtyText, unit)`, `clearQtyOverride()`.
Effects: `ShowMessage(INVALID_QTY | ADJUSTMENT_SAVED | ADJUSTMENT_CLEARED)`. Errors go through
`loadState` / `actionState`; this screen shows the error dialog but **no loading dialog** — every
action on it is a toggle or a two-field sheet over a local database, and a spinner that flashes for
one frame reads as a glitch.

`BuildPriceTrend` is covered by `shared/src/commonTest/.../BuildPriceTrendTest.kt`, which pins the
0,5 kg / 2 kg / 900 g case in both bases, the gram→kg conversion, the skip, the override, and the
promise that the receipt is left alone.

### 11.5 `features/settings`

`SettingsViewModel`: `themeFlavor`, `sessionCount`, `stockCount`, `actionState`,
effects for share / clipboard / import results.
Actions: `changeTheme`, `seedDemo`, `exportShare`, `exportCopy`, `importFromText(raw)`,
`clearAll`.
Screen: theme picker (3 cards), data rows (seed demo / export / import / clear all), a storage
note, and the tips card. Import UX: a sheet offering "Pilih file JSON" (the screen runs
`ActivityResultContracts.OpenDocument`, reads the bytes, passes the text to the ViewModel) or a
paste field. Export UX: a sheet offering "Bagikan file" or "Salin ke clipboard".

### 11.6 `features/list`

**Daftar belanja** — the plan for the next trip, which is what the user would otherwise keep in
WhatsApp or a paper note. A pushed route reached from `StartListCard` on the Belanja tab.

`ShoppingListViewModel` / `ShoppingListState`: `listId`, `hasList`,
`itemViews: List<ShoppingListItemView>`, `totalCount`, `checkedCount`, `remainingCount`,
`progress: Float`, `isComplete`, `query`, `searchChips`, `showNewItemChip`, `quickAddChips`,
`sources: List<ListSource>`, `loadState`, `actionState`.
Actions: `load()`, `startList(source)`, `onQueryChanged`, `addTyped()`, `addName(name)`,
`toggleItem(itemId, isChecked)`, `removeItem(itemId)`, `saveAsTemplate(name)`,
`deleteTemplate(templateId)`, `deleteList()`.
Effects: `ListStarted(itemCount)`, `ListCompleted`, `TemplateSaved`, `TemplateDeleted`,
`ListDeleted`, `ShowMessage(NAME_REQUIRED | ALREADY_ON_LIST)`, `ShowError`.
A successful add emits **nothing**: writing a list is a burst of six or eight items, and a pill
per item would be six pills. The new row and the counter are the feedback; only the no-op
("already on the list") needs explaining.

Templates are the only source the user creates, so they are the only one that can pile up and
the only `ListSourceRow` that draws a ✕. Deleting one confirms first (the source sheet steps
aside and comes back either way) and drops only the saved copy.

Use cases:
- `BuildListItemViews` — adds the category emoji and sorts ticked lines to the bottom. The sort
  is stable, so unticking a line puts it back where it was written.
- `BuildListSources` — the "buat daftar" menu:
  `ListSource(kind, names, label, templateId)` with `Kind` ∈ `BLANK` / `LAST_SESSION` /
  `LOW_STOCK` / `TEMPLATE`. Every source arrives with the names it would add already resolved, so
  the sheet is a menu rather than a wizard and a finished list costs one tap. A source with
  nothing to add is left out rather than shown empty. `label` is user data (a template's name, a
  store name); the fixed part of the title is a string resource, resolved in the composable.

Design rules this screen is built to, in order of priority:
1. **A list is names only** — no price, no quantity, `note` optional and hidden until asked for.
   A planning screen that demands numbers is a planning screen nobody fills in.
2. **The first list is one tap** — `ListSourceSheet` opens by itself on arrival when there is no
   plan (once per screen instance, so deleting a list does not trap the user in a sheet).
3. **The keyboard never has to close** — Enter adds and keeps the caret, silently; a blank list
   focuses the field itself, since typing is the only sensible next action.
4. **Quick-add chips exclude what is already on the list** — a chip that would do nothing is
   dropped rather than shown and then refused.
5. **Ticking is the reward** — `ListCheckCircle` fills and springs, the row strikes through and
   sinks to the bottom via `Modifier.animateItem()`, the count rolls in `AnimatedContent`, and
   the last tick calls `AppUiController.celebrate()`.

### 11.7 App shell

`AppViewModel` (`features/app/presentation/`): `themeFlavor`, `hasActiveSession`, `loadState`.
`themeFlavor` is collected from `SettingsRepository.observeSettings()` in the ViewModel's `init`,
so a colour picked in Settings — or arriving via an import or a wipe — repaints the app at once.
`ShellTabBar` takes a `stockBadgeCount` and draws a coral count pill on the Stok tab, fed from
the hoisted `StockViewModel`'s `lowCount`.
`MainActivity` collects it to pick the `ThemeFlavor` for `AppTheme` and to decide whether to
open straight into the live session on cold start.

---

## 12. Non-negotiables

1. No mutable state outside a ViewModel except Compose-local `remember` for pure UI concerns.
2. No helper functions returning UI inside another composable's file — one top-level
   `@Composable` per file.
3. ViewModels depend only on repositories and use cases.
4. No Koin `get()` / `inject()` outside Koin modules, `MainActivity`, and `koinViewModel()`.
5. Guard clauses over nested `else`; no `else if` ladders; exhaustive `when` on sealed types
   and enums.
6. No computed properties on state classes.
7. No `Snackbar` — use `AppUiController`.
8. No hardcoded user-facing strings — `stringResource(...)`.
9. No hardcoded `Color`, `sp`, `FontWeight` or `TextStyle` in screens or components —
   `AppTheme.colors.*` / `AppTheme.typography.*`.
10. `AppButton(enabled = false)` renders the disabled style; never fake it with an empty lambda.
11. Only `.sq` files contain SQL.
12. `features.x` never imports `features.y`.
