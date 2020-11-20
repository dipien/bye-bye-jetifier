package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.common.AbstractTask
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
            byeByeJetifierTask.ignoreImportsFilePath = extension.ignoreImportsFile
            byeByeJetifierTask.ignoreConfigsFilePath = extension.ignoreConfigsFile
        }
    }

    private fun initTask(task: AbstractTask) {
        task.logLevel = extension.logLevel
    }
}
