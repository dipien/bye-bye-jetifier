package com.dipien.byebyejetifier.common

import org.gradle.api.Project

val Project.propertyResolver: PropertyResolver
    get() = PropertyResolver(this)
