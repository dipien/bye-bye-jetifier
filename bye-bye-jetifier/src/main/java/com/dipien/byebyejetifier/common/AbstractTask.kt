package com.dipien.byebyejetifier.common

import com.dipien.byebyejetifier.ByeByeJetifierGradlePlugin
import com.dipien.byebyejetifier.ByeByeJetifierExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

abstract class AbstractTask : DefaultTask() {

    @get:Input
    @get:Optional
    var verbose = false

    @TaskAction
    fun doExecute() {

        LoggerHelper.logger = logger
        LoggerHelper.verbose = verbose

        onExecute()
    }

    @Internal
    protected fun getExtension(): ByeByeJetifierExtension {
        return project.extensions.getByName(ByeByeJetifierGradlePlugin.EXTENSION_NAME) as ByeByeJetifierExtension
    }

    protected abstract fun onExecute()
}
