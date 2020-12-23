package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.scanner.ScanResult
import com.dipien.byebyejetifier.scanner.ScannerProcessor
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolvedArtifact
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
        private const val ANDROIDX_GROUP_ID_PREFIX = "androidx"
    }

    fun analyze(projectAnalyzerResult: ProjectAnalyzerResult) {

        var includeSupportLibrary = false
        var thereAreSupportLibraryDependencies = false
        var hasExternalDependencies = false
        val projectScanResult = mutableMapOf<ExternalDependency, MutableList<ScanResult>>()

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
        configurations
            .map {
                it.getExternalDependencies()
            }
            .flatten()
            .distinctBy { it.artifactDefinition }
            .forEach { externalDependency ->
                if (!externalDependency.isAndroidX && !externalDependency.isLegacyAndroidSupport) {
                    val result = mutableListOf<ScanResult>()

                    externalDependency.moduleArtifacts.forEach {
                        hasExternalDependencies = true
                        val library = Archive.Builder.extract(it.file)
                        var scanResults = scannerProcessor.scanLibrary(library)
                        scanResults = filterSupportAnnotationsIfNeeded(scanResults)
                        result.addAll(scanResults)
                    }

                    externalDependency.children.forEach {
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

        projectScanResult.forEach { (resolvedDependency, scanResults) ->
            if (scanResults.isNotEmpty()) {
                LoggerHelper.lifeCycle("")
                LoggerHelper.lifeCycle("Scanning ${resolvedDependency.artifactDefinition}")
                resolvedDependency.moduleArtifacts.forEach {
                    LoggerHelper.lifeCycle("${it.file}")
                }
                scanResults.forEach { scanResult ->
                    LoggerHelper.lifeCycle(" * ${scanResult.relativePath} -> ${scanResult.legacyDependency}")
                }
                thereAreSupportLibraryDependencies = true
            } else {
                LoggerHelper.info("")
                LoggerHelper.info("Scanning ${resolvedDependency.artifactDefinition}")
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
                !scanResult.legacyDependency.startsWith("android/support/annotation") &&
                    !scanResult.legacyDependency.startsWith("android.support.annotation")
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

    private sealed class ExternalDependency(
        private val dependency: ResolvedDependency,
        private val legacyGroupIdPrefixes: List<String>
    ) {

        data class FirstLevel(
            val dependency: ResolvedDependency,
            val legacyGroupIdPrefixes: List<String>
        ) : ExternalDependency(dependency, legacyGroupIdPrefixes)

        data class Child(
            val dependency: ResolvedDependency,
            val legacyGroupIdPrefixes: List<String>
        ) : ExternalDependency(dependency, legacyGroupIdPrefixes)

        val artifactDefinition: String by lazy {
            "${dependency.moduleGroup}:${dependency.moduleName}:${dependency.moduleVersion}"
        }

        val isLegacyAndroidSupport: Boolean by lazy {
            legacyGroupIdPrefixes.any { dependency.moduleGroup.startsWith(it) }
        }

        val moduleArtifacts: List<ResolvedArtifact> by lazy {
            dependency.moduleArtifacts.toList()
        }

        val children: List<ExternalDependency> by lazy {
            dependency.children.map { Child(it, legacyGroupIdPrefixes) }
        }

        val isFirstLevel: Boolean by lazy {
            this is FirstLevel
        }

        val isAndroidX: Boolean by lazy {
            dependency.moduleGroup.startsWith(ANDROIDX_GROUP_ID_PREFIX)
        }
    }
}
