package com.mountaya.build.contract

import com.mountaya.build.gates.HotPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HotPathsTest {
    private val document =
        """
        # Every hot path an SDK budgets.
        paths:
          - id: route-measure
            sdks: [typescript, dart, swift, kotlin]
            holds: >-
              A navigation reading fix after fix measures the route's line once.
          - id: channel-queries
            sdks: [dart]
            holds: A pointer over the live map costs one renderer query per frame.
        """.trimIndent()

    @Test
    fun eachEntryIsReadWithItsIdAndTheSdksThatBudgetIt() {
        assertEquals(
            listOf(
                HotPath("route-measure", listOf("typescript", "dart", "swift", "kotlin")),
                HotPath("channel-queries", listOf("dart")),
            ),
            HotPaths.parse(document, "hot-paths.yaml"),
        )
    }

    @Test
    fun aListOfAnyOtherShapeIsRefusedSayingWhy() {
        val refused =
            mapOf(
                "paths: []" to "`paths` must be a non-empty list",
                "- id: a" to "expected a mapping at the top level",
                "paths: [route-measure]" to "hot path #1 is not a mapping",
                "paths: [{id: Route_Measure, sdks: [kotlin], holds: Why.}]" to "has no kebab-case `id`",
                "paths: [{id: a, sdks: [kotlin], holds: Why.}, {id: a, sdks: [dart], holds: Why.}]" to "listed twice",
                "paths: [{id: a, sdks: [], holds: Why.}]" to "`a` lists no `sdks`",
                "paths: [{id: a, sdks: [rust], holds: Why.}]" to "lists `rust` among its sdks",
                "paths: [{id: a, sdks: [kotlin]}]" to "`a` holds no sentence",
                "paths: [{id: a, sdks: [kotlin], holds: Why.}" to "does not read as YAML",
            )
        for ((text, reason) in refused) {
            val failure = assertFailsWith<IllegalArgumentException>(text) { HotPaths.parse(text, "hot-paths.yaml") }
            assertTrue(reason in failure.message.orEmpty(), "$text: ${failure.message}")
        }
    }

    @Test
    fun theContractsOwnListReadsWhereTheCheckoutIsBesideThisOne() {
        val file = File("../../schemas/${ContractLayout.HOT_PATHS}").takeIf { it.isFile } ?: return
        val listed = HotPaths.read(file)

        assertTrue(listed.any { "kotlin" in it.sdks }, "the contract lists no hot path for kotlin")
    }
}
