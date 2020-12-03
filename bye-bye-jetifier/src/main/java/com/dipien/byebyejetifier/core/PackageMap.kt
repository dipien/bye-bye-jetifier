package com.dipien.byebyejetifier.core

import com.dipien.byebyejetifier.core.type.PackageName
import com.google.gson.annotations.SerializedName

/**
 * Package map to be used to rewrite packages. The rewrite rules allow duplicities where the
 * artifact name prefix defined in a rule determines if such rule should be used or skipped.
 * The priority is determined only by the order (top to bottom). Having a rule with no file prefix
 * as first means that it is always applied.
 */
class PackageMap(private val rules: List<PackageRule>) {

    /**
     * Returns a new package name for the given [fromPackage].
     */
    fun getPackageFor(fromPackage: PackageName): PackageName? {
        val rule = rules.find { it.from == fromPackage.fullName }
        if (rule != null) {
            return PackageName(rule.to)
        }
        return null
    }

    data class PackageRule(val from: String, val to: String) {

        /**
         * JSON data model for [PackageRule].
         */
        data class JsonData(
            @SerializedName("from")
            val from: String,
            @SerializedName("to")
            val to: String
        ) {
            /** Creates instance of [PackageRule] */
            fun toMappings(): PackageRule {
                return PackageRule(from, to)
            }
        }
    }
}
