package com.mountaya.build.readme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ReadmeSnippetsTest {
    private val readme =
        listOf(
            "# A document",
            "",
            "```kotlin",
            "import com.mountaya.client.Mountaya",
            "",
            "val client = Mountaya(credential)",
            "",
            "client.close()",
            "```",
            "",
            "<!-- kotlin:skip a build script, which the consumer declares -->",
            "```kotlin",
            "plugins { kotlin(\"multiplatform\") }",
            "```",
            "",
            "```sh",
            "mise run ci",
            "```",
            "",
            "```kotlin",
            "import com.mountaya.client.Mountaya",
            "import com.mountaya.geo.formatDistance",
            "println(formatDistance(1.0, units))",
            "```",
        ).joinToString("\n")

    @Test
    fun everyKotlinFenceIsReadInOrderASkippedOneWithItsReasonAndNoOtherLanguage() {
        val fences = ReadmeSnippets.fences(readme)

        assertEquals(listOf(3, 12, 20), fences.map { it.line })
        assertEquals(listOf(null, "a build script, which the consumer declares", null), fences.map { it.skipped })
        assertEquals("val client = Mountaya(credential)", fences[0].code[2])
    }

    @Test
    fun theFileGathersTheImportsAndWrapsEachCompiledSnippetInAFunctionOfItsOwn() {
        val file =
            ReadmeSnippets.file(
                ReadmeSnippets.fences(readme),
                "README.md",
                "mise run readme",
                "com.example.readme",
            )

        assertTrue(file.startsWith("// Printed from README.md by the build."), file)
        assertTrue("//   mise run readme\n" in file, file)
        assertTrue(
            "// 2 snippets compiled, 1 skipped.\n// Skipped: a build script, which the consumer declares.\n" in file,
            file,
        )
        assertTrue(
            "package com.example.readme\n\n" +
                "import com.mountaya.client.Mountaya\n" +
                "import com.mountaya.geo.formatDistance\n\n" in file,
            file,
        )
        assertTrue(
            "    suspend fun snippet1() {\n" +
                "        val client = Mountaya(credential)\n\n" +
                "        client.close()\n" +
                "    }\n\n" +
                "    suspend fun snippet2() {\n" +
                "        println(formatDistance(1.0, units))\n" +
                "    }\n}\n" in file,
            file,
        )
        assertTrue("plugins" !in file, file)
    }

    @Test
    fun aFenceIsReadWhereverItStandsIndentedInsideATagOrWithAnInfoStringAfterItsLanguage() {
        val markdown =
            listOf(
                "1. Add the client:",
                "",
                "   ```kotlin",
                "   val client = Mountaya(credential)",
                "     client.close()",
                "   ```",
                "",
                "{% code-group %}",
                "{% code-block label=\"Kotlin\" language=\"kotlin\" %}",
                "<!-- kotlin:skip a build script -->",
                "```kotlin",
                "plugins { kotlin(\"jvm\") }",
                "```",
                "{% /code-block %}",
                "{% /code-group %}",
                "",
                "```kotlin {% title=\"Map\" %}",
                "MapView(map)",
                "```",
                "",
                "````kotlin",
                "val fence = \"\"\"",
                "```",
                "\"\"\"",
                "````",
            ).joinToString("\n")

        val fences = ReadmeSnippets.fences(markdown)

        assertEquals(listOf(3, 11, 17, 21), fences.map { it.line })
        assertEquals(listOf("val client = Mountaya(credential)", "  client.close()"), fences[0].code)
        assertEquals(listOf(null, "a build script", null, null), fences.map { it.skipped })
        assertEquals(listOf("MapView(map)"), fences[2].code)
        assertEquals(listOf("val fence = \"\"\"", "```", "\"\"\""), fences[3].code)
    }

    @Test
    fun aFenceNeverClosedAMarkerNamingNoReasonAndAMarkerAboveNoKotlinFenceAreRefused() {
        val refused =
            listOf(
                "```kotlin\nval a = 1\n" to "a fence that is never closed",
                "<!-- kotlin:skip -->\n```kotlin\nval a = 1\n```" to "names the reason",
                "<!-- kotlin:skip a reason -->\n```sh\nls\n```" to "above no Kotlin fence",
                "<!-- kotlin:skip a reason -->\nprose\n```kotlin\nval a = 1\n```" to "above no fence",
                "```kotlin\nval a = 1\n```\n<!-- kotlin:skip a reason -->" to "above no fence",
                "<!-- kotlin:skip a reason" to "ends on its own line",
                "<!-- kotlin:skip a reason -->\n{% code-block %}\n```kotlin\nval a = 1\n```" to "above no fence",
                "````kotlin\nval a = 1\n```" to "a fence that is never closed",
            )
        for ((markdown, reason) in refused) {
            val refusal = assertFailsWith<ReadmeSnippets.Refusal>(markdown) { ReadmeSnippets.fences(markdown) }
            assertTrue(reason in refusal.message.orEmpty(), "${refusal.message} for $markdown")
        }
    }
}
