package com.dipien.byebyejetifier.core.rule

import com.dipien.byebyejetifier.core.type.JavaType

/**
 * Contains all [RewriteRule]s.
 */
class RewriteRulesMap(private val rewriteRules: List<RewriteRule>) {

    val ignoreRules = rewriteRules.filter { it.isIgnoreRule() }.toSet()

    /**
     * Tries to rewrite the given given type using the rules. If
     */
    fun rewriteType(type: JavaType): JavaType? {
        // Try to find a rule
        for (rule in rewriteRules) {
            val typeRewriteResult = rule.apply(type)
            if (typeRewriteResult.result == null) {
                continue
            }
            return typeRewriteResult.result
        }

        return null
    }
}
