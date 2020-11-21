package com.dipien.byebyejetifier.scanner.resource

import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.scanner.Scanner
import com.dipien.byebyejetifier.scanner.ScannerHelper
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException

class XmlResourceScanner(
    private val scannerHelper: ScannerHelper
) : Scanner {

    private var oldDependencies = mutableSetOf<String>()

    companion object {
        const val PATTERN_TYPE_GROUP = 1
    }

    /**
     * List of regular expression patterns used to find support library types references in XML
     * files.
     *
     * Matches xml tags in form of:
     * 1. '<(/)prefix(SOMETHING)'.
     * 2. <view ... class="prefix(SOMETHING)" ...>
     * 3. >SOMETHING<
     * 4. {@link SOMETHING#method()}
     *
     * Note that this can also rewrite commented blocks of XML. But on a library level we don't care
     * much about comments.
     */
    private val patterns = listOf(
            Pattern.compile("</?([a-zA-Z0-9.]+)"), // </{candidate} or <{candidate}
            Pattern.compile("[a-zA-Z0-9:]+=\"([^\"]+)\""), // any="{candidate}"
            Pattern.compile(">\\s*([a-zA-Z0-9.\$_]+)<"), // >{candidate}<
            Pattern.compile("\\{@link\\s*([a-zA-Z0-9.\$_]+)(#[^}]*)?}") // @{link {candidate}#*}
    )

    override fun scan(archiveFile: ArchiveFile) {
        val charset = getCharset(archiveFile)
        val dataStr = archiveFile.data.toString(charset)

        for (pattern in patterns) {
            val matcher = pattern.matcher(dataStr)
            while (matcher.find()) {
                val candidate = matcher.group(PATTERN_TYPE_GROUP)
                scannerHelper.verifySupportLibraryDependency(candidate)?.let {
                    archiveFile.dependsOnSupportLibrary = true
                    oldDependencies.add(candidate)
                }
            }
        }

        oldDependencies.forEach {
            LoggerHelper.log("${archiveFile.relativePath} -> $it")
        }
    }

    private fun getCharset(file: ArchiveFile): Charset {
        try {
            file.data.inputStream().use {
                val xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(it)

                xmlReader.encoding ?: return StandardCharsets.UTF_8 // Encoding was not detected

                val result = Charset.forName(xmlReader.encoding)
                if (result == null) {
                    LoggerHelper.logger.error("Failed to find charset for encoding ${xmlReader.encoding}")
                    return StandardCharsets.UTF_8
                }
                return result
            }
        } catch (e: XMLStreamException) {
            // Workaround for b/111814958. A subset of the android.jar xml files has a header that
            // causes our encoding detection to crash. However these files are otherwise valid UTF-8
            // files so we at least try to recover by defaulting to UTF-8.
            LoggerHelper.logger.warn("Received malformed sequence exception when trying to detect the encoding " +
                    "for ${file.fileName}. Defaulting to UTF-8.")
            val tracePrinter = StringWriter()
            e.printStackTrace(PrintWriter(tracePrinter))
            LoggerHelper.logger.warn(tracePrinter.toString())
            return StandardCharsets.UTF_8
        }
    }

    override fun canScan(archiveFile: ArchiveFile): Boolean =
            archiveFile.isLayoutResource() || archiveFile.isAndroidManifestFile()
}
