package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.archive.ArchiveFile

interface Scanner {

    fun scan(archiveFile: ArchiveFile)

    fun canScan(archiveFile: ArchiveFile): Boolean
}
