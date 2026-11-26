package com.mountaya.build.gates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HotPathBudgetGateTest {
    /** The contract's list as the planted trees meet it: two paths for this SDK, one for another alone. */
    private val listed =
        listOf(
            HotPath("route-measure", listOf("typescript", "dart", "swift", "kotlin")),
            HotPath("pin-images", listOf("dart", "swift", "kotlin")),
            HotPath("dirty-tick", listOf("typescript")),
        )

    /** A live budget test of `route-measure`, in the geo module's common tests. */
    private val routeMeasure =
        """
        package com.mountaya.geo

        import kotlin.test.Test
        import kotlin.test.assertTrue

        private const val ROUTE_MEASURE_BUDGET = 1

        class RouteFollowingEngineTest {
            @Test
            fun hotPathBudgetRouteMeasureThirtyFixesMeasureTheLineOnce() {
                val measured = 1
                assertTrue(measured in 1..ROUTE_MEASURE_BUDGET, "measured ${'$'}measured")
            }
        }
        """

    /** A live budget test of `pin-images`, in the elements module's device tests, asserting from a lambda. */
    private val pinImages =
        """
        package com.mountaya.elements

        import kotlin.test.Test
        import kotlin.test.assertEquals

        private const val PIN_IMAGES_BUDGET = 1

        class MapRendererTest {
            @Test
            fun hotPathBudgetPinImagesFortyMarkersDrawEachLookOnce() {
                compose.runOnIdle { assertEquals(PIN_IMAGES_BUDGET * 2, drawn) }
            }
        }
        """

    private fun run(
        files: Map<String, String>,
        listed: List<HotPath>? = this.listed,
        required: Boolean = false,
        device: Map<String, String> = mapOf("com/mountaya/elements/renderer/engines/MapRendererTest.kt" to pinImages),
    ): GateResult =
        HotPathBudgetGate.run(
            Trees()
                .add("mountaya-geo", "commonTest", files)
                .add("mountaya-elements", "androidDeviceTest", device)
                .add(
                    "mountaya-geo",
                    "commonMain",
                    // A main source set names nothing a gate of tests reads.
                    mapOf(
                        "com/mountaya/geo/route/rules/Measured.kt" to
                            "package com.mountaya.geo\nfun hotPathBudgetRouteMeasureInMain() = Unit",
                    ),
                ).build(),
            listed,
            required,
        )

    private val routeFile = "com/mountaya/geo/navigation/engines/RouteFollowingEngineTest.kt"

    @Test
    fun aLiveBudgetTestForEveryPathListedForThisSdkPassesTheDeviceTestsAmongThem() {
        val result = run(mapOf(routeFile to routeMeasure))

        assertEquals(emptyList(), result.violations)
        assertEquals(
            "checkHotPathBudgets: 2 hot paths listed for kotlin checked, 0 exempt (2 budget tests in 2 test files)",
            result.summary,
        )
    }

    @Test
    fun aPathListedForThisSdkWithNoLiveBudgetTestFails() {
        val result = run(mapOf(routeFile to routeMeasure), device = emptyMap())

        assertEquals(1, result.violations.size, result.report())
        assertTrue(result.violations.single().startsWith("sdk/hot-paths.yaml: `pin-images` is listed for kotlin"))
        assertTrue("hotPathBudgetPinImages" in result.violations.single())
        assertTrue("PIN_IMAGES_BUDGET" in result.violations.single())
    }

    @Test
    fun aBudgetTestOfAPathListedForAnotherSdkAloneOrOfNoPathAtAllFails() {
        val planted =
            """
            package com.mountaya.geo

            import kotlin.test.Test
            import kotlin.test.assertTrue

            private const val DIRTY_TICK_BUDGET = 1

            class Planted {
                @Test
                fun hotPathBudgetDirtyTickSixtyDragFrames() = assertTrue(1 <= DIRTY_TICK_BUDGET)

                @Test
                fun hotPathBudgetMinuteHandTurns() = assertTrue(true)

                @Test
                fun hotPathBudget() = assertTrue(true)
            }
            """
        val result = run(mapOf(routeFile to routeMeasure, "com/mountaya/geo/route/rules/PlantedTest.kt" to planted))

        val violations = result.violations.joinToString("\n")
        assertEquals(3, result.violations.size, violations)
        assertTrue("`hotPathBudgetDirtyTickSixtyDragFrames` budgets `dirty-tick`, which" in violations, violations)
        assertTrue("does not list for kotlin" in violations)
        assertTrue("`hotPathBudgetMinuteHandTurns` names no hot path" in violations, violations)
        assertTrue("`hotPathBudget` names no hot path" in violations, violations)
    }

    @Test
    fun aBudgetTestThatAssertsNothingAgainstItsConstantFailsAndSoDoesOneAssertingAnothersConstant() {
        val planted =
            """
            package com.mountaya.geo

            import kotlin.test.Test
            import kotlin.test.assertTrue

            private const val ROUTE_MEASURE_BUDGET = 1
            private const val PIN_IMAGES_BUDGET = 1

            class Planted {
                @Test
                fun hotPathBudgetRouteMeasureCountsButAssertsNothing() {
                    val measured = ROUTE_MEASURE_BUDGET
                    check(measured <= ROUTE_MEASURE_BUDGET)
                }

                @Test
                fun hotPathBudgetRouteMeasureAssertsAnotherPathsConstant() {
                    assertTrue(1 <= PIN_IMAGES_BUDGET)
                }
            }
            """
        val result = run(mapOf(routeFile to routeMeasure, "com/mountaya/geo/route/rules/PlantedTest.kt" to planted))

        assertEquals(2, result.violations.size, result.report())
        assertTrue(result.violations.all { "hands `ROUTE_MEASURE_BUDGET` to no assertion" in it }, result.report())
    }

    @Test
    fun aBudgetTestWhoseFileDeclaresNoPrivateConstConstantFails() {
        val planted =
            """
            package com.mountaya.geo

            import kotlin.test.Test
            import kotlin.test.assertTrue

            const val PIN_IMAGES_BUDGET = 1
            private val ROUTE_MEASURE_BUDGET = 1

            class Planted {
                private val inside = 1

                @Test
                fun hotPathBudgetRouteMeasureDeclaresItAsNoConst() = assertTrue(1 <= ROUTE_MEASURE_BUDGET)

                @Test
                fun hotPathBudgetPinImagesDeclaresItAsNoPrivate() = assertTrue(1 <= PIN_IMAGES_BUDGET)
            }
            """
        val elsewhere =
            """
            package com.mountaya.geo

            import kotlin.test.Test
            import kotlin.test.assertTrue

            class Elsewhere {
                @Test
                fun hotPathBudgetRouteMeasureNamesAConstantAnotherFileDeclares() = assertTrue(1 <= ROUTE_MEASURE_BUDGET)
            }
            """
        val result =
            run(
                mapOf(
                    routeFile to routeMeasure,
                    "com/mountaya/geo/route/rules/PlantedTest.kt" to planted,
                    "com/mountaya/geo/route/rules/ElsewhereTest.kt" to elsewhere,
                ),
            )

        val violations = result.violations.joinToString("\n")
        assertEquals(3, result.violations.size, violations)
        assertEquals(2, result.violations.count { "no top-level `private const val ROUTE_MEASURE_BUDGET`" in it })
        assertEquals(1, result.violations.count { "no top-level `private const val PIN_IMAGES_BUDGET`" in it })
    }

    @Test
    fun aBudgetTestSwitchedOffOnItselfOrOnAClassAroundItAtAnyDepthFailsAndSoDoesOneThatIsNoTest() {
        val planted =
            """
            package com.mountaya.geo

            import kotlin.test.Ignore
            import kotlin.test.Test
            import kotlin.test.assertTrue
            import org.junit.jupiter.api.Disabled

            private const val ROUTE_MEASURE_BUDGET = 1

            class Planted {
                @Ignore
                @Test
                fun hotPathBudgetRouteMeasureIgnored() = assertTrue(1 <= ROUTE_MEASURE_BUDGET)

                @Disabled("later")
                @Test
                fun hotPathBudgetRouteMeasureDisabled() = assertTrue(1 <= ROUTE_MEASURE_BUDGET)

                fun hotPathBudgetRouteMeasureNoTest() = assertTrue(1 <= ROUTE_MEASURE_BUDGET)

                @Test
                fun hotPathBudgetRouteMeasureRuns() = assertTrue(1 <= ROUTE_MEASURE_BUDGET)
            }

            @org.junit.Ignore
            class Off {
                class Inner {
                    @Test
                    fun hotPathBudgetRouteMeasureUnderAnIgnoredClass() = assertTrue(1 <= ROUTE_MEASURE_BUDGET)
                }
            }
            """
        val result = run(mapOf("com/mountaya/geo/route/rules/PlantedTest.kt" to planted))

        val violations = result.violations.joinToString("\n")
        assertEquals(4, result.violations.size, violations)
        assertTrue("`hotPathBudgetRouteMeasureIgnored` is switched off by `@Ignore`," in violations, violations)
        assertTrue("`hotPathBudgetRouteMeasureDisabled` is switched off by `@Disabled`," in violations, violations)
        assertTrue("`hotPathBudgetRouteMeasureNoTest` is not annotated `@Test`" in violations, violations)
        assertTrue(
            "`hotPathBudgetRouteMeasureUnderAnIgnoredClass` is switched off by `@Ignore` on `Off`" in violations,
            violations,
        )
    }

    @Test
    fun aNameInACommentOrAStringOrAHelperNamedOtherwiseNamesNothing() {
        val planted =
            """
            package com.mountaya.geo

            import kotlin.test.Test

            class Planted {
                // @Test fun hotPathBudgetRouteMeasureInAComment() = Unit
                private val words = "fun hotPathBudgetRouteMeasureInAString() = Unit"

                private fun hotPathBudgets(): Int = 0

                @Test
                fun aHotPathBudgetRouteMeasureOpensOtherwise() = Unit
            }
            """
        val result = run(mapOf(routeFile to routeMeasure, "com/mountaya/geo/route/rules/PlantedTest.kt" to planted))

        assertEquals(emptyList(), result.violations)
        assertTrue("(2 budget tests in 3 test files)" in result.summary, result.summary)
    }

    @Test
    fun withNoListEachBudgetTestIsHeldToItsConstantAloneAndARunThatRequiresTheListFails() {
        val undeclared =
            """
            package com.mountaya.geo

            import kotlin.test.Test
            import kotlin.test.assertTrue

            class Planted {
                @Test
                fun hotPathBudgetSourcePushesThirtyChanges() = assertTrue(1 <= SOURCE_PUSHES_BUDGET)
            }
            """
        val absent = run(mapOf(routeFile to routeMeasure), listed = null)
        assertEquals(emptyList(), absent.violations)
        assertTrue("no list of hot paths was read" in absent.summary, absent.summary)

        val held =
            run(
                mapOf(routeFile to routeMeasure, "com/mountaya/geo/route/rules/PlantedTest.kt" to undeclared),
                listed = null,
            )
        assertTrue(held.violations.single().contains("`hotPathBudgetSourcePushesThirtyChanges` names no hot path"))

        val required = run(mapOf(routeFile to routeMeasure), listed = null, required = true)
        assertTrue(required.violations.single().startsWith("sdk/hot-paths.yaml: no list of hot paths was read"))
    }

    @Test
    fun aNameBudgetsTheLongestPathWhoseWordsItContinuesWithWhole() {
        val listed = listOf(HotPath("pin", listOf("kotlin")), HotPath("pin-images", listOf("kotlin")))
        val planted =
            """
            package com.mountaya.elements

            import kotlin.test.Test
            import kotlin.test.assertTrue

            private const val PIN_IMAGES_BUDGET = 1

            class Planted {
                @Test
                fun hotPathBudgetPinImagesDrawnOnce() = assertTrue(1 <= PIN_IMAGES_BUDGET)

                @Test
                fun hotPathBudgetPinImagery() = assertTrue(1 <= PIN_BUDGET)
            }
            """
        val result =
            run(mapOf("com/mountaya/geo/route/rules/PlantedTest.kt" to planted), listed = listed, device = emptyMap())

        // `PinImagery` continues `Pin` with a word of its own, and `PinImages` with none: it budgets `pin`.
        val violations = result.violations.joinToString("\n")
        assertEquals(2, result.violations.size, violations)
        assertTrue(
            "`hotPathBudgetPinImagery` stands in a file that declares no top-level `private const val PIN_BUDGET`" in
                violations,
        )
        assertTrue("`pin` is listed for kotlin with no live budget test" in violations, violations)
        assertEquals("PROFILE_CURSOR_BUDGET", HotPathBudgetGate.constantOf("profile-cursor"))
        assertEquals("hotPathBudgetProfileCursor", HotPathBudgetGate.nameOf("profile-cursor"))
    }

    @Test
    fun anEntryCrossesToTheWorkerAndBack() {
        val path = HotPath("source-pushes", listOf("dart", "swift", "kotlin"))

        assertEquals(path, HotPath.decode(path.encode()))
    }
}
