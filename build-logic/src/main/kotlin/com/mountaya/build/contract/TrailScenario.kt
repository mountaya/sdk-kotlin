package com.mountaya.build.contract

import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.File

/**
 * TrailScenario is `sdk/trail.yaml` of the contract checkout, read at build time: Trail, the one sample app every
 * native SDK ships, the steps it takes and the journey each SDK's device test walks through it (SPEC.md section 7).
 *
 * What the gates hold this SDK's Trail to is read off it: the [version] a size baseline records, whether the [sdks] list
 * this one, the id of each step an app marks its code with, the accessibility ids the journey finds an element by, and
 * the settings with the placeholders a measured build compiles in. Each part is read for its shape, and a file of any
 * other shape is refused, since one misread would hold Trail to nothing.
 */
data class TrailScenario(
    /** The version of the scenario, which moves whenever what an app does changes. */
    val version: Int,
    /** The SDKs that ship Trail, drawn from the four the contract is written for. */
    val sdks: List<String>,
    /** The name of each setting an app or the session server reads. */
    val settings: List<String>,
    /** The id of each step, `trail.` and a kebab-case name, in the order a developer meets them. */
    val steps: List<String>,
    /** The id of each element the journey finds, of the same form. */
    val accessibility: List<String>,
    /** The placeholder each setting an app reads is compiled in with in a measured build, by the setting's name. */
    val measured: Map<String, String>,
) {
    /** The vocabulary a scenario is held to, and its reading. */
    companion object {
        /** The SDKs the list may name, and nothing else. */
        val SDKS: List<String> = listOf("typescript", "dart", "swift", "kotlin")

        /** The readers a setting may name. */
        private val READERS = setOf("app", "session-server")

        /** What a check of the journey may do where it acts. */
        private val ACTIONS = setOf("see", "press", "read")

        private val ID = Regex("^trail\\.[a-z][a-z0-9]*(-[a-z0-9]+)*$")
        private val SETTING = Regex("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$")

        /** Reads [file], failing with an [IllegalArgumentException] that says why where it holds no such scenario. */
        fun read(file: File): TrailScenario = parse(file.readText(), file.name)

        /** Reads [text], the document of the file named [name], failing as [read] does. */
        fun parse(
            text: String,
            name: String,
        ): TrailScenario {
            val root =
                try {
                    Load(LoadSettings.builder().build()).loadFromString(text)
                } catch (error: RuntimeException) {
                    throw IllegalArgumentException("$name does not read as YAML: ${error.message}", error)
                }
            require(root is Map<*, *>) { "$name: expected a mapping at the top level." }
            val version = root["version"]
            require(version is Int && version >= 1) { "$name: `version` must be a whole number from 1." }
            val sdks = strings(root["sdks"], name, "sdks")
            for (sdk in sdks) require(sdk in SDKS) { "$name: `sdks` lists `$sdk`, drawn from ${SDKS.joinToString()}." }

            val settings =
                entries(root["settings"], name, "settings").map { (index, setting) ->
                    val named = setting["name"]
                    require(named is String && SETTING.matches(named)) {
                        "$name: setting #$index has no `name` in capitals, as an environment variable is."
                    }
                    val readers = strings(setting["readers"], name, "$named.readers")
                    for (reader in readers) {
                        require(reader in READERS) { "$name: `$named` is read by `$reader`, drawn from $READERS." }
                    }
                    require(setting["required"] is Boolean) { "$name: `$named` says nothing of being `required`." }
                    require(setting["scopes"] is List<*>) { "$name: `$named` lists no `scopes`." }
                    sentence(setting["use"], name, "`$named` states no `use`")
                    named to readers
                }
            val settingNames = settings.map { it.first }
            require(settingNames.toSet().size == settingNames.size) { "$name: a setting is listed twice." }

            val steps = identified(root["steps"], name, "steps", "does")
            val accessibility = identified(root["accessibility"], name, "accessibility", "marks")

            val journey = root["journey"]
            require(journey is Map<*, *>) { "$name: `journey` must be a mapping." }
            val speed = journey["replayMetersPerSecond"]
            require(
                speed is Number && speed.toDouble() > 0,
            ) { "$name: the journey's `replayMetersPerSecond` is no speed." }
            val seconds = journey["arrivalSeconds"]
            require(
                seconds is Int && seconds > 0,
            ) { "$name: the journey's `arrivalSeconds` is no whole number of seconds." }
            strings(journey["waypoints"], name, "journey.waypoints")
            require(journey["settings"] is List<*>) { "$name: the journey lists no `settings`." }
            for ((index, check) in entries(journey["checks"], name, "journey.checks")) {
                val target = check["target"]
                require(target is String && target in accessibility) {
                    "$name: check #$index acts on `$target`, which no accessibility id is."
                }
                val action = check["action"]
                require(
                    action is String && action in ACTIONS,
                ) { "$name: check #$index does `$action`, drawn from $ACTIONS." }
                sentence(check["holds"], name, "check #$index holds no sentence")
            }

            val measured = root["measured"]
            require(measured is Map<*, *>) { "$name: `measured` must be a mapping." }
            val placeholders =
                measured.entries.associate { (setting, placeholder) ->
                    require(setting is String && setting in settingNames) {
                        "$name: `measured` names `$setting`, which no setting is."
                    }
                    require(placeholder is String && placeholder.isNotBlank()) {
                        "$name: `measured` gives `$setting` no placeholder."
                    }
                    setting to placeholder
                }
            for ((setting, readers) in settings) {
                if ("app" in
                    readers
                ) {
                    require(setting in placeholders) { "$name: `measured` gives `$setting` no placeholder." }
                }
            }
            return TrailScenario(version, sdks, settingNames, steps, accessibility, placeholders)
        }

        /** The non-empty list [value] of the field [field], each a string, or a refusal. */
        private fun strings(
            value: Any?,
            name: String,
            field: String,
        ): List<String> {
            require(value is List<*> && value.isNotEmpty()) { "$name: `$field` must be a non-empty list." }
            return value.map { item ->
                require(item is String && item.isNotBlank()) { "$name: `$field` lists $item, which is no name." }
                item
            }
        }

        /** The non-empty list [value] of the field [field], each a mapping, by its number from 1. */
        private fun entries(
            value: Any?,
            name: String,
            field: String,
        ): List<Pair<Int, Map<*, *>>> {
            require(value is List<*> && value.isNotEmpty()) { "$name: `$field` must be a non-empty list." }
            return value.mapIndexed { index, item ->
                require(item is Map<*, *>) { "$name: `$field` #${index + 1} is not a mapping." }
                index + 1 to item
            }
        }

        /** The ids of the list [value] of the field [field], each `trail.` and a kebab-case name, listed once, with [said]. */
        private fun identified(
            value: Any?,
            name: String,
            field: String,
            said: String,
        ): List<String> {
            val ids = mutableListOf<String>()
            for ((index, entry) in entries(value, name, field)) {
                val id = entry["id"]
                require(
                    id is String && ID.matches(id),
                ) { "$name: `$field` #$index has no id, `trail.` and a kebab-case name." }
                require(id !in ids) { "$name: `$id` is listed twice in `$field`." }
                sentence(entry[said], name, "`$id` states no `$said`")
                ids += id
            }
            return ids
        }

        /** Refuses [value] where it is no sentence, saying [what]. */
        private fun sentence(
            value: Any?,
            name: String,
            what: String,
        ) {
            require(value is String && value.isNotBlank()) { "$name: $what." }
        }
    }
}
