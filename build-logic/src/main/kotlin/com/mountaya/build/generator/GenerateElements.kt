package com.mountaya.build.generator

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

/**
 * GenerateElements prints the elements' contract data from the contract checkout's `sdk/elements/` into
 * `mountaya-geo`'s generated package, and the third-party notices of the markup it carries into the repository root,
 * or, with `--check`, compares what it would print with what is there and fails on any difference.
 *
 * It writes into the source tree, whose files are inputs of every compile task, so it keeps no state
 * of its own and runs whenever it is asked. A file the tables no longer print is deleted.
 */
abstract class GenerateElements : DefaultTask() {
    /** `sdk/elements/`, unset where the contract checkout is absent. */
    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val elements: DirectoryProperty

    /**
     * The contract checkout's root, whose `sdk/model.json` names the contract's tag and whose files
     * [ModelDigest.INPUTS] names make the digest every printed file's header carries.
     */
    @get:Internal
    abstract val checkout: DirectoryProperty

    /** `sdk/model.json`, an input since the headers name the tag it holds and the digest it is among. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val model: RegularFileProperty

    /** The generated package's directory in `mountaya-geo`. */
    @get:Internal
    abstract val generated: DirectoryProperty

    /** `THIRD-PARTY-NOTICES.txt` at the repository root. */
    @get:Internal
    abstract val notices: RegularFileProperty

    /** Whether an absent checkout is refused, which it is on CI. */
    @get:Input
    abstract val ci: Property<Boolean>

    /**
     * Whether the task compares what `sdk/elements/` prints with the printed files and the notices rather than
     * writing them.
     */
    @get:Input
    @get:Option(
        option = "check",
        description = "Compares what sdk/elements/ prints with the printed files and the notices.",
    )
    abstract val check: Property<Boolean>

    init {
        check.convention(false)
        doNotTrackState("It writes into the source tree, whose files are inputs of the compile tasks.")
    }

    /**
     * Prints the elements' data from the contract's tables, or, under the check, fails where a printed file differs.
     */
    @TaskAction
    fun print() {
        val directory = elements.orNull?.asFile
        val sources = TABLES.map { File(directory ?: File("."), it) }
        if (directory == null || sources.any { !it.isFile }) {
            if (check.get() && !ci.get()) {
                logger.lifecycle("$name: skipped, the contract checkout's sdk/elements/ is absent.")
                return
            }
            throw GradleException(
                "$name: the contract checkout's sdk/elements/ is absent or lacks one of ${TABLES.joinToString()}. " +
                    "Check out mountaya/schemas beside this repository.",
            )
        }
        val (words, tokens, glyphs, notice) = sources.map { it.readText() }
        val modelFile = model.orNull?.asFile
        if (modelFile == null || !modelFile.isFile) {
            throw GradleException(
                "$name: the contract checkout's sdk/model.json is absent, whose tag and digest every printed " +
                    "file names. Check out mountaya/schemas beside this repository.",
            )
        }
        val printed: Map<String, String>
        val printedNotices: String
        try {
            val data = ElementsData.parse(words, tokens, glyphs, notice)
            val contract = ModelReader.read(modelFile.readText()).contract
            printed = ElementsPrinter.files(data, contract, ModelDigest.of(checkout.get().asFile))
            printedNotices = ElementsPrinter.notices(data)
        } catch (refused: ModelException) {
            throw GradleException("$name: ${refused.message}", refused)
        }
        val packageDirectory = generated.get().asFile
        val noticesFile = notices.get().asFile
        val onDisk =
            packageDirectory
                .listFiles { candidate -> candidate.isFile && candidate.name.endsWith(".kt") }
                .orEmpty()
                .associate { it.name to it.readText() }

        if (check.get()) {
            val drift = (printed.keys + onDisk.keys).sorted().filter { printed[it] != onDisk[it] }
            val notices = noticesFile.takeIf { it.isFile }?.readText()
            val problems =
                drift.map { describe(it, onDisk[it], printed[it]) } +
                    listOfNotNull(
                        describe(noticesFile.name, notices, printedNotices).takeIf { notices != printedNotices },
                    )
            if (problems.isNotEmpty()) {
                throw GradleException(
                    "$name --check: the elements' data differs from what sdk/elements/ prints:\n" +
                        problems.joinToString("\n") { "  $it" } +
                        "\nRun ./gradlew :mountaya-geo:generateElements, and never edit a generated file by hand.",
                )
            }
            logger.lifecycle(
                "$name --check: ${printed.size} files and ${noticesFile.name}, each exactly what sdk/elements/ prints.",
            )
            return
        }

        packageDirectory.mkdirs()
        for ((file, text) in printed) {
            val target = File(packageDirectory, file)
            if (!target.isFile || target.readText() != text) target.writeText(text)
        }
        for (stale in onDisk.keys - printed.keys) File(packageDirectory, stale).delete()
        if (!noticesFile.isFile || noticesFile.readText() != printedNotices) noticesFile.writeText(printedNotices)
        logger.lifecycle("$name: ${printed.size} files and ${noticesFile.name} printed from sdk/elements/.")
    }

    /** Says how [file] differs: missing, no longer printed, or the first line that moved. */
    private fun describe(
        file: String,
        onDisk: String?,
        printed: String?,
    ): String {
        if (onDisk == null) return "$file: not on disk"
        if (printed == null) return "$file: on disk, and the tables no longer print it"
        val disk = onDisk.lines()
        val expected = printed.lines()
        val line = disk.indices.firstOrNull { it >= expected.size || disk[it] != expected[it] } ?: expected.size
        return "$file: ${disk.size} lines on disk, ${expected.size} printed, first difference at line ${line + 1}"
    }

    private companion object {
        /** The files of `sdk/elements/` the printer reads, in the order [ElementsData.parse] takes them. */
        val TABLES = listOf("words.json", "tokens.json", "glyphs.json", "NOTICE")
    }
}
