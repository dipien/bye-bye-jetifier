package com.dipien.byebyejetifier.common

import com.dipien.byebyejetifier.ByeByeJetifierGradlePlugin
import com.dipien.byebyejetifier.ByeByeJetifierExtension
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class AbstractTask : DefaultTask() {

    @get:Internal
    var logLevel: LogLevel = LogLevel.LIFECYCLE

    @get:Internal
    protected lateinit var commandExecutor: CommandExecutor

    init {
        group = "Bye Bye Jetifier"
    }

    @TaskAction
    fun doExecute() {

        LoggerHelper.logger = logger
        LoggerHelper.logLevel = logLevel

        commandExecutor = CommandExecutor(project, logLevel)
        onExecute()
    }

    @Internal
    protected fun getExtension(): ByeByeJetifierExtension {
        return project.extensions.getByName(ByeByeJetifierGradlePlugin.EXTENSION_NAME) as ByeByeJetifierExtension
    }

    protected fun log(message: String) {
        LoggerHelper.log(message)
    }

    protected abstract fun onExecute()
}
