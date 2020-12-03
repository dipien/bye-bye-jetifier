package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.core.TypeRewriter
import com.dipien.byebyejetifier.core.config.Config

class ScannerContext(val config: Config, private val excludedFilesFromScanning: List<String>) {

    val typeRewriter: TypeRewriter = TypeRewriter(config)

    fun isExcludedFileFromScanning(archiveFile: ArchiveFile): Boolean =
        excludedFilesFromScanning
            .any { archiveFile.relativePath.toString().startsWith(it) }
}
