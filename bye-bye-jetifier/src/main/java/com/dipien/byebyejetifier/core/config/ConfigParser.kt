package com.dipien.byebyejetifier.core.config

import com.dipien.byebyejetifier.common.LoggerHelper
import com.google.gson.GsonBuilder
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

object ConfigParser {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun loadFromFile(configInputStream: InputStream): Config {
        return parseFromString(readText(configInputStream)) ?: throw RuntimeException("Failed to parseFromString the config file")
    }

    private fun parseFromString(inputText: String): Config? {
        LoggerHelper.info("Parsing config file")
        return gson.fromJson(inputText, Config.JsonData::class.java).toConfig()
    }

    private fun readText(configInputStream: InputStream): String {
        val bufferSize = 1024
        val buffer = CharArray(bufferSize)
        val output = StringBuilder()
        val input: Reader = InputStreamReader(configInputStream, StandardCharsets.UTF_8)
        var charsRead: Int
        while (input.read(buffer, 0, buffer.size).also { charsRead = it } > 0) {
            output.append(buffer, 0, charsRead)
        }
        return output.toString()
    }
}
