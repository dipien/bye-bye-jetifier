package com.dipien.byebyejetifier.common

import org.gradle.api.Project

class PropertyResolver(private val project: Project) {

    private fun getProp(propertyName: String, defaultValue: Any? = null): Any? {
        return if (project.hasProperty(propertyName)) {
            project.property(propertyName)
        } else if (System.getenv().containsKey(propertyName)) {
            System.getenv(propertyName)
        } else {
            defaultValue
        }
    }

    fun hasProp(propertyName: String): Boolean {
        return project.hasProperty(propertyName) || System.getenv().containsKey(propertyName)
    }

    fun getStringProp(propertyName: String, defaultValue: String? = null): String? {
        val value = getProp(propertyName)
        return value?.toString() ?: defaultValue
    }

    fun getIntegerProp(propertyName: String, defaultValue: Int? = null): Int? {
        val value = getProp(propertyName)
        return if (value == null) {
            defaultValue
        } else {
            Integer.parseInt(value.toString())
        }
    }

    fun getLongProp(propertyName: String, defaultValue: Long? = null): Long? {
        val value = getProp(propertyName)
        return if (value == null) {
            defaultValue
        } else {
            java.lang.Long.parseLong(value.toString())
        }
    }

    fun getDoubleProp(propertyName: String, defaultValue: Double? = null): Double? {
        val value = getProp(propertyName)
        return if (value == null) {
            defaultValue
        } else {
            java.lang.Double.parseDouble(value.toString())
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringListProp(propertyName: String, defaultValue: List<String>? = null): List<String>? {
        val value = getProp(propertyName)
        return if (value == null || value.toString().isEmpty()) {
            defaultValue
        } else {
            value as? List<String> ?: value.toString().split(",")
        }
    }

    fun getRequiredStringListProp(propertyName: String, defaultValue: List<String>): List<String> {
        return getStringListProp(propertyName, defaultValue) ?: defaultValue
    }
}
