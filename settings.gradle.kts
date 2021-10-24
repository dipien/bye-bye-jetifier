plugins {
    id("com.gradle.enterprise").version("3.7.1")
}

if (System.getenv("CI") == "true") {
    buildCache {
        local {
            directory = File(System.getProperty("user.home"), "/gradle-build-cache")
        }
    }
}

include(":bye-bye-jetifier")
