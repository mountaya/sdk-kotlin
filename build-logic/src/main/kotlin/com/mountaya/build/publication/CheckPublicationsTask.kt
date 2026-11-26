package com.mountaya.build.publication

import com.mountaya.build.generator.ElementsPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * CheckPublicationsTask holds every module's published metadata to what Maven Central requires of it
 * ([Publication.problems]), and each archive that carries third-party markup to the notices of its licences.
 */
@DisableCachingByDefault(because = "It reads a handful of small files.")
abstract class CheckPublicationsTask : DefaultTask() {
    /** Every module's generated publication metadata. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val poms: ConfigurableFileCollection

    /** The archives that carry the glyph markup, each of which must carry its notices too. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val noticeArchives: ConfigurableFileCollection

    /** Reads each POM and fails on every element one leaves out. */
    @TaskAction
    fun check() {
        val files = poms.files.sorted()
        val problems =
            if (files.isEmpty()) {
                listOf("No published metadata was handed to the check.")
            } else {
                files.flatMap { pom -> Publication.problems(pom.readText()).map { "${pom.path}: $it" } }
            } +
                noticeArchives.files.sorted().mapNotNull { archive ->
                    val entry = ElementsPrinter.NOTICES_ENTRY
                    "${archive.path} carries no $entry.".takeUnless { Publication.carries(archive, entry) }
                }
        if (problems.isNotEmpty()) {
            val heading = "The published metadata, held to what Maven Central requires:\n"
            throw GradleException(problems.joinToString("\n", heading) { "  $it" })
        }
        val archives = noticeArchives.files.size
        logger.lifecycle(
            "checkPublications: ${files.size} metadata files, each stating what Maven Central requires; " +
                "$archives archives carrying the third-party notices",
        )
    }
}
