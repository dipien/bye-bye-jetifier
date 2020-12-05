package com.dipien.byebyejetifier.core

import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.core.config.Config
import com.dipien.byebyejetifier.core.type.JavaType

class TypeRewriter(private val config: Config) {

    fun rewriteType(type: JavaType): JavaType? {
        val result = config.typesMap.mapType(type)
        if (result != null) {
            LoggerHelper.debug("Map: $type -> $result")
            return result
        }

        if (!config.isEligibleForRewrite(type)) {
            return type
        }

        val rulesResult = config.rulesMap.rewriteType(type)
        if (rulesResult != null) {
            LoggerHelper.debug("Using fallback: $type -> $rulesResult")
            return rulesResult
        }

        return null
    }
}
