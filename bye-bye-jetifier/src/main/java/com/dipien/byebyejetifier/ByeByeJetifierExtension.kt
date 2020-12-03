package com.dipien.byebyejetifier

open class ByeByeJetifierExtension {

    var legacyGroupIdPrefixes: List<String> = listOf("android.arch", "com.android.support")

    var excludedConfigurations: List<String> = listOf("lintClassPath", "ktlint")

    var verbose = false
}
