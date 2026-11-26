package com.mountaya.build.size

/**
 * AppSize is what the SDK adds to the release build of Trail, `examples/trail`, the sample app of the contract's
 * `sdk/trail.yaml`, as R8 left it: the bytes of its dex code, package by package, as `apkanalyzer dex packages`
 * attributes them, the native libraries left out (SPEC.md section 7).
 *
 * Every hand-written file of a module declares the module's one package (SPEC.md section 6), so each module's share is
 * its package's bytes exactly, its printed `generated` package among them: [modules]. Beside them stand the libraries
 * the SDK brings into an app, each a share of its own, [brought]: data or code an app pays for without naming it lands
 * in the library that carries it, so a figure that summed the SDK's packages alone would miss it. Each of those is
 * measured at the release the app's build resolved, [versions], since a library's own release moves its share as much
 * as the SDK does.
 */
data class AppSize(
    /** The version of the scenario, `sdk/trail.yaml`'s, that the measured app follows. */
    val scenario: Int,
    /** The Android Gradle plugin the app was built with, whose R8 shrank it. */
    val agp: String,
    /** The Kotlin the app and the modules were compiled with. */
    val kotlin: String,
    /** Every byte of the app's dex code, the app's own, the SDK's and every library's. */
    val dex: Long,
    /** Each module's package, [MODULES], with its bytes; a package the app does not reach holds 0. */
    val modules: Map<String, Long>,
    /** Each library the SDK brings, by the package of [BROUGHT] its code sits under, with its bytes. */
    val brought: Map<String, Long>,
    /** Each library the SDK brings, by the same package, with the version the app's release build resolved it at. */
    val versions: Map<String, String>,
) {
    /** The figures the gate holds to the baseline, each a share: every module's, then every library's the SDK brings. */
    val shares: Map<String, Long> get() = modules + brought

    /** The SDK's modules together, which the report states and the gate never compares. */
    val sdk: Long get() = modules.values.sum()

    /** The parts a measure is made of, and its reading. */
    companion object {
        /** The package of each module the SDK publishes, which a consumer imports one of per module. */
        val MODULES: List<String> =
            listOf("com.mountaya.client", "com.mountaya.elements", "com.mountaya.geo", "com.mountaya.navigation")

        /**
         * The libraries the SDK brings into an app, each by the package its code sits under, with the module, as
         * `group:name`, whose resolved version is the library's: Ktor's client and kotlinx.serialization, which
         * `mountaya-client` sends and reads through, OkHttp, the engine both the client and MapLibre fetch with, and
         * MapLibre Native's Java side, which `mountaya-elements` draws on. Each is released whole, every module of it at
         * one version, but for MapLibre's GeoJSON and Turf modules, which its SDK's own release names exactly.
         */
        val BROUGHT: Map<String, String> =
            linkedMapOf(
                "io.ktor" to "io.ktor:ktor-client-core",
                "kotlinx.serialization" to "org.jetbrains.kotlinx:kotlinx-serialization-core",
                "okhttp3" to "com.squareup.okhttp3:okhttp",
                "org.maplibre" to "org.maplibre.gl:android-sdk",
            )

        /**
         * The modules each library's share names, `group:name` or `group:prefix*`: every module of its release, which the
         * module of [BROUGHT] names the version of, and whose own dependencies are that release's to bring. A library of
         * the release runtime classpath an SDK module reaches without passing through one of these, and the app does not
         * reach through a dependency of its own, is one no share names, which fails the gate ([ReleaseGraph.unnamed]).
         */
        val NAMES: Map<String, List<String>> =
            linkedMapOf(
                "io.ktor" to listOf("io.ktor:*"),
                "kotlinx.serialization" to listOf("org.jetbrains.kotlinx:kotlinx-serialization-*"),
                "okhttp3" to listOf("com.squareup.okhttp3:*"),
                "org.maplibre" to listOf("org.maplibre.gl:*"),
            )

        /** Returns the share whose [NAMES] name [module], `group:name`, or null where none does. */
        fun shareNaming(module: String): String? =
            NAMES.entries
                .firstOrNull { (_, patterns) ->
                    patterns.any { pattern ->
                        if (pattern.endsWith("*")) module.startsWith(pattern.dropLast(1)) else module == pattern
                    }
                }?.key

        /**
         * Returns the figures of the build of scenario [scenario] made with [agp] and [kotlin], read from the bytes
         * [packages] attributes to each package, each library at the version [resolved] holds for it.
         *
         * @throws IllegalArgumentException where [resolved] holds no version of a library's module, which would leave the
         * share compared at a release nothing recorded.
         */
        fun measure(
            packages: DexPackages,
            resolved: ResolvedVersions,
            scenario: Int,
            agp: String,
            kotlin: String,
        ): AppSize =
            AppSize(
                scenario = scenario,
                agp = agp,
                kotlin = kotlin,
                dex = packages.total,
                modules = MODULES.associateWith { packages.bytesOf(it) },
                brought = BROUGHT.keys.associateWith { packages.bytesOf(it) },
                versions =
                    BROUGHT.mapValues { (share, module) ->
                        requireNotNull(resolved.versionOf(module)) {
                            "the release runtime classpath resolves no $module, the module `$share`'s version is read from"
                        }
                    },
            )
    }
}

