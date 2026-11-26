package com.mountaya.build.generator

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The printer of the elements' data against miniature tables holding one of every kind, compared byte
 * for byte with the files it is meant to print.
 *
 * The miniatures sit in `src/test/resources/generator/elements/`, `sdk-swift`'s own, and the files they
 * print in `elements-golden/`, beside the notices. `MOUNTAYA_UPDATE_GOLDEN=1` rewrites the golden files
 * after a deliberate change to the printer, and the diff is what a reviewer reads.
 */
class ElementsPrinterTest {
    private val resources = File("src/test/resources/generator")

    /** The tag and the digest every printed file's header names, as a print of the checkout's model hands them. */
    private val contract = "v0.8.0"
    private val digest = "6c1cf0ef10563c2bb2d6c6250bbb734b976eeea9bcbd280d67325516b4deb15c"

    private fun files(data: ElementsData): Map<String, String> = ElementsPrinter.files(data, contract, digest)

    private fun miniature(file: String): String = File(resources, "elements/$file").readText()

    private fun data(
        words: String = miniature("words.json"),
        tokens: String = miniature("tokens.json"),
        glyphs: String = miniature("glyphs.json"),
        notice: String = miniature("NOTICE"),
    ): ElementsData = ElementsData.parse(words, tokens, glyphs, notice)

    /** What printing the miniatures with one file replaced refuses, or null. */
    private fun refusal(
        words: String = miniature("words.json"),
        tokens: String = miniature("tokens.json"),
        glyphs: String = miniature("glyphs.json"),
        notice: String = miniature("NOTICE"),
    ): String? =
        try {
            val data = data(words, tokens, glyphs, notice)
            files(data)
            ElementsPrinter.notices(data)
            null
        } catch (refused: ModelException) {
            refused.message
        }

    @Test
    fun theMiniatureTablesPrintTheGoldenFilesAndTheNoticesByteForByte() {
        val printed = files(data())
        val notices = ElementsPrinter.notices(data())
        val golden = File(resources, "elements-golden")
        if (System.getenv("MOUNTAYA_UPDATE_GOLDEN") == "1") {
            golden.deleteRecursively()
            golden.mkdirs()
            for ((name, text) in printed) File(golden, "$name.txt").writeText(text)
            File(golden, ElementsPrinter.NOTICES_FILE).writeText(notices)
        }
        val expected =
            golden
                .listFiles()
                .orEmpty()
                .filter { it.name.endsWith(".kt.txt") }
                .associate { it.name.removeSuffix(".txt") to it.readText() }

        assertEquals(
            expected.keys.sorted(),
            printed.keys.sorted(),
            "the golden files are not the files the miniatures print",
        )
        for ((name, text) in printed) assertEquals(expected[name], text, name)
        assertEquals(File(golden, ElementsPrinter.NOTICES_FILE).readText(), notices)
    }

    @Test
    fun printingIsDeterministic() {
        assertEquals(files(data()), files(data()))
        assertEquals(ElementsPrinter.notices(data()), ElementsPrinter.notices(data()))
    }

    @Test
    fun everyFileSaysItIsPrintedAndEveryGlyphFileCarriesTheLicencesButNotTheNoticesOwnParagraph() {
        val files = files(data())
        val glyphFiles = files.filterKeys { it.startsWith("GlyphSet") }

        assertTrue(files.values.all { it.startsWith("// GENERATED FILE. DO NOT EDIT.\n") })
        // Every header names the contract's tag and the model's digest, as the client printer's does.
        assertTrue(files.values.all { "contract $contract, model digest\n// $digest.\n" in it })
        assertEquals(listOf("GlyphSetControl.kt", "GlyphSetMarker.kt"), glyphFiles.keys.sorted())
        for ((name, text) in glyphFiles) {
            assertTrue("// ISC License\n" in text, name)
            assertTrue("// The MIT License (MIT) (for the icons listed above)\n" in text, name)
            assertTrue("beside this file" !in text, name)
        }
        assertTrue(files.filterKeys { !it.startsWith("GlyphSet") }.values.none { "ISC License" in it })
        val notices = ElementsPrinter.notices(data())
        assertTrue(notices.startsWith("Third-party licences in sdk-kotlin\n"), notices)
        assertTrue(ElementsPrinter.NOTICES_ENTRY in notices && "beside this file" !in notices, notices)
    }

