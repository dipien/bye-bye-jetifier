package com.dipien.byebyejetifier

open class ByeByeJetifierExtension {

    var legacyGroupIdPrefixes: List<String> = listOf("android.arch", "com.android.support")

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

        // org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.10
        "org/jetbrains/kotlin/com/intellij/codeInsight/NullableNotNullManager",

        // com.facebook.android:facebook-core:8.1.0
        "com/facebook/appevents/codeless/internal/ViewHierarchy",

        // com.squareup.leakcanary:shark-android:2.5
        "shark/AndroidReferenceMatchers",

        // com.squareup.leakcanary:leakcanary-object-watcher-android-support-fragments:2.5
        "leakcanary/internal/AndroidSupportFragmentDestroyWatcher"
    )

    var excludedProjectsFromScanning: List<String> = emptyList()

    var excludeSupportAnnotations = true

    var verbose = false
}
