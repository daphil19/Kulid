package dev.phillipslabs.kulid.benchmark

import dev.phillipslabs.kulid.ULID
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class ULIDBenchmark {
    @Benchmark
    fun generateULID() {
        ULID.generate()
    }
}