    @Test
    fun aMemberATableGainsThatThePrinterHasNoPlaceForIsRefusedNeverSkipped() {
        val sizes = miniature("tokens.json").replace("\"dashes\": {", "\"sizes\": {}, \"dashes\": {")
        val studio = miniature("glyphs.json").replace("\"artwork\": [", "\"studio\": {}, \"artwork\": [")
        val colour =
            miniature(
                "tokens.json",
            ).replace("{ \"hex\": \"#1c7ed6\" }", "{ \"hex\": \"#1c7ed6\", \"name\": \"blue\" }")
        val twice =
            miniature(
                "tokens.json",
            ).replace("\"accent\": { \"light\":", "\"accent\": { \"light\": { \"hex\": \"#000000\" }, \"light\":")

        assertTrue(
            refusal(tokens = sizes)?.contains("`sizes`, which this printer has no place for") == true,
            refusal(tokens = sizes),
        )
        assertTrue(refusal(glyphs = studio)?.contains("`studio`") == true, refusal(glyphs = studio))
        assertTrue(refusal(tokens = colour)?.contains("`name`") == true, refusal(tokens = colour))
        assertTrue(refusal(tokens = twice)?.contains("writes `light` twice") == true, refusal(tokens = twice))
        assertTrue(refusal(words = "{ \"languages\": ")?.contains("words.json is not JSON") == true)
    }

    @Test
    fun aWordThatIsNoStringCountedWordOrSectionIsRefused() {
        val number = miniature("words.json").replace("\"close\": \"Close\"", "\"close\": 3")
        val mixed =
            miniature(
                "words.json",
            ).replace("\"title\": \"Zoom in\"", "\"title\": \"Zoom in\", \"one\": \"A zoom\"")
        val noEnglish =
            miniature(
                "words.json",
            ).replace("\"en\": {\n      \"controls\"", "\"de\": {\n      \"controls\"")
        val language = miniature("words.json").replace("\"ja\": \"日本語\"", "\"JA\": \"日本語\"")

        assertTrue(
            refusal(words = number)?.contains("`en.controls.close` is a number") == true,
            refusal(words = number),
        )
        assertTrue(refusal(words = mixed)?.contains("is no counted word") == true, refusal(words = mixed))
        assertTrue(refusal(words = noEnglish)?.contains("carries no English") == true, refusal(words = noEnglish))
        assertTrue(refusal(words = language)?.contains("`JA`") == true, refusal(words = language))
    }

