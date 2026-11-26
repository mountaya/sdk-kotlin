package com.mountaya.build.gates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NamingGateTest {
    private val clean =
        """
        package com.mountaya.client

        /** Clean. */
        internal class SystemDefaults(val onValueChange: () -> Unit, val loadHttpUrl: String) {
            fun onTogglePanel() = Unit
        }

        public const val SCHEMA_VERSION: String = "v0.6.0"
        """

    private fun run(
        main: String,
        test: String = "package com.mountaya.client\nclass SystemDefaultsTest",
        exemptions: Map<String, String> = emptyMap(),
    ): GateResult =
        NamingGate.run(
            Trees()
                .add(
                    "mountaya-client",
                    "commonMain",
                    mapOf("com/mountaya/client/seams/rules/SystemDefaults.kt" to main),
                ).add(
                    "mountaya-client",
                    "commonTest",
                    mapOf("com/mountaya/client/seams/rules/SystemDefaultsTest.kt" to test),
                ).build(),
            exemptions,
        )

    @Test
    fun aCleanFilePassesAndCountsWhatItChecked() {
        val result = run(clean)

        assertEquals(emptyList(), result.violations)
        assertEquals("lintNaming: 5 checked, 0 exempt", result.summary)
    }

    @Test
    fun eachRefusedNameFails() {
        val planted =
            """
            package com.mountaya.client

            class MountayaClock
            class NoopClock
            class NoOpLocationSource
            class NeverSleeper
            class SilentNotifier
            val noopClock = Unit
            class FormatHelper
            class Viewer(val onTap: () -> Unit)
            fun parseURL() = Unit
            val MAX_COUNT = 3
            """
        val violations = run(planted).violations.joinToString("\n")

        val refused =
            listOf(
                "MountayaClock",
                "NoopClock",
                "NoOpLocationSource",
                "NeverSleeper",
                "SilentNotifier",
                "noopClock",
                "FormatHelper",
                "onTap",
                "parseURL",
                "MAX_COUNT",
            )
        for (name in refused) {
            assertTrue("`$name`" in violations, "expected a violation naming $name, got:\n$violations")
        }
    }

    @Test
    fun theEngineWordsAreRefusedOnlyWhereAMachineIsFiled() {
        val trees =
            Trees()
                .add(
                    "mountaya-client",
                    "commonMain",
                    mapOf(
                        "com/mountaya/client/search/engines/SearchSession.kt" to
                            "package com.mountaya.client\nclass SearchSession",
                        "com/mountaya/client/segments/contracts/SegmentEditSession.kt" to
                            "package com.mountaya.client\nclass SegmentEditSession",
                    ),
                ).build()
        val violations = NamingGate.run(trees, emptyMap()).violations

        assertEquals(1, violations.size, violations.joinToString("\n"))
        assertTrue("`SearchSession`" in violations.single())
    }

    /**
     * A machine is filed under `engines` by its directory: every file declares the module's package, so a package
     * that spells `engines` files nothing there, and one that does not leaves a file under `engines/` filed.
     */
    @Test
    fun aMachineIsFiledByItsDirectoryAndNeverByItsPackage() {
        val trees =
            Trees()
                .add(
                    "mountaya-geo",
                    "commonMain",
                    mapOf(
                        "com/mountaya/geo/search/engines/SearchManager.kt" to
                            "package com.mountaya.geo\nclass SearchManager",
                        "com/mountaya/geo/segments/contracts/EditSession.kt" to
                            "package com.mountaya.geo.segments.engines\nclass EditSession",
                    ),
                ).build()
        val violations = NamingGate.run(trees, emptyMap()).violations

        assertEquals(1, violations.size, violations.joinToString("\n"))
        assertTrue("SearchManager.kt:2: `SearchManager` ends in `Manager`" in violations.single(), violations.single())
    }

    @Test
    fun aWordIsReadWholeAndAHandlerOnlyWhereAConsumerIsHandedOne() {
        val result =
            run(
                """
                package com.mountaya.client

                class NullableValue
                class Nothing
                internal class Transport(val onFinish: () -> Unit)
                """,
            )

        assertEquals(emptyList(), result.violations)
    }

    @Test
    fun aCompositionLocalLeadsWithLocalAndThePackageSubjectAndNoOtherPropertyDoes() {
        val result =
            run(
                """
                package com.mountaya.client

                val LocalMountayaScope: ProvidableCompositionLocal<String> = staticCompositionLocalOf { "" }
                internal val LocalMountayaOpenPanel = compositionLocalOf<Int?> { null }
                val LocalScope: ProvidableCompositionLocal<String> = staticCompositionLocalOf { "" }
                val LocalMountaya = compositionLocalOf { 0 }
                val Scope = staticCompositionLocalOf { 0 }
                val LocalMountayaClock = 0
                """,
            )

        val violations = result.violations
        assertEquals(4, violations.size, violations.joinToString("\n"))

        fun of(name: String) = violations.single { ": `$name` is" in it }
        assertTrue(violations.none { ": `LocalMountayaScope` is" in it || ": `LocalMountayaOpenPanel` is" in it })
        assertTrue(of("LocalScope").contains("CompositionLocal"))
        assertTrue(of("LocalMountaya").contains("CompositionLocal"))
        assertTrue(of("Scope").contains("CompositionLocal"))
        assertTrue(of("LocalMountayaClock").contains("lowerCamelCase"), "a value of another kind is a member")
    }

    /**
     * A member of an open vocabulary, a constant a value class over its spelling declares in its companion of the class
     * itself, its type stated or its initializer the class's constructor, is UpperCamelCase as Compose's
     * `TextAlign.Center` is, and a lowerCamelCase one is refused. A constant of another type there, or of its own type
     * in a class that is no value class or in a value class over anything but a `String`, is a member as any other
     * property is.
     */
    @Test
    fun aVocabularyMemberIsUpperCamelCaseAndNoOtherPropertyIs() {
        val result =
            run(
                """
                package com.mountaya.client

                public class WebhookSignatureException(public val reason: Reason) {
                    @JvmInline
                    public value class Reason internal constructor(public val value: String) {
                        public companion object {
                            public val MissingHeader: Reason = Reason("missing-header")
                            public val Default = Reason("default")
                            public val signature: WebhookSignatureException.Reason = Reason("signature")
                            public val Limit: Int = 3
                        }
                    }
                }

                public class Plain(public val value: String) {
                    public companion object {
                        public val Standard: Plain = Plain("standard")
                    }
                }

                @JvmInline
                public value class Pause private constructor(public val duration: Duration) {
                    public companion object {
                        public val standard: Pause = Pause(Duration.ZERO)
                        public val Long: Pause = Pause(Duration.INFINITE)
                    }
                }
                """,
            )

        val violations = result.violations
        assertEquals(4, violations.size, violations.joinToString("\n"))

        fun of(name: String) = violations.single { ": `$name` is" in it }
        assertTrue(of("signature").contains("member of the open vocabulary `Reason` and is not UpperCamelCase"))
        assertTrue(of("Limit").contains("is not lowerCamelCase"), "a constant of another type is a member")
        assertTrue(of("Standard").contains("is not lowerCamelCase"), "a class that is no value class has no vocabulary")
        assertTrue(
            violations.none { ": `standard` is" in it },
            "a value class over anything but its spelling has no vocabulary, so its default instance is lowerCamelCase",
        )
        assertTrue(of("Long").contains("is not lowerCamelCase"), "nor is any other constant of it UpperCamelCase")
    }

    @Test
    fun aDoubleBangFailsInATestToo() {
        val result =
            run(
                clean,
                test =
                    "package com.mountaya.client\n" +
                        "class SystemDefaultsTest { val x = listOf<String?>(null)[0]!! }",
            )

        assertTrue(result.violations.single().contains("`!!`"))
    }

    @Test
    fun anExemptionIsCountedAndAnUnusedOneFails() {
        val exempted =
            run(
                "package com.mountaya.client\nclass MountayaException",
                exemptions =
                    mapOf("MountayaException" to "It would shadow kotlin.Exception."),
            )

        assertEquals(emptyList(), exempted.violations)
        assertEquals(1, exempted.exempt)

        val unused = run(clean, exemptions = mapOf("MountayaException" to "It would shadow kotlin.Exception."))
        assertTrue(unused.violations.single().contains("exempts nothing"))
    }

    @Test
    fun theGeneratedPackageIsOutsideTheLaw() {
        val result =
            NamingGate.run(
                Trees()
                    .add(
                        "mountaya-client",
                        "commonMain",
                        mapOf(
                            "com/mountaya/client/generated/RestSchema.kt" to
                                "package com.mountaya.client.generated\nclass MountayaRESTThing",
                        ),
                    ).build(),
                emptyMap(),
            )

        assertEquals(emptyList(), result.violations)
    }
}
