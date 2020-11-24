package com.dipien.byebyejetifier.archive

import com.dipien.byebyejetifier.scanner.ScanResult

/**
 * Visitor for [ArchiveItem]
 */
interface ArchiveItemVisitor {

    fun visit(archive: Archive, scanResults: MutableList<ScanResult>)

    fun visit(archiveFile: ArchiveFile, scanResults: MutableList<ScanResult>)
}
