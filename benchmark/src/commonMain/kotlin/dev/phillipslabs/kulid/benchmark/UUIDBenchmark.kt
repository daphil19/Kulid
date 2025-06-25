package dev.phillipslabs.kulid.benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@State(Scope.Benchmark)
class UUIDBenchmark {
    @OptIn(ExperimentalUuidApi::class)
    @Benchmark
    fun generateUUID() {
        // right now,  ULID encodes to string on creation
        Uuid.random().toString()
    }
}
