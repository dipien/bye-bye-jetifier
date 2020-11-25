package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.archive.ArchiveFile

class ScannerHelper(
    private val legacyPackagesPrefixes: List<String>,
    private val ignoredLegacyPackagesPrefixes: List<String>,
    private val excludedFilesFromScanning: List<String>
) {

    fun isExcludedFileFromScanning(archiveFile: ArchiveFile): Boolean = excludedFilesFromScanning.any { archiveFile.relativePath.toString().startsWith(it) }

    fun verifySupportLibraryDependency(typeStrArg: String): String? {
        val typeStr = typeStrArg.replace("/", ".")
        if (hasLegacyPackagePrefix(typeStr) && !isIgnoredLegacyClass(typeStr)) {
            return typeStr
        }
        return null
    }

    private fun hasLegacyPackagePrefix(typeStr: String): Boolean = legacyPackagesPrefixes.any { typeStr.startsWith(it) }

    private fun isIgnoredLegacyClass(typeStr: String): Boolean = ignoredLegacyPackagesPrefixes.any { typeStr.startsWith(it) }
}
