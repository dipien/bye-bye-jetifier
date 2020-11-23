package com.dipien.byebyejetifier.common

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger

object LoggerHelper {

    lateinit var logger: Logger
    var verbose = false

    fun lifeCycle(message: String) {
        logger.log(LogLevel.LIFECYCLE, message)
    }

    fun error(message: String) {
        logger.log(LogLevel.ERROR, message)
    }

    fun warn(message: String) {
        logger.log(LogLevel.WARN, message)
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
