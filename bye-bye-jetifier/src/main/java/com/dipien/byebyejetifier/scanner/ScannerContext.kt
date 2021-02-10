package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.common.toFilePath
import com.dipien.byebyejetifier.core.TypeRewriter
import com.dipien.byebyejetifier.core.config.Config
import com.dipien.byebyejetifier.core.type.JavaType

class ScannerContext(val config: Config, private val excludedFilesFromScanning: List<String>) {

    val typeRewriter: TypeRewriter = TypeRewriter(config)

    fun isExcludedFileFromScanning(archiveFile: ArchiveFile): Boolean =
        excludedFilesFromScanning
            .any { archiveFile.relativePath.toString().startsWith(it.toFilePath()) }

    fun reportNoMappingFoundFailure(tag: String, type: JavaType) {
        LoggerHelper.warn("No mapping for: $type", tag)
    }
}
