package com.mountaya.build.gates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeterminismGateTest {
    @Test
    fun eachReadOfTheEnvironmentOutsideSeamsFails() {
        val rule =
            """
            package com.mountaya.client

            import kotlin.random.Random
            import kotlin.time.Clock
            import kotlinx.coroutines.Dispatchers

            fun now() = Clock.System.now()
            fun draw() = Random.nextDouble()
            fun dispatcher() = Dispatchers.IO
            fun log() = println("x")
            """
        val found =
            DeterminismGate
                .run(
                    Trees()
                        .add(
                            "mountaya-client",
                            "commonMain",
                            mapOf("com/mountaya/client/retry/rules/Rule.kt" to rule),
                        ).build(),
                ).violations
                .joinToString("\n")

        for (read in listOf("`Clock.System`", "`Random`", "`Dispatchers`", "`println`")) {
            assertTrue(read in found, "expected $read, got:\n$found")
        }
    }

    @Test
    fun seamsAndTestsMayRead() {
        val read = "package com.mountaya.client\nimport kotlin.time.Clock\nval clock = Clock.System"
        val result =
            DeterminismGate.run(
                Trees()
                    .add(
                        "mountaya-client",
                        "commonMain",
                        mapOf("com/mountaya/client/seams/rules/SystemDefaults.kt" to read),
                    ).add(
                        "mountaya-client",
                        "commonTest",
                        mapOf("com/mountaya/client/retry/rules/RuleTest.kt" to read),
                    ).build(),
            )

        assertEquals(emptyList(), result.violations)
        assertEquals(0, result.checked)
    }

    /**
     * Every file of a module declares the module's package, so `seams/` is the directory a file sits in, never
     * a package it declares: a rule that claims a seam's package still reads nothing.
     */
    @Test
    fun theSeamsTopicIsTheDirectoryAndNeverTheDeclaredPackage() {
        val result =
            DeterminismGate.run(
                Trees()
                    .add(
                        "mountaya-geo",
                        "commonMain",
                        mapOf(
                            "com/mountaya/geo/profile/rules/Claims.kt" to
                                "package com.mountaya.geo.seams.engines\nimport kotlin.time.Clock\nval claims = Clock.System",
                            "com/mountaya/geo/seams/engines/Reads.kt" to
                                "package com.mountaya.geo\nimport kotlin.time.Clock\nval reads = Clock.System",
                        ),
                    ).build(),
            )

        assertEquals(1, result.violations.size, result.violations.joinToString("\n"))
        val claims = "mountaya-geo/commonMain/com/mountaya/geo/profile/rules/Claims.kt:3"
        assertTrue(result.violations.single().startsWith(claims), result.violations.single())
        assertEquals(1, result.checked)
    }
}
