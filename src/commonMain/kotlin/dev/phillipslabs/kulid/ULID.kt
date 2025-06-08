package dev.phillipslabs.kulid

import kotlinx.datetime.Clock
import org.kotlincrypto.random.CryptoRand
import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue

private const val TIMESTAMP_BIT_SIZE = 48
private const val MAX_TIME = (1 shl TIMESTAMP_BIT_SIZE) - 1 // 2^48 - 1
private const val RANDOM_BIT_SIZE = 80
private const val ULID_BIT_SIZE = TIMESTAMP_BIT_SIZE + RANDOM_BIT_SIZE

private const val ULID_BYTE_SIZE = ULID_BIT_SIZE / 8
private const val RANDOM_BYTE_SIZE = RANDOM_BIT_SIZE / 8
private const val TIMESTAMP_BYTE_SIZE = TIMESTAMP_BIT_SIZE / 8

private const val ENCODED_ULID_SIZE = 26

// Crockford's base32 alphabet
private val ENCODING_CHARS = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray()

// TODO we will eventually move this to a byte array once we want to support the binary format
@JvmInline
value class ULID private constructor(
    val value: String,
) {
    companion object {
        fun generate(timestamp: Long = Clock.System.now().toEpochMilliseconds()): ULID {
            check(timestamp >= 0L) { "Time must be non-negative" }
            check(timestamp <= MAX_TIME) { "Time must be less than $MAX_TIME" }

            val randomness = CryptoRand.Default.nextBytes(ByteArray(RANDOM_BYTE_SIZE))

            val combined = ByteArray(ULID_BYTE_SIZE)

            for (i in 0 until TIMESTAMP_BYTE_SIZE) {
                // toByte() uses the LSBs, so we need to shift the byte we're storing to the end to get it in correctly
                combined[i] = (timestamp shr (8 * (TIMESTAMP_BYTE_SIZE - i - 1))).toByte()
            }

            randomness.copyInto(combined, destinationOffset = TIMESTAMP_BYTE_SIZE)

            return ULID(encodeCrockfordBase32(combined))
        }

        // ULIDS are nice in that we are byte-aligned but also don't have to worry about padding (I think?)
        internal fun encodeCrockfordBase32(data: ByteArray) =
            buildString(ENCODED_ULID_SIZE) {
                var prevByteIdx = -1
                var encodingIdx = 0
                var currentByte = 0

                // iterate over every bit
                for (i in 0 until data.size * 8) {
                    val byteIdx = i / 8
                    // determine the current byte we are working in
                    if (prevByteIdx != byteIdx) {
                        prevByteIdx = byteIdx
                        currentByte = data[byteIdx].toInt()
                    }

                    val bitIdxInByte = 7 - (i % 8)

                    // first, get the bit we want by shifting a mask to the right position
                    val bitMask = 0b1 shl bitIdxInByte
                    val bit = currentByte and bitMask

                    // then, shift that bit to the right position for the encoding
                    val bitIndexInEncodingIdx = 4 - (i % 5)
                    val encodingShiftAmount = bitIdxInByte - bitIndexInEncodingIdx

                    // normally shift to the right, but if we get a negative right shift,
                    // shift to the left by the absolute value instead
                    val bitForEncodingIdx =
                        if (encodingShiftAmount >= 0) {
                            encodingIdx or (bit shr encodingShiftAmount)
                        } else {
                            encodingIdx or (bit shl encodingShiftAmount.absoluteValue)
                        }

                    encodingIdx = encodingIdx or bitForEncodingIdx

                    // if we reach the 5 bit mark (or are at the end), do the lookup and add the char to the string
                    if (i % 5 == 4 || i == data.size * 8 - 1) {
                        append(ENCODING_CHARS[encodingIdx % ENCODING_CHARS.size])
                        encodingIdx = 0
                    }
                }
            }
    }
}
