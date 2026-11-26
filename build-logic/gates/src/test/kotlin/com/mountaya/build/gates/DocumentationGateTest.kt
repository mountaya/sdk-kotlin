package com.mountaya.build.gates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentationGateTest {
    private val documented =
        """
        package com.mountaya.geo

        /** A paint. */
        public class MapPaint(
            /** The line's colour. */
            public val line: String,
            private val hidden: String,
            width: Double,
        ) {
            /** A paint of another line. */
            public constructor(line: Int) : this(line.toString(), "", 1.0)

            /** The paint's width. */
            public val width: Double = 1.0

            /** Draws it. */
            protected fun draw(): Unit = Unit

            override fun toString(): String = line

            /** The paint the contract ships. */
            public companion object {
                /** The standard paint. */
                public val standard: MapPaint = MapPaint("#000", "", 1.0)
            }

            /** What the paint strokes. */
            public enum class Kind {
                /** A filled area. */
                Fill,
            }

            internal fun stroke(): Unit = Unit

            private class Cache(val held: String)
        }

        /** A colour. */
        public typealias Colour = String

        /** Paints [paint]. */
        public fun paint(paint: MapPaint): Unit {
            class Local(val inside: Int)
            val local = object : Comparable<Int> {
                override fun compareTo(other: Int): Int = 0
                fun alsoInside(): Unit = Unit
            }
        }

        internal class Hidden(val nobody: Int) {
            public fun reachesNoOne(): Unit = Unit
        }
        """

    private fun run(
        main: String,
        exemptions: Map<String, String> = emptyMap(),
        test: String = "package com.mountaya.geo\nclass MapPaintTest { fun undocumented() = Unit }",
        generated: String = "package com.mountaya.geo.generated\npublic class Printed(public val words: String)",
    ): GateResult =
        DocumentationGate.run(
            Trees()
                .add(
                    "mountaya-geo",
                    "commonMain",
                    mapOf(
                        "com/mountaya/geo/paint/contracts/MapPaint.kt" to main,
                        "com/mountaya/geo/generated/Printed.kt" to generated,
                    ),
                ).add(
                    "mountaya-geo",
                    "commonTest",
                    mapOf("com/mountaya/geo/paint/contracts/MapPaintTest.kt" to test),
                ).build(),
            exemptions,
        )

    /**
     * The type, its constructor property, its secondary constructor, its property, its protected
     * function, its companion, the companion's property, the enum, its entry, the alias and the
     * function: eleven declarations a consumer reaches, and nothing private, internal, local, in an
     * object expression, overriding, in a test or generated is read.
     */
    @Test
    fun aDocumentedFilePassesAndCountsWhatItChecked() {
        val result = run(documented)

        assertEquals(emptyList(), result.violations)
        assertEquals("lintDocumentation: 11 checked, 0 exempt", result.summary)
    }

    @Test
    fun everyReachedDeclarationWithNoKDocFailsByItsPath() {
        val planted =
            """
            package com.mountaya.geo

            public class MapPaint(public val line: String) {
                public constructor(line: Int) : this(line.toString())

                public val width: Double = 1.0

                protected fun draw(): Unit = Unit

                public companion object {
                    public val standard: MapPaint = MapPaint("#000")
                }

                public object Named

                public enum class Kind { Fill }

                public interface Drawn {
                    fun drawn(): Unit
                }
            }

            public typealias Colour = String

            public fun paint(paint: MapPaint): Unit = Unit
            """
        val violations = run(planted).violations

        val refused =
            listOf(
                "MapPaint",
                "MapPaint.line",
                "MapPaint.constructor",
                "MapPaint.width",
                "MapPaint.draw",
                "MapPaint.Companion",
                "MapPaint.Companion.standard",
                "MapPaint.Named",
                "MapPaint.Kind",
                "MapPaint.Kind.Fill",
                "MapPaint.Drawn",
                "MapPaint.Drawn.drawn",
                "Colour",
                "paint",
            )
        assertEquals(refused.map { "`$it`" }, violations.map { Regex("`[^`]+`").find(it)?.value })
        val companion = violations.single { "`MapPaint.Companion`" in it }
        assertTrue(
            companion.startsWith("mountaya-geo/commonMain/com/mountaya/geo/paint/contracts/MapPaint.kt:10: "),
            companion,
        )
        assertTrue("is public and carries no KDoc" in companion, companion)
        assertTrue("`MapPaint.draw` is protected" in violations.single { "draw`" in it })
    }

    @Test
    fun anExemptionIsKeyedByPathAndCountedAndOneThatExemptsNothingOrSaysNothingFails() {
        val planted =
            """
            package com.mountaya.geo

            /** A paint. */
            public class MapPaint {
                public companion object
            }
            """
        val exempt = run(planted, mapOf("MapPaint.Companion" to "Its members are each documented."))
        assertEquals(emptyList(), exempt.violations)
        assertEquals("lintDocumentation: 2 checked, 1 exempt", exempt.summary)

        val leftover =
            run(
                planted,
                mapOf("MapPaint.Companion" to "Its members are each documented.", "paint" to "A sentence."),
            )
        assertEquals(listOf("lintDocumentation exemption \"paint\" exempts nothing. Remove it."), leftover.violations)

        val blank = run(planted, mapOf("MapPaint.Companion" to " "))
        assertTrue(blank.violations.any { "carries no sentence" in it }, blank.violations.toString())
    }

    /** A comment that is not KDoc documents nothing a consumer reads. */
    @Test
    fun aLineOrBlockCommentIsNoKDoc() {
        val planted =
            """
            package com.mountaya.geo

            // A paint.
            public class MapPaint

            /* A colour. */
            public typealias Colour = String
            """

        assertEquals(2, run(planted).violations.size)
    }
}
