package dev.phillipslabs.kulid

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestULIDMonotonic {
    @Test
    fun generatorProducesNonDecreasingSequence() {
        val gen = ULID.MonotonicGenerator()
        var prev = gen.next()
        // Generate a reasonably large sequence to likely span multiple milliseconds
        repeat(2_000) {
            val next = gen.next()
            assertTrue(
                prev <= next,
                "MonotonicGenerator produced a decreasing ULID: prev=$prev next=$next",
            )
            prev = next
        }
    }

    @Test
    fun ulidsWithinSameMillisecondAreStrictlyIncreasing() {
        val staticTime = Clock.System.now().toEpochMilliseconds()
        // insecure random is faster for tests
        val gen = ULID.MonotonicGenerator(randomProvider = { randomBytes(false, it) }, clock = { staticTime })

        val first = gen.next()
        val second = gen.next()

        assertTrue(
            second > first,
            "ULIDs within same millisecond must be strictly increasing: first=$first second=$second",
        )

        assertTrue(
            second.value
                .toByteArray()
                .drop(TIMESTAMP_BYTE_SIZE)
                .reversed()
                .zip(
                    first.value
                        .toByteArray()
                        .drop(TIMESTAMP_BYTE_SIZE)
                        .reversed(),
                )
                // any short circuits once we've found a true value to the predicate
                // since we only have the random bytes and they're in reverse order,
                // we should find the byte that incremented
                .any { (next, prev) ->
                    // max value check to help avoid an overflow error
                    prev != 0xff.toByte() && (prev + 1).toByte() == next
                },
        )
    }

    @Test
    fun ulidThrowsErrorWhenRandomIsAtMax() {
        val staticTime = Clock.System.now().toEpochMilliseconds()
        val gen =
            ULID.MonotonicGenerator(
                randomProvider = { it.fill(0xff.toByte()) },
                clock = { staticTime },
            )
        // first ULID should be ok, as it uses timestamp and random to make a max ulid
        gen.next()
        assertFailsWith<IllegalStateException>("Failed to throw at a max-random ULID increment attempt") {
            gen.next()
        }
    }
}
