package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.archive.ArchiveFile

abstract class AbstractScanner(protected val scannerHelper: ScannerHelper) : Scanner {

    override fun canScan(archiveFile: ArchiveFile): Boolean =
        !scannerHelper.isExcludedFileFromScanning(archiveFile)
}
