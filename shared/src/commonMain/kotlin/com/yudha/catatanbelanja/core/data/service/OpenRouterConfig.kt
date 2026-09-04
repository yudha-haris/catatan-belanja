package com.yudha.catatanbelanja.core.data.service

/**
 * Everything the receipt scanner needs to reach OpenRouter. Built by the platform entry point —
 * on Android from `BuildConfig`, which reads `local.properties` — and handed to
 * [initKoin][com.yudha.catatanbelanja.core.di.initKoin], so the key is never a constant in
 * source and never in the database.
 *
 * [model] is a plain slug from openrouter.ai/models and is deliberately data rather than code:
 * the catalogue moves faster than releases do, so swapping the model is a one-line edit in
 * `local.properties` and a rebuild, not a change here.
 */
data class OpenRouterConfig(
    val apiKey: String = "",
    val model: String = DEFAULT_MODEL,
) {
    /**
     * False while `local.properties` still holds the placeholder, which is how a fresh clone
     * arrives. The scanner refuses early rather than spending a round trip on a 401.
     */
    val isConfigured: Boolean
        get() = apiKey.isNotBlank() && apiKey != PLACEHOLDER_KEY

    companion object {
        /**
         * A Flash-tier vision model: reading a receipt is transcription plus light structure, not
         * reasoning, so the cheap tier does it as well as the expensive one. At roughly 1–2k image
         * tokens per scan this lands near Rp 50 a receipt.
         */
        const val DEFAULT_MODEL = "google/gemini-3.8-flash"

        /** What `androidApp/build.gradle.kts` compiles in when no key has been pasted yet. */
        const val PLACEHOLDER_KEY = "<API_KEY>"
    }
}
