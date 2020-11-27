# Bye Bye Jetifier Gradle Plugin
Gradle Plugin to verify if you can keep Android Jetifier disabled

## Features
This plugin verifies on each dependency JAR/AAR (and its transitives) if:
* any class is using a support library import
* any layout is referencing a support library class
* the Android Manifest is referencing a support library class

It also verifies if any support library dependency is resolved on the project.

## Setup

Add the following configuration to your root `build.gradle`, replacing X.Y.Z by the [latest version](https://github.com/dipien/bye-bye-jetifier/releases/latest)

```groovy
buildscript {
    repositories {
        mavenCentral() // or gradlePluginPortal()
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

Scanning com.github.bumptech.glide:glide:3.8.0
 * com/bumptech/glide/Glide.class -> android/support/v4/app/FragmentActivity
 * com/bumptech/glide/Glide.class -> android/support/v4/app/Fragment
 * com/bumptech/glide/manager/RequestManagerRetriever.class -> android/support/v4/app/FragmentManager
 * com/bumptech/glide/manager/RequestManagerRetriever.class -> android/support/v4/app/FragmentActivity
 * com/bumptech/glide/manager/RequestManagerRetriever.class -> android/support/v4/app/Fragment
 * com/bumptech/glide/manager/SupportRequestManagerFragment.class -> android/support/v4/app/Fragment

Legacy support dependencies:
 * com.android.support:support-annotations:28.0.0

> Task :canISayByeByeJetifier FAILED
```

If you don't have any legacy android support library usages, the task will finish successfully, so it's safe to remove the `android.enableJetifier` flag from your `gradle.properties`.

Once you have disabled jetifier, you don't want to add a new support-library-dependent library by mistake when adding/upgrading a dependency on your project. To avoid that kind of issues, you can run the `canISayByeByeJetifier` task on your CI tool as part of the PR checks.

## Advanced configuration
You can configure the plugin using the `byeByeJetifier` extension. These are the default values for each property:

```groovy
byeByeJetifier {
    legacyGroupIdPrefixes = ["android.arch", "com.android.support"]
    legacyPackagesPrefixes = [
        "com.executor",
        "lifecycle",
        "paging",
        "persistence.db",
        "persistence.room",
        "android.databinding",
        "android.support",
        "android.test.espresso"
    ]
    excludedConfigurations = ["lintClassPath"]
    excludedLegacyPackagesPrefixes = ["android.support.v4.media", "android.support.FILE_PROVIDER_PATHS"]
    excludedFilesFromScanning = [
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
    ]
    verbose = false
}
```

## Donations

Donations are greatly appreciated. You can help us to pay for our domain and the plugin development.

* [Donate cryptocurrency](http://donations.dipien.com/)
* [Donate with PayPal](https://www.paypal.com/paypalme/maxirosson)

## Follow us
* [Twitter](https://twitter.com/dipien_)
* [Medium](https://medium.com/dipien)
