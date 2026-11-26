package com.mountaya.build.gates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SuppressionGateTest {
    private val trees =
        Trees().add(
            "mountaya-client",
            "commonMain",
            mapOf(
                "com/mountaya/client/retry/rules/Cast.kt" to
                    "package com.mountaya.client\n@Suppress(\"UNCHECKED_CAST\")\nfun <T> cast(x: Any) = x as T",
            ),
        )
    private val key =
        "mountaya-client/commonMain/com/mountaya/client/retry/rules/Cast.kt: @Suppress(\"UNCHECKED_CAST\")"

    @Test
    fun aSuppressionWithNoExemptionFails() {
        val result = SuppressionGate.run(trees.build(), emptyMap())

        assertTrue(result.violations.single().startsWith(key))
    }

    @Test
    fun anExemptSuppressionIsCountedAndAnUnusedExemptionFails() {
        val exempt = SuppressionGate.run(trees.build(), mapOf(key to "The cast is checked by the caller."))
        assertEquals(emptyList(), exempt.violations)
        assertEquals("checkSuppressions: 1 checked, 1 exempt", exempt.summary)

        val unused = SuppressionGate.run(trees.build(), mapOf(key to "A sentence.", "nowhere: \"X\"" to "Another."))
        assertTrue(unused.violations.single().contains("exempts nothing"))
    }

    /** Android Lint's suppression and Java's are counted as Kotlin's is, each keyed by its own annotation. */
    @Test
    fun aLintSuppressionAndAJavaOneAreCountedToo() {
        val view = "mountaya-elements/androidMain/com/mountaya/elements/renderer/components/Mark.kt"
        val sources =
            Trees()
                .add(
                    "mountaya-elements",
                    "androidMain",
                    mapOf(
                        "com/mountaya/elements/renderer/components/Mark.kt" to
                            "package com.mountaya.elements\n" +
                            "@SuppressLint(\"ViewConstructor\")\nclass Mark\n" +
                            "@java.lang.SuppressWarnings(\"unchecked\")\nclass Raw",
                    ),
                ).build()

        val bare = SuppressionGate.run(sources, emptyMap())
        assertEquals(
            listOf("$view: @SuppressLint(\"ViewConstructor\")", "$view: @SuppressWarnings(\"unchecked\")"),
            bare.violations.map { it.substringBefore(" (line") },
        )

        val exempt =
            SuppressionGate.run(
                sources,
                mapOf(
                    "$view: @SuppressLint(\"ViewConstructor\")" to "Built in code alone.",
                    "$view: @SuppressWarnings(\"unchecked\")" to "Checked by the caller.",
                ),
            )
        assertEquals(emptyList(), exempt.violations)
    }
}
