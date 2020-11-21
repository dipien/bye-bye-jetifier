package com.dipien.byebyejetifier.task

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.scanner.ScannerProcessor
import com.dipien.byebyejetifier.common.AbstractTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.lang.RuntimeException
import java.util.LinkedList
import java.util.Queue

open class ByeByeJetifierTask : AbstractTask() {

    companion object {
        const val TASK_NAME = "byeByeJetifier"

        private const val ANDROIDX_GROUP_ID_PREFIX = "androidx"

        // https://developer.android.com/jetpack/androidx/migrate/class-mappings#androidsupport
        private val OLD_CLASSES_PREFIXES = listOf(
                "com.executor",
                "lifecycle",
                "paging",
                "persistence.db",
                "persistence.room",
                "android.databinding",
                "android.support",
                "android.test.espresso"
        )
    }

    @get:Input
    @get:Optional
    var legacyGroupIdPrefixes: List<String> = emptyList()

    @get:Input
    @get:Optional
    var ignoredPackages: List<String> = emptyList()

    @get:Input
    @get:Optional
    var ignoredConfigurations: List<String> = emptyList()

    private val scannerProcessor by lazy {
        ScannerProcessor(logger, OLD_CLASSES_PREFIXES, ignoredPackages)
    }

    private var includeSupportLibrary = false

    override fun onExecute() {
        logger.lifecycle("ignoredPackages: $ignoredPackages")
        logger.lifecycle("ignoredConfigurations: $ignoredConfigurations")

        project.allprojects.forEach {
            log("Scanning project: ${it.name}")
            it.doAnalyze()
        }

        if (scannerProcessor.thereAreSupportLibraryDependencies || includeSupportLibrary) {
            throw RuntimeException("You can not say Bye Bye Jetifier")
        } else {
            log("No dependencies with legacy android support usages! You can say Bye Bye Jetifier.")
        }
    }

    private fun Project.doAnalyze() {
        val externalDependencies = configurations
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
                    if (!it.isAndroidX() && !it.isOldArtifact()) {
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
                    if (it.isOldArtifact()) {
                        it
                    } else {
                        null
                    }
                }
                .forEach {
                    if (!includeSupportLibrary) {
                        includeSupportLibrary = true
                    }
                    log("Old artifact: " + it.name)
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

    private fun ResolvedDependency.isOldArtifact(): Boolean =
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
