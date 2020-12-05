package com.dipien.byebyejetifier.scanner.bytecode

import com.dipien.byebyejetifier.core.type.JavaType
import org.objectweb.asm.commons.Remapper

class CustomRemapper(
    private val remapper: CoreRemapper
) : Remapper() {

    var legacyDependencies = mutableSetOf<String>()

    override fun map(typeName: String): String {
        verifyType(JavaType(typeName))
        return super.map(typeName)
    }

    override fun mapPackageName(name: String): String {
        verifyType(JavaType(name))
        return super.mapPackageName(name)
    }

    override fun mapValue(value: Any?): Any? {
        val stringVal = value as? String
        if (stringVal == null) {
            return super.mapValue(value)
        }

        fun mapPoolReferenceType(typeDeclaration: String): String {
            if (!typeDeclaration.contains(".")) {
                verifyType(JavaType(typeDeclaration))
                return typeDeclaration
            }

            if (typeDeclaration.contains("/")) {
                // Mixed "." and "/"  - not something we know how to handle
                return typeDeclaration
            }

            val toRewrite = typeDeclaration.replace(".", "/")
            verifyType(JavaType(toRewrite))
            return typeDeclaration
        }

        if (stringVal.startsWith("L") && stringVal.endsWith(";")) {
            // L denotes a type declaration. For some reason there are references in the constant
            // pool that ASM skips.
            val typeDeclaration = stringVal.substring(1, stringVal.length - 1)
            if (typeDeclaration.isEmpty()) {
                return value
            }

            if (typeDeclaration.contains(";L")) {
                // We have array of constants
                typeDeclaration
                    .split(";L")
                    .forEach { mapPoolReferenceType(it) }
                return super.mapValue(value)
            }

            mapPoolReferenceType(typeDeclaration)
            return super.mapValue(value)
        }

        verifyString(stringVal)
        return super.mapValue(value)
    }

    private fun verifyType(type: JavaType) {
        if (remapper.rewriteType(type) != type) {
            legacyDependencies.add(type.fullName)
        }
    }

    private fun verifyString(value: String) {
        if (remapper.rewriteString(value) != value) {
            legacyDependencies.add(value)
        }
    }
}
