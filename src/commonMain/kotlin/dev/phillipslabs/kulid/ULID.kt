package dev.phillipslabs.kulid

const val MAX_TIME = (1 shl 48) - 1 // 2^48 - 1

// TODO does it make sense to have a type?
data class ULID(val time: Long, val random: String) {
    override fun toString(): String {
        TODO()
    }

    companion object {
        // TODO factory methods?
    }
}
