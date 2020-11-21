package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.archive.ArchiveItemVisitor
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.scanner.bytecode.BytecodeScanner
import com.dipien.byebyejetifier.scanner.resource.XmlResourceScanner

class ScannerProcessor(private val oldModulesPrefixes: List<String>, ignoredPackages: List<String>) : ArchiveItemVisitor {

    var thereAreSupportLibraryDependencies = false
        private set

    private val scannerList: List<Scanner> by lazy {
        val scannerHelper = ScannerHelper(oldModulesPrefixes, ignoredPackages)
        listOf(BytecodeScanner(scannerHelper), XmlResourceScanner(scannerHelper))
    }

    fun scanLibrary(archive: Archive) {
        archive.accept(this)
        if (archive.dependsOnSupportLibrary()) {
            LoggerHelper.log("From: ${archive.artifactDefinition}")
            if (!thereAreSupportLibraryDependencies) {
                thereAreSupportLibraryDependencies = true
            }
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
                it.scan(archiveFile)
            }
        }
    }
}
