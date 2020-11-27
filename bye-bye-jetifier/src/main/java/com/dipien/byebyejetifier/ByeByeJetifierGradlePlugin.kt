package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.task.CanISayByeByeJetifierTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ByeByeJetifierGradlePlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "byeByeJetifier"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, ByeByeJetifierExtension::class.java)

        val canISayByeByeJetifierTask = project.tasks.create(CanISayByeByeJetifierTask.TASK_NAME, CanISayByeByeJetifierTask::class.java)
        project.afterEvaluate {
            canISayByeByeJetifierTask.legacyGroupIdPrefixes = extension.legacyGroupIdPrefixes
            canISayByeByeJetifierTask.legacyPackagesPrefixes = extension.legacyPackagesPrefixes
            canISayByeByeJetifierTask.excludedLegacyPackagesPrefixes = extension.excludedLegacyPackagesPrefixes
            canISayByeByeJetifierTask.excludedFilesFromScanning = extension.excludedFilesFromScanning
            canISayByeByeJetifierTask.excludedConfigurations = extension.excludedConfigurations
            canISayByeByeJetifierTask.verbose = extension.verbose
        }
    }
}
