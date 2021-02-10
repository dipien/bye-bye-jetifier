package com.dipien.byebyejetifier.common

import java.io.File

fun String.toFilePath(): String {
    return this.replace('/', File.separatorChar)
}
