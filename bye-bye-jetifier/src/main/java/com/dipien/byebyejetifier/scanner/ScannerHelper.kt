package com.dipien.byebyejetifier.scanner

class ScannerHelper(private val oldModulesPrefixes: List<String>, private val ignoredPackages: List<String>) {

    fun verifySupportLibraryDependency(typeStrArg: String): String? {
        val typeStr = typeStrArg.replace("/", ".")
        if (oldModulesPrefixes.any { prefix -> typeStr.startsWith(prefix) } && !ignoredPackages.filter { typeStr.startsWith(it) }.any()) {
            return typeStr
        }
        return null
    }
}
