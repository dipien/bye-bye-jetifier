package com.dipien.byebyejetifier.task

import com.dipien.byebyejetifier.archive.Archive
import com.dipien.byebyejetifier.scanner.IgnoreFileUtil
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

        private val ANDROIDX_MODULES_PREFIXES = listOf("androidx")

        private val OLD_MODULES_PREFIXES = listOf(
                "android.arch",
                "com.android.support"
        )

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
    var ignoreImportsFilePath: String? = null

    @get:Input
    @get:Optional
    var ignoreConfigsFilePath: String? = null

    private val scannerProcessor by lazy {
        ScannerProcessor(logger, OLD_CLASSES_PREFIXES, ignoreImportsFilePath)
    }

    private val ignoredConfigurations by lazy {
        val ignored = mutableSetOf(
                "lintClassPath"
        )
        ignoreImportsFilePath?.let {
            IgnoreFileUtil.loadIgnoreFile(it, ignored)
        }
        ignored
    }

    private var includeSupportLibrary = false

    override fun onExecute() {
        logger.lifecycle("ignoreImportsFilePath: $ignoreImportsFilePath")
        logger.lifecycle("ignoreConfigurationsFilePath: $ignoreConfigsFilePath")

        project.allprojects.forEach {
            log("")
            log("Scanning module: ${it.name}")
            it.doAnalyze()
        }

        if (scannerProcessor.thereAreSupportLibraryDependencies || includeSupportLibrary) {
            throw RuntimeException("You can not say Bye Bye Jetifier")
        } else {
            log("No dependencies on old artifacts! You can say Bye Bye Jetifier.")
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
        return ANDROIDX_MODULES_PREFIXES.any { moduleGroup.startsWith(it) }
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
            OLD_MODULES_PREFIXES.any { moduleGroup.startsWith(it) }

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
