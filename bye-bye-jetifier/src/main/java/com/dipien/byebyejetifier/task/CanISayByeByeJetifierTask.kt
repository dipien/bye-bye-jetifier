package com.dipien.byebyejetifier.task

import com.dipien.byebyejetifier.ProjectAnalyzer
import com.dipien.byebyejetifier.ProjectAnalyzerResult
import com.dipien.byebyejetifier.scanner.ScannerProcessor
import com.dipien.byebyejetifier.common.AbstractTask
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.core.config.ConfigParser
import com.dipien.byebyejetifier.scanner.ScannerContext
import com.dipien.byebyejetifier.scanner.bytecode.BytecodeScanner
import com.dipien.byebyejetifier.scanner.resource.XmlResourceScanner
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.lang.RuntimeException

open class CanISayByeByeJetifierTask : AbstractTask() {

    companion object {
        const val TASK_NAME = "canISayByeByeJetifier"
        const val ENABLE_JETIFIER_PROPERTY = "android.enableJetifier"

        private const val DEFAULT_CONFIG = "default.config"
    }

    @get:Input
    @get:Optional
    var legacyGroupIdPrefixes: List<String> = emptyList()

    @get:Input
    @get:Optional
    var excludedConfigurations: List<String> = emptyList()

    @get:Input
    @get:Optional
    var excludedFilesFromScanning: List<String> = emptyList()

    @get:Input
    @get:Optional
    var excludeSupportAnnotations = false

    private val scannerProcessor by lazy {
        val inputStream = javaClass.classLoader.getResourceAsStream(DEFAULT_CONFIG)
        val config = ConfigParser.loadFromFile(inputStream)
        val scannerContext = ScannerContext(config, excludedFilesFromScanning)
        val scannerList = listOf(BytecodeScanner(scannerContext), XmlResourceScanner(scannerContext))
        ScannerProcessor(scannerContext, scannerList)
    }

    init {
        group = "Verification"
        description = "Verifies if you can keep Android Jetifier disabled"
    }

    override fun onExecute() {

        if (project.hasProperty(ENABLE_JETIFIER_PROPERTY) && project.property(ENABLE_JETIFIER_PROPERTY) == "true") {
            throw GradleException("This task needs to be run with Jetifier disabled: ./gradlew $TASK_NAME -P$ENABLE_JETIFIER_PROPERTY=false")
        }

        LoggerHelper.log("excludedConfigurations: $excludedConfigurations")
        LoggerHelper.log("excludedFilesFromScanning: $excludedFilesFromScanning")
        LoggerHelper.log("excludeSupportAnnotations: $excludeSupportAnnotations")

        project.allprojects.forEach {
            ProjectAnalyzer(it, excludedConfigurations, legacyGroupIdPrefixes, scannerProcessor, excludeSupportAnnotations).analyze()
        }

        if (ProjectAnalyzerResult.thereAreSupportLibraryDependencies || ProjectAnalyzerResult.includeSupportLibrary) {
            throw RuntimeException("You can not say Bye Bye Jetifier")
        } else {
            LoggerHelper.lifeCycle("")
            LoggerHelper.lifeCycle("=====================================================================================")
            LoggerHelper.lifeCycle("* No dependencies with legacy android support usages! You can say Bye Bye Jetifier. *")
            LoggerHelper.lifeCycle("=====================================================================================")
        }
    }
}
