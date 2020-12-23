package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.scanner.ScanResult
import java.nio.file.Path

class ProjectAnalyzerResult {
    var includeSupportLibrary = false
    var thereAreSupportLibraryDependencies = false
    val scanResultsCache = mutableMapOf<Path, List<ScanResult>>()
}
