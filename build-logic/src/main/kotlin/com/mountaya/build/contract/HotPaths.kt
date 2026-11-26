package com.mountaya.build.contract

import com.mountaya.build.gates.HotPath
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.File

/**
 * HotPaths is `sdk/hot-paths.yaml` of the contract checkout, read at build time: every hot path, the SDKs that budget
 * it and the sentence saying what its budget holds (SPEC.md section 7).
 *
 * The numbers live beside each SDK's budget tests and never in the list, so what is read is the shape alone: a
 * `paths` list whose entries each hold a kebab-case `id`, listed once, the `sdks` that budget it, drawn from the four
 * the contract is written for, and the sentence it `holds`. A list of any other shape is refused, since one misread
 * would hold this SDK to nothing.
 */
object HotPaths {
    /** The SDKs an entry may list, and nothing else. */
    val SDKS: List<String> = listOf("typescript", "dart", "swift", "kotlin")

    private val KEBAB = Regex("^[a-z][a-z0-9]*(-[a-z0-9]+)*$")

    /** Reads [file], failing with an [IllegalArgumentException] that says why where it holds no such list. */
    fun read(file: File): List<HotPath> = parse(file.readText(), file.name)

    /** Reads [text], the document of the file named [name], failing as [read] does. */
    fun parse(
        text: String,
        name: String,
    ): List<HotPath> {
        val root =
            try {
                Load(LoadSettings.builder().build()).loadFromString(text)
            } catch (error: RuntimeException) {
                throw IllegalArgumentException("$name does not read as YAML: ${error.message}", error)
            }
        require(root is Map<*, *>) { "$name: expected a mapping at the top level." }
        val paths = root["paths"]
        require(paths is List<*> && paths.isNotEmpty()) { "$name: `paths` must be a non-empty list." }
        val read = mutableListOf<HotPath>()
        for ((index, entry) in paths.withIndex()) {
            require(entry is Map<*, *>) { "$name: hot path #${index + 1} is not a mapping." }
            val id = entry["id"]
            val sdks = entry["sdks"]
            val holds = entry["holds"]
            require(id is String && KEBAB.matches(id)) { "$name: hot path #${index + 1} has no kebab-case `id`." }
            require(read.none { it.id == id }) { "$name: `$id` is listed twice." }
            require(sdks is List<*> && sdks.isNotEmpty()) { "$name: `$id` lists no `sdks`." }
            val named = sdks.map { sdk -> sdk as? String ?: throw IllegalArgumentException("$name: `$id` lists $sdk.") }
            for (sdk in named) {
                require(sdk in SDKS) { "$name: `$id` lists `$sdk` among its sdks, drawn from ${SDKS.joinToString()}." }
            }
            require(holds is String && holds.isNotBlank()) { "$name: `$id` holds no sentence." }
            read += HotPath(id, named)
        }
        return read
    }
}
