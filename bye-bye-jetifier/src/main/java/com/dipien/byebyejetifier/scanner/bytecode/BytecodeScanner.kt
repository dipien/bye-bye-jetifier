package com.dipien.byebyejetifier.scanner.bytecode

import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.scanner.ScanResult
import com.dipien.byebyejetifier.scanner.Scanner
import com.dipien.byebyejetifier.scanner.ScannerContext
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper

class BytecodeScanner(private val context: ScannerContext) : Scanner {

    override fun scan(archiveFile: ArchiveFile): List<ScanResult> {
        val reader = ClassReader(archiveFile.data)
        val writer = ClassWriter(0 /* flags */)
        val customRemapper = CustomRemapper(CoreRemapper(context))
        val visitor = ClassRemapper(writer, customRemapper)

        reader.accept(visitor, 0 /* flags */)

        return customRemapper.legacyDependencies.map { ScanResult(archiveFile.relativePath.toString(), it) }
    }

    override fun canScan(archiveFile: ArchiveFile): Boolean = archiveFile.isClassFile()
}
