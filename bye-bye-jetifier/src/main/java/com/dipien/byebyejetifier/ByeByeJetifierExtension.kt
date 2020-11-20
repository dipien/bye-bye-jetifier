package com.dipien.byebyejetifier

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

open class ByeByeJetifierExtension(project: Project) {

    var logLevel = LogLevel.LIFECYCLE

    var ignoreImportsFile: String? = null
    var ignoreConfigsFile: String? = null
}
