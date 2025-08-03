package dev.phillipslabs.kulid.benchmark

import dev.phillipslabs.kulid.ULID
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
class ULIDBenchmark {
    @Benchmark
    fun generateULID(bh: Blackhole) {
        bh.consume(ULID.generate())
    }

    @Benchmark
    fun generateULIDString(bh: Blackhole) {
        bh.consume(ULID.generate().toString())
    }

    @Benchmark
    fun generateInsecureRandomULID(bh: Blackhole) {
        bh.consume(ULID.generate(secureRandom = false))
    }

    @Benchmark
    fun generateInsecureRandomULIDString(bh: Blackhole) {
        bh.consume(ULID.generate(secureRandom = false).toString())
    }
}
