package com.mountaya.build.changelog

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * CheckChangelogTask holds `CHANGELOG.md` to its shape and to the API the committed ABI dumps publish
 * ([ChangelogCheck]).
 */
@DisableCachingByDefault(because = "It reads one document and the dumps beside the modules.")
abstract class CheckChangelogTask : DefaultTask() {
    /** `CHANGELOG.md` at the repository root. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val changelog: RegularFileProperty

    /** Every published module's committed ABI dumps. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dumps: ConfigurableFileCollection

    /** The group every module is published under. */
    @get:Input
    abstract val groupId: Property<String>

    /** The published modules. */
    @get:Input
    abstract val modules: SetProperty<String>

    /** Reads the changelog and fails on every way it falls short. */
    @TaskAction
    fun check() {
        val text = changelog.get().asFile.readText()
        val published = dumps.files.flatMapTo(mutableSetOf()) { ChangelogCheck.publishedNames(it.readText()) }
        // The control: dumps read to nothing would wave every name through.
        val problems =
            if (published.isEmpty()) {
                listOf("No ABI dump was handed to the check.")
            } else {
                ChangelogCheck.problems(text, groupId.get(), modules.get(), published)
            }
        if (problems.isNotEmpty()) throw GradleException(problems.joinToString("\n", "CHANGELOG.md:\n") { "  $it" })
        val releases = text.lines().count { it.startsWith("## [") } - 1
        val names = published.size
        logger.lifecycle(
            "checkChangelog: [Unreleased] and $releases releases, every name among the $names the modules publish",
        )
    }
}
