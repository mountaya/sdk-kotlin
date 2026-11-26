package com.mountaya.build.wrapper

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * CheckWrapperTask holds the Gradle wrapper to the Gradle `mise.toml` pins.
 *
 * `mise.toml` is the one place a tool version is written. The wrapper carries a version of its
 * own because that is how a wrapper works, so this is what keeps it a quote of the pin: the
 * distribution it downloads and the checksum it verifies are the ones `mise run wrapper` writes.
 */
abstract class CheckWrapperTask : DefaultTask() {
    /** `mise.toml` at the repository root. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val miseFile: RegularFileProperty

    /** `gradle/wrapper/gradle-wrapper.properties`. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val wrapperProperties: RegularFileProperty

    /** Compares the two files and fails on any difference. */
    @TaskAction
    fun check() {
        val problems = problems(miseFile.get().asFile.readText(), wrapperProperties.get().asFile.readText())
        if (problems.isNotEmpty()) {
            val lines = listOf("The Gradle wrapper and mise.toml disagree:") + problems.map { "  $it" }
            throw GradleException(lines.joinToString("\n"))
        }
        logger.lifecycle("checkWrapper: the wrapper is the Gradle mise.toml pins.")
    }

    /** The patterns the pin and the wrapper are read by. */
    companion object {
        private val PIN = Regex("""^gradle\s*=\s*"([^"]+)"\s*$""", RegexOption.MULTILINE)
        private val TASK_VERSION = Regex("""--gradle-version\s+(\S+)""")
        private val TASK_CHECKSUM = Regex("""--gradle-distribution-sha256-sum\s+([0-9a-f]{64})""")
        private val DISTRIBUTION =
            Regex("""^distributionUrl=.*/gradle-(.+)-(bin|all)\.zip\s*$""", RegexOption.MULTILINE)
        private val CHECKSUM = Regex("""^distributionSha256Sum=([0-9a-f]{64})\s*$""", RegexOption.MULTILINE)

        /** Returns every disagreement between `mise.toml` and the wrapper's properties. */
        fun problems(
            mise: String,
            wrapper: String,
        ): List<String> {
            val problems = mutableListOf<String>()
            val pin = PIN.find(mise)?.groupValues?.get(1)
            val taskVersion = TASK_VERSION.find(mise)?.groupValues?.get(1)
            val taskChecksum = TASK_CHECKSUM.find(mise)?.groupValues?.get(1)
            val distribution = DISTRIBUTION.find(wrapper)?.groupValues?.get(1)
            val checksum = CHECKSUM.find(wrapper)?.groupValues?.get(1)

            if (pin == null) problems += "mise.toml pins no Gradle version under [tools]."
            if (taskVersion != pin) {
                problems += "mise.toml's wrapper task generates Gradle $taskVersion, and [tools] pins $pin."
            }
            if (distribution != pin) {
                problems +=
                    "The wrapper downloads Gradle $distribution, and mise.toml pins $pin. Run `mise run wrapper`."
            }
            if (checksum == null) {
                problems += "The wrapper verifies no checksum of the distribution it downloads."
            } else if (checksum != taskChecksum) {
                problems +=
                    "The wrapper verifies checksum $checksum, and mise.toml's wrapper task writes $taskChecksum."
            }
            return problems
        }
    }
}
