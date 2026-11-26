package com.mountaya.build.gates

import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/**
 * BoundaryGate is the boundary table of SPEC section 7, as a scan of what each module's sources name.
 *
 * A rule that holds only where someone remembered it is a convention. This reads every import
 * and every qualified name of every main source set, and fails on a package or a type the
 * module's row of the table says it never names. What a module reaches transitively is the
 * other half, and the runtime classpath walk holds that.
 *
 * A module's hand-written files declare one package, the module's, and the directory a file sits
 * in names its topic and its kind (SPEC section 6). So the table names a topic or a kind by the
 * path its directory spells, `com.mountaya.client.transport` for the client's transport, and the
 * scan reads every name of a module where it is declared: `com.mountaya.client.Mountaya` is read
 * as `com.mountaya.client.transport.engines.Mountaya`, since `Mountaya.kt` sits in
 * `com/mountaya/client/transport/engines/`. A file is placed by its directory the same way.
 */
object BoundaryGate {
    /** The task name the root build registers this gate under. */
    const val NAME = "checkBoundaries"

    /**
     * Refused is a package a module never names, unless the naming file sits in the directory [unlessIn] or the name is
     * under one of [unlessUnder]. A topic or a kind of a module is named by the path its directory spells.
     */
    data class Refused(
        /** The package refused, and every package beneath it. */
        val prefix: String,
        /** The sentence a violation gives for the refusal. */
        val because: String,
        /** The directory whose files may name it, or null where none may. */
        val unlessIn: String? = null,
        /** The names under the refused package that stay allowed. */
        val unlessUnder: List<String> = emptyList(),
    )

    /** RefusedName is a type a module never names, unless the naming file sits in the directory [unlessIn]. */
    data class RefusedName(
        /** The type refused, by its full name. */
        val name: String,
        /** The sentence a violation gives for the refusal. */
        val because: String,
        /** The directory whose files may name it, or null where none may. */
        val unlessIn: String? = null,
    )

    /** Boundary is one row of the table. */
    data class Boundary(
        /** The packages the row's module never names. */
        val packages: List<Refused>,
        /** The types the row's module never names. */
        val names: List<RefusedName> = emptyList(),
    )

    /** The directory of the platform's exchange and the engines it sends through (SPEC section 4). */
    private const val PLATFORM_EXCHANGE = "com.mountaya.client.seams.engines"
    private const val FERROSTAR = "com.stadiamaps.ferrostar"

    /** The client's wire vocabulary, the one part of it navigation names: what Geo's own signatures carry. */
    private val WIRE_VOCABULARY = listOf("com.mountaya.client.generated", "com.mountaya.client.geojson")
    private const val FERROSTAR_FFI = "uniffi.ferrostar"

    /** The one directory of `mountaya-elements` that names Android, Compose, MapLibre or OkHttp: the renderer. */
    private const val RENDERER = "com.mountaya.elements.renderer"

    /** The renderer's engines, the one place a layer is added to MapLibre's map, in the registry's order. */
    private const val RENDERER_ENGINES = "com.mountaya.elements.renderer.engines"

    /** MapLibre's calls that add a layer to its style, each a place a layer could skip the one registry. */
    private val ADD_LAYER = listOf("addLayer", "addLayerAt", "addLayerAbove", "addLayerBelow")

    private val android =
        listOf(
            Refused("android", "it is an Android platform package"),
            Refused("androidx", "it is AndroidX, Compose included"),
        )

    /** Geo's seams, the one topic of `mountaya-geo` that reads the platform (SPEC section 6). */
    private const val GEO_SEAMS = "com.mountaya.geo.seams"

    /**
     * The two Android names Geo's seams read: the platform's ICU, which writes an instant on android from API
     * 24, since android's `java.text` follows the device's 12- or 24-hour setting (SPEC section 8), and the
     * release a device runs, which says whether it carries ICU.
     */
    private val ANDROID_LOCALE_SERVICES = listOf("android.icu", "android.os.Build")

    /**
     * Geo names no Android package but [ANDROID_LOCALE_SERVICES], and names those in its seams alone: the
     * gate's exemptions are each enough alone, so the names are let past the first row and held to the
     * directory by the rows after it.
     */
    private val geoAndroid =
        listOf(
            Refused("android", "it is an Android platform package", unlessUnder = ANDROID_LOCALE_SERVICES),
            Refused("androidx", "it is AndroidX, Compose included"),
        ) +
            ANDROID_LOCALE_SERVICES.map { name ->
                Refused(name, "Geo reads the platform's locale services in its seams alone", unlessIn = GEO_SEAMS)
            }
    private val renderers =
        listOf(
            Refused("org.maplibre", "MapLibre belongs to mountaya-elements alone"),
        )

    /** Why no module names a navigation library. */
    private const val NO_NAVIGATION_LIBRARY = "the navigation is Geo's machine with its guidance, and links no library"

