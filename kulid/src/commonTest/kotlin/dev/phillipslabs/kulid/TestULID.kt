package dev.phillipslabs.kulid

import kotlinx.datetime.Clock
import kotlinx.io.bytestring.ByteString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestULID {
    @Test
    fun correctCrockfordEncoding() {
        // equivalent to max-time a ULID supports
        val bytes = ByteString(ByteArray(ULID_BYTE_SIZE) { -1 })

        val encoded = ULID.encodeCrockfordBase32(bytes)
        assertEquals("7ZZZZZZZZZZZZZZZZZZZZZZZZZ", encoded)
    }

    @Test
    fun correctTimestampBitsForMaxULID() {
        val maxTimeULID = ULID.generate(MAX_TIME)
        assertEquals(ULID.MAX.toString().take(10), maxTimeULID.toString().take(10))
        assertTrue { maxTimeULID < ULID.MAX }
    }

    @Test
    fun correctTimestampBitsForMinULID() {
        val minTimeULID = ULID.generate(0L)
        assertEquals(ULID.MIN.toString().take(10), minTimeULID.toString().take(10))
        assertTrue { minTimeULID >= ULID.MIN }
    }

    @Test
    fun ulidForCurrentTimeValid() {
        val now = Clock.System.now().toEpochMilliseconds()
        val ulid = ULID.generate(now)
        assertTrue("ULID $ulid for timestamp $now outside of expected bounds!") {
            ulid <= ULID.MAX &&
                ulid >= ULID.MIN
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

    @Test
    fun testCorrectParsingOfULIDString() {
        val ulid = ULID.fromString("01EAWYQD59KTN275S079C9ESX7")
        assertEquals("01EAWYQD59KTN275S079C9ESX7", ulid.toString())
    }
}
