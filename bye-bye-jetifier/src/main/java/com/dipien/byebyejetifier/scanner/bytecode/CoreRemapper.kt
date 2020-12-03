package com.dipien.byebyejetifier.scanner.bytecode

import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.core.type.JavaType
import com.dipien.byebyejetifier.scanner.ScannerContext

class CoreRemapper(private val context: ScannerContext) {

    companion object {

        const val TAG = "CoreRemapper"

        val AMBIGUOUS_STRINGS = setOf(
            JavaType.fromDotVersion("android.support.v4"),
            JavaType.fromDotVersion("android.support.v4.content"),
            JavaType.fromDotVersion("android.support.v4.widget"),
            JavaType.fromDotVersion("android.support.v4.view"),
            JavaType.fromDotVersion("android.support.v4.media"),
            JavaType.fromDotVersion("android.support.v13"),
            JavaType.fromDotVersion("android.support.v13.view"),
            JavaType.fromDotVersion("android.support.v13.app"),
            JavaType.fromDotVersion("android.support.design.widget")
        )
    }

    fun rewriteType(type: JavaType): JavaType {
        val result = context.typeRewriter.rewriteType(type)
        if (result != null) {
            return result
        }

        context.reportNoMappingFoundFailure(TAG, type)
        return type
    }

    fun rewriteString(value: String): String {
        val hasDotSeparators = value.contains(".")
        val hasSlashSeparators = value.contains("/")

        if (hasDotSeparators && hasSlashSeparators) {
            // We do not support mix of both separators
            return value
        }

        val type = if (hasDotSeparators) {
            JavaType.fromDotVersion(value)
        } else {
            JavaType(value)
        }

        if (!context.config.isEligibleForRewrite(type)) {
            return value
        }

        // Verify that we did not make an ambiguous mapping, see b/116745353
        if (AMBIGUOUS_STRINGS.contains(type)) {
            throw AmbiguousStringJetifierException(
                "The given artifact contains a string literal " +
                    "with a package reference '$value' that cannot be safely rewritten. " +
                    "Libraries using reflection such as annotation processors need to be " +
                    "updated manually to add support for androidx."
            )
        }

        // Strings map has a priority over types map
        val mappedString = context.config.stringsMap.mapType(type)
        if (mappedString != null) {
            LoggerHelper.debug("Map string: '$type' -> '$mappedString'")
            return if (hasDotSeparators) mappedString.toDotNotation() else mappedString.fullName
        }

        val mappedType = context.config.typesMap.mapType(type)
        if (mappedType != null) {
            LoggerHelper.debug("Map string: '$type' -> '$mappedType'")
            return if (hasDotSeparators) mappedType.toDotNotation() else mappedType.fullName
        }

        // We might be working with an internal type or field reference, e.g.
        // AccessibilityNodeInfoCompat.PANE_TITLE_KEY. So we try to remove last segment to help it.
        if (value.contains(".")) {
            val subTypeResult = context.config.typesMap.mapType(type.getParentType())
            if (subTypeResult != null) {
                val result = subTypeResult.toDotNotation() + '.' + value.substringAfterLast('.')
                LoggerHelper.debug("Map string: '$value' -> '$result' via type fallback")
                return result
            }
        }

        // Try rewrite rules
        val rewrittenType = context.config.rulesMap.rewriteType(type)
        if (rewrittenType != null) {
            LoggerHelper.debug("Map string: '$value' -> '$rewrittenType' via fallback")
            return if (hasDotSeparators) {
                rewrittenType.toDotNotation()
            } else {
                rewrittenType.fullName
            }
        }

        // We do not treat string content mismatches as errors
        LoggerHelper.debug("Found string '$value' but failed to rewrite")
        return value
    }
}

/**
 * Thrown when jetifier finds a string reference to a package that has ambiguous mapping.
 */
class AmbiguousStringJetifierException(message: String) : Exception(message)
