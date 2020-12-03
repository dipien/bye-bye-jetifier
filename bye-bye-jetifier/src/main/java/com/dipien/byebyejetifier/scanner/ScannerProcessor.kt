package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.archive.ArchiveItemVisitor
import com.dipien.byebyejetifier.common.LoggerHelper

class ScannerProcessor(
    private val context: ScannerContext,
    private val scannerList: List<Scanner>
) : ArchiveItemVisitor {

    fun scanLibrary(archive: Archive): List<ScanResult> {
        LoggerHelper.debug("")
        LoggerHelper.debug("Artifact: ${archive.artifactDefinition}")

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
        if (context.isExcludedFileFromScanning(archiveFile)) {
            LoggerHelper.debug("Excluded for scanning: ${archiveFile.relativePath}")
            return
        }

        var fileLogged = false

        scannerList.forEach {
            if (it.canScan(archiveFile)) {
                if (!fileLogged) {
                    fileLogged = true
                    LoggerHelper.debug("File: ${archiveFile.relativePath}")
                }
                scanResults.addAll(it.scan(archiveFile))
            }
        }
    }
}
