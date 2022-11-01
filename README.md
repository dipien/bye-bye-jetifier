[![Dipien](https://raw.githubusercontent.com/dipien/dipien-component-builder/master/.github/dipien_logo.png)](http://www.dipien.com)

# Bye Bye Jetifier Gradle Plugin
Gradle Plugin to verify if you can keep Android Jetifier disabled

You can read more details about this plugin on this [article](https://medium.com/dipien/say-bye-bye-to-android-jetifier-a7e0d388f5d6).

## Features
This plugin verifies on each dependency JAR/AAR (and its transitives) if:
* any class is using a support library import
* any layout is referencing a support library class
* the Android Manifest is referencing a support library class

It also verifies if any support library dependency is resolved on the project.

#### Why should I use this plugin instead of can-i-drop-jetifier?

The [can-i-drop-jetifier](https://github.com/plnice/can-i-drop-jetifier) plugin only checks for legacy support libraries on the dependencies graph. That's not enough to decide if you can drop Jetifier. Lots of libraries don't properly declare on their POMs the legacy support libraries they use as transitive dependencies. So, for those cases, `can-i-drop-jetifier` says that you can disable Jetifier. But, if you do that, then you are going to have runtime errors when the logic using the legacy support library is executed.

`Bye bye Jetifier` inspects each JAR/AAR, searching for legacy support libraries usages, so it will find more libraries than `can-i-drop-jetifier`, and you will avoid those runtime errors.

## Setup

Add the following configuration to your root `build.gradle`, replacing X.Y.Z with the [latest version](https://github.com/dipien/bye-bye-jetifier/releases/latest)

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.dipien:bye-bye-jetifier:X.Y.Z")
    }
}

apply plugin: "com.dipien.byebyejetifier"
```

## Usage

To validate if your project dependencies (and its transitives) have any usage of the legacy android support library, you need to execute the following task:

    ./gradlew canISayByeByeJetifier -Pandroid.enableJetifier=false

If you have any legacy android support library usage, the task will fail and print a report with all the details. For example:

```
========================================
Project: app
========================================

Scanning com.squareup.rx.idler:rx2-idler:0.9.1
 Absolute path: ~/.gradle/caches/modules-2/files-2.1/com.squareup.rx.idler/rx2-idler/0.9.1/378e25e3c2f/rx2-idler-0.9.1.aar
 Graphs to this dependency:
 +---com.squareup.rx.idler:rx2-idler:0.9.1
 Issues found:
 * com/squareup/rx2/idler/DelegatingIdlingResourceScheduler.class -> android/support/test/espresso/IdlingResource$ResourceCallback
 * com/squareup/rx2/idler/DelegatingIdlingResourceScheduler.class -> android/support/test/espresso/IdlingResource
 * com/squareup/rx2/idler/IdlingResourceScheduler.class -> android/support/test/espresso/IdlingResource
 * com/squareup/rx2/idler/Rx2Idler$1.class -> android/support/test/espresso/IdlingResource
 * com/squareup/rx2/idler/Rx2Idler$1.class -> android/support/test/espresso/Espresso
 * pom -> com.android.support.test.espresso:espresso-core:2.2.2
 * pom -> com.android.support:support-annotations:25.4.0

Explicit declarations of legacy support dependencies on this project:
 * android.arch.core:common:1.1.1
 * android.arch.lifecycle:common:1.1.0

> Task :canISayByeByeJetifier FAILED
```

If you don't have any legacy android support library usages, the task will finish successfully, so it's safe to remove the `android.enableJetifier` flag from your `gradle.properties`.

Once you have disabled jetifier, you don't want to add a new support-library-dependent library by mistake when adding/upgrading a dependency on your project. To avoid that kind of issues, you can run the `canISayByeByeJetifier` task on your CI tool as part of the PR checks.

## Advanced configuration
You can configure the plugin using the `byeByeJetifier` extension. These are the default values for each property:

```groovy
byeByeJetifier {
    legacyGroupIdPrefixes = ["android.arch", "com.android.support"]
    excludedConfigurations = ["lintClassPath"]
    excludedFilesFromScanning = [
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
        
        // com.squareup.leakcanary:shark-android:2.5
        "shark/AndroidReferenceMatchers",

        // com.squareup.leakcanary:leakcanary-object-watcher-android-support-fragments:2.5
        "leakcanary/internal/AndroidSupportFragmentDestroyWatcher",
        
        // com.squareup.leakcanary:leakcanary-android:2.8.1
        "curtains/internal/WindowCallbackWrapper"
    ]
    excludedProjectsFromScanning = [] // Here you can define a list of Gradle project names to be excluded from the scanning analysis
    excludeSupportAnnotations = true
    verbose = false
}
```
## Versioning

This project uses the [Semantic Versioning guidelines](http://semver.org/) for transparency into our release cycle.

## Sponsor this project

Sponsor this open source project to help us get the funding we need to continue working on it.

* [Donate cryptocurrency](http://coinbase.dipien.com/)
* [Donate with credit card](http://kofi.dipien.com/)
* [Donate on Patreon](http://patreon.dipien.com/)
* [Become a member of Medium](https://membership.medium.dipien.com) [We will receive a portion of your membership fee]

## Follow us

* [Twitter](http://twitter.dipien.com)
* [Medium](http://medium.dipien.com)
* [Instagram](http://instagram.dipien.com)
* [TikTok](https://tiktok.dipien.com)
* [Pinterest](http://pinterest.dipien.com)
* [GitHub](http://github.dipien.com)
