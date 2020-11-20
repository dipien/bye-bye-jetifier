package com.dipien.byebyejetifier.scanner

import java.io.File

object IgnoreFileUtil {

    fun loadIgnoreFile(file: String, target: MutableSet<String>) {
        val ignoreFile = File(file)
        if (ignoreFile.exists()) {
            ignoreFile.forEachLine { ignoredFile ->
                if (!ignoredFile.startsWith("#") && ignoredFile.isNotEmpty()) {
                    target.add(ignoredFile)
                }
            }
        }
    }
}
