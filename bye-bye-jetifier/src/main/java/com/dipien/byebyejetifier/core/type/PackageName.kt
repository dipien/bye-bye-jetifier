package com.dipien.byebyejetifier.core.type

/**
 * Wrapper for Java package name declaration.
 */
data class PackageName(val fullName: String) {

    init {
        if (fullName.contains('.')) {
            throw IllegalArgumentException("The type does not support '.' as a package separator!")
        }
    }

    companion object {
        /** Creates the package from notation where packages are separated using '.' */
        fun fromDotVersion(fullName: String): PackageName {
            return PackageName(fullName.replace('.', '/'))
        }
    }

    /** Returns the package as a string where packages are separated using '.' */
    fun toDotNotation(): String {
        return fullName.replace('/', '.')
    }

    override fun toString() = fullName
}
