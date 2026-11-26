package com.mountaya.build.contract

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TrailScenarioTest {
    private val document =
        """
        # Trail, as the contract writes it.
        version: 1
        sdks: [dart, swift, kotlin]
        settings:
          - name: MOUNTAYA_PUBLISHABLE_KEY
            readers: [app, session-server]
            required: true
            scopes: [tiles]
            use: The key.
          - name: MOUNTAYA_SECRET_KEY
            readers: [session-server]
            required: true
            scopes: []
            use: The secret.
        steps:
          - id: trail.client
            does: One client.
          - id: trail.no-key
            does: A notice.
        accessibility:
          - id: trail.notice
            marks: The notice.
        journey:
          settings: []
          replayMetersPerSecond: 100
          arrivalSeconds: 30
          waypoints: [Les Nants]
          checks:
            - target: trail.notice
              action: see
              holds: The notice stands.
        measured:
          MOUNTAYA_PUBLISHABLE_KEY: pk_size_gate
        """.trimIndent()

    @Test
    fun theVersionTheSdksTheStepsAndTheAccessibilityIdsAreRead() {
        val read = TrailScenario.parse(document, "trail.yaml")

        assertEquals(1, read.version)
        assertEquals(listOf("dart", "swift", "kotlin"), read.sdks)
        assertEquals(listOf("MOUNTAYA_PUBLISHABLE_KEY", "MOUNTAYA_SECRET_KEY"), read.settings)
        assertEquals(listOf("trail.client", "trail.no-key"), read.steps)
        assertEquals(listOf("trail.notice"), read.accessibility)
        assertEquals(mapOf("MOUNTAYA_PUBLISHABLE_KEY" to "pk_size_gate"), read.measured)
    }

    @Test
    fun aScenarioOfAnyOtherShapeIsRefusedSayingWhy() {
        val refused =
            mapOf(
                document.replace("version: 1", "version: one") to "`version` must be a whole number from 1",
                document.replace("sdks: [dart, swift, kotlin]", "sdks: [rust]") to "`sdks` lists `rust`",
                document.replace("id: trail.client", "id: client") to "`steps` #1 has no id",
                document.replace("id: trail.no-key", "id: trail.client") to "`trail.client` is listed twice in `steps`",
                document.replace("    does: A notice.\n", "") to "`trail.no-key` states no `does`",
                document.replace("readers: [session-server]", "readers: [browser]") to "is read by `browser`",
                document.replace("target: trail.notice", "target: trail.map") to "acts on `trail.map`",
                document.replace("action: see", "action: swipe") to "does `swipe`",
                document.replace("replayMetersPerSecond: 100", "replayMetersPerSecond: 0") to "is no speed",
                document.replace("  MOUNTAYA_PUBLISHABLE_KEY: pk_size_gate", "  MOUNTAYA_SECRET_KEY: sk_size_gate") to
                    "`measured` gives `MOUNTAYA_PUBLISHABLE_KEY` no placeholder",
                "- version: 1" to "expected a mapping at the top level",
                "version: [1" to "does not read as YAML",
            )
        for ((text, reason) in refused) {
            val failure = assertFailsWith<IllegalArgumentException>(text) { TrailScenario.parse(text, "trail.yaml") }
            assertTrue(reason in failure.message.orEmpty(), "$reason: ${failure.message}")
        }
    }

    @Test
    fun theContractsOwnScenarioReadsWhereTheCheckoutIsBesideThisOne() {
        val file = File("../../schemas/${ContractLayout.TRAIL}").takeIf { it.isFile } ?: return
        val read = TrailScenario.read(file)

        assertTrue("kotlin" in read.sdks, "the contract ships no Trail for kotlin")
        assertTrue(read.steps.isNotEmpty() && read.accessibility.isNotEmpty())
    }
}
