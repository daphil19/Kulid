package dev.phillipslabs.kulid

import kotlin.test.Test
import kotlin.test.assertTrue

class TestULIDMonotonic {
    @Test
    fun generatorProducesNonDecreasingSequence() {
        val gen = ULID.MonotonicGenerator(secureRandom = true)
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
        val gen = ULID.MonotonicGenerator(secureRandom = true)

        // Keep trying to capture a batch generated within the same millisecond.
        // We will iterate up to some upper bound to avoid an infinite loop on very slow environments.
        var attempts = 0
        var foundBatch = false
        while (attempts++ < 50 && !foundBatch) {
            // Start a new batch
            val first = gen.next()
            val tsPrefix = first.toString().take(10) // timestamp portion in Crockford Base32

            val prev = first

            // Collect additional ULIDs while still in the same millisecond (same timestamp prefix)
            val candidate = gen.next()
            if (candidate.toString().startsWith(tsPrefix)) {
                // Within same millisecond, should be strictly increasing by randomness
                assertTrue(
                    prev < candidate,
                    "ULIDs within same millisecond must be strictly increasing: prev=$prev next=$candidate",
                )

                assertTrue(
                    candidate.value
                        .toByteArray()
                        .drop(TIMESTAMP_BYTE_SIZE)
                        .reversed()
                        .zip(
                            prev.value
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
                foundBatch = true
                break
            }
        }

        assertTrue(foundBatch, "Failed to observe two or more ULIDs within the same millisecond to validate strict increase")
    }
}
