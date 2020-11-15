plugins {
    id("com.gradle.enterprise").version("3.3.4")
}

include(":bye-bye-jetifier")

apply(from = java.io.File(settingsDir, "buildCacheSettings.gradle"))
