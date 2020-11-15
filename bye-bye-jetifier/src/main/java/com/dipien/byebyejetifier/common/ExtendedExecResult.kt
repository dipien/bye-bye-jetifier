package com.dipien.byebyejetifier.common

import java.io.ByteArrayOutputStream
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException

class ExtendedExecResult(
    private val execResult: ExecResult,
    private val standardOutputStream: ByteArrayOutputStream,
    private val errorOutputStream: ByteArrayOutputStream
) : ExecResult {

    fun isSuccessful(): Boolean {
        return exitValue == 0
    }

    fun getStandardOutput(): String {
        return standardOutputStream.toString()
    }

    fun getErrorOutput(): String {
        return errorOutputStream.toString()
    }

    override fun getExitValue(): Int {
        return execResult.exitValue
    }

    @Throws(ExecException::class)
    override fun assertNormalExitValue(): ExecResult {
        return execResult.assertNormalExitValue()
    }

    @Throws(ExecException::class)
    override fun rethrowFailure(): ExecResult {
        return execResult.rethrowFailure()
    }
}
