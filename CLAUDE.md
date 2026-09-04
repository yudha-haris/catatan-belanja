# Catatan Belanja

Offline-first Kotlin Multiplatform app for planning a grocery trip, logging it, comparing
prices between trips, and tracking what's left at home. Indonesian-first. No backend, no
network calls.

Modules: `:shared` (KMP — domain, data, presentation/ViewModels) and `:androidApp`
(Jetpack Compose UI). iOS targets compile; only the Android UI exists today.

## Read first

- `docs/architecture.md` — the binding architecture contract (packages, models, repository
  signatures, design-system component names, theme tokens, string conventions). **Follow it exactly.**
- `../code_rules.md` — the code style rulebook, plus its Flutter→Kotlin mapping in
  `docs/architecture.md` §1.
- `../catatan-belanja.html` — the concept prototype; source of truth for product behaviour,
  copy and visual design.

## Commands

```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:allTests
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew lint
```

## Non-negotiables

No mutable state outside a ViewModel (except Compose-local `remember` for pure UI concerns).
One top-level composable/class per file; no helper functions returning UI. ViewModels depend
only on repositories and use cases. No Koin `get()` outside modules. Guard clauses over
`else if`; exhaustive `when` on sealed types and enums. No computed properties on state
classes. No `Snackbar`. No hardcoded user-facing strings, colors, `sp` sizes or font weights
in UI. Only `.sq` files contain SQL, and every schema change ships a `<n>.sqm` migration beside them.
`features.x` never imports `features.y` — anything two features share lives in `core`.
