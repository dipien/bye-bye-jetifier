package com.dipien.byebyejetifier.common

import org.apache.tools.ant.types.Commandline
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import java.io.ByteArrayOutputStream
import java.io.File

class CommandExecutor(private val project: Project, private val logLevel: LogLevel) {

    @Suppress("UNCHECKED_CAST")
    fun execute(command: String, workingDirectory: File? = project.rootProject.projectDir, logStandardOutput: Boolean = true, logErrorOutput: Boolean = true, ignoreExitValue: Boolean = false): ExtendedExecResult {
        log("Executing command: $command")

        val standardOutputStream = ByteArrayOutputStream()
        val errorOutputStream = ByteArrayOutputStream()

        val execResult = project.exec { execSpec ->
            if (workingDirectory != null) {
                execSpec.workingDir = workingDirectory
            }
            execSpec.setCommandLine(*Commandline.translateCommandline(command) as Array<Any>)
            execSpec.isIgnoreExitValue = true
            if (logStandardOutput) {
                execSpec.standardOutput = standardOutputStream
            }
            if (logErrorOutput) {
                execSpec.errorOutput = errorOutputStream
            }
        }
        if (standardOutputStream.size() > 0) {
            log(standardOutputStream.toString())
        }

        if (errorOutputStream.size() > 0) {
            project.logger.error(errorOutputStream.toString())
        }

        if (!ignoreExitValue && execResult.exitValue > 0) {
            throw RuntimeException("Failed execution of command: $command")
        }

        return ExtendedExecResult(execResult, standardOutputStream, errorOutputStream)
    }

    private fun log(message: String) {
        project.logger.log(logLevel, message)
    }
}
