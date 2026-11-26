package com.mountaya.build.readme

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * DocSnippetsTask prints the Kotlin fences of the Markdown documents it is handed into the consumer build's generated
 * sources, each fence in a file of its own in the source set its imports name ([DocSnippets]), beside an index that
 * maps each printed file back to its document's lines, which the consumer then compiles. It fails, naming each
 * document and line, where a fence cannot be read or compiles on no target, and where the documents hold no Kotlin
 * fence at all. Nothing it prints is committed, and it reads no path it is not handed.
 */
@DisableCachingByDefault(because = "It reads documents outside the build and writes a few small files.")
abstract class DocSnippetsTask : DefaultTask() {
    /** The documents' absolute paths, in the order given. */
    @get:Input
    abstract val paths: ListProperty<String>

    /** The directory a message names each document from: the one the command was run in. */
    @get:Input
    abstract val shownFrom: Property<String>

    /** The documents, declared so an edit to one prints the fences again. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val documents: ConfigurableFileCollection

    /** The committed ABI dumps, which say what each target alone declares. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dumps: ConfigurableFileCollection

    /** The consumer's generated sources: one `<source set>/kotlin` directory for each target a fence compiles on. */
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    /** Prints the documents' fences into the consumer's sources, failing where a fence cannot stand. */
    @TaskAction
    fun print() {
        val out = output.get().asFile
        out.deleteRecursively()
        out.mkdirs()

        val files = paths.get().map(::File)
        if (files.isEmpty()) {
            throw GradleException(
                "$name: no document named. Hand each Markdown document as an argument: " +
                    "`mise run snippets -- <file.md> [<file.md>...]`.",
            )
        }
        val base = File(shownFrom.get())
        val missing = files.filterNot { it.isFile }
        if (missing.isNotEmpty()) {
            throw GradleException(missing.joinToString("\n") { "${shown(it, base)} is not a file." })
        }
        val texts = files.map { shown(it, base) to it.readText() }
        val snippets =
            try {
                DocSnippets.snippets(texts)
            } catch (refusal: DocSnippets.Refusal) {
                throw GradleException(refusal.message.orEmpty())
            }
        if (snippets.isEmpty()) {
            throw GradleException(
                "$name: none of the ${files.size} documents holds a ```kotlin fence: name the documents whose Kotlin " +
                    "fences to compile.",
            )
        }

        val declarations = declarations()
        val problems = mutableListOf<String>()
        val sorted = linkedMapOf<SkillSnippets.Snippet, SkillSnippets.Target>()
        for (snippet in snippets.filter { it.fence.skipped == null }) {
            try {
                sorted[snippet] = DocSnippets.targetOf(snippet, declarations)
            } catch (refusal: DocSnippets.Refusal) {
                problems += refusal.message.orEmpty()
            }
        }
        if (problems.isNotEmpty()) throw GradleException(problems.joinToString("\n"))

        val index = StringBuilder()
        val packageDirectory = DocSnippets.PACKAGE.replace('.', '/')
        for ((snippet, target) in sorted) {
            val composable = target == SkillSnippets.Target.ANDROID && SkillSnippets.isComposable(snippet, declarations)
            val printed = DocSnippets.print(snippet, target, composable)
            val directory = File(out, "${target.sourceSet}/kotlin/$packageDirectory")
            directory.mkdirs()
            val file = File(directory, "${printed.objectName}.kt")
            file.writeText(printed.source)
            index.append(printed.indexLine(file.absolutePath)).append('\n')
        }
        File(out, DocSnippets.INDEX).writeText(index.toString())
        val tally = DocSnippets.tally(snippets, sorted, files.size)
        File(out, DocSnippets.TALLY).writeText(tally.joinToString("\n", postfix = "\n"))
        val skipped = snippets.size - sorted.size
        logger.lifecycle("$name: ${sorted.size} fences printed into ${out.path}, $skipped skipped")
    }

    /** How a message names [file]: from [base] where it is below it, else by its absolute path. */
    private fun shown(
        file: File,
        base: File,
    ): String {
        val absolute = file.absoluteFile.normalize()
        val relative = absolute.relativeToOrNull(base.absoluteFile.normalize())?.invariantSeparatorsPath
        return relative?.takeUnless { it.startsWith("..") } ?: absolute.path
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

    /** The properties the command hands the build. */
    companion object {
        /** The Gradle property that names the documents: their absolute paths, apart by the path separator. */
        const val PROPERTY = "mountaya.snippets"

        /** The Gradle property that names the directory the command was run in, which messages name documents from. */
        const val SHOWN_FROM = "mountaya.snippetsFrom"
    }
}
