package com.dipien.byebyejetifier.archive

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Represents an archive (zip, jar, aar ...)
 */
class Archive(
    override val relativePath: Path,
    files: List<ArchiveItem>
) : ArchiveItem {

    private val _files: MutableList<ArchiveItem> = files.toMutableList()

    val files: List<ArchiveItem> = _files

    companion object {
        /** Defines file extensions that are recognized as archives */
        val ARCHIVE_EXTENSIONS = listOf(".jar", ".zip", ".aar")
    }

    override val fileName: String = relativePath.fileName.toString()

    override fun dependsOnSupportLibrary(): Boolean =
            files.any { it.dependsOnSupportLibrary() }

    override fun accept(visitor: ArchiveItemVisitor) {
        visitor.visit(this)
    }

    val artifactDefinition: String by lazy {
        val splits = relativePath.toString().split("/")
        val groupId = splits[splits.size - 5]
        val artifactId = splits[splits.size - 4]
        val version = splits[splits.size - 3]
        "$groupId:$artifactId:$version"
    }

    object Builder {

        /**
         * @param recursive Whether nested archives should be also extracted.
         */
        @Throws(IOException::class)
        fun extract(archiveFile: File, recursive: Boolean = true): Archive {
            val inputStream = FileInputStream(archiveFile)
            inputStream.use {
                return extractArchive(
                    it, archiveFile.toPath(), recursive
                )
            }
        }

        @Throws(IOException::class)
        private fun extractArchive(
            inputStream: InputStream,
            relativePath: Path,
            recursive: Boolean
        ): Archive {
            val zipIn = ZipInputStream(inputStream)
            val files = mutableListOf<ArchiveItem>()

            var entry: ZipEntry? = zipIn.nextEntry
            // iterates over entries in the zip file
            while (entry != null) {
                if (!entry.isDirectory) {
                    val entryPath = Paths.get(entry.name)
                    if (isArchive(entry) && recursive) {
                        files.add(extractArchive(zipIn, entryPath, recursive))
                    } else {
                        files.add(extractFile(zipIn, entryPath))
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            // Cannot close the zip stream at this moment as that would close also any parent zip
            // streams in case we are processing a nested archive.
            return Archive(relativePath, files.toList())
        }

        @Throws(IOException::class)
        private fun extractFile(
            zipIn: ZipInputStream,
            relativePath: Path
        ): ArchiveFile {
            val data = zipIn.readBytes()
            return ArchiveFile(relativePath, data)
        }

        private fun isArchive(zipEntry: ZipEntry): Boolean {
            return ARCHIVE_EXTENSIONS.any { zipEntry.name.endsWith(it, ignoreCase = true) }
        }
    }
}
