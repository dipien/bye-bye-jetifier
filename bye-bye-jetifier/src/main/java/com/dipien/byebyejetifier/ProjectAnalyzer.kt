package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.scanner.ScanResult
import com.dipien.byebyejetifier.scanner.ScannerProcessor
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolvedDependency
import java.util.LinkedList
import java.util.Queue

class ProjectAnalyzer(
    private val project: Project,
    private var excludedConfigurations: List<String>,
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
        val scanResultsCache = mutableMapOf<Archive, List<ScanResult>>()

        LoggerHelper.lifeCycle("")
        LoggerHelper.lifeCycle("=========================================")
        LoggerHelper.lifeCycle("Project: ${project.name}")
        LoggerHelper.lifeCycle("=========================================")

        val configurations = project.configurations
            .filter {
                !excludedConfigurations.contains(it.name)
            }
        LoggerHelper.log("Configurations to scan: $configurations")

        val externalDependencies = configurations
            .map {
                it.getExternalDependencies()
            }
            .flatten()
            .distinct()

        externalDependencies
            .map {
                if (!it.isAndroidX() && !it.isLegacyAndroidSupport()) {
                    it.moduleArtifacts
                } else {
                    emptySet()
                }
            }
            .flatten()
            .distinct()
            .forEach {
                val library = Archive.Builder.extract(it.file)
                scanResultsCache[library] = scannerProcessor.scanLibrary(library)
                hasExternalDependencies = true
            }

        scanResultsCache.forEach { (library, scanResults) ->
            if (scanResults.isNotEmpty()) {
                LoggerHelper.lifeCycle("")
                LoggerHelper.lifeCycle("Scanning ${library.artifactDefinition}")
                LoggerHelper.lifeCycle("${library.relativePath}")
                scanResults.forEach { scanResult ->
                    LoggerHelper.lifeCycle(" * ${scanResult.relativePath} -> ${scanResult.legacyDependency}")
                }
                thereAreSupportLibraryDependencies = true
            } else {
                LoggerHelper.info("")
                LoggerHelper.info("Scanning ${library.artifactDefinition}")
                LoggerHelper.info(" * No legacy android support usages found")
            }
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

        if (LoggerHelper.verbose) {
            if (!includeSupportLibrary && !hasExternalDependencies && !hasExternalDependencies) {
                LoggerHelper.info(" * No legacy android support usages found")
            }
        } else {
            if (!thereAreSupportLibraryDependencies && !includeSupportLibrary) {
                LoggerHelper.lifeCycle(" * No legacy android support usages found")
            }
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
                LoggerHelper.warn("Error when accessing configuration $name")
                throw e
            }
        }

        val resolvedDependencies = mutableSetOf<ResolvedDependency>()
        val projectDependencies = allDependencies.filterIsInstance(ProjectDependency::class.java)
        firstLevelDependencies
            .forEach { firstLevelDependency ->
                if (firstLevelDependency.isExternalDependency(projectDependencies)) {
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

    private fun ResolvedDependency.isExternalDependency(projectDependencies: List<ProjectDependency>): Boolean {
        return projectDependencies.none { projectDependency ->
            projectDependency.group == this.moduleGroup && projectDependency.name == this.moduleName
        }
    }

    private fun ResolvedDependency.isLegacyAndroidSupport(): Boolean =
        legacyGroupIdPrefixes.any { moduleGroup.startsWith(it) }
}
