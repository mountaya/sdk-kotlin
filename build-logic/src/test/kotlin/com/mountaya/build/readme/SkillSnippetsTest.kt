package com.mountaya.build.readme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkillSnippetsTest {
    private val entry =
        listOf(
            "# The skill",
            "",
            "A consumer compiles with Kotlin 2.4 or newer, and its code targets Java 17 or newer. The Android",
            "modules run from API level 23 and ask an app to compile against API level 37 or newer.",
            "",
            "<!-- kotlin:skip a build script, as the README's install lines are -->",
            "```kotlin",
            "implementation(\"com.mountaya:mountaya-client:0.1.0\")",
            "```",
            "",
            "```kotlin",
            "import com.mountaya.client.Credential",
            "",
            "val credential = Credential.Publishable(\"pk_live_0a1b\")",
            "```",
        ).joinToString("\n")

    private val reference =
        listOf(
            "# A reference",
            "",
            "```kotlin",
            "import com.mountaya.elements.MapView",
            "MapView(map)",
            "```",
        ).joinToString("\n")

    private val documents = mapOf("references/map.md" to reference, "SKILL.md" to entry)

    private val floors =
        mapOf("kotlin-floor" to "2.4", "jvm-target" to "17", "android-minSdk" to "23", "android-compileSdk" to "37")

    /** What dumps of the three kinds declare: common code, the jvm's alone and Android's alone. */
    private val declarations =
        SkillSnippets.Declarations.read(
            klib =
                listOf(
                    listOf(
                        "// Klib ABI Dump",
                        "abstract interface com.mountaya.client/Credential { // com.mountaya.client/Credential|null[0]",
                        "final class com.mountaya.elements/MapEngine { // com.mountaya.elements/MapEngine|null[0]",
                    ).joinToString("\n"),
                ),
            jvm =
                listOf(
                    listOf(
                        "public final class com/mountaya/client/ConstructWebhookEventKt {",
                        "\tpublic static final fun constructWebhookEvent-8Mi8wO0 ([BLjava/lang/String;)V",
                        "}",
                    ).joinToString("\n"),
                ),
            android =
                listOf(
                    listOf(
                        "public final class com/mountaya/elements/MapViewKt {",
                        "\tpublic static final fun MapView (Lcom/mountaya/elements/MapEngine;" +
                            "Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V",
                        "\tpublic static final fun mapPadding ()F",
                        "}",
                        "public final class com/mountaya/navigation/DeviceNavigationEngine {",
                        "\tpublic final fun start (Ljava/lang/Object;)V",
                        "}",
                    ).joinToString("\n"),
                ),
        )

    /** A snippet of [imports] alone, as the skill's `SKILL.md` would hold it at line 1. */
    private fun snippet(vararg imports: String): SkillSnippets.Snippet =
        SkillSnippets.Snippet("SKILL.md", 1, ReadmeSnippets.Fence(1, imports.map { "import $it" } + "Unit"))

    @Test
    fun theEntryIsReadFirstThenEveryOtherDocumentByPath() {
        assertEquals(
            listOf("SKILL.md", "references/client.md", "references/map.md"),
            SkillSnippets.documentOrder(listOf("references/map.md", "SKILL.md", "references/client.md")),
        )
    }

    @Test
    fun everyFenceIsNumberedAcrossTheDocumentsItsSkipMarkerHonoured() {
        val snippets = SkillSnippets.snippets(documents)

        assertEquals(listOf(1, 2, 3), snippets.map { it.number })
        assertEquals(
            listOf("SKILL.md, line 7", "SKILL.md, line 11", "references/map.md, line 3"),
            snippets.map { it.place },
        )
        assertEquals(
            listOf("a build script, as the README's install lines are", null, null),
            snippets.map { it.fence.skipped },
        )
        assertEquals(listOf("import com.mountaya.client.Credential"), snippets[1].imports)
    }

    @Test
    fun aDocumentWhoseFencesDoNotReadIsRefusedByItsPath() {
        val refusal =
            assertFailsWith<SkillSnippets.Refusal> {
                SkillSnippets.snippets(mapOf("SKILL.md" to entry, "references/map.md" to "```kotlin\nval a = 1\n"))
            }

        assertEquals("references/map.md: line 1: a fence that is never closed", refusal.message)
    }

    @Test
    fun aSkillStatingEveryFloorAsTheCatalogDoesHasNoProblem() {
        assertEquals(emptyList(), SkillSnippets.floorProblems(documents, floors))
    }

    @Test
    fun aFloorStatedAsAnotherFigureFailsAtItsLine() {
        val stale =
            documents +
                (
                    "references/build.md" to
                        "Set `minSdk = 25` in the app.\nIt runs from API level 25.\nKotlin 2.3 or newer."
                )

        assertEquals(
            listOf(
                "references/build.md, line 3: `Kotlin 2.3 or newer` states the Kotlin a consumer compiles with " +
                    "as 2.3, and the version catalog's `kotlin-floor` is 2.4.",
                "references/build.md, line 1: `minSdk = 25` states the API level the Android modules run from as 25, " +
                    "and the version catalog's `android-minSdk` is 23.",
                "references/build.md, line 2: `from API level 25` states the API level the Android modules run from " +
                    "as 25, and the version catalog's `android-minSdk` is 23.",
            ),
            SkillSnippets.floorProblems(stale, floors),
        )
    }

    /** A document reflowed by an editor still states its floors, and a stale one is found at the line it opens on. */
    @Test
    fun aFloorStatedAcrossALineBreakIsReadWhole() {
        val wrapped =
            mapOf(
                "SKILL.md" to
                    "Kotlin 2.4 or\nnewer, Java 17\nor newer.\n" +
                    "The modules run from API\nlevel 21, against API level\n37.",
            )

        assertEquals(
            listOf(
                "SKILL.md, line 4: `from API level 21` states the API level the Android modules run from as 21, " +
                    "and the version catalog's `android-minSdk` is 23.",
            ),
            SkillSnippets.floorProblems(wrapped, floors),
        )
    }

    @Test
    fun aFloorStatedNowhereFails() {
        val silent = mapOf("SKILL.md" to "# The skill\n\nA consumer compiles with Kotlin 2.4 or newer.")
        val problems = SkillSnippets.floorProblems(silent, floors)

        assertEquals(3, problems.size, problems.toString())
        assertTrue(
            "The Kotlin skill states nowhere the API level the Android modules run from: the version catalog's " +
                "`android-minSdk` is 23, which it states as `from API level 23`." in problems,
            problems.toString(),
        )
    }

    @Test
    fun aFloorTheBuildDoesNotHandIsRefused() {
        assertFailsWith<SkillSnippets.Refusal> { SkillSnippets.floorProblems(documents, floors - "jvm-target") }
    }

    @Test
    fun aFenceIsCommonCodeUnlessAnImportNamesWhatOneTargetAloneHas() {
        fun targetOf(vararg imports: String) = SkillSnippets.targetOf(snippet(*imports), declarations)

        assertEquals(
            SkillSnippets.Target.COMMON,
            targetOf("com.mountaya.client.Credential", "kotlinx.coroutines.launch"),
        )
        assertEquals(SkillSnippets.Target.COMMON, targetOf("com.mountaya.elements.*"))
        assertEquals(SkillSnippets.Target.COMMON, targetOf("com.mountaya.client.Nowhere"))
        assertEquals(
            SkillSnippets.Target.ANDROID,
            targetOf("com.mountaya.elements.MapEngine", "com.mountaya.elements.MapView"),
        )
        assertEquals(SkillSnippets.Target.ANDROID, targetOf("com.mountaya.navigation.DeviceNavigationEngine"))
        assertEquals(SkillSnippets.Target.ANDROID, targetOf("android.content.Context", "java.io.File"))
        assertEquals(SkillSnippets.Target.ANDROID, targetOf("androidx.compose.runtime.remember"))
        assertEquals(SkillSnippets.Target.JVM, targetOf("com.mountaya.client.constructWebhookEvent"))
        assertEquals(SkillSnippets.Target.JVM, targetOf("java.io.File as Path"))
    }

    @Test
    fun aFenceThatNeedsTheJvmAloneAndAndroidIsRefused() {
        val refusal =
            assertFailsWith<SkillSnippets.Refusal> {
                SkillSnippets.targetOf(
                    snippet("com.mountaya.client.constructWebhookEvent", "com.mountaya.elements.MapView"),
                    declarations,
                )
            }

        assertTrue(
            refusal.message.orEmpty().startsWith(
                "SKILL.md, line 1: it imports `com.mountaya.client.constructWebhookEvent`, which the jvm alone " +
                    "declares, and `com.mountaya.elements.MapView`, which is Android's",
            ),
            refusal.message,
        )
    }

    @Test
    fun anAndroidFenceIsAComposablesBodyWhereItImportsComposeOrAComposable() {
        assertTrue(SkillSnippets.isComposable(snippet("com.mountaya.elements.MapView"), declarations))
        assertTrue(SkillSnippets.isComposable(snippet("androidx.compose.runtime.LaunchedEffect"), declarations))
        assertFalse(SkillSnippets.isComposable(snippet("com.mountaya.elements.mapPadding"), declarations))
        assertFalse(SkillSnippets.isComposable(snippet("com.mountaya.navigation.DeviceNavigationEngine"), declarations))
    }

    @Test
    fun eachTargetsFileGathersItsImportsAndPrintsEachSnippetUnderItsPlaceAndNumber() {
        val snippets = SkillSnippets.snippets(documents)
        val sorted =
            snippets.filter { it.fence.skipped == null }.associateWith {
                SkillSnippets.targetOf(
                    it,
                    declarations,
                )
            }
        val tally = SkillSnippets.tally(snippets, sorted, documents.size)

        assertEquals(
            listOf(
                "3 snippets in 2 documents: 1 in commonMain, 0 in jvmMain, 1 in androidMain, 1 skipped.",
                "Skipped: SKILL.md, line 7: a build script, as the README's install lines are.",
            ),
            tally,
        )

        val common =
            SkillSnippets.file(
                SkillSnippets.Target.COMMON,
                listOf(snippets[1]),
                emptySet(),
                tally,
                "com.example",
            )
        assertTrue(
            common.startsWith("// Printed from the Kotlin skill, skills/mountaya-sdk-kotlin of the ai checkout"),
            common,
        )
        assertTrue(
            "// Skipped: SKILL.md, line 7: a build script, as the README's install lines are.\n" in common,
            common,
        )
        assertTrue("package com.example\n\nimport com.mountaya.client.Credential\n\n" in common, common)
        assertTrue(
            "internal object SkillSnippets {\n" +
                "    // SKILL.md, line 11.\n" +
                "    suspend fun snippet2() {\n" +
                "        val credential = Credential.Publishable(\"pk_live_0a1b\")\n" +
                "    }\n}\n" in common,
            common,
        )

        val android =
            SkillSnippets.file(
                SkillSnippets.Target.ANDROID,
                listOf(snippets[2]),
                setOf(snippets[2]),
                tally,
                "com.example",
            )
        assertTrue(
            "import androidx.compose.runtime.Composable\nimport com.mountaya.elements.MapView\n\n" in android,
            android,
        )
        assertTrue(
            "internal object AndroidSkillSnippets {\n" +
                "    // references/map.md, line 3.\n" +
                "    @Composable\n" +
                "    fun snippet3() {\n" +
                "        MapView(map)\n" +
                "    }\n}\n" in android,
            android,
        )
    }
}
