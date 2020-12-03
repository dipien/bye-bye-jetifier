package com.dipien.byebyejetifier.core.rule

import com.dipien.byebyejetifier.core.type.JavaType
import com.google.gson.annotations.SerializedName
import java.util.regex.Pattern

/**
 * Rule that rewrites a Java type based on the given arguments.
 *
 * @param from Regular expression where packages are separated via '/' and inner class separator
 * is "$". Used to match the input type.
 * @param to A string to be used as a replacement if the 'from' pattern is matched. It can also
 * apply groups matched from the original pattern using {x} annotation, e.g. {0}.
 */
class RewriteRule(private val from: String, private val to: String) {

    companion object {
        const val IGNORE_RUNTIME = "ignore"
    }

    // We escape '$' so we don't conflict with regular expression symbols.
    private val inputPattern = Pattern.compile("^${from.replace("$", "\\$")}$")
    private val outputPattern = to.replace("$", "\$")

    /*
     * Whether this is any type of an ignore rule.
     */
    fun isIgnoreRule() = to == IGNORE_RUNTIME

    /**
     * Rewrites the given java type. Returns null if this rule is not applicable for the given type.
     */
    fun apply(input: JavaType): TypeRewriteResult {
        val matcher = inputPattern.matcher(input.fullName)
        if (!matcher.matches()) {
            return TypeRewriteResult.NOT_APPLIED
        }

        if (isIgnoreRule()) {
            return TypeRewriteResult.IGNORED
        }

        var result = outputPattern
        for (i in 0 until matcher.groupCount()) {
            result = result.replace("{$i}", matcher.group(i + 1))
        }

        return TypeRewriteResult(JavaType(result))
    }

    override fun toString(): String {
        return "$inputPattern -> $outputPattern "
    }

    /**
     * JSON data model for [RewriteRule].
     */
    data class JsonData(
        @SerializedName("from")
        val from: String,

        @SerializedName("to")
        val to: String
    ) {

        /** Creates instance of [RewriteRule] */
        fun toRule(): RewriteRule {
            return RewriteRule(from, to)
        }
    }

    /**
     * Result of java type rewrite using [RewriteRule]
     */
    data class TypeRewriteResult(val result: JavaType?, val isIgnored: Boolean = false) {

        companion object {
            val NOT_APPLIED = TypeRewriteResult(result = null, isIgnored = false)

            val IGNORED = TypeRewriteResult(result = null, isIgnored = true)
        }
    }
}
