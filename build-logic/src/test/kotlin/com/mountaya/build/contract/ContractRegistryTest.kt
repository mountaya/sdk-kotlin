package com.mountaya.build.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContractRegistryTest {
    private fun registry(text: String) = ContractRegistry.read(temporaryFile("registry.yaml", text.trimIndent()))

    @Test
    fun aRegistryIsReadInItsOrder() {
        val read =
            registry(
                """
                version: 6
                capabilities:
                  - id: transport.retry.backoff
                    layer: transport
                    api: false
                    rule: One sentence.
                    fixture: transport/retry-backoff.json
                  - id: geo.layers.order
                    layer: geo
                    api: true
                    rule: One sentence.
                    fixture: geo/layers-order.json
                """,
            )

        assertEquals(6, read.version)
        assertEquals(listOf("transport.retry.backoff", "geo.layers.order"), read.capabilities.map { it.id })
        assertEquals(Capability("geo.layers.order", "geo", true, "geo/layers-order.json"), read.capabilities[1])
    }

    /**
     * The version moves with the contract's expected values, not with the file's format, so any
     * version is read and carried into the report rather than refused.
     */
    @Test
    fun noVersionIsPinnedAndTheVersionReadIsCarried() {
        for (version in listOf(1, 6, 7, 40)) {
            val read =
                registry(
                    """
                    version: $version
                    capabilities:
                      - { id: a.b, layer: geo, api: true, fixture: a.json }
                    """,
                )

            assertEquals(version, read.version)
        }
    }

    @Test
    fun aVersionThatIsNotAPositiveIntegerIsRefused() {
        for (version in listOf("0", "-1", "\"6\"", "6.5", "~")) {
            val error =
                assertFailsWith<IllegalArgumentException> {
                    registry(
                        """
                        version: $version
                        capabilities:
                          - { id: a.b, layer: geo, api: true, fixture: a.json }
                        """,
                    )
                }

            assertTrue(error.message.orEmpty().contains("`version` must be a positive integer"), version)
        }
    }

    @Test
    fun everyLayerTheRegistryNamesIsRead() {
        val read =
            registry(
                """
                version: 6
                capabilities:
                  - { id: transport.a, layer: transport, api: true, fixture: transport/a.json }
                  - { id: geo.a, layer: geo, api: false, fixture: geo/a.json }
                  - { id: map.a, layer: map, api: true, fixture: map/a.json }
                  - { id: studio.a, layer: studio, api: true, fixture: studio/a.json }
                """,
            )

        assertEquals(listOf("transport", "geo", "map", "studio"), read.capabilities.map { it.layer })
    }

    @Test
    fun anIdDeclaredTwiceIsRefused() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                registry(
                    """
                    version: 6
                    capabilities:
                      - { id: a.b, layer: geo, api: true, fixture: a.json }
                      - { id: a.b, layer: geo, api: true, fixture: b.json }
                    """,
                )
            }

        assertTrue(error.message.orEmpty().contains("declared twice: a.b"))
    }

    @Test
    fun eachSdksColumnIsReadInItsOrderScopedIncludedAndAnEntryStatingNoneHasNone() {
        val read =
            registry(
                """
                version: 11
                capabilities:
                  - id: map.camera.pose
                    layer: map
                    api: true
                    rule: One sentence.
                    fixture: map/camera-pose.json
                    published:
                      typescript:
                        - MountayaMap.jump
                        - CameraTarget
                      dart:
                        - MapEngine.jump
                      swift:
                        - MapEngine.jump(to:)
                      kotlin:
                        - MapEngine.jump
                        - ApiException.IdempotentAnswer.status
                        - haversineMeters
                  - id: map.camera.studio
                    layer: map
                    api: true
                    fixture: map/camera-studio.json
                    published:
                      typescript: [StudioCamera]
                      dart: scoped
                      swift: scoped
                      kotlin: scoped
                  - { id: geo.layers.order, layer: geo, api: false, fixture: geo/layers-order.json }
                """,
            )

        val pose = read.capabilities[0].published
        assertEquals(names("MountayaMap.jump", "CameraTarget"), pose?.typescript)
        assertEquals(names("MapEngine.jump(to:)"), pose?.swift)
        assertEquals(names("MapEngine.jump", "ApiException.IdempotentAnswer.status", "haversineMeters"), pose?.kotlin)
        assertEquals(PublishedColumn.Scoped, read.capabilities[1].published?.kotlin)
        assertEquals(names("StudioCamera"), read.capabilities[1].published?.typescript)
        assertNull(read.capabilities[2].published)
    }

    @Test
    fun aPublishedMappingThatIsNotTheFourColumnsInTheirShapeIsRefused() {
        val four = "typescript: [A], dart: [A], swift: [A]"
        val refused =
            listOf(
                "{ $four }" to "maps exactly",
                "{ $four, kotlin: [A], java: [A] }" to "maps exactly",
                "{ $four, kotlin: [] }" to "`published.kotlin` must be `scoped` or a non-empty list",
                "{ $four, kotlin: none }" to "`published.kotlin` must be `scoped` or a non-empty list",
                "{ $four, kotlin: [1] }" to "`published.kotlin` holds 1, which is no name",
                "{ $four, kotlin: [null] }" to "`published.kotlin` holds nothing",
                "{ $four, kotlin: [~] }" to "`published.kotlin` holds `~`, which is no path after a package",
                "{ $four, kotlin: [\"MapEngine.jump(to:)\"] }" to "which is no path after a package",
                "{ $four, kotlin: [\"Map Engine\"] }" to "which is no path after a package",
                "{ $four, kotlin: [A, A] }" to "lists `A` twice",
                "{ typescript: [\" A\"], dart: [A], swift: [A], kotlin: [A] }" to "`published.typescript` holds  A",
                "MapEngine" to "must map `typescript`, `dart`, `swift` and `kotlin`",
                "[MapEngine.jump, CameraTarget]" to "must map `typescript`, `dart`, `swift` and `kotlin`",
            )
        for ((published, reason) in refused) {
            val error =
                assertFailsWith<IllegalArgumentException>(published) {
                    registry(
                        """
                        version: 11
                        capabilities:
                          - { id: a.b, layer: geo, api: true, fixture: a.json, published: $published }
                        """,
                    )
                }

            assertTrue(error.message.orEmpty().startsWith("registry.yaml: `a.b`: "), error.message)
            assertTrue(reason in error.message.orEmpty(), "${error.message} for $published")
        }
    }

    @Test
    fun anEntryMissingAFieldIsRefused() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                registry(
                    """
                    version: 6
                    capabilities:
                      - { id: a.b, layer: geo, fixture: a.json }
                    """,
                )
            }

        assertTrue(error.message.orEmpty().contains("capability #1"))
    }

    @Test
    fun aScopedIdOutranksItsLayerAndAnythingElseIsNotScoped() {
        val byId = sampleRegistry.capabilities.single { it.id == "transport.browser.secret-key" }
        val byLayer = sampleRegistry.capabilities.single { it.layer == "studio" }
        val neither = sampleRegistry.capabilities.single { it.id == "geo.layers.order" }

        assertEquals("No browser refusal for the secret key.", sampleScopes.sentenceFor(byId))
        assertEquals("No studio layer.", sampleScopes.sentenceFor(byLayer))
        assertNull(sampleScopes.sentenceFor(neither))
    }

    @Test
    fun theOwnershipTableNamesAModuleForEachLayerAndFlagAndNothingElse() {
        fun moduleOf(id: String) = sampleOwnership.moduleFor(sampleRegistry.capabilities.single { it.id == id })

        assertEquals("mountaya-client", moduleOf("transport.retry.backoff"))
        assertEquals("conformance", moduleOf("transport.credentials"))
        assertEquals("conformance", moduleOf("geo.layers.order"))
        assertEquals("mountaya-geo", moduleOf("geo.camera.ground-meters"))
        assertEquals("mountaya-elements", moduleOf("map.edit.snap"))
        assertNull(sampleOwnership.moduleFor(Capability("studio.a", "studio", false, "studio/a.json")))
        assertNull(sampleOwnership.moduleFor(Capability("video.a", "video", true, "video/a.json")))
    }
}
