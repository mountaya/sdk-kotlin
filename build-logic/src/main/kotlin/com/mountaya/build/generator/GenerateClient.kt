package com.mountaya.build.generator

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

/**
 * GenerateClient prints one unit's generated surface from the contract checkout's `sdk/model.json` into its generated
 * package, the core's into `mountaya-client` and the admin unit's into `mountaya-admin`, or, with `--check`, compares
 * what it would print with what is there and fails on any difference. Every file names the digest of the model and the
 * elements' tables it was printed beside, so a table that moved without a reprint fails the check too.
 *
 * It writes into the source tree, whose files are inputs of every compile task, so it keeps no
 * state of its own and runs whenever it is asked. A file the model no longer prints is deleted,
 * so a component the contract removes leaves nothing behind.
 */
abstract class GenerateClient : DefaultTask() {
    /** `sdk/model.json`, unset where the contract checkout is absent. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val model: RegularFileProperty

    /**
     * The contract checkout's root, whose files [ModelDigest.INPUTS] names: the model and the elements' tables, of
     * which the printed surface names the digest, `modelDigest`.
     */
    @get:Internal
    abstract val checkout: DirectoryProperty

    /** The generated package's directory. */
    @get:Internal
    abstract val generated: DirectoryProperty

    /** The unit printed, as the model spells it: `core` or `admin`. */
    @get:Input
    abstract val unit: Property<String>

    /** Whether an absent checkout is refused, which it is on CI. */
    @get:Input
    abstract val ci: Property<Boolean>

    /** Whether the task compares what the model prints with the generated package rather than writing it. */
    @get:Input
    @get:Option(
        option = "check",
        description = "Compares what the model prints with the generated package, and fails on any difference.",
    )
    abstract val check: Property<Boolean>

    init {
        check.convention(false)
        doNotTrackState("It writes into the source tree, whose files are inputs of the compile tasks.")
    }

    /** Prints the unit's surface from the model, or, under the check, fails where a printed file differs from it. */
    @TaskAction
    fun print() {
        val file = model.orNull?.asFile
        if (file == null || !file.isFile) {
            if (check.get() && !ci.get()) {
                logger.lifecycle("$name: skipped, the contract checkout's sdk/model.json is absent.")
                return
            }
            throw GradleException(
                "$name: the contract checkout's sdk/model.json is absent. Check out mountaya/schemas beside this repository.",
            )
        }
        val printedUnit =
            ClientUnit.of(unit.get()) ?: throw GradleException("$name: `${unit.get()}` is no unit of the client.")
        val printed =
            try {
                ClientPrinter.print(
                    ModelReader.read(file.readText()),
                    printedUnit,
                    ModelDigest.of(checkout.get().asFile),
                )
            } catch (refused: ModelException) {
                throw GradleException("$name: ${refused.message}", refused)
            }
        val directory = generated.get().asFile
        val onDisk =
            directory
                .listFiles { candidate -> candidate.isFile && candidate.name.endsWith(".kt") }
                .orEmpty()
                .associate { it.name to it.readText() }

        if (check.get()) {
            val drift = (printed.keys + onDisk.keys).sorted().filter { printed[it] != onDisk[it] }
            if (drift.isNotEmpty()) {
                throw GradleException(
                    "$name --check: the generated package differs from what sdk/model.json prints:\n" +
                        drift.joinToString("\n") { "  ${describe(it, onDisk[it], printed[it])}" } +
                        "\nRun ./gradlew generateClient, and never edit a generated file by hand.",
                )
            }
            logger.lifecycle("$name --check: ${printed.size} files, each exactly what sdk/model.json prints.")
            return
        }

        directory.mkdirs()
        for ((name, text) in printed) {
            val target = File(directory, name)
            if (!target.isFile || target.readText() != text) target.writeText(text)
        }
        for (stale in onDisk.keys - printed.keys) File(directory, stale).delete()
        logger.lifecycle("$name: ${printed.size} files printed from sdk/model.json.")
    }

    /** Says how [file] differs: missing, no longer printed, or the first line that moved. */
    private fun describe(
        file: String,
        onDisk: String?,
        printed: String?,
    ): String {
        if (onDisk == null) return "$file: not on disk"
        if (printed == null) return "$file: on disk, and the model no longer prints it"
        val disk = onDisk.lines()
        val expected = printed.lines()
        val line = disk.indices.firstOrNull { it >= expected.size || disk[it] != expected[it] } ?: expected.size
        return "$file: ${disk.size} lines on disk, ${expected.size} printed, first difference at line ${line + 1}"
    }
}
