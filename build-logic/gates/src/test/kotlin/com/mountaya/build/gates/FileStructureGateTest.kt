package com.mountaya.build.gates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileStructureGateTest {
    private val rule = "package com.mountaya.client\nfun retryBackoff() = Unit"
    private val ruleTest = "package com.mountaya.client\nclass RetryBackoffTest"

    private fun violations(
        trees: Trees,
        untested: Map<String, String> = emptyMap(),
        packages: Map<String, String> = emptyMap(),
    ): String = FileStructureGate.run(trees.build(), untested, packages).violations.joinToString("\n")

    @Test
    fun aFileAtTopicAndKindWithItsMirroredTestPasses() {
        val result =
            FileStructureGate.run(
                Trees()
                    .add(
                        "mountaya-client",
                        "commonMain",
                        mapOf("com/mountaya/client/retry/rules/RetryBackoff.kt" to rule),
                    ).add(
                        "mountaya-client",
                        "commonTest",
                        mapOf("com/mountaya/client/retry/rules/RetryBackoffTest.kt" to ruleTest),
                    ).build(),
                emptyMap(),
            )

        assertEquals(emptyList(), result.violations)
        assertEquals("lintFileStructure: 2 checked, 0 exempt", result.summary)
    }

    @Test
    fun aMisplacedOrMisnamedFileFails() {
        val found =
            violations(
                Trees().add(
                    "mountaya-client",
                    "commonMain",
                    mapOf(
                        "com/mountaya/client/retry/Loose.kt" to "package com.mountaya.client\nclass Loose",
                        "com/mountaya/client/retry/helpers/Helper.kt" to "package com.mountaya.client\nclass Helper",
                        "com/mountaya/client/retry/rules/Misnamed.kt" to
                            "package com.mountaya.client\nclass SomethingElse",
                    ),
                ),
                untested =
                    mapOf(
                        "mountaya-client/commonMain/com/mountaya/client/retry/Loose.kt" to "Planted.",
                        "mountaya-client/commonMain/com/mountaya/client/retry/helpers/Helper.kt" to "Planted.",
                        "mountaya-client/commonMain/com/mountaya/client/retry/rules/Misnamed.kt" to "Planted.",
                    ),
            )

        assertTrue("Loose.kt: sits 1 directories below" in found, found)
        assertTrue("`helpers` is not a kind" in found, found)
        assertTrue("declares nothing named `Misnamed`" in found, found)
    }

    /**
     * The directory names the topic and the kind, and the package is the module's: a file that declares the
     * package its directory spells, as an editor offers, fails, in main code and in test code alike.
     */
    @Test
    fun aFileDeclaresItsModulesPackageWhateverDirectoryItSitsIn() {
        val found =
            violations(
                Trees()
                    .add(
                        "mountaya-client",
                        "commonMain",
                        mapOf(
                            "com/mountaya/client/retry/rules/RetryBackoff.kt" to
                                "package com.mountaya.client.retry.rules\nfun retryBackoff() = Unit",
                            "com/mountaya/client/retry/rules/RetryBudget.kt" to "fun retryBudget() = Unit",
                            "com/mountaya/client/generated/RestSchema.kt" to
                                "package com.mountaya.client.generated\nclass RestSchema",
                        ),
                    ).add(
                        "mountaya-client",
                        "commonTest",
                        mapOf(
                            "com/mountaya/client/retry/rules/RetryBackoffTest.kt" to
                                "package com.mountaya.client.retry.rules\nclass RetryBackoffTest",
                            "com/mountaya/client/retry/rules/RetryBudgetTest.kt" to
                                "package com.mountaya.client\nclass RetryBudgetTest",
                        ),
                    ),
            )

        assertTrue(
            "commonMain/com/mountaya/client/retry/rules/RetryBackoff.kt: declares `com.mountaya.client.retry.rules`, " +
                "where every file of mountaya-client declares `com.mountaya.client`" in found,
            found,
        )
        assertTrue("commonTest/com/mountaya/client/retry/rules/RetryBackoffTest.kt: declares" in found, found)
        assertTrue("RetryBudget.kt: declares no package, where" in found, found)
        assertTrue("RetryBudgetTest.kt" !in found, found)
        assertTrue("RestSchema.kt" !in found, found)
    }

    @Test
    fun aFileKeepsAnotherPackageOnlyWithASentence() {
        val trees =
            Trees()
                .add(
                    "mountaya-geo",
                    "commonMain",
                    mapOf(
                        "com/mountaya/geo/profile/rules/Totals.kt" to
                            "package com.mountaya.geo\nfun totals() = Unit",
                    ),
                ).add(
                    "mountaya-geo",
                    "commonTest",
                    mapOf(
                        "com/mountaya/geo/profile/rules/TotalsTest.kt" to
                            "package com.mountaya.geo.profile.rules\nclass TotalsTest",
                    ),
                )
        val key = "mountaya-geo/commonTest/com/mountaya/geo/profile/rules/TotalsTest.kt"

        val exempt = FileStructureGate.run(trees.build(), emptyMap(), mapOf(key to "Its class name is taken."))
        assertEquals(emptyList(), exempt.violations)
        assertEquals("lintFileStructure: 2 checked, 1 exempt", exempt.summary)

        val blank = violations(trees, packages = mapOf(key to " "))
        assertTrue("TotalsTest.kt: declares `com.mountaya.geo.profile.rules`" in blank, blank)
        assertTrue("exemption \"$key\" carries no sentence" in blank, blank)

        val unused =
            violations(
                trees,
                packages =
                    mapOf(
                        key to "Its class name is taken.",
                        "nowhere/Nothing.kt" to "Planted.",
                    ),
            )
        assertTrue("exemption \"nowhere/Nothing.kt\" exempts nothing" in unused, unused)
    }

    @Test
    fun aMissingTestAnEmptyDirectoryAndAPlatformDeclarationOutsideSeamsFail() {
        val found =
            violations(
                Trees()
                    .add(
                        "mountaya-client",
                        "commonMain",
                        mapOf(
                            "com/mountaya/client/retry/rules/RetryBackoff.kt" to rule,
                            "com/mountaya/client/retry/rules/Sha.kt" to
                                "package com.mountaya.client\nexpect fun sha(): String",
                        ),
                        emptyDirectories = listOf("com/mountaya/client/empty"),
                    ).add(
                        "mountaya-client",
                        "jvmMain",
                        mapOf(
                            "com/mountaya/client/seams/rules/Orphan.kt" to
                                "package com.mountaya.client\nactual fun orphan(): String = \"\"",
                        ),
                    ),
                untested = mapOf("mountaya-client/commonMain/com/mountaya/client/retry/rules/Sha.kt" to "Planted."),
            )

        assertTrue("RetryBackoff.kt: no test at the mirrored path" in found, found)
        assertTrue("com/mountaya/client/empty: an empty directory" in found, found)
        assertTrue("Sha.kt: `expect` and `actual` appear under `seams` only" in found, found)
        assertTrue("Orphan.kt: an `actual` with no `expect`" in found, found)
    }

    /**
     * A seam is placed by its directory: a file that declares the module's package but sits outside `seams/` holds
     * no `expect`, and an `actual` sits at the mirrored path of its `expect`, whatever both declare.
     */
    @Test
    fun aSeamIsPlacedByItsDirectory() {
        val found =
            violations(
                Trees()
                    .add(
                        "mountaya-client",
                        "commonMain",
                        mapOf(
                            "com/mountaya/client/seams/engines/Clock.kt" to
                                "package com.mountaya.client\nexpect fun clock(): Long",
                            "com/mountaya/client/retry/rules/Hidden.kt" to
                                "package com.mountaya.client\nexpect fun hidden(): Long",
                        ),
                    ).add(
                        "mountaya-client",
                        "jvmMain",
                        mapOf(
                            "com/mountaya/client/seams/engines/Clock.kt" to
                                "package com.mountaya.client\nactual fun clock(): Long = 0",
                            "com/mountaya/client/seams/rules/Clock.kt" to
                                "package com.mountaya.client\nactual fun clock(): Long = 0",
                        ),
                    ),
                untested =
                    mapOf(
                        "mountaya-client/commonMain/com/mountaya/client/seams/engines/Clock.kt" to "Planted.",
                        "mountaya-client/commonMain/com/mountaya/client/retry/rules/Hidden.kt" to "Planted.",
                        "mountaya-client/jvmMain/com/mountaya/client/seams/engines/Clock.kt" to "Planted.",
                        "mountaya-client/jvmMain/com/mountaya/client/seams/rules/Clock.kt" to "Planted.",
                    ),
            )

        assertTrue("Hidden.kt: `expect` and `actual` appear under `seams` only" in found, found)
        assertTrue("jvmMain/com/mountaya/client/seams/rules/Clock.kt: an `actual` with no `expect`" in found, found)
        assertTrue("seams/engines/Clock.kt" !in found, found)
    }

    @Test
    fun aTestsOwnSeamIsDeclaredInCommonTestAndActualizedAtItsMirroredPath() {
        val path = "com/mountaya/conformance/seams/engines/SecretKey.kt"
        val expect = "package com.mountaya.conformance\nexpect fun secretKey(): String?"
        val actual = "package com.mountaya.conformance\nactual fun secretKey(): String? = null"
        val passing =
            violations(
                Trees()
                    .add("conformance", "commonTest", mapOf(path to expect))
                    .add("conformance", "jvmTest", mapOf(path to actual))
                    .add("conformance", "iosTest", mapOf(path to actual)),
            )
        val failing =
            violations(
                Trees()
                    .add("conformance", "jvmTest", mapOf(path to expect))
                    .add("conformance", "iosTest", mapOf(path to actual))
                    // A main-side expect at the same path is not the test's: the actual still has none.
                    .add("conformance", "commonMain", mapOf(path to expect)),
                untested = mapOf("conformance/commonMain/$path" to "Planted."),
            )

        assertEquals("", passing)
        assertTrue("SecretKey.kt: an `expect` outside commonTest" in failing, failing)
        assertTrue("SecretKey.kt: an `actual` with no `expect` at the mirrored path of commonTest" in failing, failing)
    }

    @Test
    fun twinPlatformFilesMustBeIdenticalAndAGeoRuleStaysCommon() {
        val seam = "package com.mountaya.geo\nactual fun platformLocale(): String = \"jvm\""
        val found =
            violations(
                Trees()
                    .add(
                        "mountaya-geo",
                        "commonMain",
                        mapOf(
                            "com/mountaya/geo/seams/rules/PlatformLocale.kt" to
                                "package com.mountaya.geo\nexpect fun platformLocale(): String",
                        ),
                    ).add(
                        "mountaya-geo",
                        "jvmMain",
                        mapOf(
                            "com/mountaya/geo/seams/rules/PlatformLocale.kt" to seam,
                            "com/mountaya/geo/profile/rules/Totals.kt" to
                                "package com.mountaya.geo\nfun totals() = Unit",
                        ),
                    ).add(
                        "mountaya-geo",
                        "androidMain",
                        mapOf("com/mountaya/geo/seams/rules/PlatformLocale.kt" to seam.replace("jvm", "android")),
                    ).add(
                        "mountaya-geo",
                        "commonTest",
                        mapOf(
                            "com/mountaya/geo/seams/rules/PlatformLocaleTest.kt" to
                                "package com.mountaya.geo\nclass PlatformLocaleTest",
                        ),
                    ),
                untested = mapOf("mountaya-geo/jvmMain/com/mountaya/geo/profile/rules/Totals.kt" to "Planted."),
            )

        assertTrue("differs from its copy in androidMain" in found, found)
        assertTrue("Totals.kt: a geo rule outside commonMain" in found, found)
    }

    @Test
    fun anUntestedFileIsExemptOnlyWithASentence() {
        val trees =
            Trees().add(
                "mountaya-client",
                "commonMain",
                mapOf("com/mountaya/client/retry/rules/RetryBackoff.kt" to rule),
            )
        val result =
            FileStructureGate.run(
                trees.build(),
                mapOf("mountaya-client/commonMain/com/mountaya/client/retry/rules/RetryBackoff.kt" to "A sentence."),
            )

        assertEquals(emptyList(), result.violations)
        assertEquals(1, result.exempt)
    }
}
