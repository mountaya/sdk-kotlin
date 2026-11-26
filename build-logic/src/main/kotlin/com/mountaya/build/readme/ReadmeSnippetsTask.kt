package com.mountaya.build.readme

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * ReadmeSnippetsTask prints the README's Kotlin snippets into the consumer build, or, where [write] is false, fails
 * where the printed file differs from what the README prints (SPEC section 10), and where the README's floors table
 * differs from the version catalog's floors (SPEC section 2, [ReadmeFloors]).
 *
 * The check is the gate's, so a snippet edited in the README and not printed again fails it, and so
 * does a floor moved in the catalog and not in the table; the write is run after a deliberate change,
 * and the diff is the review.
 */
@DisableCachingByDefault(because = "It compares or writes one small file in the source tree.")
abstract class ReadmeSnippetsTask : DefaultTask() {
    /** The document the snippets are read from. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val readme: RegularFileProperty

    /** The printed file, in the consumer build's sources. */
    @get:Internal
    abstract val printed: RegularFileProperty

    /** The package the printed file declares. */
    @get:Input
    abstract val packageName: Property<String>

    /** Whether the file is written, rather than compared. */
    @get:Input
    abstract val write: Property<Boolean>

    /** The command that prints the file afresh, which its header and a failure name. */
    @get:Input
    abstract val rewrite: Property<String>

    /** The floors the README's table states, each keyed by its version catalog key and valued by the catalog's figure. */
    @get:Input
    abstract val floors: MapProperty<String, String>

    /**
     * Prints the README's snippets, or, under the check, fails where the printed file differs or a floor is misstated.
     */
    @TaskAction
    fun run() {
        val document = readme.get().asFile
        val markdown = document.readText()
        val fences =
            try {
                ReadmeSnippets.fences(markdown)
            } catch (refusal: ReadmeSnippets.Refusal) {
                throw GradleException("${document.name}: ${refusal.message}")
            }
        if (fences.none { it.skipped == null }) {
            throw GradleException(
                "${document.name} compiles no Kotlin snippet at all.",
            )
        }
        val text = ReadmeSnippets.file(fences, document.name, rewrite.get(), packageName.get())
        val target = printed.get().asFile
        if (write.get()) {
            target.parentFile.mkdirs()
            target.writeText(text)
            logger.lifecycle("$name: ${fences.count { it.skipped == null }} snippets printed into ${target.name}")
            return
        }
        val problems = ReadmeFloors.problems(document.name, markdown, floors.get()).toMutableList()
        if (!target.isFile || target.readText() != text) {
            problems += "${document.name}'s Kotlin snippets moved: run `${rewrite.get()}` and review the diff."
        }
        if (problems.isNotEmpty()) throw GradleException(problems.joinToString("\n"))
        logger.lifecycle(
            "$name: ${fences.size} fences, ${fences.count { it.skipped != null }} skipped, as printed; " +
                "${Floors.all.size} floors, each on the floors table's row at the version catalog's figure",
        )
    }
}
