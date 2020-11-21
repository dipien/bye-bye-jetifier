package com.dipien.byebyejetifier.archive

import java.nio.file.Path

/**
 * Abstraction to represent archive and its files as a one thing before and after transformation
 * together with information if any changes happened during the transformation.
 */
interface ArchiveItem {

    /**
     * Relative path of the item according to its location in the archive.
     *
     * Files in a nested archive have a path relative to that archive not to the parent of
     * the archive. The root archive has the file system path set as its relative path.
     */
    val relativePath: Path

    val fileName: String

    fun dependsOnSupportLibrary(): Boolean

    fun accept(visitor: ArchiveItemVisitor)

    fun isLayoutResource() = fileName.startsWith("res/layout", ignoreCase = true) &&
            fileName.endsWith(".xml", ignoreCase = true)

    fun isAndroidManifestFile() = fileName.endsWith("AndroidManifest.xml", ignoreCase = true)

    fun isClassFile() = fileName.endsWith(".class", ignoreCase = true)
}
