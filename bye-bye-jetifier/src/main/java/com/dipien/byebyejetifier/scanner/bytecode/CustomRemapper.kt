package com.dipien.byebyejetifier.scanner.bytecode

import com.dipien.byebyejetifier.scanner.ScannerHelper
import org.objectweb.asm.commons.Remapper

class CustomRemapper(
    private val scannerHelper: ScannerHelper
) : Remapper() {

    var oldDependencies = mutableSetOf<String>()

    override fun map(typeName: String): String {
        onDiscoveredType(typeName)
        return super.map(typeName)
    }

    override fun mapPackageName(name: String): String {
        onDiscoveredType(name)
        return super.mapPackageName(name)
    }

    override fun mapValue(value: Any?): Any? {
        val stringVal = value as? String
        if (stringVal == null) {
            return super.mapValue(value)
        }

        fun mapPoolReferenceType(typeDeclaration: String): String {
            if (!typeDeclaration.contains(".")) {
                onDiscoveredType(typeDeclaration)
                return typeDeclaration
            }

            if (typeDeclaration.contains("/")) {
                // Mixed "." and "/"  - not something we know how to handle
                return typeDeclaration
            }

            val toRewrite = typeDeclaration.replace(".", "/")
            onDiscoveredType(toRewrite)
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

        onDiscoveredType(stringVal)
        return super.mapValue(value)
    }

    private fun onDiscoveredType(type: String) {
        scannerHelper.verifySupportLibraryDependency(type)?.let {
            oldDependencies.add(type)
        }
    }
}