    /** A navigation library's packages, which no module names, `mountaya-navigation` least of all. */
    private val navigationLibraries =
        listOf(
            Refused(FERROSTAR, NO_NAVIGATION_LIBRARY),
            Refused(FERROSTAR_FFI, NO_NAVIGATION_LIBRARY),
        )
    private val network =
        listOf(
            Refused("io.ktor", "this module never reaches the network"),
            Refused("java.net", "this module never reaches the network"),
            Refused("okhttp3", "this module never reaches the network"),
        )

    /** Why the admin unit opens no connection: its calls leave through the core's transport. */
    private const val THROUGH_THE_CORE =
        "the admin unit sends through the core's transport, and opens no connection of its own"

    /** Why the admin unit names no module but the core: it depends on `mountaya-client` alone. */
    private const val CORE_ALONE = "the admin unit depends on the core alone, `mountaya-client`"

    /** The table, keyed by module. A module with no row is not scanned. */
    val TABLE: Map<String, Boundary> =
        mapOf(
            "mountaya-client" to
                Boundary(
                    packages =
                        android + renderers + navigationLibraries +
                            listOf(
                                Refused(
                                    "io.ktor",
                                    "the platform's exchange is the one type that reaches the network",
                                    unlessIn = PLATFORM_EXCHANGE,
                                ),
                                Refused(
                                    "okhttp3",
                                    "the platform's exchange is the one type that reaches the network",
                                    unlessIn = PLATFORM_EXCHANGE,
                                ),
                            ),
                    names =
                        listOf(
                            RefusedName(
                                "HttpClient",
                                "Ktor's client is named by the platform's exchange and nowhere else",
                                unlessIn = PLATFORM_EXCHANGE,
                            ),
                        ),
                ),
            "mountaya-admin" to
                Boundary(
                    packages =
                        android + renderers + navigationLibraries +
                            listOf(
                                Refused("io.ktor", THROUGH_THE_CORE),
                                Refused("java.net", THROUGH_THE_CORE),
                                Refused("okhttp3", THROUGH_THE_CORE),
                                Refused("com.mountaya.geo", CORE_ALONE),
                                Refused("com.mountaya.elements", CORE_ALONE),
                                Refused("com.mountaya.navigation", CORE_ALONE),
                            ),
                ),
            "mountaya-geo" to
                Boundary(
                    packages =
                        network + geoAndroid + renderers + navigationLibraries +
                            listOf(
                                Refused("com.mountaya.client.transport", "the geo layer never names the transport"),
                                Refused("com.mountaya.client.session", "the geo layer never names the session engine"),
                            ),
                    names =
                        listOf(
                            RefusedName("Mountaya", "the geo layer never names the client"),
                            RefusedName("SessionTokenEngine", "the geo layer never names the session engine"),
                        ),
                ),
            "mountaya-navigation" to
                Boundary(
                    packages =
                        network + navigationLibraries +
                            listOf(
                                Refused("androidx.compose", "navigation renders nothing"),
                                Refused("org.maplibre", "MapLibre belongs to mountaya-elements alone"),
                                Refused(
                                    "com.mountaya.client",
                                    "navigation names nothing of the client beyond its wire vocabulary: " +
                                        "the generated types and the GeoJSON values",
                                    unlessUnder = WIRE_VOCABULARY,
                                ),
                            ),
                    names = listOf(RefusedName("Mountaya", "navigation never names the client")),
                ),
            "mountaya-elements" to
                Boundary(
                    packages =
                        navigationLibraries +
                            listOf(
                                Refused(
                                    "com.mountaya.navigation",
                                    "the views render the state Geo declares, never the engine",
                                ),
                                Refused(
                                    "androidx.compose.material",
                                    "Material is a design system, and the views bring none",
                                ),
                                Refused(
                                    "androidx.compose.material3",
                                    "Material is a design system, and the views bring none",
                                ),
                                Refused("com.google.android.gms.maps", "MapLibre is the one map engine"),
                                Refused("com.mapbox", "MapLibre is the one map engine"),
                                Refused("io.ktor", "the views open no connection of their own"),
                                Refused(
                                    "android",
                                    "the map's state, its engines and the `map` rules name no Android type, and the renderer alone does",
                                    unlessIn = RENDERER,
                                ),
                                Refused(
                                    "androidx",
                                    "the map's state, its engines and the `map` rules name no Compose or AndroidX type, and the renderer alone does",
                                    unlessIn = RENDERER,
                                ),
                                Refused(
                                    "org.maplibre",
                                    "the map's state, its engines and the `map` rules name no MapLibre type, and the renderer alone does",
                                    unlessIn = RENDERER,
                                ),
                                Refused(
                                    "okhttp3",
                                    "OkHttp is the client MapLibre fetches with, which the renderer's request hook alone names",
                                    unlessIn = RENDERER,
                                ),
                            ),
                    names =
                        ADD_LAYER.map { call ->
                            RefusedName(
                                call,
                                "a layer is added to MapLibre's map by the renderer alone, in the order the one layer registry ranks it",
                                unlessIn = RENDERER_ENGINES,
                            )
                        },
                ),
        )

