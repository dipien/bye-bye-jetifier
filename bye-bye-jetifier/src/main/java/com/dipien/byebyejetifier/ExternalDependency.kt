package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.common.LoggerHelper
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency

sealed class ExternalDependency(
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
        dependency.moduleGroup.startsWith("androidx")
    }

    fun buildDependencyGraphs(): List<DependencyGraph> {
        return recursiveBuildDependencyGraphs(dependency, DependencyGraph(mutableListOf()))
    }

    private fun recursiveBuildDependencyGraphs(dependency: ResolvedDependency, graph: DependencyGraph): List<DependencyGraph> {
        return if (dependency.parents.isEmpty()) {
            // Root is ignored because it is a project module
            listOf(graph)
        } else {
            val result = mutableListOf<DependencyGraph>()
            graph.add(dependency.name)
            for (parent in dependency.parents) {
                val newGraph = graph.clone()
                result.addAll(recursiveBuildDependencyGraphs(parent, newGraph))
            }
            result
        }
    }

    data class DependencyGraph(private val nodes: MutableList<String>) {

        fun add(node: String) {
            nodes.add(node)
        }

        fun clone(): DependencyGraph =
            DependencyGraph(nodes.toMutableList())

        fun print() {
            for ((level, node) in nodes.reversed().withIndex()) {
                val separator: String = if (level == 0) " " else "     ".repeat(level)
                LoggerHelper.lifeCycle("$separator+---$node")
            }
        }

        override fun toString(): String {
            return "DependencyGraph(nodes=$nodes)"
        }
    }
}
