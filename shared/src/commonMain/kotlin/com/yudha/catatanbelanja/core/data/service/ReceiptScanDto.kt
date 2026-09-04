package com.yudha.catatanbelanja.core.data.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The OpenRouter request/response family, plus the receipt payload the model is asked to write.
 *
 * These get the loose-typing treatment `docs/architecture.md` §3 reserves for JSON that comes
 * from outside the app — the same exemption the backup DTOs have, and for a stronger reason: this
 * JSON is *generated*, so every field is optional, every field has a default, and nothing here
 * may throw on a reply that is merely disappointing.
 */
@Serializable
internal data class ChatRequestDto(
    val model: String,
    val messages: List<ChatMessageDto>,
    @SerialName("max_tokens") val maxTokens: Int,
)

/**
 * `content` is always the array form, never the bare string. OpenAI-compatible APIs accept a
 * string for text-only messages and an array for mixed ones, and modelling both would mean a
 * polymorphic serializer for no gain — a receipt scan is always text plus one image.
 */
@Serializable
internal data class ChatMessageDto(
    val role: String,
    val content: List<ContentPartDto>,
)

@Serializable
internal data class ContentPartDto(
    val type: String,
    val text: String? = null,
    @SerialName("image_url") val imageUrl: ImageUrlDto? = null,
)

@Serializable
internal data class ImageUrlDto(val url: String)

@Serializable
internal data class ChatResponseDto(
    val choices: List<ChatChoiceDto> = emptyList(),
    val error: ChatErrorDto? = null,
)

@Serializable
internal data class ChatChoiceDto(val message: ChatReplyDto? = null)

@Serializable
internal data class ChatReplyDto(val content: String = "")

@Serializable
internal data class ChatErrorDto(val message: String = "")

/** The receipt itself, as the model was told to write it. */
@Serializable
internal data class ScannedReceiptDto(
    val store: String = "",
    val date: String = "",
    val items: List<ScannedItemDto> = emptyList(),
)

/**
 * [price] is what the whole line cost, in rupiah. A `Double` rather than an `Int` because the
 * model is asked for an integer and does not always oblige — "7000.0" and a quoted "7000" both
 * have to survive the parse, and rounding once here is cheaper than a failed scan.
 */
@Serializable
internal data class ScannedItemDto(
    val name: String = "",
    val qty: Double? = null,
    val unit: String? = null,
    val price: Double? = null,
)
