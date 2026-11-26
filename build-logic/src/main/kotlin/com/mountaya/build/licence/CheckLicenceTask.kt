package com.mountaya.build.licence

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** CheckLicenceTask holds the licence texts, the README and every module's published metadata to [LicenceCheck]. */
@DisableCachingByDefault(because = "It reads a handful of small files.")
abstract class CheckLicenceTask : DefaultTask() {
    /** The repository root, where the texts and the README stand. */
    @get:Internal
    abstract val root: DirectoryProperty

    /** The texts and the README, as the task's inputs. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val documents: ConfigurableFileCollection

    /** Every module's generated publication metadata. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val poms: ConfigurableFileCollection

    /** The TypeScript checkout beside this one, where there is one. */
    @get:Internal
    abstract val typescript: DirectoryProperty

    /** The TypeScript packages' texts, as inputs, where that checkout is beside. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val typescriptTexts: ConfigurableFileCollection

    /** Checks the licence texts and every published module's metadata, failing with every problem it found. */
    @TaskAction
    fun check() {
        val sibling = typescript.orNull?.asFile?.takeIf { it.isDirectory }
        val problems = LicenceCheck.problems(root.get().asFile, poms.files.sorted(), sibling)
        if (problems.isNotEmpty()) throw GradleException(problems.joinToString("\n", "The licence:\n") { "  $it" })
        val compared = sibling?.let { "byte for byte the TypeScript packages' own" } ?: "no TypeScript checkout beside"
        logger.lifecycle(
            "checkLicence: ${LicenceCheck.TEXTS.size} texts and the examples' licence, " +
                "${poms.files.size} metadata files, $compared",
        )
    }
}
