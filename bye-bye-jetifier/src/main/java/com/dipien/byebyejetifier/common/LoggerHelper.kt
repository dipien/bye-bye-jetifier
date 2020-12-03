package com.dipien.byebyejetifier.common

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger

object LoggerHelper {

    lateinit var logger: Logger
    var verbose = false

    fun debug(message: String) {
        logger.log(getLogLevel(LogLevel.DEBUG), message)
    }

    fun info(message: String) {
        logger.log(getLogLevel(LogLevel.INFO), message)
    }

    fun lifeCycle(message: String) {
        logger.log(LogLevel.LIFECYCLE, message)
    }

    fun warn(message: String, tag: String = "") {
        logger.log(LogLevel.WARN, "[WARNING] [$tag] $message")
    }

    fun quiet(message: String) {
        logger.log(LogLevel.QUIET, message)
    }

    fun error(message: String, tag: String = "") {
        logger.log(LogLevel.ERROR, "[ERROR] [$tag] $message")
    }

    fun log(message: String, logLevel: LogLevel = LogLevel.INFO) {
        logger.log(getLogLevel(logLevel), message)
    }

    fun log(message: String, throwable: Throwable, logLevel: LogLevel = LogLevel.INFO) {
        logger.log(getLogLevel(logLevel), message, throwable)
    }

    private fun getLogLevel(logLevel: LogLevel): LogLevel {
        return if (verbose) {
            if (logLevel.ordinal < LogLevel.LIFECYCLE.ordinal) {
                LogLevel.LIFECYCLE
            } else {
                logLevel
            }
        } else {
            logLevel
        }
    }
}
