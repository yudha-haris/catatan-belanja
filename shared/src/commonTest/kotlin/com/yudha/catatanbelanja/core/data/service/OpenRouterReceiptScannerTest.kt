package com.yudha.catatanbelanja.core.data.service

import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.domain.service.NetworkMonitor
import com.yudha.catatanbelanja.core.domain.service.ReceiptScanException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

/**
 * The scanner's contract is "survive whatever a language model sends back". These cover the
 * shapes that actually turn up: a clean object, one wrapped in prose and a code fence, one with
 * no items in it, and an error body instead of a reply.
 */
class OpenRouterReceiptScannerTest {

    private val image = ByteArray(8) { it.toByte() }

    @Test
    fun `reads items and quantities and units and the date off a clean reply`() = runTest {
        val scanner = scannerReplying(
            """
            {"store":"alfamart pondok jaya","date":"2026-08-14","items":[
              {"name":"indomie goreng","qty":2,"unit":"pcs","price":7000},
              {"name":"beras pandan wangi","qty":5,"unit":"KG","price":68500}
            ]}
            """.trimIndent(),
        )

        val scan = scanner.scan(image)

        assertEquals("Alfamart Pondok Jaya", scan.store)
        assertEquals(2, scan.items.size)
        assertEquals("Indomie Goreng", scan.items[0].name)
        assertEquals(7000, scan.items[0].price)
        assertEquals(2.0, scan.items[0].qty)
        assertEquals("pcs", scan.items[0].unit)
        // "KG" is not "kg" to the price trend, which matches units by name.
        assertEquals("kg", scan.items[1].unit)
        assertTrue(scan.purchasedAt != null && scan.purchasedAt!! > 0)
    }

    @Test
    fun `digs the object out of a fenced reply with prose around it`() = runTest {
        val scanner = scannerReplying(
            """
            Here is the receipt you asked for:

            ```json
            {"store":"Indomaret","date":"","items":[{"name":"telur ayam","qty":1,"unit":"kg","price":28900}]}
            ```

            Let me know if you need anything else.
            """.trimIndent(),
        )

        val scan = scanner.scan(image)

        assertEquals("Indomaret", scan.store)
        assertEquals(1, scan.items.size)
        assertEquals(28900, scan.items[0].price)
        // An unreadable date is null rather than a guess; the review screen offers today instead.
        assertNull(scan.purchasedAt)
    }

    @Test
    fun `rounds a price the model wrote as a decimal and drops an unknown unit`() = runTest {
        val scanner = scannerReplying(
            """{"store":"","date":"","items":[{"name":"kopi","qty":1,"unit":"renteng","price":12500.0}]}""",
        )

        val scan = scanner.scan(image)

        assertEquals(12500, scan.items[0].price)
        assertNull(scan.items[0].unit)
    }

    @Test
    fun `a photo with no line items on it is its own failure`() = runTest {
        val scanner = scannerReplying("""{"store":"","date":"","items":[]}""")

        val failure = assertFailsWith<ReceiptScanException> { scanner.scan(image) }

        assertEquals(ReceiptScanException.NO_ITEMS, failure.code)
    }

    @Test
    fun `a reply that is not JSON at all is reported as unreadable`() = runTest {
        val scanner = scannerReplying("I cannot read this picture, sorry.")

        val failure = assertFailsWith<ReceiptScanException> { scanner.scan(image) }

        assertEquals(ReceiptScanException.UNREADABLE_REPLY, failure.code)
    }

    @Test
    fun `an error body is reported as a failed request rather than parsed`() = runTest {
        val scanner = scannerOn(
            MockEngine {
                respond(
                    content = ByteReadChannel("""{"error":{"message":"No auth credentials found"}}"""),
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
            },
        )

        val failure = assertFailsWith<ReceiptScanException> { scanner.scan(image) }

        assertEquals(ReceiptScanException.REQUEST_FAILED, failure.code)
    }

    @Test
    fun `refuses before the request when the device is offline`() = runTest {
        var calls = 0
        val scanner = scannerOn(
            engine = MockEngine {
                calls += 1
                respond(ByteReadChannel(""))
            },
            online = false,
        )

        val failure = assertFailsWith<ReceiptScanException> { scanner.scan(image) }

        assertEquals(ReceiptScanException.OFFLINE, failure.code)
        // The point of the check: no photo is uploaded and no timeout is waited out.
        assertEquals(0, calls)
    }

    @Test
    fun `refuses before the request when no key has been pasted in`() = runTest {
        var calls = 0
        val scanner = scannerOn(
            engine = MockEngine {
                calls += 1
                respond(ByteReadChannel(""))
            },
            config = OpenRouterConfig(apiKey = OpenRouterConfig.PLACEHOLDER_KEY),
        )

        val failure = assertFailsWith<ReceiptScanException> { scanner.scan(image) }

        assertEquals(ReceiptScanException.MISSING_KEY, failure.code)
        assertEquals(0, calls)
    }

    private fun TestScope.scannerReplying(content: String): OpenRouterReceiptScanner {
        val body = Json.encodeToString(
            ChatResponseDto.serializer(),
            ChatResponseDto(choices = listOf(ChatChoiceDto(ChatReplyDto(content)))),
        )
        return scannerOn(
            MockEngine {
                respond(
                    content = ByteReadChannel(body),
                    headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
            },
        )
    }

    private fun TestScope.scannerOn(
        engine: MockEngine,
        config: OpenRouterConfig = OpenRouterConfig(apiKey = "sk-or-test"),
        online: Boolean = true,
    ): OpenRouterReceiptScanner = OpenRouterReceiptScanner(
        client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; isLenient = true }) }
        },
        config = config,
        networkMonitor = FixedNetworkMonitor(online),
        idGenerator = SequentialIdGenerator(),
        dispatcher = UnconfinedTestDispatcher(testScheduler),
    )
}

private class FixedNetworkMonitor(private val online: Boolean) : NetworkMonitor {
    override fun isOnline(): Boolean = online
}

/** Ids that read as themselves in a failing assertion, unlike the random production ones. */
private class SequentialIdGenerator : IdGenerator {
    private var next = 0
    override fun next(): String = "item-${next++}"
}
