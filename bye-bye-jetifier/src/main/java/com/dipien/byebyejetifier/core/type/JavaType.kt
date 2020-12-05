package com.dipien.byebyejetifier.core.type

/**
 * Wrapper for Java type declaration.
 *
 * For packages use [PackageName].
 */
data class JavaType(val fullName: String) {

    init {
        if (fullName.contains('.')) {
            throw IllegalArgumentException(
                "The type does not support '.' as package separator! Received '$fullName'."
            )
        }
    }

    companion object {
        /** Creates the type from notation where packages are separated using '.' */
        fun fromDotVersion(fullName: String): JavaType {
            if (fullName.contains("/")) {
                throw IllegalArgumentException(
                    "Type containing '/' cannot be passed to the factory expecting dot " +
                        "separators! Received '$fullName'."
                )
            }

            return JavaType(fullName.replace('.', '/'))
        }
    }

    /** Returns the type as a string where packages are separated using '.' */
    fun toDotNotation(): String {
        return fullName.replace('/', '.')
    }

    /** Whether this type references to an inner type (e.g. MyClass$Inner) */
    fun hasInnerType() = fullName.contains('$')

    /**
     * Returns the root type of this type stripped from any inner types (e.g. for MyClass$Inner
     * returns MyClass)
     */
    fun getRootType(): JavaType {
        if (!hasInnerType()) {
            return this
        }

        return JavaType(fullName.split('$').first())
    }

    /**
     * Returns this type with its root top level type replaced with the give root type.
     */
    fun remapWithNewRootType(root: JavaType): JavaType {
        if (root.hasInnerType()) {
            throw IllegalArgumentException("Cannot remap type with a nested types as a root!")
        }

        val tokens = fullName.split('$').toMutableList()
        tokens[0] = root.fullName
        return JavaType(tokens.joinToString("$"))
    }

    /**
     * Returns parent type of this types (e.g. for test.Class.InnerClass -> returns test.Class). For
     * top level packages returns identity.
     */
    fun getParentType(): JavaType {
        if (fullName.contains("/")) {
            return JavaType(fullName.substringBeforeLast('/'))
        }
        return this
    }

    override fun toString() = fullName
}
