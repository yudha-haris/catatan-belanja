package com.yudha.catatanbelanja.core.data.backup

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** Current backup format version. Documents without a `version` key are read as this one. */
const val BACKUP_VERSION: Int = 1

/**
 * The backup document, byte-compatible with the prototype's "Salin data".
 * The Indonesian keys (`stok`, `stokLog`, `min`, `full`, `at`) are part of the format.
 * The prototype's `active` key is deliberately not modelled — an in-progress session is
 * never exported and never imported.
 */
@Serializable
data class BackupDocument(
    @Serializable(with = NullableIntOrStringSerializer::class)
    val version: Int? = null,
    @Serializable(with = NullableLongOrStringSerializer::class)
    val exportedAt: Long? = null,
    val theme: String = "",
    val sessions: List<BackupSessionDto> = emptyList(),
    val stok: List<BackupStockDto> = emptyList(),
    val stokLog: List<BackupStockLogDto> = emptyList(),
    /**
     * Shopping lists — the active plan plus every saved template. Newer than the prototype's
     * format, so an older document simply has none and imports fine.
     */
    val daftar: List<BackupListDto> = emptyList(),
)

@Serializable
data class BackupSessionDto(
    val id: String = "",
    val name: String = "",
    val store: String = "",
    @Serializable(with = LongOrStringSerializer::class)
    val startedAt: Long = 0L,
    @Serializable(with = NullableLongOrStringSerializer::class)
    val endedAt: Long? = null,
    val items: List<BackupItemDto> = emptyList(),
)

@Serializable
data class BackupItemDto(
    val id: String = "",
    val name: String = "",
    @Serializable(with = NullableDoubleOrStringSerializer::class)
    val qty: Double? = null,
    val unit: String? = null,
    @Serializable(with = IntOrStringSerializer::class)
    val price: Int = 0,
    val note: String = "",
)

@Serializable
data class BackupListDto(
    val id: String = "",
    val name: String = "",
    @Serializable(with = NullableLongOrStringSerializer::class)
    val createdAt: Long? = null,
    @Serializable(with = NullableLongOrStringSerializer::class)
    val updatedAt: Long? = null,
    @Serializable(with = LooseBooleanSerializer::class)
    val isTemplate: Boolean = false,
    @Serializable(with = NullableLongOrStringSerializer::class)
    val archivedAt: Long? = null,
    val items: List<BackupListItemDto> = emptyList(),
)

@Serializable
data class BackupListItemDto(
    val name: String = "",
    val note: String = "",
    @Serializable(with = LooseBooleanSerializer::class)
    val checked: Boolean = false,
)

@Serializable
data class BackupStockDto(
    val id: String = "",
    val name: String = "",
    @Serializable(with = NullableDoubleOrStringSerializer::class)
    val qty: Double? = null,
    val unit: String = DEFAULT_UNIT,
    @Serializable(with = NullableDoubleOrStringSerializer::class)
    val min: Double? = null,
    @Serializable(with = NullableDoubleOrStringSerializer::class)
    val full: Double? = null,
    @Serializable(with = NullableLongOrStringSerializer::class)
    val updatedAt: Long? = null,
)

@Serializable
data class BackupStockLogDto(
    val id: String = "",
    val month: String = "",
    @Serializable(with = NullableLongOrStringSerializer::class)
    val at: Long? = null,
    val items: List<BackupStockLogItemDto> = emptyList(),
)

@Serializable
data class BackupStockLogItemDto(
    val name: String = "",
    @Serializable(with = NullableDoubleOrStringSerializer::class)
    val qty: Double? = null,
    val unit: String = DEFAULT_UNIT,
)

/** Reads an `Int` written as a JSON number, a quoted string or a bool. Anything else reads as 0. */
object IntOrStringSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntOrString", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeInt(value)

    override fun deserialize(decoder: Decoder): Int = decoder.looseNumber()?.roundToInt() ?: 0
}

object NullableIntOrStringSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NullableIntOrString", PrimitiveKind.INT).nullable

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) {
            encoder.encodeNull()
            return
        }
        encoder.encodeInt(value)
    }

    override fun deserialize(decoder: Decoder): Int? = decoder.looseNumber()?.roundToInt()
}

/** Reads a flag written as `true`, `"true"`, `1` or `"1"`. Anything else reads as false. */
object LooseBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LooseBoolean", PrimitiveKind.BOOLEAN)

    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeBoolean(value)

    override fun deserialize(decoder: Decoder): Boolean = decoder.looseNumber()?.let { it != 0.0 } ?: false
}

object NullableDoubleOrStringSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NullableDoubleOrString", PrimitiveKind.DOUBLE).nullable

    override fun serialize(encoder: Encoder, value: Double?) {
        if (value == null) {
            encoder.encodeNull()
            return
        }
        encoder.encodeDouble(value)
    }

    override fun deserialize(decoder: Decoder): Double? = decoder.looseNumber()
}

object LongOrStringSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LongOrString", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Long) = encoder.encodeLong(value)

    override fun deserialize(decoder: Decoder): Long = decoder.looseLong() ?: 0L
}

object NullableLongOrStringSerializer : KSerializer<Long?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NullableLongOrString", PrimitiveKind.LONG).nullable

    override fun serialize(encoder: Encoder, value: Long?) {
        if (value == null) {
            encoder.encodeNull()
            return
        }
        encoder.encodeLong(value)
    }

    override fun deserialize(decoder: Decoder): Long? = decoder.looseLong()
}

private const val DEFAULT_UNIT = "pcs"
private const val TRUE_TEXT = "true"
private const val FALSE_TEXT = "false"

/** Timestamps can exceed `Double` precision, so parse them as `Long` first. */
private fun Decoder.looseLong(): Long? {
    val raw = looseText() ?: return null
    return raw.toLongOrNull() ?: raw.toLooseDouble()?.roundToLong()
}

private fun Decoder.looseNumber(): Double? = looseText()?.toLooseDouble()

private fun Decoder.looseText(): String? {
    val jsonDecoder = this as? JsonDecoder ?: return null
    val primitive = jsonDecoder.decodeJsonElement() as? JsonPrimitive ?: return null
    val raw = primitive.contentOrNull?.trim().orEmpty()
    if (raw.isEmpty()) return null
    return raw
}

private fun String.toLooseDouble(): Double? {
    if (this == TRUE_TEXT) return 1.0
    if (this == FALSE_TEXT) return 0.0
    // "NaN" and "Infinity" parse but cannot be rounded or stored; they read as absent instead.
    return toDoubleOrNull()?.takeIf { it.isFinite() }
}
