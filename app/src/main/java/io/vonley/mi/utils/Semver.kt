package io.vonley.mi.utils
//credits: https://github.com/glwithu06/Semver.kt

import java.math.BigDecimal

/**
 * [Semver] represent a semantic version according to [the Semantic Versioning Specification](https://semver.org/spec/v2.0.0.html).
 *
 * @property major major in string.
 * @property minor minor in string.
 * @property patch patch in string.
 * @property prereleaseIdentifiers dot separated list of pre-release identifiers.
 * @property buildMetadataIdentifiers dot separated list of build metadata identifiers.
 */
data class Semver internal constructor (
    val major: String,
    val minor: String = "0",
    val patch: String = "0",
    val prereleaseIdentifiers: List<String> = emptyList(),
    val buildMetadataIdentifiers: List<String> = emptyList()
) : Comparable<Semver> {

    /**
     * Primary constructor to create [Semver].
     *
     * @param major major in number.
     * @param minor minor in number.
     * @param patch patch in number.
     * @param prereleaseIdentifiers dot separated list of pre-release identifiers.
     * @param buildMetadataIdentifiers dot separated list of build metadata identifiers.
     */
    constructor(major: Number,
                minor: Number = 0,
                patch: Number = 0,
                prereleaseIdentifiers: List<String> = emptyList(),
                buildMetadataIdentifiers: List<String> = emptyList())
            : this("$major", "$minor", "$patch", prereleaseIdentifiers, buildMetadataIdentifiers)

    /**
     * Specifies string representation style.
     */
    enum class Style {
        /**
         * Specifies a compact style, *Major.Minor.Patch* only, such as “1.2.3”.
         */
        COMPACT,
        /**
         * Specifies a COMPARABLE style, *Major.Minor.Patch-PreReleaseIdentifiers*, such as “1.2.3-rc.1”.
         */
        COMPARABLE,
        /**
         * Specifies a FULL style, *Major.Minor.Patch-PreReleaseIdentifiers+BuildMetadataIdentifiers*, such as “1.2.3-rc.1+SHA.a0f21”.
         */
        FULL
    }

    /**
     * Returns a string representation of the [Semver].
     *
     * @param style Specifies string representation [Style].
     *
     * @return a string representation of the [Semver].
     */
    fun toString(style: Style): String {
        val version = arrayOf(major, minor, patch).joinToString(DOT_DELIMITER)
        val prerelease = prereleaseIdentifiers.let {
            if (it.isNotEmpty()) PRERELEASE_DELIMITER + it.joinToString(DOT_DELIMITER) else ""
        }
        val buildMetadata = buildMetadataIdentifiers.let {
            if (it.isNotEmpty()) BUILD_METADATA_DELIMITER + it.joinToString(DOT_DELIMITER) else ""
        }
        return when (style) {
            Style.COMPACT -> version
            Style.COMPARABLE -> version + prerelease
            Style.FULL -> version + prerelease + buildMetadata
        }
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String {
        return toString(Style.FULL)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Semver) {
            return major.toBigDecimal() == other.major.toBigDecimal() &&
                    minor.toBigDecimal() == other.minor.toBigDecimal() &&
                    patch.toBigDecimal() == other.patch.toBigDecimal() &&
                    prereleaseIdentifiers.count() == other.prereleaseIdentifiers.count() &&
                    prereleaseIdentifiers.zip(other.prereleaseIdentifiers)
                        .fold(true) { result, it ->
                            val itInDecimal = Pair(it.first.toBigDecimalOrNull(), it.second.toBigDecimalOrNull())
                            return@fold result && when {
                                itInDecimal.first != null && itInDecimal.second != null -> itInDecimal.first == itInDecimal.second
                                itInDecimal.first == null && itInDecimal.second == null -> it.first == it.second
                                else -> false
                            }
                        }
        }
        return false
    }

    @Suppress("ReturnCount")
    override fun compareTo(other: Semver): Int {
        fun Semver.versionsInDecimal(): List<BigDecimal> {
            return listOf(major, minor, patch).map { it.toBigDecimal() }
        }

        for (it in this.versionsInDecimal() zip other.versionsInDecimal()) {
            if (it.first != it.second) {
                return it.first.compareTo(it.second)
            }
        }

        if (prereleaseIdentifiers.count() == 0) {
            return if (other.prereleaseIdentifiers.count() == 0) 0 else 1
        }
        if (other.prereleaseIdentifiers.count() == 0) {
            return -1
        }

        loop@ for (it in prereleaseIdentifiers zip other.prereleaseIdentifiers) {
            val lhs = it.first.toBigDecimalOrNull()
            val rhs = it.second.toBigDecimalOrNull()
            return when {
                lhs != null && rhs != null -> if (lhs != rhs) lhs.compareTo(rhs) else continue@loop
                lhs != null && rhs == null -> -1
                lhs == null && rhs != null -> 1
                else -> if (it.first != it.second) it.first.compareTo(it.second) else continue@loop
            }
        }
        return prereleaseIdentifiers.count().compareTo(other.prereleaseIdentifiers.count())
    }

    internal companion object {
        internal const val DOT_DELIMITER = "."
        internal const val PRERELEASE_DELIMITER = "-"
        internal const val BUILD_METADATA_DELIMITER = "+"
    }
}