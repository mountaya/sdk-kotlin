package com.mountaya.build.contract

import org.gradle.api.Project
import java.io.File

/**
 * ContractCheckout says where the contract checkout is: the sibling `schemas` directory, or wherever
 * `-Pmountaya.schemasDir` says.
 *
 * It is tolerated as absent on a contributor's machine and refused when `CI` is set, so a
 * contributor builds without it and a green CI run always means the fixtures ran.
 */
class ContractCheckout(
    project: Project,
) {
    /** The checkout's root directory, whether or not it exists. */
    val directory: File =
        project.rootProject.file((project.findProperty(PROPERTY) as String?) ?: DEFAULT)

    /** `sdk/registry.yaml`, or null when the checkout is absent. */
    val registry: File? = File(directory, ContractLayout.REGISTRY).takeIf { it.isFile }

    /** `sdk/fixtures`, or null when the checkout is absent. */
    val fixtures: File? = File(directory, ContractLayout.FIXTURES).takeIf { it.isDirectory }

    /** `sdk/hot-paths.yaml`, the hot paths each SDK budgets, or null when the checkout is absent. */
    val hotPaths: File? = File(directory, ContractLayout.HOT_PATHS).takeIf { it.isFile }

    /** `sdk/trail.yaml`, the scenario of the sample app every native SDK ships, or null when the checkout is absent. */
    val trail: File? = File(directory, ContractLayout.TRAIL).takeIf { it.isFile }

    /** `sdk/trail/itinerary.json`, the demo itinerary every Trail ships a copy of, or null when the checkout is absent. */
    val trailItinerary: File? = File(directory, ContractLayout.TRAIL_ITINERARY).takeIf { it.isFile }

    /** Where this SDK's conformance report goes in the checkout. */
    val report: File = File(directory, ContractLayout.REPORT)

    /** The places the checkout is looked for. */
    companion object {
        /** The Gradle property that names another contract checkout. */
        const val PROPERTY = "mountaya.schemasDir"
        private const val DEFAULT = "../schemas"

        /** Answers whether this run is one that must not tolerate an absent checkout. */
        fun isCi(project: Project): Boolean =
            !project.providers
                .environmentVariable("CI")
                .orNull
                .isNullOrEmpty()
    }
}

/**
 * configure points [task] at the contract checkout, and at the scope and ownership tables the root build file states.
 */
fun ContractCheckout.configure(
    task: ContractTask,
    project: Project,
) {
    val root = project.rootProject.extensions.getByType(com.mountaya.build.MountayaBuildExtension::class.java)
    registry?.let { task.registry.set(it) }
    fixtures?.let { task.fixtures.set(it) }
    task.ci.set(ContractCheckout.isCi(project))
    task.scopedLayers.set(root.scopedLayers)
    task.scopedCapabilities.set(root.scopedCapabilities)
    task.apiRunnerModules.set(root.apiRunnerModules)
    task.internalRunnerModules.set(root.internalRunnerModules)
}

/**
 * ConformanceRunnersExtension is what a module states about the conformance runners it holds. Which entries are its own
 * is the root build's table, never the module's, so the table also names the modules not yet built.
 */
abstract class ConformanceRunnersExtension {
    /** The package its runners are declared in, the module's own (SPEC.md section 6). */
    abstract val runnerPackage: org.gradle.api.provider.Property<String>

    /**
     * The directory its runners sit in below a test source set's `kotlin/`, in the common tests and in
     * the jvm tests alike: the module's base, then the topic and the kind they are filed under.
     */
    abstract val runnerDirectory: org.gradle.api.provider.Property<String>

    /** Whether this module states the scoped capabilities as tests. One module does. */
    abstract val statesScopes: org.gradle.api.provider.Property<Boolean>
}
