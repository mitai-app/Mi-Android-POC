package io.vonley.mi.utils
//credits: https://github.com/glwithu06/Semver.kt

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
private fun require(condition: Boolean, message: String) {
    contract { returns() implies condition }
    if (!condition) throw IllegalArgumentException(message)
}

private class SemverExt {
    internal companion object {
        @OptIn(ExperimentalContracts::class)
        internal fun parse(input: String): Semver {
            val major: String
            val minor: String
            val patch: String
            val prereleaseIdentifiers: List<String>
            val buildMetadataIdentifiers: List<String>

            val dotDelimiterInRegex = """\${Semver.DOT_DELIMITER}"""
            val prereleaseDelimiterInRegex = """\${Semver.PRERELEASE_DELIMITER}"""
            val buildMetadataDelimiterInRegex = """\${Semver.BUILD_METADATA_DELIMITER}"""

            val validateRegex =
                "^([0-9A-Za-z$prereleaseDelimiterInRegex$dotDelimiterInRegex$buildMetadataDelimiterInRegex]+)$".toRegex()
            if (validateRegex.find(input)?.value?.length != input.length) {
                throw IllegalArgumentException("Invalid character in version ($input)")
            }

            var remainder = input
            major = "[0-9]+".toRegex().find(remainder)
                ?.also {
                    require(
                        remainder.getOrNull(it.range.first - 1) != '-',
                        "Major cannot be negative .($input)"
                    )
                    remainder = remainder.substring(it.range.last + 1)
                }
                ?.value
                ?: throw IllegalArgumentException("No major in version ($input)")

            val versionNumericRegex = "(?<=^$dotDelimiterInRegex)[0-9]+".toRegex()
            minor = versionNumericRegex.find(remainder)
                ?.also { remainder = remainder.substring(it.range.last + 1) }
                ?.value
                ?: "0"
            patch = versionNumericRegex.find(remainder)
                ?.also { remainder = remainder.substring(it.range.last + 1) }
                ?.value
                ?: "0"

            prereleaseIdentifiers =
                "(?<=^$prereleaseDelimiterInRegex)([0-9A-Za-z$prereleaseDelimiterInRegex$dotDelimiterInRegex]+)".toRegex()
                    .find(remainder)
                    ?.also { remainder = remainder.substring(it.range.last + 1) }
                    ?.value
                    ?.split(Semver.DOT_DELIMITER)
                    ?: emptyList()

            buildMetadataIdentifiers =
                "(?<=^$buildMetadataDelimiterInRegex)([0-9A-Za-z$prereleaseDelimiterInRegex$dotDelimiterInRegex]+)".toRegex()
                    .find(remainder)
                    ?.also { remainder = remainder.substring(it.range.last + 1) }
                    ?.value
                    ?.split(Semver.DOT_DELIMITER)
                    ?: emptyList()

            require(remainder.isEmpty(), "Invalid version ($input)")

            return Semver(major, minor, patch, prereleaseIdentifiers, buildMetadataIdentifiers)
        }
    }
}

/**
 * Parses the string as a [Semver] and returns the result.
 * @throws IllegalArgumentException if the string is not a valid representation of a semantic version.
 *
 * @return parsed [Semver].
 */
@Suppress("FunctionNaming")
fun Semver(version: String): Semver = SemverExt.parse(version)

/**
 * Parses the number as a [Semver] and returns the result.
 * @throws IllegalArgumentException if the number is not a valid representation of a semantic version.
 *
 * @return parsed [Semver].
 */
@Suppress("FunctionNaming")
fun Semver(version: Number): Semver = SemverExt.parse("$version")

/**
 * Parses the string as a [Semver] and returns the result.
 * @throws IllegalArgumentException if the string is not a valid representation of a semantic version.
 *
 * @return parsed [Semver].
 */
fun String.toVersion(): Semver {
    return Semver(this)
}

/**
 * Parses the string as a [Semver] and returns the result.
 *
 * @return parsed [Semver] or `null` if the string is not a valid representation of a semantic version.
 */
fun String.toVersionOrNull(): Semver? {
    return try {
        Semver(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Parses the number as a [Semver] and returns the result.
 * @throws IllegalArgumentException if the number is not a valid representation of a semantic version.
 *
 * @return parsed [Semver].
 */
fun Number.toVersion(): Semver {
    return Semver(this)
}


/**
 * Parses the number as a [Semver] and returns the result.
 *
 * @return parsed [Semver] or `null` if the number is not a valid representation of a semantic version.
 */
fun Number.toVersionOrNull(): Semver? {
    return try {
        Semver(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}