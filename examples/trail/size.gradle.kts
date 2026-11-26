import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

// What the repository's app-size gate reads of Trail's release build beside its APK (SPEC.md section 7), kept apart from
// the app's own build script, which an app copied from Trail leaves out.

/**
 * WriteReleaseRuntimeVersions writes the version of each module the release build carries, as Gradle's resolution of
 * the release runtime classpath chose it, and the graph that resolution drew, which the repository's app-size gate
 * reads: a library the SDK brings is held to the share its baseline records only at the version it was recorded at, so
 * a release a range admits, MapLibre's, is reported there rather than failing a commit that changed nothing, and a
 * library an SDK module brings that no share names fails the gate. Both are read from the resolution result and never
 * from a file's name.
 *
 * The versions are one `group:name:version` a line, in order; the SDK's own modules, which the composite build stands
 * in for, are left out. The graph is one edge a line, `<from> -> <to>`, in order, each end the app, `app`, one of the
 * SDK's modules, `sdk:<name>`, or a library, `group:name`; a constraint a platform states is no edge.
 */
abstract class WriteReleaseRuntimeVersions : DefaultTask() {
    /** The root of the release runtime classpath's resolution result, which both lists are read from. */
    @get:Input
    abstract val resolution: Property<ResolvedComponentResult>

    /**
     * The files of the libraries the classpath resolved. They have Gradle resolve its graph as it plans the build, as
     * the Android Gradle plugin's own report of the app's libraries does: read first as the task runs, the graph would
     * wait on the repository's build, which includes this one and waits on the task.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val libraries: ConfigurableFileCollection

    /** The versions it writes. */
    @get:OutputFile
    abstract val output: RegularFileProperty

    /** The graph it writes. */
    @get:OutputFile
    abstract val graph: RegularFileProperty

    /** Writes both lists, the versions into [output] and the graph into [graph], read off [resolution]. */
    @TaskAction
    fun write() {
        val root = resolution.get()
        val modules = sortedSetOf<String>()
        val edges = sortedSetOf<String>()
        val seen = mutableSetOf<ResolvedComponentResult>()
        val pending = ArrayDeque(listOf(root))
        while (pending.isNotEmpty()) {
            val component = pending.removeFirst()
            if (!seen.add(component)) continue
            val id = component.id
            if (id is ModuleComponentIdentifier && id.group != SDK_GROUP) {
                modules += "${id.group}:${id.module}:${id.version}"
            }
            for (dependency in component.dependencies) {
                if (dependency !is ResolvedDependencyResult || dependency.isConstraint) continue
                edges += "${node(component, root)} -> ${node(dependency.selected, root)}"
                pending += dependency.selected
            }
        }
        output.get().asFile.writeText(modules.joinToString(separator = "\n", postfix = "\n"))
        graph.get().asFile.writeText(edges.joinToString(separator = "\n", postfix = "\n"))
    }

    /** Returns how the graph names [component]: the app, an SDK module, or a library. */
    private fun node(
        component: ResolvedComponentResult,
        root: ResolvedComponentResult,
    ): String =
        when (val id = component.id) {
            root.id -> {
                "app"
            }

            is ProjectComponentIdentifier -> {
                "sdk:${id.projectName}"
            }

            is ModuleComponentIdentifier -> {
                if (id.group == SDK_GROUP) "sdk:${id.module}" else "${id.group}:${id.module}"
            }

            else -> {
                id.displayName
            }
        }

    /** The constants the task reads. */
    private companion object {
        /** The group every module of the SDK is published under, which a build resolving them from a repository names. */
        const val SDK_GROUP = "com.mountaya"
    }
}

tasks.register<WriteReleaseRuntimeVersions>("writeReleaseRuntimeVersions") {
    description = "Writes the version of each module the release build carries and the graph it was resolved from, " +
        "which the app-size gate reads."
    val classpath = configurations.named("releaseRuntimeClasspath")
    resolution.set(classpath.flatMap { it.incoming.resolutionResult.rootComponent })
    libraries.from(
        classpath.map { configuration ->
            configuration.incoming.artifactView { componentFilter { it is ModuleComponentIdentifier } }.files
        },
    )
    output.set(layout.buildDirectory.file("outputs/size/release-runtime-versions.txt"))
    graph.set(layout.buildDirectory.file("outputs/size/release-runtime-graph.txt"))
}