/**
 * DexPackages is the dex code of an APK, package by package, as `apkanalyzer dex packages --defined-only` prints it,
 * its names read back through R8's mapping: a row a node, its kind (`P` a package, `C` a class, `M` a method, `F` a
 * field), its state, then, separated by tabs, the methods it defines, those it references, its bytes, and its name. A
 * package's bytes are every class's under it, those of the packages below it included, and the row named `<TOTAL>`
 * holds the whole.
 */
class DexPackages(
    /** Every byte of the dex code. */
    val total: Long,
    private val packages: Map<String, Long>,
) {
    /**
     * Returns the bytes of [name]'s package, the packages below it included, or 0 where the dex code holds no class
     * under it.
     */
    fun bytesOf(name: String): Long = packages[name] ?: 0

    /** The reading of `apkanalyzer`'s output. */
    companion object {
        private const val TOTAL = "<TOTAL>"
        private val ROW = Regex("""^([PCMF]) ([a-z]) (\d+)\t(\d+)\t(-?\d+)\t(.+)$""")

        /**
         * Reads [text], the output of `apkanalyzer dex packages`, failing with an [IllegalArgumentException] that says why
         * where a row is of no shape it prints or no row holds the whole, since a figure misread would hold the gate to
         * nothing.
         */
        fun read(text: String): DexPackages {
            val packages = mutableMapOf<String, Long>()
            var total: Long? = null
            for ((index, line) in text.lines().withIndex()) {
                if (line.isBlank()) continue
                val row =
                    requireNotNull(ROW.matchEntire(line)) {
                        "line ${index + 1} is no row apkanalyzer prints: $line"
                    }
                val (kind, _, _, _, bytes, name) = row.destructured
                if (kind != "P") continue
                if (name == TOTAL) total = bytes.toLong() else packages[name] = bytes.toLong()
            }
            return DexPackages(requireNotNull(total) { "no row holds the whole, `$TOTAL`" }, packages)
        }
    }
}

/**
 * ResolvedVersions is the version of each module Trail's release build carries, as Gradle's resolution of its release
 * runtime classpath chose it, which the app's build writes one module a line, `group:name:version`, read from the
 * resolution result and never from a file's name; the modules of the build's own composite, the SDK's, are no
 * repository's modules and are left out.
 */
class ResolvedVersions(
    private val versions: Map<String, String>,
) {
    /** Returns the version [module], `group:name`, resolved at, or null where the classpath holds no such module. */
    fun versionOf(module: String): String? = versions[module]

    /** The reading of the list the app's build writes. */
    companion object {
        private val LINE = Regex("""^([^:\s]+):([^:\s]+):([^:\s]+)$""")

        /**
         * Reads [text], the list the app's build writes, failing with an [IllegalArgumentException] that says why where a
         * line is of no shape it writes or names a module twice, since a version misread would compare a share at a
         * release nothing recorded.
         */
        fun read(text: String): ResolvedVersions {
            val versions = linkedMapOf<String, String>()
            for ((index, line) in text.lines().withIndex()) {
                if (line.isBlank()) continue
                val match =
                    requireNotNull(LINE.matchEntire(line.trim())) {
                        "line ${index + 1} names no module at a version, `group:name:version`: $line"
                    }
                val (group, name, version) = match.destructured
                val module = "$group:$name"
                require(versions.put(module, version) == null) { "line ${index + 1} names $module a second time" }
            }
            return ResolvedVersions(versions)
        }
    }
}

