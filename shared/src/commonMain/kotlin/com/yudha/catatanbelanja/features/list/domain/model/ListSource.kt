package com.yudha.catatanbelanja.features.list.domain.model

/**
 * One way to start a plan without typing it out. The whole point of the feature is that the
 * first list costs a single tap, so every source arrives with the [names] it would add already
 * resolved — the sheet is a menu, not a wizard.
 *
 * [label] is user data (a template's own name, a store name); the fixed part of the title comes
 * from [kind] and is a string resource on the Android side.
 */
data class ListSource(
    val kind: Kind,
    val names: List<String>,
    val label: String = "",
    val templateId: String? = null,
) {
    enum class Kind { BLANK, LAST_SESSION, LOW_STOCK, TEMPLATE }
}
