package com.dipien.byebyejetifier

open class ByeByeJetifierExtension {

    var legacyGroupIdPrefixes: List<String> = listOf("android.arch", "com.android.support")

    // https://developer.android.com/jetpack/androidx/migrate/class-mappings#androidsupport
    var legacyPackagesPrefixes: List<String> = listOf(
        "com.executor",
        "lifecycle",
        "paging",
        "persistence.db",
        "persistence.room",
        "android.databinding",
        "android.support",
        "android.test.espresso"
    )
    var ignoredConfigurations: List<String> = listOf("lintClassPath")

    var ignoredPackages: List<String> = listOf("android.support.v4.media", "android.support.FILE_PROVIDER_PATHS")
}
