package com.mountaya.build.contract

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class ConformanceReportTest {
    private val manifests =
        listOf(
            ConformanceManifest(
                "conformance",
                listOf("transport.credentials", "geo.layers.order"),
                listOf(
                    RunnerManifest(
                        "transport.credentials",
                        "a.TransportCredentialsConformanceTest",
                        listOf(CaseManifest("one", "one"), CaseManifest("two", "two")),
                    ),
                    RunnerManifest(
                        "geo.layers.order",
                        "a.GeoLayersOrderConformanceTest",
                        listOf(CaseManifest("one", "one"), CaseManifest("two", "two")),
                    ),
                ),
                emptyList(),
            ),
        )

    @Test
    fun aCapabilityPassesOnlyWhenEveryCasePassed() {
        val passed =
            setOf(
                "a.TransportCredentialsConformanceTest" to "one",
                "a.TransportCredentialsConformanceTest" to "two",
                "a.GeoLayersOrderConformanceTest" to "one",
            )
        val statuses = ConformanceReport.statuses(sampleRegistry, sampleScopes, manifests, passed)

        // A capability whose module is not in the build yet has no runner, and is a `fail` like any other.
        assertEquals(
            mapOf(
                "geo.camera.ground-meters" to "fail",
                "geo.layers.order" to "fail",
                "map.edit.snap" to "fail",
                "studio.share.links" to "scoped",
                "transport.browser.secret-key" to "scoped",
                "transport.credentials" to "pass",
                "transport.retry.backoff" to "fail",
            ),
            statuses,
        )
        assertEquals(statuses.keys.sorted(), statuses.keys.toList())
    }

    /**
     * The keys are the ones `PARITY.md` and the contract's gate read, and the version is the registry's own,
     * carried rather than pinned: a report keyed on another version counts as no report at all, and one whose
     * digest is not the contract's files' fails that gate.
     */
    @Test
    fun theReportIsSortedTwoSpacesAndEndsInANewline() {
        val text =
            ConformanceReport.render(
                mapOf("b.c" to "fail", "a.b" to "pass"),
                registryVersion = sampleRegistry.version,
                modelDigest = DIGEST,
            )

        assertEquals(
            """
            {
              "capabilities": {
                "a.b": "pass",
                "b.c": "fail"
              },
              "modelDigest": "$DIGEST",
              "registryVersion": 6,
              "sdk": "kotlin"
            }

            """.trimIndent(),
            text,
        )
    }

    /**
     * Every capability whose `kotlin` column is a list, the studio's `scoped` one left out, each with
     * the names of its column the dumps declare, in the column's order.
     */
    @Test
    fun publishedHoldsTheDeclaredNamesOfEveryKotlinListInItsColumnsOrder() {
        val registry =
            sampleRegistry.copy(
                capabilities =
                    sampleRegistry.capabilities.map { capability ->
                        if (capability.id != "geo.layers.order") {
                            capability
                        } else {
                            capability.copy(
                                published = columns(names("tileSourceLayerRanks", "tileLayerRank", "layerRankOf")),
                            )
                        }
                    },
            )

        assertEquals(
            mapOf(
                "transport.credentials" to listOf("Credential", "Credential.headers", "ConfigurationException"),
                "geo.layers.order" to listOf("tileSourceLayerRanks", "tileLayerRank"),
            ),
            ConformanceReport.published(registry, sampleApi),
        )
        assertEquals(
            mapOf("transport.credentials" to emptyList(), "geo.layers.order" to emptyList<String>()),
            ConformanceReport.published(sampleRegistry, ApiDeclarations.read(emptyList())),
        )
    }

    @Test
    fun publishedIsRenderedBetweenTheCapabilitiesAndTheVersionItsIdsSortedItsNamesInOrder() {
        val text =
            ConformanceReport.render(
                mapOf("a.b" to "pass", "b.c" to "fail"),
                registryVersion = 11,
                modelDigest = DIGEST,
                published = mapOf("b.c" to listOf("tileSourceLayerRanks", "tileLayerRank"), "a.b" to emptyList()),
            )

        assertEquals(
            """
            {
              "capabilities": {
                "a.b": "pass",
                "b.c": "fail"
              },
              "modelDigest": "$DIGEST",
              "published": {
                "a.b": [],
                "b.c": [
                  "tileSourceLayerRanks",
                  "tileLayerRank"
                ]
              },
              "registryVersion": 11,
              "sdk": "kotlin"
            }

            """.trimIndent(),
            text,
        )
    }

    @Test
    fun aTestPassedOnlyWithNoFailureErrorOrSkipAndItsTargetSuffixIsDropped() {
        val directory = Files.createTempDirectory("results").toFile()
        directory.resolve("TEST-a.GeoLayersOrderConformanceTest.xml").writeText(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <testsuite name="a.GeoLayersOrderConformanceTest" tests="4">
              <testcase name="one[jvm]" classname="a.GeoLayersOrderConformanceTest"/>
              <testcase name="two[jvm]" classname="a.GeoLayersOrderConformanceTest">
                <failure message="geo.layers.order · two: expected 1.0, got 2.0">trace</failure>
              </testcase>
              <testcase name="three" classname="a.GeoLayersOrderConformanceTest"><skipped/></testcase>
              <testcase name="four" classname="a.GeoLayersOrderConformanceTest"><error/></testcase>
            </testsuite>
            """.trimIndent(),
        )

        assertEquals(
            setOf("a.GeoLayersOrderConformanceTest" to "one"),
            ConformanceReport.passedTests(listOf(directory)),
        )
    }

    private companion object {
        /** A digest no file of the sample registry is the digest of. */
        const val DIGEST = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
    }
}
