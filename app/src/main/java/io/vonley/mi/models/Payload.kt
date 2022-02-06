package io.vonley.mi.models

/**
 * status: -1 failed, 0 nothing, 1 succeeded
 */
data class Payload(val name: String, val data: ByteArray, var status: Int = 0) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Payload) return false

        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}