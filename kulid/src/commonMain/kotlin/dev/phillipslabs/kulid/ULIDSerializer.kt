package dev.phillipslabs.kulid

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
public object ULIDSerializer : KSerializer<ULID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ULID", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: ULID,
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): ULID = ULID.fromString(decoder.decodeString())
}
