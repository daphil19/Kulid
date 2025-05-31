package dev.phillipslabs.kulid

import kotlinx.datetime.Clock
import org.kotlincrypto.random.CryptoRand
import kotlin.jvm.JvmInline

internal const val MAX_TIME = (1 shl 48) - 1 // 2^48 - 1
internal const val RANDOM_BYTE_SIZE = 10 // 80 bits -> 10 bytes

// TODO does it make sense to have a type?
@JvmInline
value class ULID private constructor(
    val value: String,
) {
    companion object {
        // TODO factory methods?
        // TODO is it safe to put other factory state in here?

        fun generate(): ULID {
            val time = Clock.System.now().toEpochMilliseconds()
            check(time >= 0L) { "Time must be non-negative" }
            check(time <= MAX_TIME) { "Time must be less than $MAX_TIME" }

            val randomness = CryptoRand.nextBytes(ByteArray(RANDOM_BYTE_SIZE))

            return ULID(TODO())
        }
    }
}
