package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.scanner.ScanResult
import com.dipien.byebyejetifier.scanner.ScannerProcessor
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import java.util.LinkedList
import java.util.Queue

class ProjectAnalyzer(
    private val project: Project,
    private var ignoredConfigurations: List<String>,
    private var legacyGroupIdPrefixes: List<String>,
    private val scannerProcessor: ScannerProcessor
) {

    companion object {
        private const val ANDROIDX_GROUP_ID_PREFIX = "androidx"
    }

    fun analyze() {

        var includeSupportLibrary = false
        var thereAreSupportLibraryDependencies = false
        var hasExternalDependencies = false
        val scanResultsCache = mutableMapOf<String, List<ScanResult>>()

        val externalDependencies = project.configurations
            .filter {
                !ignoredConfigurations.contains(it.name)
            }
            .map {
                it.getExternalDependencies()
            }
            .flatten()
            .distinct()

        externalDependencies
            .map {
                if (!it.isAndroidX() && !it.isLegacyAndroidSupport() && !it.isModule()) {
                    it.moduleArtifacts
                } else {
                    emptySet()
                }
            }
            .flatten()
            .distinct()
            .forEach {
                val library = Archive.Builder.extract(it.file)
                val scanResults = scanResultsCache.getOrPut(library.artifactDefinition) { scannerProcessor.scanLibrary(library) }
                if (scanResults.isNotEmpty()) {
                    if (!thereAreSupportLibraryDependencies) {
                        LoggerHelper.lifeCycle("")
                        LoggerHelper.lifeCycle("========================================")
                        LoggerHelper.lifeCycle("Project: ${project.name}")
                        LoggerHelper.lifeCycle("========================================")
                    }
                    LoggerHelper.lifeCycle("")
                    LoggerHelper.lifeCycle("Scanning ${library.artifactDefinition}")
                    scanResults.forEach { scanResult ->
                        LoggerHelper.lifeCycle(" * ${scanResult.relativePath} -> ${scanResult.legacyDependency}")
                    }
                    thereAreSupportLibraryDependencies = true
                } else {
                    LoggerHelper.info("")
                    LoggerHelper.info("========================================")
                    LoggerHelper.info("Project: ${project.name}")
                    LoggerHelper.info("========================================")
                    LoggerHelper.info("")
                    LoggerHelper.info("Scanning ${library.artifactDefinition}")
                    LoggerHelper.info(" * No legacy android support usages found")
                }
                hasExternalDependencies = true
            }

        val legacyArtifacts = externalDependencies
            .mapNotNull {
                if (it.isLegacyAndroidSupport()) {
                    it
                } else {
                    null
                }
            }
            .distinctBy { it.name }

        if (legacyArtifacts.isNotEmpty()) {
            includeSupportLibrary = true
            LoggerHelper.lifeCycle("")
            LoggerHelper.lifeCycle("Legacy support dependencies:")
            legacyArtifacts.sortedBy { it.name }.forEach {
                LoggerHelper.lifeCycle(" * ${it.name}")
            }
        }

        if (!thereAreSupportLibraryDependencies && !includeSupportLibrary && !hasExternalDependencies) {
            LoggerHelper.info(" * No legacy android support usages found")
        }

        if (includeSupportLibrary) {
            ProjectAnalyzerResult.includeSupportLibrary = true
        }
        if (thereAreSupportLibraryDependencies) {
            ProjectAnalyzerResult.thereAreSupportLibraryDependencies = true
        }
    }

    private fun ResolvedDependency.isAndroidX(): Boolean {
        return moduleGroup.startsWith(ANDROIDX_GROUP_ID_PREFIX)
    }

    private fun Configuration.getExternalDependencies(): Set<ResolvedDependency> {
        var firstLevelDependencies = emptySet<ResolvedDependency>()
        try {
            if (isCanBeResolved) {
                firstLevelDependencies = resolvedConfiguration.firstLevelModuleDependencies
            }
        } catch (e: Throwable) {
            if (name.endsWith("Metadata") || name.endsWith("archives")) {
                // TODO analyze errors from Configurations whose name ends in "metadata"
                LoggerHelper.info("Error when accessing configuration $name" + e.message)
            } else {
                throw e
            }
        }

        val resolvedDependencies = mutableSetOf<ResolvedDependency>()
        firstLevelDependencies
            .forEach { firstLevelDependency ->
                if (!firstLevelDependency.isModule()) {
                    resolvedDependencies.add(firstLevelDependency)
                    resolvedDependencies.traverseAndAddChildren(firstLevelDependency)
                }
            }
        return resolvedDependencies
    }

    private data class QueueElement(val children: Set<ResolvedDependency>)

    private fun MutableSet<ResolvedDependency>.traverseAndAddChildren(firstLevelDependency: ResolvedDependency) {
        val queue: Queue<QueueElement> = LinkedList()

        queue.offer(QueueElement(firstLevelDependency.children))

        while (queue.isNotEmpty()) {
            val element = queue.poll()
            element.children.forEach { child ->
                if (!child.isAndroidX()) {
                    add(child)
                    queue.offer(QueueElement(child.children))
                }
            }
        }
    }

    private fun ResolvedDependency.isModule(): Boolean =
        moduleGroup == project.rootProject.name

    private fun ResolvedDependency.isLegacyAndroidSupport(): Boolean =
        legacyGroupIdPrefixes.any { moduleGroup.startsWith(it) }
}