/**
 * ReleaseGraph is the graph Gradle's resolution of Trail's release runtime classpath drew, as the app's build writes
 * it, one edge a line, `<from> -> <to>`, each end the app, `app`, one of the SDK's modules, `sdk:<name>`, or a library,
 * `group:name`, a constraint a platform states being no edge: what the gate reads which libraries the SDK brings from.
 */
class ReleaseGraph(
    private val edges: Map<String, List<String>>,
) {
    /**
     * Unnamed is a library an SDK module brings into the app that no share names, and the SDK module it reaches the app
     * through.
     */
    data class Unnamed(
        /** The library, `group:name`. */
        val library: String,
        /** The SDK module it reaches the app through, by its name. */
        val through: String,
    )

    /**
     * Returns every library an SDK module reaches without passing through a library a share names ([AppSize.NAMES]),
     * and the app reaches through no dependency of its own, with the SDK module it is first reached through, in the
     * order of their names. A named library's own dependencies are its release's to bring, as its share's version is,
     * and a library the app depends on, or reaches through one it depends on, is the app's: neither is the SDK's to
     * answer for.
     */
    fun unnamed(): List<Unnamed> {
        val apps = mutableSetOf<String>()
        val appPending = ArrayDeque(edges[APP].orEmpty().filterNot(::isSdk))
        while (appPending.isNotEmpty()) {
            val library = appPending.removeFirst()
            if (apps.add(library)) appPending += edges[library].orEmpty().filterNot(::isSdk)
        }
        val modules = (edges.keys + edges.values.flatten()).filter(::isSdk).toSortedSet()
        val seen = modules.toMutableSet()
        val pending = ArrayDeque(modules.map { it to it })
        val found = sortedMapOf<String, String>()
        while (pending.isNotEmpty()) {
            val (node, through) = pending.removeFirst()
            for (next in edges[node].orEmpty()) {
                if (isSdk(next) || AppSize.shareNaming(next) != null || next in apps) continue
                found.putIfAbsent(next, through.removePrefix(SDK))
                if (seen.add(next)) pending += next to through
            }
        }
        return found.map { (library, through) -> Unnamed(library, through) }
    }

    /** The reading of the graph the app's build writes. */
    companion object {
        /** The app's end of an edge. */
        const val APP = "app"

        /** What an SDK module's end of an edge opens with, before its name. */
        const val SDK = "sdk:"

        private val EDGE = Regex("""^(\S+) -> (\S+)$""")
        private val NODE = Regex("""^(app|sdk:[^:\s]+|[^:\s]+:[^:\s]+)$""")

        private fun isSdk(node: String): Boolean = node.startsWith(SDK)

        /**
         * Reads [text], the graph the app's build writes, failing with an [IllegalArgumentException] that says why where a
         * line is no edge of the shape it writes or no edge leaves the app, since a graph misread would hold the SDK to
         * no library it brings.
         */
        fun read(text: String): ReleaseGraph {
            val edges = linkedMapOf<String, MutableList<String>>()
            for ((index, line) in text.lines().withIndex()) {
                if (line.isBlank()) continue
                val edge =
                    requireNotNull(EDGE.matchEntire(line.trim())) {
                        "line ${index + 1} is no edge, `<from> -> <to>`: $line"
                    }
                val (from, to) = edge.destructured
                for (node in listOf(from, to)) {
                    require(NODE.matches(node)) {
                        "line ${index + 1} names $node, which is neither `app`, `sdk:<name>` nor `group:name`"
                    }
                }
                edges.getOrPut(from) { mutableListOf() } += to
            }
            require(APP in edges) { "no edge leaves the app, `$APP`" }
            return ReleaseGraph(edges)
        }
    }
}
