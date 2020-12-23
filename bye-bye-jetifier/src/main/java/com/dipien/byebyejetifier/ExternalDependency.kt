package com.dipien.byebyejetifier

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
}
