package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.task.ByeByeJetifierTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ByeByeJetifierGradlePlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "byeByeJetifier"
    }

    lateinit var extension: ByeByeJetifierExtension
        private set

    override fun apply(project: Project) {
        extension = project.extensions.create(EXTENSION_NAME, ByeByeJetifierExtension::class.java, project)

        val byeByeJetifierTask = project.tasks.create(ByeByeJetifierTask.TASK_NAME, ByeByeJetifierTask::class.java)
        project.afterEvaluate {
            byeByeJetifierTask.legacyGroupIdPrefixes = extension.legacyGroupIdPrefixes
            byeByeJetifierTask.ignoredPackages = extension.ignoredPackages
            byeByeJetifierTask.ignoredConfigurations = extension.ignoredConfigurations
        }
    }
}
