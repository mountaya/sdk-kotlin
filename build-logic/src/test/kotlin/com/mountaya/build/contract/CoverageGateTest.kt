package com.mountaya.build.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoverageGateTest {
    private fun manifest(
        module: String,
        owned: List<String>,
        runners: List<String> = owned,
        strays: List<String> = emptyList(),
    ) = ConformanceManifest(
        module,
        owned,
        runners.map { RunnerManifest(it, "$module.Test", listOf(CaseManifest("case", "case"))) },
        strays,
    )

    private val built =
        listOf(
            manifest("mountaya-client", listOf("transport.retry.backoff")),
            manifest("conformance", listOf("transport.credentials", "geo.layers.order")),
        )

    private val complete =
        built +
            listOf(
                manifest("mountaya-geo", listOf("geo.camera.ground-meters")),
                manifest("mountaya-elements", listOf("map.edit.snap")),
            )

    private fun run(
        manifests: List<ConformanceManifest>,
        ownership: Ownership = sampleOwnership,
        scopes: Scopes = sampleScopes,
        spec: String = sampleSpec,
        api: ApiDeclarations = sampleApi,
        registry: ContractRegistry = sampleRegistry,
    ) = CoverageGate.run(registry, scopes, ownership, manifests, spec, ci = false, api = api)

    /** What [sampleKlibDump] declares without its lines that mention any of [left]. */
    private fun apiWithout(vararg left: String): ApiDeclarations {
        val kept = sampleKlibDump.lines().filterNot { line -> left.any { it in line } }
        return ApiDeclarations.read(listOf(kept.joinToString("\n")))
    }

    /** [sampleRegistry] with the `kotlin` column of [id] in place of the one it writes. */
    private fun withKotlinColumn(
        id: String,
        kotlin: PublishedColumn,
    ) = sampleRegistry.copy(
        capabilities =
            sampleRegistry.capabilities.map { capability ->
                val published = capability.published
                if (capability.id != id || published == null) {
                    capability
                } else {
                    capability.copy(published = published.copy(kotlin = kotlin))
                }
            },
    )

    /**
     * The studio's `ShareLink` is TypeScript's column alone, and the studio is scoped here as this
     * SDK's column scopes it, so nothing of it is looked for.
     */
    @Test
    fun everyCapabilityRunOrScopedAndEveryNameOfOneCarriedDeclaredPasses() {
        val result = run(complete)

        assertEquals(emptyList(), result.violations)
        assertEquals(
            "checkConformanceCoverage: 7 capabilities, 5 with a runner, 2 scoped, 0 with no runner; " +
                "5 of 5 published names declared",
            result.summary,
        )
    }

    @Test
    fun aPublishedNameNoDumpDeclaresFailsNamingItsCapability() {
        val result = run(complete, api = apiWithout("Credential.headers", "ConfigurationException"))

        assertEquals(
            listOf(
                "`transport.credentials` is published here as `Credential.headers`, and no module's ABI dump " +
                    "declares it.",
                "`transport.credentials` is published here as `ConfigurationException`, and no module's ABI dump " +
                    "declares it.",
            ),
            result.violations,
        )
        assertTrue(result.summary.endsWith("3 of 5 published names declared"), result.summary)
    }

    /** The column is this SDK's own spelling, so a name another SDK spells, or another case, is no name here. */
    @Test
    fun aNameSpelledOtherwiseThanTheDumpsDeclareItFails() {
        val registry =
            withKotlinColumn(
                "transport.credentials",
                names("Credential", "credential.Headers", "ConfigurationError", "Credential.Companion.headers"),
            )
        val result = run(complete, registry = registry)

        assertEquals(
            listOf("credential.Headers", "ConfigurationError", "Credential.Companion.headers").map {
                "`transport.credentials` is published here as `$it`, and no module's ABI dump declares it."
            },
            result.violations,
        )
    }

    @Test
    fun aCapabilityScopedByIdIsNeverLookedForUnderItsNames() {
        val scopes =
            Scopes(
                layers = sampleScopes.layers,
                ids = sampleScopes.ids + ("geo.layers.order" to "No studio layer."),
            )
        val manifests =
            complete.map { held ->
                if (held.module == "conformance") manifest("conformance", listOf("transport.credentials")) else held
            }
        val registry = withKotlinColumn("geo.layers.order", PublishedColumn.Scoped)

        val result = run(manifests, scopes = scopes, api = apiWithout("tile"), registry = registry)

        assertEquals(emptyList(), result.violations)
        assertTrue(result.summary.endsWith("3 of 3 published names declared"), result.summary)
    }

    @Test
    fun aKotlinColumnThatDisagreesWithThisSdksScopesFailsEitherWay() {
        val scopedHere =
            Scopes(
                layers = sampleScopes.layers,
                ids = sampleScopes.ids + ("geo.layers.order" to "No studio layer."),
            )
        val manifests =
            complete.map { held ->
                if (held.module == "conformance") manifest("conformance", listOf("transport.credentials")) else held
            }

        assertEquals(
            listOf(
                "`geo.layers.order`: the registry's `kotlin` column names `tileLayerRank`, `tileSourceLayerRanks`, " +
                    "and this SDK scopes the capability (\"No studio layer.\"): the column is `scoped`.",
            ),
            run(manifests, scopes = scopedHere).violations,
        )
        assertEquals(
            listOf(
                "`geo.layers.order`: the registry's `kotlin` column is `scoped`, and this SDK carries the " +
                    "capability: no bullet of SPEC.md section 11 scopes it.",
            ),
            run(complete, registry = withKotlinColumn("geo.layers.order", PublishedColumn.Scoped)).violations,
        )
    }

    /** The control: dumps read to nothing fail every name, and the reason is not the names. */
    @Test
    fun noDumpDeclaringAnythingIsSaidAsSuchBeforeTheNamesItLeavesUndeclared() {
        val result = run(complete, api = ApiDeclarations.read(emptyList()))

        assertEquals(
            "No ABI dump declares anything, so no published name could be looked for in one.",
            result.violations.first(),
        )
        assertEquals(6, result.violations.size)
    }

    @Test
    fun aCapabilityWithNoRunnerIsListedUnderTheModuleTheTableNamesForIt() {
        val manifests =
            listOf(
                manifest("mountaya-client", listOf("transport.retry.backoff"), runners = emptyList()),
                manifest("conformance", listOf("transport.credentials", "geo.layers.order"), runners = emptyList()),
            )
        val result = run(manifests)

        assertEquals(
            listOf(
                "mountaya-client: 1 capability has no runner: transport.retry.backoff.",
                "conformance: 2 capabilities have no runner: transport.credentials, geo.layers.order.",
                "mountaya-geo, not in the build yet: 1 capability has no runner: geo.camera.ground-meters.",
                "mountaya-elements, not in the build yet: 1 capability has no runner: map.edit.snap.",
            ),
            result.violations,
        )
        assertTrue(result.summary.contains("0 with a runner, 2 scoped, 5 with no runner;"), result.summary)
    }

    /**
     * A module is named in the table before it is in the build, so its entries are the work left
     * rather than entries nothing could ever run.
     */
    @Test
    fun aModuleNotInTheBuildYetHasItsEntriesListedAsHavingNoRunner() {
        val result = run(built)

        assertEquals(
            listOf(
                "mountaya-geo, not in the build yet: 1 capability has no runner: geo.camera.ground-meters.",
                "mountaya-elements, not in the build yet: 1 capability has no runner: map.edit.snap.",
            ),
            result.violations,
        )
        assertTrue(result.violations.none { "owns" in it || "no module" in it })
    }

    @Test
    fun aLayerOrAFlagTheTableNamesNoModuleForFails() {
        val ownership =
            Ownership(
                apiModules = sampleOwnership.apiModules,
                internalModules = sampleOwnership.internalModules - "map",
            )

        assertEquals(
            listOf(
                "`map.edit.snap`: the root build names no module for `map` entries with `api: false`, " +
                    "so nothing could run it.",
            ),
            run(complete, ownership).violations,
        )
    }

    @Test
    fun aScopedEntryNeedsNoModule() {
        val ownership =
            Ownership(
                apiModules = sampleOwnership.apiModules - "studio",
                internalModules = sampleOwnership.internalModules,
            )

        assertEquals(emptyList(), run(complete, ownership).violations)
    }

    @Test
    fun aScopeWithNoBulletInSectionElevenFails() {
        val spec = sampleSpec.replace("- **No studio layer.**", "- No studio layer.")
        val result = run(complete, spec = spec)

        assertEquals(1, result.violations.size)
        assertTrue(result.violations.single().contains("\"No studio layer.\""))
    }

    @Test
    fun aScopeForSomethingTheRegistryDoesNotCarryFails() {
        val scopes =
            Scopes(
                layers = sampleScopes.layers + ("video" to "No studio layer."),
                ids = sampleScopes.ids + ("geo.gone" to "No studio layer."),
            )

        assertEquals(
            listOf(
                "The layer `video` is scoped, and the registry has no such layer.",
                "`geo.gone` is scoped, and the registry carries no such capability.",
            ),
            run(complete, scopes = scopes).violations,
        )
    }

    @Test
    fun aStrayRunnerFails() {
        val manifests =
            built +
                listOf(
                    manifest("mountaya-geo", listOf("geo.camera.ground-meters"), strays = listOf("GeoGoneRunner")),
                    manifest("mountaya-elements", listOf("map.edit.snap")),
                )
        val result = run(manifests)

        assertEquals(1, result.violations.size)
        assertTrue(result.violations.single().startsWith("mountaya-geo: `GeoGoneRunner` runs nothing"))
    }

    @Test
    fun anAbsentCheckoutIsToleratedLocallyAndRefusedOnCi() {
        val local =
            CoverageGate.run(null, sampleScopes, sampleOwnership, emptyList(), sampleSpec, ci = false, api = sampleApi)
        val ci =
            CoverageGate.run(null, sampleScopes, sampleOwnership, emptyList(), sampleSpec, ci = true, api = sampleApi)

        assertEquals(emptyList(), local.violations)
        assertEquals(1, ci.violations.size)
    }
}
