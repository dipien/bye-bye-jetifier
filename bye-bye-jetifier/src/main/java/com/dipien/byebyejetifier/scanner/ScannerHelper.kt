package com.dipien.byebyejetifier.scanner

class ScannerHelper(private val oldModulesPrefixes: List<String>, ignoreImportsFilePath: String?) {

    private var ignoredImports = mutableSetOf<String>()

    init {
        ignoredImports.add("android.support.v4.media")
        ignoredImports.add("android.support.FILE_PROVIDER_PATHS")
        ignoreImportsFilePath?.let {
            IgnoreFileUtil.loadIgnoreFile(it, ignoredImports)
        }
    }

    fun verifySupportLibraryDependency(typeStrArg: String): String? {
        val typeStr = typeStrArg.replace("/", ".")
        if (oldModulesPrefixes.any { prefix -> typeStr.startsWith(prefix) } && !ignoredImports.filter { typeStr.startsWith(it) }.any()) {
            return typeStr
        }
        return null
    }
}
