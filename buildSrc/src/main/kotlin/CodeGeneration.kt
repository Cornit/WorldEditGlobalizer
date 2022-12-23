import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.gradle.api.Project
import java.io.File
import java.util.*


fun buildEnumClass(
    packageName: String,
    className: String,
    entries: Map<String, List<String>>,
    fieldNames: List<String>
): String {
    val entriesStr = entries.map { entry ->
        "    ${entry.key}(" + entry.value.map { GsonBuilder().disableHtmlEscaping().create().toJson(it) }.joinToString(", ") + ")"
    }.joinToString(",\n") + "\n"

    val head = """
        package ${packageName};
    
        public enum ${className} {
        
    """.trimIndent()

    fun getField(name: String): String {
        return "private final String ${name};"
    }

    fun getConstructor(): String {
        return "${className}(" + fieldNames.map { "String ${it}" }.joinToString(", ") + ") {\n" +
                fieldNames.map { "    this.${it} = ${it};" }.joinToString("\n") + "\n" +
                "}"
    }
    fun getGetter(name: String): String {
        return "public String get${name.capitalize()}() {\n" +
                "    return this.${name};\n" +
                "}"
    }
    return head + entriesStr + "    ;\n" +
            fieldNames.map { "    " + getField(it) }.joinToString("\n") + "\n" +
            getConstructor().prependIndent("    ") + "\n" +
            fieldNames.map { getGetter(it).prependIndent("    ") }.joinToString("\n") + "\n" +
            "}"
}


fun Project.registerTranslationKeySourcesGenerationTask(resourceDirs: Set<File>) {
    tasks.register("generateTranslationKeySources") {
        group = "generation"
        doLast {
            val keys = HashSet<String>()
            resourceDirs.forEach { dir ->
                dir.walkTopDown()
                    .filter { it.isFile && it.name.startsWith("messages_") && it.extension == "properties" }
                    .forEach { file ->
                        val properties = Properties()
                        properties.load(file.reader())
                        properties.keys.map { any -> any as String }.forEach(keys::add)
                    }
            }
            val classPath = "me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey"
            val keysFile =
                project.file("build/generated/sources/translationkeys/main/java/${classPath.replace('.', '/')}.java")
            keysFile.parentFile.mkdirs()
            val writer = keysFile.printWriter()
            writer.println(
                buildEnumClass(
                    classPath.substring(0, classPath.lastIndexOf('.')),
                    "TranslationKey",
                    keys.associate { key -> key.toUpperCase() to listOf(key) }.toSortedMap(),
                    listOf("key")
                )
            )
            writer.flush()
            writer.close()
        }
    }

    tasks.named("compileJava").get().dependsOn(tasks.named("generateTranslationKeySources"))

}

fun Project.registerPermissionSourcesGenerationTask() {
    tasks.register("generatePermissionSources") {
        group = "generation"
        doLast {
            data class PermissionInfo(var description: String?, var permission: String) {
                constructor() : this("", "")
            }

            val permissions = HashMap<String, PermissionInfo>()
            val file = rootProject.file("weg-common/src/main/resources/permissions.json")
            val obj = Gson().fromJson<JsonObject>(file.readText(), JsonObject::class.java)
            fun rec(path: List<String>, currentObject: JsonObject) {
                val pathStr = path.joinToString(".")
                if (currentObject.has("@")) {
                    if (path.size == 0) {
                        throw IllegalStateException("permissions.json:${pathStr}: found @ member in root object")
                    }
                    if (!currentObject["@"].isJsonObject) {
                        throw IllegalStateException("permissions.json:${pathStr}: @ member must be an object")
                    }
                    val perm = Gson().fromJson(currentObject["@"].asJsonObject.toString(), PermissionInfo::class.java);
                    perm.permission = path.joinToString(".")
                    permissions.put(
                        path.joinToString("."),
                        perm
                    )
                }
                for (key in currentObject.keySet()) {
                    if (key != "@") {
                        if (!currentObject[key].isJsonObject) {
                            throw IllegalStateException("permissions.json:${pathStr}.${key}: found non-object type")
                        }
                        val newPath = path.toMutableList()
                        newPath += key
                        rec(newPath.toList(), currentObject[key].asJsonObject)
                    }
                }
            }
            rec(listOf(), obj["permissions"].asJsonObject)
            for (permissionInfo in permissions.values) {
                if (permissionInfo.description == null || permissionInfo.description!!.trim().isEmpty()) {
                    throw IllegalStateException("permissions.json:${permissionInfo.permission}: has an empty description")
                }
            }
            val classPath = "me.illgilp.worldeditglobalizer.common.permission.Permission"
            val keysFile =
                project.file("build/generated/sources/permissions/main/java/${classPath.replace('.', '/')}.java")
            keysFile.parentFile.mkdirs()
            val writer = keysFile.printWriter()
            writer.println(
                buildEnumClass(
                    classPath.substring(0, classPath.lastIndexOf('.')),
                    "Permission",
                    permissions
                        .map { pair -> pair.value }
                        .associate { p -> p.permission.replace(".", "_").toUpperCase().substring(p.permission.indexOf(".") + 1) to listOf(p.permission, p.description!!) }
                        .toSortedMap(),
                    listOf("permission", "description")
                )
            )
            writer.flush()
            writer.close()
        }
    }

    tasks.named("compileJava").get().dependsOn(tasks.named("generatePermissionSources"))

}
