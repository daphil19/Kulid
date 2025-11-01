@file:Suppress("ktlint:standard:function-expression-body")

package dev.phillipslabs.kulid.benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@State(Scope.Benchmark)
class UUIDBenchmark {
    @OptIn(ExperimentalUuidApi::class)
    @Benchmark
    fun generateUUID(bh: Blackhole) {
        bh.consume(Uuid.random())
    }

    @OptIn(ExperimentalUuidApi::class)
    @Benchmark
    fun generateUUIDString(): String {
        return Uuid.random().toString()
    }
}
