package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.archive.ArchiveItemVisitor

class ScannerProcessor(private val scannerList: List<Scanner>) : ArchiveItemVisitor {

    fun scanLibrary(archive: Archive): List<ScanResult> {
        val scanResults = mutableListOf<ScanResult>()
        archive.accept(this, scanResults)
        return scanResults
    }

    override fun visit(archive: Archive, scanResults: MutableList<ScanResult>) {
        archive.files.forEach {
            it.accept(this, scanResults)
        }
    }

    override fun visit(archiveFile: ArchiveFile, scanResults: MutableList<ScanResult>) {
        scannerList.forEach {
            if (it.canScan(archiveFile)) {
                scanResults.addAll(it.scan(archiveFile))
            }
        }
    }
}
