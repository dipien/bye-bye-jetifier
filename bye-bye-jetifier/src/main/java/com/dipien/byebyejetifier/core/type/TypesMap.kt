package com.dipien.byebyejetifier.core.type

import java.util.SortedMap

/**
 * Contains all the mappings needed to rewrite java types.
 */
data class TypesMap(private val types: Map<JavaType, JavaType>) {

    companion object {
        val EMPTY = TypesMap(emptyMap())
    }

    init {
        val containsNestedTypes = types.any { it.key.hasInnerType() || it.value.hasInnerType() }
        if (containsNestedTypes) {
            throw IllegalArgumentException("Types map does not support nested types!")
        }
    }

    /** Maps the given type using this map. */
    fun mapType(type: JavaType): JavaType? {
        if (type.hasInnerType()) {
            val rootMapResult = types[type.getRootType()] ?: return null
            return type.remapWithNewRootType(rootMapResult)
        }
        return types[type]
    }

    /**
     * JSON data model for [TypesMap].
     */
    data class JsonData(val types: SortedMap<String, String>) {

        /** Creates instance of [TypesMap] */
        fun toMappings(): TypesMap {
            return TypesMap(
                types = types
                    .orEmpty()
                    .map { JavaType(it.key) to JavaType(it.value) }
                    .toMap()
            )
        }
    }
}
