package com.dipien.byebyejetifier.scanner.resource

import com.dipien.byebyejetifier.archive.ArchiveFile
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.core.type.JavaType
import com.dipien.byebyejetifier.core.type.PackageName
import com.dipien.byebyejetifier.scanner.ScanResult
import com.dipien.byebyejetifier.scanner.Scanner
import com.dipien.byebyejetifier.scanner.ScannerContext
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.regex.Pattern
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException

class XmlResourceScanner(
    private val context: ScannerContext
) : Scanner {

    companion object {

        const val TAG = "XmlResourceScanner"

        const val PATTERN_TYPE_GROUP = 1

        /***
         * Matches anything that could be java type or package
         */
        val JAVA_TOKEN_MATCHER = "^[a-zA-Z0-9.\$_]+$".toRegex()
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

    override fun scan(archiveFile: ArchiveFile): List<ScanResult> {
        val charset = getCharset(archiveFile)
        val dataStr = archiveFile.data.toString(charset)

        return replaceWithPatterns(dataStr, patterns, archiveFile.relativePath)
    }

    private fun getCharset(file: ArchiveFile): Charset {
        try {
            file.data.inputStream().use {
                val xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(it)

                xmlReader.encoding ?: return StandardCharsets.UTF_8 // Encoding was not detected

                val result = Charset.forName(xmlReader.encoding)
                if (result == null) {
                    LoggerHelper.error("Failed to find charset for encoding ${xmlReader.encoding}")
                    return StandardCharsets.UTF_8
                }
                return result
            }
        } catch (e: XMLStreamException) {
            // Workaround for b/111814958. A subset of the android.jar xml files has a header that
            // causes our encoding detection to crash. However these files are otherwise valid UTF-8
            // files so we at least try to recover by defaulting to UTF-8.
            LoggerHelper.warn("Received malformed sequence exception when trying to detect the encoding " +
                "for ${file.fileName}. Defaulting to UTF-8.")
            val tracePrinter = StringWriter()
            e.printStackTrace(PrintWriter(tracePrinter))
            LoggerHelper.warn(tracePrinter.toString())
            return StandardCharsets.UTF_8
        }
    }

    /**
     * For each pattern in [patterns] matching a portion of the string represented by [sb], applies
     * [mappingFunction] to the match and puts the result back into [sb].
     */
    private fun replaceWithPatterns(
        dataStr: String,
        patterns: List<Pattern>,
        filePath: Path
    ): List<ScanResult> {
        val result = mutableSetOf<ScanResult>()

        for (pattern in patterns) {
            val matcher = pattern.matcher(dataStr)
            while (matcher.find()) {
                val toReplace = matcher.group(PATTERN_TYPE_GROUP)
                val replacement =
                    if (toReplace.matches(JAVA_TOKEN_MATCHER)) {
                        if (isPackage(toReplace)) {
                            rewritePackage(toReplace, filePath)
                        } else {
                            rewriteType(toReplace)
                        }
                    } else {
                        toReplace
                    }

                if (replacement != toReplace) {
                    result.add(ScanResult(filePath.toString(), toReplace))
                }
            }
        }

        return result.toList()
    }

    private fun isPackage(token: String): Boolean {
        return !token.any { it.isUpperCase() }
    }

    private fun rewriteType(typeName: String): String {
        if (typeName.contains(" ")) {
            return typeName
        }

        val type = JavaType.fromDotVersion(typeName)
        val result = context.typeRewriter.rewriteType(type)
        if (result != null) {
            return result.toDotNotation()
        }

        context.reportNoMappingFoundFailure(TAG, type)
        return typeName
    }

    private fun rewritePackage(packageName: String, filePath: Path): String {
        if (!packageName.contains('.')) {
            // Single word packages are not something we need or should rewrite
            return packageName
        }

        val pckg = PackageName.fromDotVersion(packageName)

        val result = context.config.packageMap.getPackageFor(pckg)
        if (result != null) {
            LoggerHelper.debug("Map package: $packageName -> $result")
            return result.toDotNotation()
        }

        if (context.config.isEligibleForRewrite(pckg)) {
            LoggerHelper.error("No mapping for package '$packageName' in '$filePath', keeping identity")
        }

        return packageName
    }

    override fun canScan(archiveFile: ArchiveFile): Boolean =
        archiveFile.isLayoutResource() || archiveFile.isAndroidManifestFile()
}
