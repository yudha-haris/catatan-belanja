package com.yudha.catatanbelanja.core.data.service

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.capitalizeWords
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ReceiptScan
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.service.ReceiptScanException
import com.yudha.catatanbelanja.core.domain.service.ReceiptScanner
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.json.Json

/**
 * Sends the receipt photo to a vision model on OpenRouter and reads the line items back out of the
 * JSON it replies with.
 *
 * The model is asked for JSON in the prompt rather than pinned with `response_format`: the slug is
 * a `local.properties` setting the user is expected to change, and structured-output support is
 * not universal across the catalogue, so a request that only works on today's model would be a
 * trap. [extractJsonObject] does the tidying that costs instead.
 */
class OpenRouterReceiptScanner(
    private val client: HttpClient,
    private val config: OpenRouterConfig,
    private val idGenerator: IdGenerator,
    private val dispatcher: CoroutineDispatcher,
) : ReceiptScanner {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override suspend fun scan(image: ByteArray): ReceiptScan = withContext(dispatcher) {
        if (!config.isConfigured) {
            throw ReceiptScanException(MSG_MISSING_KEY, ReceiptScanException.MISSING_KEY)
        }

        val reply = requestReply(image)
        val payload = parsePayload(reply)
        val items = payload.items.mapNotNull { it.toDomain() }
        if (items.isEmpty()) {
            throw ReceiptScanException(MSG_NO_ITEMS, ReceiptScanException.NO_ITEMS)
        }

        ReceiptScan(
            store = payload.store.trim().capitalizeWords(),
            purchasedAt = payload.date.toEpochMillisOrNull(),
            items = items,
        )
    }

    /**
     * Everything that can go wrong on the wire — no network, a rejected key, an unknown model
     * slug, a timeout, a 500 — collapses into one failure, because none of them is anything the
     * user can act on differently. A key that was never pasted is the exception, and that is
     * caught before the request rather than after it.
     */
    private suspend fun requestReply(image: ByteArray): String {
        val response = try {
            client.post(ENDPOINT) {
                header(HttpHeaders.Authorization, BEARER_PREFIX + config.apiKey)
                // Not required, and not identifying: OpenRouter uses these only to label the
                // request in the account's own activity log.
                header(REFERER_HEADER, APP_URL)
                header(TITLE_HEADER, APP_TITLE)
                contentType(ContentType.Application.Json)
                setBody(buildRequest(image))
            }.body<ChatResponseDto>()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            throw ReceiptScanException(
                MSG_REQUEST_FAILED + " " + (error.message ?: ""),
                ReceiptScanException.REQUEST_FAILED,
            )
        }

        val apiError = response.error?.message
        if (!apiError.isNullOrBlank()) {
            throw ReceiptScanException(apiError, ReceiptScanException.REQUEST_FAILED)
        }
        val content = response.choices.firstOrNull()?.message?.content.orEmpty()
        if (content.isBlank()) {
            throw ReceiptScanException(MSG_EMPTY_REPLY, ReceiptScanException.UNREADABLE_REPLY)
        }
        return content
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun buildRequest(image: ByteArray): ChatRequestDto = ChatRequestDto(
        model = config.model,
        maxTokens = MAX_TOKENS,
        messages = listOf(
            ChatMessageDto(
                role = ROLE_USER,
                content = listOf(
                    ContentPartDto(type = PART_TEXT, text = PROMPT),
                    ContentPartDto(
                        type = PART_IMAGE,
                        imageUrl = ImageUrlDto(DATA_URI_PREFIX + Base64.encode(image)),
                    ),
                ),
            ),
        ),
    )

    private fun parsePayload(reply: String): ScannedReceiptDto {
        val body = reply.extractJsonObject()
            ?: throw ReceiptScanException(MSG_UNREADABLE, ReceiptScanException.UNREADABLE_REPLY)
        return try {
            json.decodeFromString(ScannedReceiptDto.serializer(), body)
        } catch (error: Throwable) {
            throw ReceiptScanException(
                MSG_UNREADABLE + " " + (error.message ?: ""),
                ReceiptScanException.UNREADABLE_REPLY,
            )
        }
    }

    private fun ScannedItemDto.toDomain(): ShoppingItem? {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return null

        return ShoppingItem(
            id = idGenerator.next(),
            name = cleanName.capitalizeWords(),
            price = price?.takeIf { it.isFinite() && it > 0 }?.roundToInt() ?: 0,
            qty = qty?.takeIf { it.isFinite() && it > 0 },
            unit = unit?.toKnownUnit(),
        )
    }

    /**
     * Maps whatever the model printed onto a unit the app's own dropdown offers, so an imported
     * item behaves like a typed one — the price trend converts between units by name, and "Kg"
     * is not "kg" to it. An unrecognised unit is dropped rather than invented.
     */
    private fun String.toKnownUnit(): String? {
        val key = normalized()
        if (key.isEmpty()) return null
        CatalogData.units.firstOrNull { it == key }?.let { return it }
        return UNIT_ALIASES[key]
    }

    /**
     * Epoch millis at the start of the printed day, in the device's zone — the app stores every
     * timestamp that way (`docs/architecture.md` §1, deviation 4). A receipt with no readable
     * date, or one the model wrote in some other shape, comes back null and the review screen
     * offers today instead.
     */
    private fun String.toEpochMillisOrNull(): Long? {
        val text = trim()
        if (text.isEmpty()) return null
        return try {
            LocalDate.parse(text).atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        } catch (error: IllegalArgumentException) {
            null
        }
    }

    private companion object {
        const val ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"
        const val BEARER_PREFIX = "Bearer "
        const val REFERER_HEADER = "HTTP-Referer"
        const val TITLE_HEADER = "X-Title"
        const val APP_URL = "https://github.com/yudha-haris/catatan-belanja"
        const val APP_TITLE = "Catatan Belanja"
        const val DATA_URI_PREFIX = "data:image/jpeg;base64,"
        const val ROLE_USER = "user"
        const val PART_TEXT = "text"
        const val PART_IMAGE = "image_url"

        /** A long supermarket receipt runs to 60-odd lines; this leaves room for all of them. */
        const val MAX_TOKENS = 4000

        // Developer-facing, per Failure's contract — the screen picks its own wording off the
        // Failure code and never shows these.
        const val MSG_MISSING_KEY = "No OpenRouter API key: set openrouter.apiKey in local.properties"
        const val MSG_REQUEST_FAILED = "The OpenRouter request failed."
        const val MSG_EMPTY_REPLY = "OpenRouter returned an empty reply"
        const val MSG_UNREADABLE = "The reply was not the receipt JSON that was asked for."
        const val MSG_NO_ITEMS = "No line items were found on the photo"

        val UNIT_ALIASES: Map<String, String> = mapOf(
            "g" to "gram",
            "gr" to "gram",
            "grm" to "gram",
            "kilo" to "kg",
            "kilogram" to "kg",
            "l" to "liter",
            "lt" to "liter",
            "ltr" to "liter",
            "litre" to "liter",
            "mililiter" to "ml",
            "cc" to "ml",
            "pc" to "pcs",
            "pieces" to "pcs",
            "piece" to "pcs",
            "unit" to "pcs",
            "pack" to "bungkus",
            "pak" to "bungkus",
            "sachet" to "bungkus",
            "saset" to "bungkus",
            "renceng" to "bungkus",
            "btl" to "botol",
            "bh" to "buah",
            "box" to "kotak",
            "dus" to "kotak",
        )

        val PROMPT: String = """
            Read this Indonesian shop receipt (struk belanja) and return ONLY a JSON object.
            No prose, no explanation, no markdown code fences.

            Shape:
            {"store":"","date":"YYYY-MM-DD","items":[{"name":"","qty":null,"unit":null,"price":0}]}

            Rules:
            - store: the shop name printed at the top. Empty string if unreadable.
            - date: the transaction date as YYYY-MM-DD. Empty string if unreadable.
              Indonesian receipts print day first, so 05/03/26 is 2026-03-05.
            - items: one entry per product line, in the order printed.
            - name: the product in normal Indonesian title case. Expand obvious abbreviations
              (INDOMIE GRG -> Indomie Goreng, MNYK GRG -> Minyak Goreng, TLR AYM -> Telur Ayam).
              Drop PLU codes, stock numbers and trailing barcodes.
            - price: what that line actually cost, in whole rupiah, as a plain integer. No
              thousands separators, no decimals, no Rp. "3.500" is 3500. For "2 X 3.500 7.000"
              the price is 7000, not 3500.
            - qty: how many units the line bought, or null when it does not say. "2 X 3.500" is 2.
              "0,5 KG" is 0.5.
            - unit: the printed unit (kg, gram, liter, ml, pcs, botol, bungkus, ...) or null.
            - A discount printed under an item belongs to that item: subtract it from that item's
              price instead of writing a line of its own.
            - Skip every line that is not a product: SUBTOTAL, TOTAL, GRAND TOTAL, PPN, PB1,
              PAJAK, TUNAI, CASH, DEBIT, QRIS, KEMBALI, KEMBALIAN, DISKON, HEMAT, POIN, member
              and loyalty lines, the cashier, the address, the phone number and the footer.
            - If the picture is not a receipt, return {"store":"","date":"","items":[]}.
        """.trimIndent()
    }
}

/**
 * The JSON object inside a reply that may also carry a "Here is the receipt:" preamble or a
 * ```json fence. Returns null when there is no balanced object to be found.
 *
 * Braces are counted rather than the last `}` taken, so a trailing "Let me know if…" after the
 * object does not swallow the parse, and a string containing a brace is skipped over.
 */
private fun String.extractJsonObject(): String? {
    val start = indexOf('{')
    if (start < 0) return null

    var depth = 0
    var inString = false
    var escaped = false
    for (index in start until length) {
        val char = this[index]
        if (escaped) {
            escaped = false
            continue
        }
        if (inString) {
            when (char) {
                '\\' -> escaped = true
                '"' -> inString = false
            }
            continue
        }
        when (char) {
            '"' -> inString = true
            '{' -> depth += 1
            '}' -> {
                depth -= 1
                if (depth == 0) return substring(start, index + 1)
            }
        }
    }
    return null
}
