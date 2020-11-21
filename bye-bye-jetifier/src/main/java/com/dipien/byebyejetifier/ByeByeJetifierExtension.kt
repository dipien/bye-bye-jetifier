package com.dipien.byebyejetifier

import org.gradle.api.Project

open class ByeByeJetifierExtension(project: Project) {

    var legacyGroupIdPrefixes: List<String> = listOf("android.arch", "com.android.support")
    var ignoredConfigurations: List<String> = listOf("lintClassPath")
    var ignoredPackages: List<String> = listOf("android.support.v4.media", "android.support.FILE_PROVIDER_PATHS")
}
