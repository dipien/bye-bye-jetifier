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

    var excludedLegacyPackagesPrefixes: List<String> = listOf("android.support.v4.media", "android.support.FILE_PROVIDER_PATHS")

    var excludedConfigurations: List<String> = listOf("lintClassPath", "ktlint")

    var excludedFilesFromScanning: List<String> = listOf(
        // org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.20
        "org/jetbrains/kotlin/load/java/JvmAnnotationNamesKt",

        // org.jetbrains.kotlin:kotlin-reflect:1.4.20
        "kotlin/reflect/jvm/internal/impl/load/java/JvmAnnotationNamesKt",

        // org.jetbrains.kotlin:kotlin-android-extensions:1.4.20
        "org/jetbrains/kotlin/android/synthetic/AndroidConst",
        "org/jetbrains/kotlin/android/synthetic/codegen/AndroidIrTransformer",
        "org/jetbrains/kotlin/android/synthetic/codegen/ResourcePropertyStackValue",

        // org.robolectric:robolectric:4.4
        "org/robolectric/internal/AndroidConfigurer",

        // org.robolectric:sandbox:4.4
        "org/robolectric/internal/bytecode/InvocationProfile"
    )

    var verbose = false
}
