package com.dipien.byebyejetifier.archive

import com.dipien.byebyejetifier.common.toFilePath
import com.dipien.byebyejetifier.scanner.ScanResult
import java.nio.file.Path

/**
 * Represents a file in the archive that is not an archive.
 */
class ArchiveFile(relativePath: Path, data: ByteArray) : ArchiveItem {

    companion object {
        val RES_LAYOUT_PATH = "res/layout".toFilePath()
    }

    override var relativePath = relativePath
        private set

    override var fileName: String = relativePath.fileName.toString()
        private set

    var data: ByteArray = data
        private set

    override fun accept(visitor: ArchiveItemVisitor, scanResults: MutableList<ScanResult>) {
        visitor.visit(this, scanResults)
    }

    fun isLayoutResource() = relativePath.toString().startsWith(RES_LAYOUT_PATH, ignoreCase = true) &&
        fileName.endsWith(".xml", ignoreCase = true)

    fun isAndroidManifestFile() = fileName.endsWith("AndroidManifest.xml", ignoreCase = true)

    fun isClassFile() = fileName.endsWith(".class", ignoreCase = true)
}