    @Test
    fun aColourATokenOrADrawingTheTableCannotHoldFaithfullyIsRefused() {
        val short = miniature("tokens.json").replace("#1c7ed6", "#FFF")
        val opacity = miniature("tokens.json").replace("\"opacity\": 0.12", "\"opacity\": 1.5")
        val twice = miniature("tokens.json").replace("\"profile-cursor-dash\"", "\"control-bg\"")
        val noLight = miniature("tokens.json").replace("\"accent\": { \"light\":", "\"accent\": { \"dark\":")
        val token =
            miniature(
                "glyphs.json",
            ).replace("{ \"token\": \"control-bg\" }", "{ \"token\": \"control-background\" }")
        val width = miniature("glyphs.json").replace(", \"width\": 1 }", " }")
        val glyph = miniature("glyphs.json").replace("\"name\": \"map-pin\"", "\"name\": \"airport\"")
        // A stroke set of no width draws nothing, where a fill set of none is drawn.
        val noWidth =
            miniature("glyphs.json").replace(
                "\"strokeWidth\": 2,\n    \"viewBox\": 24,\n    \"glyphs\": [{",
                "\"strokeWidth\": 0,\n    \"viewBox\": 24,\n    \"glyphs\": [{",
            )
        val fillNoWidth =
            noWidth.replace(
                "\"paint\": \"stroke\",\n    \"strokeWidth\": 0",
                "\"paint\": \"fill\",\n    \"strokeWidth\": 0",
            )

        assertTrue(refusal(tokens = short)?.contains("six lowercase digits") == true, refusal(tokens = short))
        assertTrue(refusal(tokens = opacity)?.contains("from 0 to 1") == true, refusal(tokens = opacity))
        assertTrue(refusal(tokens = twice)?.contains("prints as `controlBg`") == true, refusal(tokens = twice))
        assertTrue(refusal(tokens = noLight)?.contains("states no light value") == true, refusal(tokens = noLight))
        assertTrue(
            refusal(glyphs = token)?.contains("names no colour of the tokens table") == true,
            refusal(glyphs = token),
        )
        assertTrue(refusal(glyphs = width)?.contains("states no `width`") == true, refusal(glyphs = width))
        assertTrue(refusal(glyphs = glyph)?.contains("`airport` twice") == true, refusal(glyphs = glyph))
        assertTrue(noWidth != miniature("glyphs.json"))
        assertTrue(
            refusal(glyphs = noWidth)?.contains("states no `strokeWidth` above 0") == true,
            refusal(glyphs = noWidth),
        )
        assertNull(refusal(glyphs = fillNoWidth))
    }

    @Test
    fun aMeasureADrawingsShadowOrAGradientTheTableCannotHoldFaithfullyIsRefused() {
        val columns =
            miniature(
                "tokens.json",
            ).replace("\"panel-layer-card-columns\": 3", "\"panel-layer-card-columns\": 2.5")
        val text = miniature("tokens.json").replace("\"panel-width\": 340", "\"panel-width\": \"340\"")
        val blur = miniature("glyphs.json").replace("\"blur\": 12.1452", "\"blur\": -1")
        val opacity = miniature("glyphs.json").replace("\"opacity\": 0.9,", "\"opacity\": 1.9,")
        val point = miniature("glyphs.json").replace("\"from\": [23.7246, 40.8919]", "\"from\": [23.7246]")
        val order =
            miniature(
                "glyphs.json",
            ).replace("{ \"offset\": 1, \"hex\": \"#dddbdc\"", "{ \"offset\": -1, \"hex\": \"#dddbdc\"")
        val both =
            miniature(
                "glyphs.json",
            ).replace("\"fill\": { \"linear\": {", "\"fill\": { \"hex\": \"#ffffff\", \"linear\": {")

        assertTrue(refusal(tokens = columns)?.contains("no whole number from 1") == true, refusal(tokens = columns))
        assertTrue(refusal(tokens = text)?.contains("no finite number") == true, refusal(tokens = text))
        assertTrue(refusal(glyphs = blur)?.contains("no `blur` from 0") == true, refusal(glyphs = blur))
        assertTrue(refusal(glyphs = opacity)?.contains("from 0 to 1") == true, refusal(glyphs = opacity))
        assertTrue(refusal(glyphs = point)?.contains("two finite numbers") == true, refusal(glyphs = point))
        assertTrue(refusal(glyphs = order)?.contains("past the stop before it") == true, refusal(glyphs = order))
        assertTrue(refusal(glyphs = both)?.contains("`hex`") == true, refusal(glyphs = both))
    }

    @Test
    fun aDigestOrATagNoHeaderCanCarryIsRefused() {
        val refusedDigest =
            try {
                ElementsPrinter.files(data(), contract, "ABC")
                null
            } catch (refused: ModelException) {
                refused.message
            }
        assertTrue(refusedDigest?.contains("64 lowercase hexadecimal") == true, refusedDigest)
    }

    @Test
    fun aNoticeCarryingNoIscAndMitLicenceIsRefused() {
        assertTrue(refusal(notice = "Lucide is licensed.\n")?.contains("carries no ISC and MIT licence") == true)
    }
}
