package dev.phillipslabs.kulid

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestULID {
    @Test
    fun correctCrockfordEncoding() {
        // equivalent to max-time a ULID supports
        val bytes = ByteArray(6) { -1 }

        val encoded = ULID.encodeCrockfordBase32(bytes)
        assertEquals("7ZZZZZZZZZ", encoded)
    }

    @Test
    fun correctTimestampBitsForMaxULID() {
        val maxTimeULID = ULID.generate(MAX_TIME)
        assertEquals(ULID.MAX.value.take(10), maxTimeULID.value.take(10))
        assertTrue { maxTimeULID.value <= ULID.MAX.value }
    }

    @Test
    fun correctTimestampBitsForMinULID() {
        val minTimeULID = ULID.generate(0L)
        assertEquals(ULID.MIN.value.take(10), minTimeULID.value.take(10))
        assertTrue { minTimeULID.value >= ULID.MIN.value }
    }

    @Test
    fun ulidForCurrentTimeValid() {
        val now = Clock.System.now().toEpochMilliseconds()
        val ulid = ULID.generate(now)
        assertTrue("ULID ${ulid.value} for timestamp $now outside of expected bounds!") {
            ulid.value <= ULID.MAX.value &&
                ulid.value >= ULID.MIN.value
        }
    }

    @Test
    fun testInvalidTimestamps() {
        // Test with negative timestamp
        assertFailsWith<IllegalStateException>("Time cannot be negative") {
            ULID.generate(-1L)
        }

        // Test with timestamp exceeding maximum
        val tooLargeTimestamp = (1L shl 48)
        assertFailsWith<IllegalStateException>("Time $tooLargeTimestamp is outside of expected bounds") {
            ULID.generate(tooLargeTimestamp)
        }
    }
}
