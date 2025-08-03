package dev.phillipslabs.kulid

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for the ULID class using kotlinx.serialization.
 *
 * This serializer converts ULIDs to and from their string representation for serialization purposes.
 * ULIDs are serialized as 26-character Crockford Base32 encoded strings.
 */
public object ULIDSerializer : KSerializer<ULID> {
    /**
     * The serial descriptor for ULID, describing it as a primitive string.
     */
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ULID", PrimitiveKind.STRING)

    /**
     * Serializes a ULID to its string representation.
     *
     * @param encoder The encoder to write the serialized form to.
     * @param value The ULID to serialize.
     */
    override fun serialize(
        encoder: Encoder,
        value: ULID,
    ) {
        encoder.encodeString(value.toString())
    }

    /**
     * Deserializes a ULID from its string representation.
     *
     * @param decoder The decoder to read the serialized form from.
     * @return The deserialized ULID.
     * @throws IllegalArgumentException If the string is not a valid ULID representation.
     */
    override fun deserialize(decoder: Decoder): ULID = ULID.fromString(decoder.decodeString())
}
