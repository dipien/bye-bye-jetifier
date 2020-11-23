package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.scanner.ScannerProcessor
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
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
        LoggerHelper.log("========================================")
        LoggerHelper.log("Project: ${project.name}")
        LoggerHelper.log("========================================")
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
                if (!it.isAndroidX() && !it.isLegacyArtifact()) {
                    it.toResolvedArtifactSet()
                } else {
                    emptySet()
                }
            }
            .flatten()
            .distinct()
            .forEach {
                val aar = it.file
                val library = Archive.Builder.extract(aar)
                scannerProcessor.scanLibrary(library)
            }

        externalDependencies
            .mapNotNull {
                if (it.isLegacyArtifact()) {
                    it
                } else {
                    null
                }
            }
            .forEach {
                scannerProcessor.includeSupportLibrary = true
                LoggerHelper.log("Old artifact: " + it.name)
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
        } catch (ignored: Throwable) {
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

    private fun ResolvedDependency.isLegacyArtifact(): Boolean =
        legacyGroupIdPrefixes.any { moduleGroup.startsWith(it) }

    private fun ResolvedDependency.toResolvedArtifactSet(): Set<ResolvedArtifact> {
        return when {
            isModule() ->
                emptySet()
            else -> {
                // External dependency
                moduleArtifacts
            }
        }
    }
}
