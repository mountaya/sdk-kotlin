package com.mountaya.build.trail

import com.mountaya.build.contract.ContractCheckout
import com.mountaya.build.contract.ContractLayout
import com.mountaya.build.contract.TrailScenario
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * CheckTrailTask holds Trail, `examples/trail`, to the contract's `sdk/trail.yaml` ([TrailCheck]). The scenario and the
 * demo itinerary are read from the contract checkout, whose absence leaves the markers and the copy unchecked on a
 * contributor's machine and fails the run under CI, where `CI` is set.
 */
@DisableCachingByDefault(because = "It reads a handful of small files.")
abstract class CheckTrailTask : DefaultTask() {
    /** The repository's `examples/` directory. */
    @get:Internal
    abstract val examples: DirectoryProperty

    /** Every file under it, as the task's inputs. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    /** The contract's `sdk/trail.yaml`, unset where the checkout is absent. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val scenario: RegularFileProperty

    /** The contract's `sdk/trail/itinerary.json`, unset where the checkout is absent. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val itinerary: RegularFileProperty

    /** Whether an absent checkout fails the run, as it does under CI. */
    @get:Input
    abstract val ci: Property<Boolean>

    /** Runs the gate, prints its line, and fails with every problem it found. */
    @TaskAction
    fun check() {
        val file = scenario.orNull?.asFile
        val read =
            file?.let {
                try {
                    TrailScenario.read(it)
                } catch (refused: IllegalArgumentException) {
                    throw GradleException("${TrailCheck.NAME}: ${refused.message}", refused)
                }
            }
        val problems = mutableListOf<String>()
        if (read == null) {
            val absent =
                "${ContractLayout.TRAIL}: no scenario was read, the contract checkout being absent, so no step " +
                    "is held to a marker and the itinerary to no copy: fetch the schemas checkout beside this one, " +
                    "or name one with -P${ContractCheckout.PROPERTY}."
            if (ci.get()) problems += absent else logger.lifecycle("${TrailCheck.NAME}: $absent")
        }
        val shipped = read == null || "kotlin" in read.sdks
        if (!shipped) logger.lifecycle("${TrailCheck.NAME}: ${ContractLayout.TRAIL} lists no kotlin among its sdks.")
        val verdict =
            TrailCheck.check(
                examples.get().asFile,
                read?.steps?.takeIf { shipped },
                itinerary.orNull?.asFile?.readBytes(),
            )
        problems += verdict.problems
        val summary = verdict.summary + (read?.let { ", scenario version ${it.version}" } ?: "")
        if (problems.isEmpty()) {
            logger.lifecycle(summary)
            return
        }
        throw GradleException((listOf(summary) + problems.map { "  $it" }).joinToString("\n"))
    }
}
