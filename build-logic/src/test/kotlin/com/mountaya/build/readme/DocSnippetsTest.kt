package com.mountaya.build.readme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DocSnippetsTest {
    private val client =
        listOf(
            "---",
            "title: \"Client basics\"",
            "---",
            "",
            "{% code-group %}",
            "{% code-block label=\"Kotlin\" language=\"kotlin\" %}",
            "```kotlin",
            "import com.mountaya.client.Credential",
            "import com.mountaya.client.Mountaya",
            "",
            "",
            "val key = System.getenv(\"MOUNTAYA_PUBLISHABLE_KEY\").orEmpty()",
            "Mountaya(Credential.Publishable(key)).close()",
            "```",
            "{% /code-block %}",
            "{% /code-group %}",
            "",
            "<!-- kotlin:skip a build script -->",
            "```kotlin",
            "implementation(\"com.mountaya:mountaya-client:0.1.0\")",
            "```",
        ).joinToString("\n")

    private val map =
        listOf(
            "# Show a map",
            "",
            "```kotlin",
            "import com.mountaya.elements.MapView",
            "",
            "// System.getenv is never read here: a comment names it.",
            "MapView(map)",
            "```",
            "",
            "```kotlin",
            "import com.mountaya.client.Credential",
            "",
            "val credential = Credential.Publishable(\"pk_your_publishable_key\")",
            "```",
        ).joinToString("\n")

    private val documents = listOf("pages/client.md" to client, "pages/map.md" to map)

    /** What dumps of the three kinds declare: common code, the jvm's alone and Android's alone. */
    private val declarations =
        SkillSnippets.Declarations.read(
            klib =
                listOf(
                    "// Klib ABI Dump\n" +
                        "abstract interface com.mountaya.client/Credential { // com.mountaya.client/Credential|null[0]",
                ),
            jvm = emptyList(),
            android =
                listOf(
                    listOf(
                        "public final class com/mountaya/elements/MapViewKt {",
                        "\tpublic static final fun MapView (Lcom/mountaya/elements/MapEngine;" +
                            "Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V",
                        "}",
                    ).joinToString("\n"),
                ),
        )

    @Test
    fun everyFenceOfEveryDocumentIsNumberedInTheOrderGivenWhereverItStands() {
        val snippets = DocSnippets.snippets(documents)

        assertEquals(listOf(1, 2, 3, 4), snippets.map { it.number })
        assertEquals(
            listOf("pages/client.md" to 7, "pages/client.md" to 19, "pages/map.md" to 3, "pages/map.md" to 10),
            snippets.map { it.document to it.fence.line },
        )
        assertEquals(listOf(null, "a build script", null, null), snippets.map { it.fence.skipped })
    }

    @Test
    fun aFenceThatReadsSystemIsTheJvmsOneThatReadsBuildConfigAndroidsAndAnAndroidImportStaysAndroids() {
        val snippets = DocSnippets.snippets(documents)

        assertEquals(SkillSnippets.Target.JVM, DocSnippets.targetOf(snippets[0], declarations))
        assertEquals(SkillSnippets.Target.ANDROID, DocSnippets.targetOf(snippets[2], declarations))
        assertEquals(SkillSnippets.Target.COMMON, DocSnippets.targetOf(snippets[3], declarations))
        val app =
            SkillSnippets.Snippet(
                "pages/app.md",
                9,
                ReadmeSnippets.Fence(1, listOf("val key = BuildConfig.MOUNTAYA_PUBLISHABLE_KEY", "println(key)")),
            )
        assertEquals(SkillSnippets.Target.ANDROID, DocSnippets.targetOf(app, declarations))
    }

    @Test
    fun eachFenceIsPrintedInAFileOfItsOwnWithItsBodyMappedToTheDocumentsLines() {
        val snippet = DocSnippets.snippets(documents)[0]
        val printed = DocSnippets.print(snippet, SkillSnippets.Target.JVM, composable = false)
        val lines = printed.source.lines()

        assertEquals("Snippet1", printed.objectName)
        assertTrue(
            printed.source.startsWith("// Printed by `mise run snippets` from pages/client.md"),
            printed.source,
        )
        assertTrue("package ${DocSnippets.PACKAGE}\n\nimport com.mountaya.client.Credential\n" in printed.source)
        assertTrue("internal object Snippet1 {\n" in printed.source, printed.source)
        assertEquals(12, printed.documentLine)
        assertEquals(2, printed.bodyLength)
        assertEquals(
            "        val key = System.getenv(\"MOUNTAYA_PUBLISHABLE_KEY\").orEmpty()",
            lines[printed.bodyLine - 1],
        )
        assertEquals("        Mountaya(Credential.Publishable(key)).close()", lines[printed.bodyLine])
        assertEquals(
            listOf("/out/Snippet1.kt", "${printed.bodyLine}", "pages/client.md", "7", "12", "2"),
            printed.indexLine("/out/Snippet1.kt").split('\t'),
        )
    }

    @Test
    fun aComposableFenceIsPrintedAsAComposablesBody() {
        val snippet = DocSnippets.snippets(documents)[2]
        val printed = DocSnippets.print(snippet, SkillSnippets.Target.ANDROID, composable = true)

        assertTrue(
            "import androidx.compose.runtime.Composable\nimport com.mountaya.elements.MapView\n" in printed.source,
            printed.source,
        )
        assertTrue("    @Composable\n    fun snippet3() {\n" in printed.source, printed.source)
        assertEquals(6, printed.documentLine)
        assertEquals(
            "        // System.getenv is never read here: a comment names it.",
            printed.source.lines()[printed.bodyLine - 1],
        )
    }

    @Test
    fun aFenceOfDeclarationsIsPrintedAsTheObjectsMembersAndAnyOtherAsAFunctionsBody() {
        fun fence(vararg code: String) =
            SkillSnippets.Snippet("pages/app.md", 7, ReadmeSnippets.Fence(3, code.toList()))

        val screen =
            fence(
                "import androidx.compose.runtime.Composable",
                "",
                "class TrailActivity : ComponentActivity() {",
                "    override fun onCreate(state: Bundle?) = setContent { TrailScreen() }",
                "}",
                "",
                "/** The screen. */",
                "@Composable",
                "fun TrailScreen() {",
                "}",
                "",
                "private const val ZOOM = 12.0",
            )
        val statements =
            fence("val secret = System.getenv(\"MOUNTAYA_WEBHOOK_SECRET\")", "fun receive(): String = secret")

        assertTrue(DocSnippets.isDeclarations(screen))
        assertFalse(DocSnippets.isDeclarations(statements))
        assertFalse(DocSnippets.isDeclarations(fence("import com.mountaya.client.Mountaya", "")))

        val printed = DocSnippets.print(screen, SkillSnippets.Target.ANDROID, composable = true)
        assertTrue(
            "internal object Snippet7 {\n" +
                "    // pages/app.md, line 3.\n" +
                "    class TrailActivity : ComponentActivity() {\n" in printed.source,
            printed.source,
        )
        assertTrue("fun snippet7" !in printed.source, printed.source)
        assertEquals(
            "    class TrailActivity : ComponentActivity() {",
            printed.source.lines()[printed.bodyLine - 1],
        )
        assertEquals(6, printed.documentLine)
    }

    @Test
    fun theTallyCountsEachTargetAndNamesEverySkippedFenceByItsLine() {
        val snippets = DocSnippets.snippets(documents)
        val sorted =
            snippets.filter { it.fence.skipped == null }.associateWith { DocSnippets.targetOf(it, declarations) }

        assertEquals(
            listOf(
                "Kotlin fences compiled: 3 (1 in commonMain, 1 in jvmMain, 1 in androidMain); skipped: 1; " +
                    "documents read: 2.",
                "Skipped: pages/client.md:19: a build script.",
            ),
            DocSnippets.tally(snippets, sorted, documents.size),
        )
    }

    @Test
    fun aDocumentWhoseFencesDoNotReadIsRefusedByItsName() {
        val refusal =
            assertFailsWith<DocSnippets.Refusal> {
                DocSnippets.snippets(documents + ("pages/broken.md" to "```kotlin\nval a = 1\n"))
            }

        assertEquals("pages/broken.md: line 1: a fence that is never closed", refusal.message)
    }
}
