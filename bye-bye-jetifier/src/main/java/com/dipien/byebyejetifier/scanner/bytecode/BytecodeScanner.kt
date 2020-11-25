package com.dipien.byebyejetifier.scanner.bytecode

import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.scanner.ScanResult
import com.dipien.byebyejetifier.scanner.Scanner
import com.dipien.byebyejetifier.scanner.ScannerHelper
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper

class BytecodeScanner(private val scannerHelper: ScannerHelper) : Scanner {

    override fun scan(archiveFile: ArchiveFile): List<ScanResult> {
        val reader = ClassReader(archiveFile.data)
        val writer = ClassWriter(0 /* flags */)
        val customRemapper = CustomRemapper(scannerHelper)
        val visitor = ClassRemapper(writer, customRemapper)

        reader.accept(visitor, 0 /* flags */)

        archiveFile.dependsOnSupportLibrary = customRemapper.oldDependencies.isNotEmpty()

        return customRemapper.oldDependencies.map { ScanResult(archiveFile.relativePath.toString(), it) }
    }

    override fun canScan(archiveFile: ArchiveFile): Boolean = archiveFile.isClassFile()
}
