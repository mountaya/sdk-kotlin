package com.mountaya.build.publication

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * CheckReleaseRepositoryTask holds the release repository every module was just published into to what the Central
 * Portal validates ([ReleaseRepository.problems]), before it is zipped into the bundle the Portal takes.
 */
@DisableCachingByDefault(because = "It reads what a release is about to upload, which is never taken from a cache.")
abstract class CheckReleaseRepositoryTask : DefaultTask() {
    /** The release repository. */
    @get:Internal
    abstract val repository: DirectoryProperty

    /** The group every module is published under. */
    @get:Input
    abstract val groupId: Property<String>

    /** The version being released, the one every module is published at. */
    @get:Input
    abstract val releaseVersion: Property<String>

    /** The modules a release publishes, each handed in by its own build. */
    @get:Input
    abstract val modules: SetProperty<String>

    /** Whether every file must carry its signature. */
    @get:Input
    abstract val signed: Property<Boolean>

    /** The directories of this machine no published file may name. */
    @get:Input
    abstract val machinePaths: ListProperty<String>

    /** Reads the repository and fails on everything the Portal would refuse. */
    @TaskAction
    fun check() {
        val root = repository.get().asFile
        val version = releaseVersion.get()
        val problems =
            ReleaseRepository.problems(root, groupId.get(), version, modules.get(), signed.get(), machinePaths.get())
        if (problems.isNotEmpty()) {
            throw GradleException(problems.joinToString("\n", "The release repository $root:\n") { "  $it" })
        }
        val files = root.walkTopDown().count { it.isFile }
        val signatures = if (signed.get()) "signed" else "unsigned"
        logger.lifecycle(
            "checkReleaseRepository: ${modules.get().size} modules at $version, $files files, $signatures, in $root",
        )
    }
}