    /** Runs the gate over the main source sets of [trees]. */
    fun run(trees: List<SourceTree>): GateResult {
        val violations = mutableListOf<String>()
        var checked = 0

        KotlinParser().use { parser ->
            val parsed = trees.filterNot { it.isTest }.kotlinFiles().associateWith { parser.parse(it.file) }
            val declared = Declared(parsed)
            for ((source, file) in parsed) {
                val boundary = TABLE[source.tree.module] ?: continue
                checked += 1
                violations += scan(source, file, boundary, declared)
            }
        }
        return GateResult(NAME, checked, 0, violations.distinct())
    }

    /**
     * Where each top-level declaration of the scanned modules is declared: its qualified name, under the
     * package its file declares, mapped to the directory its file sits in, read with dots. A name a module
     * declares in two directories, an overload, sits in both.
     */
    internal class Declared(
        parsed: Map<SourceFile, KtFile>,
    ) {
        private val places = mutableMapOf<String, MutableSet<String>>()
        private val packages = mutableMapOf<String, MutableSet<String>>()

        init {
            for ((source, file) in parsed) {
                val declaredPackage = file.packageFqName.asString()
                for (declaration in file.declarations.filterIsInstance<KtNamedDeclaration>()) {
                    val name = declaration.name ?: continue
                    places.getOrPut("$declaredPackage.$name") { sortedSetOf() } += source.place
                    packages.getOrPut(declaredPackage) { sortedSetOf() } += source.place
                }
            }
        }

        /**
         * [name] as the directories its declaration sits in spell it: the longest part of it a module
         * declares, its package read as its file's directory, the rest kept. A name no module declares is
         * read as written.
         */
        fun locate(name: String): List<String> {
            val parts = name.split('.')
            for (end in parts.size downTo 2) {
                val found = places[parts.subList(0, end).joinToString(".")] ?: continue
                val declared = parts.subList(end - 1, parts.size).joinToString(".")
                return found.map { "$it.$declared" }
            }
            return listOf(name)
        }

        /** What a star import of [packageName] names: every directory its declarations sit in, or the package. */
        fun locateAll(packageName: String): List<String> = packages[packageName]?.toList() ?: listOf(packageName)
    }

    private fun scan(
        source: SourceFile,
        file: KtFile,
        boundary: Boundary,
        declared: Declared,
    ): List<String> {
        val place = source.place
        val problems = mutableListOf<String>()

        // What the file names by package: its imports, and any name it spells out in full. A name is read
        // whole, never the prefixes it is built of, so `com.mountaya.client.generated.RestSchema` names
        // the generated package and not `com.mountaya.client`; an import is read by its own name. A name
        // a module declares is read where it is declared, and a star import names every directory of the
        // package it imports.
        val imports =
            file.importDirectives.flatMap { directive ->
                val name = directive.importedFqName?.asString() ?: return@flatMap emptyList()
                val located = if (directive.isAllUnder) declared.locateAll(name) else declared.locate(name)
                located.map { it to directive.line() }
            }
        val expressions =
            file
                .collectDescendantsOfType<KtDotQualifiedExpression> {
                    it.parent !is KtDotQualifiedExpression && it.getStrictParentOfType<KtImportDirective>() == null
                }.flatMap { expression ->
                    declared.locate(expression.text.substringBefore('(')).map { it to expression.line() }
                }
        val types =
            file
                .collectDescendantsOfType<KtUserType> { it.parent !is KtUserType }
                .flatMap { type -> declared.locate(type.text.substringBefore('<')).map { it to type.line() } }
        val qualified = imports + expressions + types

        for ((name, line) in qualified) {
            for (refused in boundary.packages) {
                if (!name.isUnder(refused.prefix)) continue
                if (refused.unlessIn != null && place.isUnder(refused.unlessIn)) continue
                if (refused.unlessUnder.any { name.isUnder(it) }) continue
                problems += "${source.display}:$line: names `${refused.prefix}`, and ${refused.because}."
            }
        }

        val references =
            file.collectDescendantsOfType<KtNameReferenceExpression>().map { it.getReferencedName() to it.line() }
        val typeNames =
            file.collectDescendantsOfType<KtUserType>().mapNotNull { type ->
                type.referencedName?.let { name -> name to type.line() }
            }
        val simple = references + typeNames

        for ((name, line) in simple) {
            for (refused in boundary.names) {
                if (name != refused.name) continue
                if (refused.unlessIn != null && place.isUnder(refused.unlessIn)) continue
                problems += "${source.display}:$line: names `${refused.name}`, and ${refused.because}."
            }
        }
        return problems
    }

    private fun String.isUnder(prefix: String): Boolean = this == prefix || startsWith("$prefix.")
}
