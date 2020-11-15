package com.dipien.byebyejetifier

import com.dipien.byebyejetifier.common.AbstractTask
import com.dipien.byebyejetifier.task.SampleTask
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

        val sampleTask = project.tasks.create(SampleTask.TASK_NAME, SampleTask::class.java)
        project.afterEvaluate {
            initTask(sampleTask)
        }
    }

    private fun initTask(task: AbstractTask) {
        task.logLevel = extension.logLevel
    }
}
