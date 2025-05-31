package dev.phillipslabs.kulid

import kotlinx.datetime.Clock
import org.kotlincrypto.random.CryptoRand
import kotlin.jvm.JvmInline

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

// TODO we will eventually move this to a byte array once we want ot support the binary format
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

        // TODO this feels wrong and brittle! I feel like I should have some tests to make sure things are working correctly...
        private fun encodeCrockfordBase32(data: ByteArray) =
            buildString(ENCODED_ULID_SIZE) {
                var prevByteIdx = -1
                var bitIdx = 0
                var encodingIdx = 0
                var currentByte = 0
                while (bitIdx < data.size * 8) {
                    if (prevByteIdx != bitIdx % 8) {
                        prevByteIdx = bitIdx % 8
                        currentByte = data[bitIdx / 8].toInt()
                    }

                    encodingIdx = encodingIdx or (currentByte shr (7 - (bitIdx % 8)))

                    bitIdx++
                    if (bitIdx != 0 && bitIdx % 5 == 0) {
                        append(ENCODING_CHARS[encodingIdx])
                        encodingIdx = 0
                    }
                }
            }
    }
}
