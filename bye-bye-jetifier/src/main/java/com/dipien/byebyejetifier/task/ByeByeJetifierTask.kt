package com.dipien.byebyejetifier.task

import com.dipien.byebyejetifier.ProjectAnalyzer
import com.dipien.byebyejetifier.scanner.ScannerProcessor
import com.dipien.byebyejetifier.common.AbstractTask
import com.dipien.byebyejetifier.scanner.ScannerHelper
import com.dipien.byebyejetifier.scanner.bytecode.BytecodeScanner
import com.dipien.byebyejetifier.scanner.resource.XmlResourceScanner
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.lang.RuntimeException

open class ByeByeJetifierTask : AbstractTask() {

    companion object {
        const val TASK_NAME = "canISayByeByeJetifier"
    }

    @get:Input
    @get:Optional
    var legacyGroupIdPrefixes: List<String> = emptyList()

    @get:Input
    @get:Optional
    var legacyPackagesPrefixes: List<String> = emptyList()

    @get:Input
    @get:Optional
    var ignoredPackages: List<String> = emptyList()

    @get:Input
    @get:Optional
    var ignoredConfigurations: List<String> = emptyList()

    private val scannerProcessor by lazy {
        val scannerHelper = ScannerHelper(legacyPackagesPrefixes, ignoredPackages)
        val scannerList = listOf(BytecodeScanner(scannerHelper), XmlResourceScanner(scannerHelper))
        ScannerProcessor(scannerList)
    }

    override fun onExecute() {
        logger.debug("ignoredPackages: $ignoredPackages")
        logger.debug("ignoredConfigurations: $ignoredConfigurations")

        project.allprojects.forEach {
            ProjectAnalyzer(it, ignoredConfigurations, legacyGroupIdPrefixes, scannerProcessor).analyze()
        }

        if (scannerProcessor.thereAreSupportLibraryDependencies || scannerProcessor.includeSupportLibrary) {
            throw RuntimeException("You can not say Bye Bye Jetifier")
        } else {
            log("No dependencies with legacy android support usages! You can say Bye Bye Jetifier.")
        }
    }
}
