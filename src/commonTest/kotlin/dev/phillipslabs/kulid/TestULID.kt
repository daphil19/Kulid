package dev.phillipslabs.kulid

import kotlin.test.Test
import kotlin.test.assertEquals

class TestULID {
    @Test
    fun correctCrockfordEncoding() {
        val text = "encode me pls!!!".encodeToByteArray()

        val encoded = ULID.encodeCrockfordBase32(text)
        assertEquals("CNQ66VV4CMG6TS90E1P7689144", encoded)
    }
}
