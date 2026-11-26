package com.mountaya.build.classpath

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * CheckRuntimeClasspathTask is the built-artifact walk of SPEC section 7: the half of the boundary a source scan cannot
 * hold.
 *
 * A source scan passes on a module that reaches a forbidden dependency transitively, and that
 * is exactly the shape that puts a renderer into a headless consumer's build. This reads the
 * module's resolved classpath for every target and fails on a path to one.
 */
@DisableCachingByDefault(because = "It reads a resolved dependency graph, which is not a file.")
abstract class CheckRuntimeClasspathTask : DefaultTask() {
    /** The module whose classpaths are walked. */
    @get:Input
    abstract val module: Property<String>

    /** The root of each resolved classpath, keyed by its configuration's name. */
    @get:Internal
    abstract val roots: MapProperty<String, ResolvedComponentResult>

    /** Walks every classpath and fails with each forbidden path it found. */
    @TaskAction
    fun walk() {
        val refused = refusedFor(module.get())
        val problems = mutableListOf<String>()
        val graphs = roots.get().toSortedMap()

        for ((configuration, root) in graphs) {
            val seen = mutableSetOf<ResolvedComponentResult>()
            val queue = ArrayDeque(listOf(root to listOf(module.get())))
            while (queue.isNotEmpty()) {
                val (component, path) = queue.removeFirst()
                if (!seen.add(component)) continue
                for (dependency in component.dependencies.filterIsInstance<ResolvedDependencyResult>()) {
                    val selected = dependency.selected
                    val id = selected.id
                    val name = if (id is ModuleComponentIdentifier) "${id.group}:${id.module}" else id.displayName
                    val next = path + name
                    val group = (id as? ModuleComponentIdentifier)?.group
                    refused.firstOrNull { group != null && it.matches(group) && !it.allows(group, path) }?.let { rule ->
                        problems += "$configuration: ${next.joinToString(" -> ")}. ${rule.because}"
                    }
                    queue += selected to next
                }
            }
        }

        val walked = graphs.keys.joinToString(", ")
        logger.lifecycle("checkRuntimeClasspath: ${module.get()}, ${graphs.size} classpaths walked ($walked)")
        if (problems.isNotEmpty()) {
            val lines =
                listOf("${module.get()} reaches a dependency it never may:") + problems.distinct().map { "  $it" }
            throw GradleException(lines.joinToString("\n"))
        }
    }

    /**
     * RefusedGroup is a dependency group a module's classpath never holds, but where [allowance] lets a few of its
     * groups in through one library alone.
     */
    data class RefusedGroup(
        /** The group refused, and every group beneath it. */
        val prefix: String,
        /** The sentence a problem gives for the refusal. */
        val because: String,
        /** The groups it lets in through one library, or null where it lets none in. */
        val allowance: Allowance? = null,
    ) {
        /** Answers whether [group] is this group or one beneath it. */
        fun matches(group: String): Boolean = group == prefix || group.startsWith("$prefix.")

        /** Answers whether [group], reached along [path], is one the allowance lets in. */
        fun allows(
            group: String,
            path: List<String>,
        ): Boolean = allowance != null && group in allowance.groups && allowance.carrier in path
    }

    /**
     * Allowance is the groups a refused one lets in where [carrier] brings them, and nowhere else: a path to one of
     * [groups] that passes through no [carrier] is still refused.
     */
    data class Allowance(
        /** The library, as `group:module`, the groups are reached through. */
        val carrier: String,
        /** The groups the library may bring. */
        val groups: Set<String>,
    )

    /** The groups each module refuses, and what each lets in. */
    companion object {
        private const val ELEMENTS = "mountaya-elements"

        /** The modules that draw nothing: the client's two units and the geo layer. */
        private val HEADLESS = setOf("mountaya-client", "mountaya-admin", "mountaya-geo")

        /**
         * OkHttp's Android artifact, the platform exchange's engine on android, finds its context
         * through AndroidX Startup, and brings it with the annotations and the tracing Startup
         * needs. Those three are the one AndroidX a headless module carries, through OkHttp alone,
         * and no AndroidX type reaches a signature of either module (SPEC section 7).
         */
        private val OKHTTP_ANDROID =
            Allowance(
                carrier = "com.squareup.okhttp3:okhttp-android",
                groups = setOf("androidx.annotation", "androidx.startup", "androidx.tracing"),
            )

        /** Returns the groups [module] never reaches, by the table of SPEC section 7. */
        fun refusedFor(module: String): List<RefusedGroup> =
            buildList {
                if (module != ELEMENTS) {
                    add(RefusedGroup("org.maplibre", "MapLibre is reached from mountaya-elements alone."))
                }
                // The navigation is Geo's machine with its guidance, written here, so no module carries a
                // navigation library beside it.
                add(
                    RefusedGroup(
                        "com.stadiamaps.ferrostar",
                        "No module reaches a navigation library: the navigation is Geo's machine with its guidance.",
                    ),
                )
                if (module in
                    HEADLESS
                ) {
                    add(RefusedGroup("androidx", "A headless module carries no AndroidX.", OKHTTP_ANDROID))
                }
            }
    }
}
