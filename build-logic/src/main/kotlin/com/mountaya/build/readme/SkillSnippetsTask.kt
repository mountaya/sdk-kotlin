package com.mountaya.build.readme

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * SkillSnippetsTask reads this SDK's developer skill from the `ai` checkout, fails where a floor it states is not the
 * version catalog's or a fence compiles on no target, and prints its Kotlin fences into the consumer build's generated
 * sources, one source set for each target a fence's imports name, which the consumer then compiles (SPEC section 10).
 * Nothing it prints is committed.
 *
 * An absent checkout prints nothing and is said as a note on a contributor's machine, and fails the
 * run under CI, which checks the checkout out at the commit `siblings.lock` pins.
 */
@DisableCachingByDefault(because = "It reads a sibling checkout and writes a few small files.")
abstract class SkillSnippetsTask : DefaultTask() {
    /** The `ai` checkout, whether or not it is there. */
    @get:Internal
    abstract val checkout: Property<File>

    /** How a message names the checkout: `../ai` beside this repository, or its path. */
    @get:Input
    abstract val shownCheckout: Property<String>

    /** The skill's documents, declared so an edit to one prints the snippets again. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val documents: ConfigurableFileCollection

    /** Whether an absent checkout is refused. */
    @get:Input
    abstract val ci: Property<Boolean>

    /** The floors the skill states, each keyed by its version catalog key and valued by the catalog's figure. */
    @get:Input
    abstract val floors: MapProperty<String, String>

    /** The committed ABI dumps, which say what each target alone declares. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dumps: ConfigurableFileCollection

    /** The package the printed files declare. */
    @get:Input
    abstract val packageName: Property<String>

    /** The consumer's generated sources: one `<source set>/kotlin` directory for each target a fence compiles on. */
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    /** Prints the skill's fences into the consumer's sources, failing where a fence or a floor cannot stand. */
    @TaskAction
    fun print() {
        val out = output.get().asFile
        out.deleteRecursively()
        out.mkdirs()

        val ai = checkout.get()
        val shown = shownCheckout.get()
        if (!ai.isDirectory) {
            if (ci.get()) {
                throw GradleException(
                    "$shown is not checked out, and CI compiles the Kotlin skill's snippets from it: " +
                        "`sh .agents/siblings checkout` fetches it at its pin.",
                )
            }
            logger.lifecycle(
                "$name: $shown is not checked out, so no skill snippet is compiled here; CI insists on it.",
            )
            return
        }
        val skill = File(ai, SkillSnippets.SKILL)
        if (!skill.isDirectory) {
            throw GradleException("$shown holds no ${SkillSnippets.SKILL}, the Kotlin skill the consumer compiles.")
        }
        val texts =
            documents.files
                .filter { it.isFile && it.extension == "md" }
                .associate { it.relativeTo(skill).invariantSeparatorsPath to it.readText() }
        if (SkillSnippets.ENTRY !in texts) {
            throw GradleException("$shown/${SkillSnippets.SKILL} holds no ${SkillSnippets.ENTRY}.")
        }

        val problems = mutableListOf<String>()
        val snippets =
            try {
                SkillSnippets.snippets(texts)
            } catch (refusal: SkillSnippets.Refusal) {
                throw GradleException("$shown/${SkillSnippets.SKILL}/${refusal.message}")
            }
        problems += SkillSnippets.floorProblems(texts, floors.get())

        val declarations = declarations()
        val sorted = linkedMapOf<SkillSnippets.Snippet, SkillSnippets.Target>()
        for (snippet in snippets.filter { it.fence.skipped == null }) {
            try {
                sorted[snippet] = SkillSnippets.targetOf(snippet, declarations)
            } catch (refusal: SkillSnippets.Refusal) {
                problems += refusal.message.orEmpty()
            }
        }
        if (problems.isNotEmpty()) {
            throw GradleException(
                (listOf("$name: the Kotlin skill in $shown/${SkillSnippets.SKILL}") + problems.map { "  $it" })
                    .joinToString("\n"),
            )
        }

        val tally = SkillSnippets.tally(snippets, sorted, texts.size)
        for (target in SkillSnippets.Target.entries) {
            val here = sorted.filterValues { it == target }.keys.toList()
            if (here.isEmpty()) continue
            val composable =
                if (target == SkillSnippets.Target.ANDROID) {
                    here.filter { SkillSnippets.isComposable(it, declarations) }.toSet()
                } else {
                    emptySet()
                }
            val directory = File(out, "${target.sourceSet}/kotlin/${packageName.get().replace('.', '/')}")
            directory.mkdirs()
            File(directory, "${target.objectName}.kt")
                .writeText(SkillSnippets.file(target, here, composable, tally, packageName.get()))
        }
        logger.lifecycle("$name: ${tally.first()}")
    }

    /** What each target declares, read off the committed dumps by where each sits: `api/jvm/`, `api/android/`. */
    private fun declarations(): SkillSnippets.Declarations {
        val files = dumps.files.sortedBy { it.path }

        fun texts(kind: (File) -> Boolean) = files.filter(kind).map { it.readText() }
        return SkillSnippets.Declarations.read(
            klib = texts { it.name.endsWith(".klib.api") },
            jvm = texts { it.parentFile.name == "jvm" },
            android = texts { it.parentFile.name == "android" },
        )
    }

    /** The place the `ai` checkout is read from. */
    companion object {
        /** The Gradle property that names another `ai` checkout, a draft of the skill's among them. */
        const val PROPERTY = "mountaya.aiDir"

        /** Where the `ai` checkout is otherwise, relative to this repository: beside it, where CI checks it out. */
        const val DEFAULT = "../ai"
    }
}
