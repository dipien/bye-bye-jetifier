package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.archive.ArchiveItemVisitor
import com.dipien.byebyejetifier.common.LoggerHelper

class ScannerProcessor(private val scannerList: List<Scanner>) : ArchiveItemVisitor {

    var scanResults = mutableListOf<ScanResult>()
    var includeSupportLibrary = false

    var thereAreSupportLibraryDependencies = false
        private set

    fun scanLibrary(archive: Archive) {
        archive.accept(this)
        if (archive.dependsOnSupportLibrary()) {
            LoggerHelper.log("Artifact: ${archive.artifactDefinition}")
            scanResults.forEach {
                LoggerHelper.log("* ${it.relativePath} -> ${it.legacyDependency}")
            }
            scanResults.clear()
            thereAreSupportLibraryDependencies = true
        }
    }

    override fun visit(archive: Archive) {
        archive.files.forEach {
            it.accept(this)
        }
    }

    override fun visit(archiveFile: ArchiveFile) {
        scannerList.forEach {
            if (it.canScan(archiveFile)) {
                scanResults.addAll(it.scan(archiveFile))
            }
        }
    }
}
