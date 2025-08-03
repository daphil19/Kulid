package dev.phillipslabs.kulid

import kotlinx.datetime.Clock
import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import kotlinx.serialization.Serializable
import org.kotlincrypto.random.CryptoRand
import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue
import kotlin.random.Random

// some of these are marked as internal so that tests can be conducted easier
// ideally, they would all be private unless there's a compelling reason to expose these

private const val TIMESTAMP_BIT_SIZE = 48
internal const val MAX_TIME = (1L shl TIMESTAMP_BIT_SIZE) - 1 // 2^48 - 1
private const val RANDOM_BIT_SIZE = 80
private const val ULID_BIT_SIZE = TIMESTAMP_BIT_SIZE + RANDOM_BIT_SIZE

internal const val ULID_BYTE_SIZE = ULID_BIT_SIZE / 8
private const val RANDOM_BYTE_SIZE = RANDOM_BIT_SIZE / 8

private const val ENCODED_ULID_BYTE_SIZE = 26

// ULIDs don't saturate the entirety of their encoding space, and zero-pad the bits they don't use at the front
// as a result, we need to shift around the bit we're looking at a little bit more when placing it
private const val ULID_ENCODING_FRONT_PADDING = ENCODED_ULID_BYTE_SIZE * 5 - ULID_BIT_SIZE

// Crockford's base32 alphabet
private val ENCODING_CHARS = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray()

/**
 * A Universally Unique Lexicographically Sortable Identifier (ULID).
 *
 * ULIDs are 128-bit identifiers that combine a timestamp with random data to create
 * unique, lexicographically sortable identifiers. They are encoded as 26-character
 * Crockford Base32 strings.
 *
 * Features:
 * - 128-bit compatibility with UUID
 * - Lexicographically sortable (by time, then random component)
 * - Canonically encoded as a 26 character string
 * - Uses Crockford's base32 for better human readability
 * - Case insensitive
 * - No special characters (URL safe)
 *
 * @property value The raw binary representation of the ULID as a ByteString.
 */
