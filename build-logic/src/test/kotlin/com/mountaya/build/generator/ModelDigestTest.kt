package com.mountaya.build.generator

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * `modelDigest` (`sdk/SPEC.md` section 11), held to the value the contract's own script,
 * `scripts/sdk-model-digest.mjs`, gives for the same five small files, so the definition is read alike in both
 * languages: each input's path, a line feed, its length in bytes, a line feed and its bytes, in a fixed order.
 */
class ModelDigestTest {
    private val inputs =
        listOf(
            "sdk/model.json" to "{\"contract\":\"v0.0.0-test\"}\n",
            "sdk/elements/words.json" to "{\"en\":{}}\n",
            "sdk/elements/tokens.json" to "{}\n",
            "sdk/elements/glyphs.json" to "{\"sets\":{}}\n",
            "sdk/elements/NOTICE" to "Glyphs: é\n",
        )

    @Test
    fun theDigestOfFiveSmallFilesIsTheOneTheContractsScriptGives() {
        assertEquals(EXPECTED, ModelDigest.of(inputs.map { (path, text) -> path to text.toByteArray() }))
    }

    @Test
    fun aCheckoutIsReadAtTheFivePathsInTheirOrder() {
        val checkout = createTempDirectory("model-digest").toFile()
        try {
            for ((path, text) in inputs.reversed()) File(checkout, path).apply { parentFile.mkdirs() }.writeText(text)

            assertEquals(EXPECTED, ModelDigest.of(checkout))
            File(checkout, "sdk/elements/NOTICE").delete()
            val refusal = assertFailsWith<ModelException> { ModelDigest.of(checkout) }
            assertTrue("sdk/elements/NOTICE" in assertNotNull(refusal.message, "the refusal names the missing file"))
        } finally {
            checkout.deleteRecursively()
        }
    }

    @Test
    fun theLengthFramesEachFileSoBytesMovedAcrossABoundaryChangeTheDigest() {
        val moved = inputs.toMutableList()
        moved[0] = moved[0].first to moved[0].second + "x"
        moved[1] = moved[1].first to moved[1].second.drop(1)

        assertNotEquals(EXPECTED, ModelDigest.of(moved.map { (path, text) -> path to text.toByteArray() }))
        assertTrue(ModelDigest.isWellFormed(EXPECTED))
    }

    private companion object {
        /** What `node scripts/sdk-model-digest.mjs` printed in a directory holding [inputs]. */
        const val EXPECTED = "a943a238f8b97f07e23bfd0dd732f5066cb727918e4e0b6df1c34c148f4c445a"
    }
}
