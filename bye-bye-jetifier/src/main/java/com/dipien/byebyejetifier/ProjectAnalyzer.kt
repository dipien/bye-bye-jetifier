package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.common.toFilePath
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
    private val scannerProcessor: ScannerProcessor,
    private val excludeSupportAnnotations: Boolean
) {

    companion object {
        val ANDROID_SUPPORT_ANNOTATION_PATH = "android/support/annotation".toFilePath()
        const val ANDROID_SUPPORT_ANNOTATION_PACKAGE = "android.support.annotation"
    }

    fun analyze(projectAnalyzerResult: ProjectAnalyzerResult) {

        var includeSupportLibrary = false
        var thereAreSupportLibraryDependencies = false
        var hasExternalDependencies = false
        val projectScanResult = mutableMapOf<ExternalDependency, List<ScanResult>>()

        LoggerHelper.lifeCycle("")
        LoggerHelper.lifeCycle("=========================================")
        LoggerHelper.lifeCycle("Project: ${project.name}")
        LoggerHelper.lifeCycle("=========================================")

        val configurations = project.configurations
            .filter {
                !excludedConfigurations.contains(it.name)
            }
        LoggerHelper.log("Configurations to scan: $configurations")

        val legacyArtifactsFirstLevel = mutableListOf<ExternalDependency>()
        val allExternalDependencies = configurations
            .map {
                it.getExternalDependencies()
            }
            .flatten()

        allExternalDependencies
            // Gradle does not resolve the entire tree of dependencies for each Configuration.
            // Then an artifact can appear in an Configurations without children and in other with children.
            .groupBy { it.artifactDefinition }
            .forEach { (_, groupedExtDependencies) ->
                val externalDependency = groupedExtDependencies.first()

                if (!externalDependency.isAndroidX && !externalDependency.isLegacyAndroidSupport) {
                    val result = mutableListOf<ScanResult>()

                    externalDependency.moduleArtifacts.forEach {
                        hasExternalDependencies = true
                        val library = Archive.Builder.extract(it.file)
                        result.addAll(projectAnalyzerResult.scanResultsCache.getOrPut(library.relativePath) { filterSupportAnnotationsIfNeeded(scannerProcessor.scanLibrary(library)) })
                    }

                    groupedExtDependencies
                        .map { it.children }
                        .flatten()
                        .distinct()
                        .forEach {
                            if (it.isLegacyAndroidSupport) {
                                includeSupportLibrary = true
                                result.add(ScanResult("pom", it.artifactDefinition))
                            }
                    }

                    projectScanResult[externalDependency] = result
                } else if (externalDependency.isFirstLevel && externalDependency.isLegacyAndroidSupport) {
                    legacyArtifactsFirstLevel.add(externalDependency)
                }
            }

        projectScanResult.forEach { (dependencyToReport, scanResults) ->
            if (scanResults.isNotEmpty()) {
                LoggerHelper.lifeCycle("")
                LoggerHelper.lifeCycle("Scanning ${dependencyToReport.artifactDefinition}")

                // Artifact paths
                dependencyToReport.moduleArtifacts.forEach {
                    LoggerHelper.lifeCycle(" Absoulute path: ${it.file}")
                }

                // All possible graphs
                LoggerHelper.lifeCycle(" Graphs to this dependency:")
                allExternalDependencies
                    .filter { it.artifactDefinition == dependencyToReport.artifactDefinition }
                    .map { it.buildDependencyGraphs() }
                    .flatten()
                    .distinctBy { graph -> graph.toString() }
                    .forEach { it.print() }

                // Issues found
                LoggerHelper.lifeCycle(" Issues found:")
                scanResults.forEach { scanResult ->
                    LoggerHelper.lifeCycle(" * ${scanResult.relativePath} -> ${scanResult.legacyDependency}")
                }

                thereAreSupportLibraryDependencies = true
            } else {
                LoggerHelper.info("")
                LoggerHelper.info("Scanning ${dependencyToReport.artifactDefinition}")
                LoggerHelper.info(" * No legacy android support usages found")
            }
        }

        if (legacyArtifactsFirstLevel.isNotEmpty()) {
            includeSupportLibrary = true
            LoggerHelper.lifeCycle("")
            LoggerHelper.lifeCycle("Explicit declarations of legacy support dependencies on this project:")
            legacyArtifactsFirstLevel.sortedBy { it.artifactDefinition }.forEach {
                LoggerHelper.lifeCycle(" * ${it.artifactDefinition}")
            }
        }

        if (LoggerHelper.verbose) {
            if (!includeSupportLibrary && !hasExternalDependencies) {
                LoggerHelper.info(" * No legacy android support usages found")
            }
        } else {
            if (!thereAreSupportLibraryDependencies && !includeSupportLibrary) {
                LoggerHelper.lifeCycle(" * No legacy android support usages found")
            }
        }

        if (includeSupportLibrary) {
            projectAnalyzerResult.includeSupportLibrary = true
        }
        if (thereAreSupportLibraryDependencies) {
            projectAnalyzerResult.thereAreSupportLibraryDependencies = true
        }
    }

    private fun filterSupportAnnotationsIfNeeded(results: List<ScanResult>): List<ScanResult> {
        if (excludeSupportAnnotations) {
            return results.filter { scanResult ->
                !scanResult.legacyDependency.startsWith(ANDROID_SUPPORT_ANNOTATION_PATH) &&
                    !scanResult.legacyDependency.startsWith(ANDROID_SUPPORT_ANNOTATION_PACKAGE)
            }
        }
        return results
    }

    private fun Configuration.getExternalDependencies(): Set<ExternalDependency> {
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

        val resolvedDependencies = mutableSetOf<ExternalDependency>()
        val projectDependencies = allDependencies.filterIsInstance(ProjectDependency::class.java)
        firstLevelDependencies
            .forEach { firstLevelDependency ->
                if (firstLevelDependency.isExternalDependency(projectDependencies)) {
                    val externalDependencyFirstLevel = ExternalDependency.FirstLevel(firstLevelDependency, legacyGroupIdPrefixes)
                    resolvedDependencies.add(externalDependencyFirstLevel)
                    resolvedDependencies.traverseAndAddChildren(externalDependencyFirstLevel)
                }
            }
        return resolvedDependencies
    }

    private data class QueueElement(val children: List<ExternalDependency>)

    private fun MutableSet<ExternalDependency>.traverseAndAddChildren(firstLevelDependency: ExternalDependency) {
        val queue: Queue<QueueElement> = LinkedList()

        queue.offer(QueueElement(firstLevelDependency.children))

        while (queue.isNotEmpty()) {
            val element = queue.poll()
            element.children.forEach { child ->
                if (!child.isAndroidX) {
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
}