@Serializable(with = ULIDSerializer::class)
@JvmInline
public value class ULID private constructor(
    public val value: ByteString,
) : Comparable<ULID> {
    init {
        require(value.size == ULID_BYTE_SIZE) { "ULID should have $ULID_BYTE_SIZE bytes, but had ${value.size}" }
    }

    /**
     * Compares this ULID with another ULID lexicographically.
     *
     * ULIDs are designed to be lexicographically sortable, with earlier timestamps
     * sorting before later ones. ULIDs created within the same millisecond are
     * sorted by their random component.
     *
     * @param other The ULID to compare with.
     * @return A negative value if this ULID is less than [other], zero if they're equal,
     *         or a positive value if this ULID is greater than [other].
     */
    override fun compareTo(other: ULID): Int = value.compareTo(other.value)

    /**
     * Returns the string representation of this ULID in Crockford Base32 encoding.
     *
     * @return A 26-character Crockford Base32 encoded string.
     */
    override fun toString(): String = encodeCrockfordBase32(value)

    /**
     * Companion object providing factory methods and constants for ULID creation and manipulation.
     */
    public companion object {
        /**
         * The maximum possible ULID value.
         *
         * This constant represents the largest possible ULID value, with all bits set to 1,
         * encoded as "7ZZZZZZZZZZZZZZZZZZZZZZZZZ" in Crockford Base32.
         */
        public val MAX: ULID = fromString("7ZZZZZZZZZZZZZZZZZZZZZZZZZ")

        /**
         * The minimum possible ULID value.
         *
         * This constant represents the smallest possible ULID value, with all bits set to 0,
         * encoded as "00000000000000000000000000" in Crockford Base32.
         */
        public val MIN: ULID = fromString("00000000000000000000000000")

        /**
         * Generates a new ULID.
         *
         * By default, this method uses the current system time as the timestamp component
         * and cryptographically secure random data for the random component.
         *
         * @param timestamp The timestamp to use in milliseconds since the Unix epoch.
         *                  Defaults to the current system time.
         * @param secureRandom Whether to use cryptographically secure random data.
         *                     Defaults to true. Set to false only if you don't need
         *                     cryptographic security and want better performance.
         * @return A new ULID instance.
         * @throws IllegalStateException If the timestamp is negative or exceeds the maximum
         *                               supported timestamp value (2^48 - 1).
         */
        public fun generate(
            timestamp: Long = Clock.System.now().toEpochMilliseconds(),
            secureRandom: Boolean = true,
        ): ULID {
            check(timestamp >= 0L) { "Time must be non-negative" }
            check(timestamp <= MAX_TIME) { "Time must be less than $MAX_TIME" }

            val buf = Buffer()
            // the timestamp we use is 48 bits, so we need to drop the uppermost 16 bits of a Long
            // toShort() uses the least significant 16 bits of a number
            buf.writeShort((timestamp shr 32).toShort())
            buf.writeInt(timestamp.toInt())

            val randomness =
                if (secureRandom) {
                    CryptoRand.Default.nextBytes(
                        ByteArray(RANDOM_BYTE_SIZE),
                    )
                } else {
                    Random.nextBytes(ByteArray(RANDOM_BYTE_SIZE))
                }
            buf.write(randomness)

            return ULID(buf.readByteString())
        }

        /**
         * Creates a ULID from its string representation.
         *
         * This method parses a Crockford Base32 encoded ULID string and creates a new ULID instance.
         *
         * @param encoded The Crockford Base32 encoded ULID string (26 characters).
         * @return A new ULID instance.
         * @throws IllegalArgumentException If the encoded string is not exactly 26 characters long
         *                                  or contains invalid characters.
         */
        public fun fromString(encoded: String): ULID {
            require(encoded.length == ENCODED_ULID_BYTE_SIZE) { "Encoded ULID must be $ENCODED_ULID_BYTE_SIZE characters long" }
            val buf = Buffer()
            var decodedByte = 0

            for (i in 0 until (encoded.length * 5) - ULID_ENCODING_FRONT_PADDING) {
                val charIdx = (i + ULID_ENCODING_FRONT_PADDING) / 5
                val char = encoded[charIdx]
                val decodedChar = ENCODING_CHARS.indexOf(char.uppercaseChar())
                require(decodedChar != -1) { "Invalid character in ULID: $char" }

                val bitIdxOfChar = 4 - ((i + ULID_ENCODING_FRONT_PADDING) % 5)
                val bitMask = 0b1 shl bitIdxOfChar
                val bit = decodedChar and bitMask

                val bitIdxInDecodedByte = 7 - (i % 8)

                // this is where we have to move the masked bit to in order to get it in the right spot for the lookup
                // positive is to the right, negative is to the left
                val encodingShiftAmount = bitIdxOfChar - bitIdxInDecodedByte

                val bitForDecodedByte =
                    if (encodingShiftAmount >= 0) {
                        (bit ushr encodingShiftAmount)
                    } else {
                        (bit shl encodingShiftAmount.absoluteValue)
                    }

                decodedByte = decodedByte or bitForDecodedByte

                if (i % 8 == 7) {
                    buf.writeByte(decodedByte.toByte())
                    decodedByte = 0
                }
            }
            return ULID(buf.readByteString())
        }

        /**
         * Encodes a ByteString as a Crockford Base32 string.
         *
         * This internal function converts the raw binary representation of a ULID
         * to its canonical 26-character Crockford Base32 string representation.
         *
         * @param bytes The ByteString to encode (must be exactly 16 bytes).
         * @return A 26-character Crockford Base32 encoded string.
         * @throws IllegalArgumentException If the ByteString is not exactly 16 bytes.
         */
        internal fun encodeCrockfordBase32(bytes: ByteString) =
            buildString(ENCODED_ULID_BYTE_SIZE) {
                require(bytes.size == ULID_BYTE_SIZE) { "ULID should have $ULID_BYTE_SIZE bytes, but had ${bytes.size}" }
                var prevByteIdx = -1
                var encodingIdx = 0
                var currentByte = 0

                // iterate over every bit
                for (i in 0 until bytes.size * 8) {
                    val byteIdx = i / 8
                    // determine the current byte we are working in
                    if (prevByteIdx != byteIdx) {
                        prevByteIdx = byteIdx
                        currentByte = bytes[byteIdx].toInt()
                    }

                    // this is what bit we are looking at in the byte we are encoding
                    val bitIdxInByte = 7 - (i % 8)

                    // first, get the bit we want by shifting a mask to the right position
                    val bitMask = 0b1 shl bitIdxInByte
                    val bit = currentByte and bitMask

                    // then, shift that bit to the correct position for the encoding lookup
                    val bitIndexInEncodingIdx = 4 - ((i + ULID_ENCODING_FRONT_PADDING) % 5)

                    // this is were we have to move the masked bit to in order to get it in the right spot for the lookup
                    // positive is to the right, negative is to the left
                    val encodingShiftAmount = bitIdxInByte - bitIndexInEncodingIdx

                    // normally shift to the right, but if we get a negative right shift,
                    // shift to the left by the absolute value instead
                    val bitForEncodingIdx =
                        if (encodingShiftAmount >= 0) {
                            (bit ushr encodingShiftAmount)
                        } else {
                            (bit shl encodingShiftAmount.absoluteValue)
                        }

                    // mask-in the shifted bit
                    encodingIdx = encodingIdx or bitForEncodingIdx

                    // if we reach the 5 bit mark (accounting for any front-padding), or are at the end, do the lookup and add the char to the string
                    if ((i + ULID_ENCODING_FRONT_PADDING) % 5 == 4 || i == bytes.size * 8 - 1) {
                        append(ENCODING_CHARS[encodingIdx])
                        encodingIdx = 0
                    }
                }
            }
    }
}
