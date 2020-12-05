package com.dipien.byebyejetifier.core.config

import com.dipien.byebyejetifier.core.PackageMap
import com.dipien.byebyejetifier.core.rule.RewriteRule
import com.dipien.byebyejetifier.core.rule.RewriteRulesMap
import com.dipien.byebyejetifier.core.type.JavaType
import com.dipien.byebyejetifier.core.type.PackageName
import com.dipien.byebyejetifier.core.type.TypesMap
import com.google.gson.annotations.SerializedName
import java.util.regex.Pattern

/**
 * @param restrictToPackagePrefixes Package prefixes that limit the scope of the rewriting. In most
 *  cases the rules have priority over this. We use this mainly to determine if we are actually
 *  missing a rule in case we fail to rewrite.
 * @param rulesMap Rules to rewrite java types.
 * @param typesMap Map of all java types and fields to be used to rewrite java types.
 * @param packageMap Package map to be used to rewrite packages.
 * @param stringsMap String map to be used to rewrite string values.
 */
class Config(
    val restrictToPackagePrefixes: Set<String>,
    val rulesMap: RewriteRulesMap,
    val typesMap: TypesMap,
    val packageMap: PackageMap,
    val stringsMap: TypesMap
) {

    // Merges all packages prefixes into one regEx pattern
    private val packagePrefixPattern = Pattern.compile(
        "^(" + restrictToPackagePrefixes.map { "($it)" }.joinToString("|") + ").*$"
    )

    /**
     * Returns whether the given type is eligible for rewrite.
     *
     * If not, the transformers should ignore it.
     */
    fun isEligibleForRewrite(type: JavaType): Boolean {
        if (!isEligibleForRewriteInternal(type.fullName)) {
            return false
        }

        val isIgnored = rulesMap.ignoreRules
            .any { it.apply(type) == RewriteRule.TypeRewriteResult.IGNORED }
        return !isIgnored
    }

    private fun isEligibleForRewriteInternal(type: String): Boolean {
        if (restrictToPackagePrefixes.isEmpty()) {
            return false
        }
        return packagePrefixPattern.matcher(type).matches()
    }

    fun isEligibleForRewrite(type: PackageName): Boolean {
        if (!isEligibleForRewriteInternal(type.fullName + "/")) {
            return false
        }

        val javaType = JavaType(type.fullName + "/")
        val isIgnored = rulesMap.ignoreRules
            .any { it.apply(javaType) == RewriteRule.TypeRewriteResult.IGNORED }
        return !isIgnored
    }

    /**
     * JSON data model for [Config].
     */
    data class JsonData(
        @SerializedName("restrictToPackagePrefixes")
        val restrictToPackages: List<String?>,

        @SerializedName("rules")
        val rules: List<RewriteRule.JsonData?>?,

        @SerializedName("packageMap")
        val packageMap: List<PackageMap.PackageRule.JsonData?>,

        @SerializedName("map")
        val mappings: TypesMap.JsonData? = null,

        @SerializedName("stringsMap")
        val stringsMap: TypesMap.JsonData? = null
    ) {

        /** Creates instance of [Config] */
        fun toConfig(): Config {
            return Config(
                restrictToPackagePrefixes = restrictToPackages.filterNotNull().toSet(),
                rulesMap = RewriteRulesMap(
                    rules.orEmpty().filterNotNull().map { it.toRule() }.toList()
                ),
                packageMap = PackageMap(
                    packageMap.filterNotNull().map { it.toMappings() }.toList()
                ),
                typesMap = mappings?.toMappings() ?: TypesMap.EMPTY,
                stringsMap = stringsMap?.toMappings() ?: TypesMap.EMPTY
            )
        }
    }
}
