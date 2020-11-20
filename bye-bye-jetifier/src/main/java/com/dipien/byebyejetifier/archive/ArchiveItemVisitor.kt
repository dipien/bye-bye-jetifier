package com.dipien.byebyejetifier.archive

/**
 * Visitor for [ArchiveItem]
 */
interface ArchiveItemVisitor {

    fun visit(archive: Archive)

    fun visit(archiveFile: ArchiveFile)
}
