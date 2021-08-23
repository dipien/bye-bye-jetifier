import org.gradle.api.Project

class PropertyResolver(private val project: Project) {

    fun getBooleanProp(name: String, defaultValue: Boolean?): Boolean? {
        val value = getProp(name, null)
        return if (value != null) {
            when (value) {
                "true" -> true
                "false" -> false
                else -> throw RuntimeException("The $name gradle property value [$value] is not a valid boolean")
            }
        } else {
            defaultValue
        }
    }

    fun getRequiredBooleanProp(name: String, defaultValue: Boolean): Boolean {
        return getBooleanProp(name, defaultValue)!!
    }

    fun getRequiredStringProp(name: String): String {
        return getProp(name, null)?.toString()!!
    }

    private fun getProp(name: String, defaultValue: Any?): Any? {
        return when {
            project.hasProperty(name) -> project.property(name)
            System.getenv().containsKey(name) -> System.getenv(name)
            else -> defaultValue
        }
    }
}
