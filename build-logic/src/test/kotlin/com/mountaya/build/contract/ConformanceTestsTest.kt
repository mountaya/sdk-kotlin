package com.mountaya.build.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConformanceTestsTest {
    private fun request(
        runnerFiles: List<String>,
        module: String = "conformance",
        jvmRunnerFiles: List<String> = emptyList(),
        statesScopes: Boolean = false,
        registry: ContractRegistry? = sampleRegistry,
    ) = ConformanceTests.Request(
        module = module,
        ownership = sampleOwnership,
        registry = registry,
        scopes = sampleScopes,
        casesOf = { listOf("first case", "second-case") },
        runnerFiles = runnerFiles,
        jvmRunnerFiles = jvmRunnerFiles,
        runnerPackage = "com.mountaya.conformance",
        generatedPackage = "com.mountaya.conformance.runners.generated",
        statesScopes = statesScopes,
    )

    @Test
    fun aRunnerIsNamedAfterItsCapability() {
        assertEquals("TransportRetryBackoffRunner", ConformanceTests.runnerName("transport.retry.backoff"))
        assertEquals("TransportBrowserSecretKeyRunner", ConformanceTests.runnerName("transport.browser.secret-key"))
    }

    @Test
    fun aModuleOwnsWhatTheTableNamesItForAndNothingScoped() {
        val harness = ConformanceTests.generate(request(emptyList()))
        val geo = ConformanceTests.generate(request(emptyList(), module = "mountaya-geo"))

        assertEquals(listOf("transport.credentials", "geo.layers.order"), harness.manifest.owned)
        assertEquals(emptyList(), harness.manifest.runners)
        assertEquals(listOf("geo.camera.ground-meters"), geo.manifest.owned)
    }

    @Test
    fun eachCaseOfAHeldRunnerBecomesOneTestThatNamesItsCapabilityAndCase() {
        val generated = ConformanceTests.generate(request(listOf("GeoLayersOrderRunner")))
        val source = generated.files.getValue("GeoLayersOrderConformanceTest.kt")

        assertEquals(listOf("geo.layers.order"), generated.manifest.runners.map { it.id })
        assertEquals(
            "com.mountaya.conformance.runners.generated.GeoLayersOrderConformanceTest",
            generated.manifest.runners
                .single()
                .className,
        )
        assertTrue(source.startsWith("// GENERATED FILE. DO NOT EDIT."))
        assertTrue(source.contains("import com.mountaya.conformance.GeoLayersOrderRunner"))
        assertTrue(
            source.contains(
                "fun first_case(): TestResult = " +
                    "runConformanceCase(GeoLayersOrderRunner, \"geo.layers.order\", \"first case\")",
            ),
        )
        assertTrue(source.contains("fun second_case(): TestResult"))
    }

    /**
     * An entry runs whole on a target or not at all there: a runner in the jvm tests has its tests
     * generated into the jvm tests, and none into the common tests every target runs.
     */
    @Test
    fun aRunnerInTheJvmTestsHasItsTestsGeneratedThereAlone() {
        val generated =
            ConformanceTests.generate(
                request(listOf("GeoLayersOrderRunner"), jvmRunnerFiles = listOf("TransportCredentialsRunner")),
            )

        assertEquals(setOf("GeoLayersOrderConformanceTest.kt"), generated.files.keys)
        assertEquals(setOf("TransportCredentialsConformanceTest.kt"), generated.jvmFiles.keys)
        assertEquals(
            listOf("transport.credentials", "geo.layers.order"),
            generated.manifest.runners.map { it.id },
        )
        assertTrue(
            generated.jvmFiles
                .getValue("TransportCredentialsConformanceTest.kt")
                .contains("runConformanceCase(TransportCredentialsRunner, \"transport.credentials\", \"first case\")"),
        )
    }

    @Test
    fun aRunnerInBothTheCommonAndTheJvmTestsIsRefused() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                ConformanceTests.generate(
                    request(
                        listOf("TransportCredentialsRunner"),
                        jvmRunnerFiles = listOf("TransportCredentialsRunner"),
                    ),
                )
            }

        assertTrue(error.message.orEmpty().startsWith("conformance: TransportCredentialsRunner sits in both"))
    }

    @Test
    fun aRunnerForAnythingTheModuleDoesNotOwnIsAStray() {
        val runners = listOf("GeoLayersOrderRunner", "TransportRetryBackoffRunner", "StudioShareLinksRunner")
        val generated =
            ConformanceTests.generate(request(runners, jvmRunnerFiles = listOf("MapEditSnapRunner")))

        assertEquals(
            listOf("MapEditSnapRunner", "StudioShareLinksRunner", "TransportRetryBackoffRunner"),
            generated.manifest.strays,
        )
        assertFalse(generated.files.containsKey("TransportRetryBackoffConformanceTest.kt"))
        assertEquals(emptyMap(), generated.jvmFiles)
    }

    @Test
    fun theModuleThatStatesScopesStatesEachOneWithItsSentence() {
        val generated = ConformanceTests.generate(request(emptyList(), statesScopes = true))
        val source = generated.files.getValue("ScopedCapabilitiesConformanceTest.kt")

        assertTrue(source.contains("fun transport_browser_secret_key__scoped() = Unit"))
        assertTrue(source.contains("fun studio_share_links__scoped() = Unit"))
        assertTrue(source.contains("SPEC.md section 11: No studio layer."))
    }

    @Test
    fun anAbsentCheckoutSaysSoInATestAndOwnsNothing() {
        val generated = ConformanceTests.generate(request(emptyList(), statesScopes = true, registry = null))

        assertEquals(setOf("ContractCheckoutConformanceTest.kt"), generated.files.keys)
        assertTrue(
            generated.files
                .getValue("ContractCheckoutConformanceTest.kt")
                .contains("fun the_contract_checkout_is_absent_so_no_fixture_ran() = Unit"),
        )
        assertEquals(emptyList(), generated.manifest.owned)
    }

    @Test
    fun aCaseNameBecomesALegalAndUniqueFunctionName() {
        val names =
            ConformanceTests
                .functionNames(listOf("a.b/c", "a-b-c", "2xx", "fun", "", "déjà vu"))
                .map { it.function }

        assertEquals(listOf("a_b_c", "a_b_c_2", "case_2xx", "case_fun", "case_", "d_j__vu"), names)
    }

    @Test
    fun aManifestReadsBackAsItWasWritten() {
        val generated = ConformanceTests.generate(request(listOf("GeoLayersOrderRunner")))
        val file = temporaryFile("manifest.json", generated.manifest.encode())

        assertEquals(generated.manifest, ConformanceManifest.decode(file))
    }
}
