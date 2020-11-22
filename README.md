# Bye Bye Jetifier Gradle Plugin
Gradle Plugin to verify if you can keep Android Jetifier disabled

## Features
This plugin verifies for each project dependency and its transitives if:
* any class is using a support library import
* any layout is referencing a support library class
* the Android Manifest is referencing a support library class

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
```

apply plugin: "com.dipien.byebyejetifier"

## Usage

To validate if your project has any dependency (including transitives) using the legacy android support library, you need to execute the following task:

    ./gradlew canISayByeByeJetifier

If you have any legacy android support library usage in your project, the task will fail and print a report with all the details. For example:

```
Scanning project: app
com/bumptech/glide/Glide.class -> android/support/v4/app/FragmentActivity
com/bumptech/glide/Glide.class -> android/support/v4/app/Fragment
com/bumptech/glide/manager/RequestManagerRetriever.class -> android/support/v4/app/FragmentManager
com/bumptech/glide/manager/RequestManagerRetriever.class -> android/support/v4/app/FragmentActivity
com/bumptech/glide/manager/RequestManagerRetriever.class -> android/support/v4/app/Fragment
com/bumptech/glide/manager/SupportRequestManagerFragment.class -> android/support/v4/app/Fragment
From: com.github.bumptech.glide:glide:3.8.0
```

If you don't have any legacy android support library the task will finish successfully, and you can remove the `android.enableJetifier` flag from your `gradle.properties`.

Once you disable jetifier, you don't want to add any dependency with the support library by mistake. So, I recommend to execute this task on your CI tools as part of the PR checks.

# Advanced configuration
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
    ignoredConfigurations = ["lintClassPath"]
    ignoredPackages = ["android.support.v4.media", "android.support.FILE_PROVIDER_PATHS"]
}
```

## Donations

Donations are greatly appreciated. You can help us to pay for our domain and the development.

* [Donate cryptocurrency](http://donations.dipien.com/)
* [Donate with PayPal](https://www.paypal.com/paypalme/maxirosson)

## Follow us
* [Twitter](https://twitter.com/dipien_)
* [Medium](https://medium.com/dipien)
