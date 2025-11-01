@file:Suppress("ktlint:standard:function-expression-body")

package dev.phillipslabs.kulid.benchmark

import dev.phillipslabs.kulid.ULID
import kotlinx.benchmark.*

@State(Scope.Benchmark)
class ULIDMonotonicBenchmark {
    @Param("true", "false")
    var secureRandom: Boolean = false

    private lateinit var generator: ULID.MonotonicGenerator

    @Setup
    fun setup() {
        generator = ULID.MonotonicGenerator(secureRandom)
    }

    @Benchmark
    fun generateMonotonicULID(bh: Blackhole) {
        // returning a inline class causes issues with JMH, so BH consume it instead
        bh.consume(generator.next())
    }

    @Benchmark
    fun generateMonotonicULIDString(): String {
        return generator.next().toString()
    }
}
