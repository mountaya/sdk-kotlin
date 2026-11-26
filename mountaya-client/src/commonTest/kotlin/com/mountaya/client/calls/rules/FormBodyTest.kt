package com.mountaya.client

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FormBodyTest {
    private val gpx = "<gpx/>".encodeToByteArray()

    @Test
    fun partsAreWrittenInOrderEachNamedByItsFieldAndTwoWritesSendTheSameBytes() {
        val parts = listOf(MultipartPart.Text("name", "Hut"), MultipartPart.File("file\"x", gpx))
        val written = FormBody.write(parts)

        assertEquals("multipart/form-data; boundary=MountayaFormBoundary7Hq2Xw", written.contentType)
        assertEquals(
            "--MountayaFormBoundary7Hq2Xw\r\nContent-Disposition: form-data; name=\"name\"\r\n\r\nHut\r\n" +
                "--MountayaFormBoundary7Hq2Xw\r\nContent-Disposition: form-data; name=\"file%22x\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n<gpx/>\r\n" +
                "--MountayaFormBoundary7Hq2Xw--\r\n",
            written.bytes.decodeToString(),
        )
        assertContentEquals(written.bytes, FormBody.write(parts).bytes)
    }

    @Test
    fun aBoundaryAPartHoldsIsLengthenedUntilNoneDoes() {
        val written = FormBody.write(listOf(MultipartPart.Text("note", "MountayaFormBoundary7Hq2Xw")))

        assertEquals("multipart/form-data; boundary=MountayaFormBoundary7Hq2Xwx", written.contentType)
    }

    /** A file's part carries the file name and the media type its caller gives it, a text part neither. */
    @Test
    fun aFilePartCarriesTheFileNameAndTheMediaTypeItsCallerGives() {
        val written =
            FormBody.write(
                listOf(
                    MultipartPart.Text("provider", "gpx"),
                    MultipartPart.File(
                        "file",
                        gpx,
                        filename = "tour-du-mont-blanc.gpx",
                        contentType = "application/gpx+xml",
                    ),
                ),
            )

        assertEquals(
            "--MountayaFormBoundary7Hq2Xw\r\nContent-Disposition: form-data; name=\"provider\"\r\n\r\ngpx\r\n" +
                "--MountayaFormBoundary7Hq2Xw\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"tour-du-mont-blanc.gpx\"\r\n" +
                "Content-Type: application/gpx+xml\r\n\r\n<gpx/>\r\n" +
                "--MountayaFormBoundary7Hq2Xw--\r\n",
            written.bytes.decodeToString(),
        )
    }

    /**
     * RFC 7578 has a sender write application/octet-stream for a file of no known type, since a part that names
     * none is read as text/plain.
     */
    @Test
    fun aFilePartGivenNoMediaTypeIsSentAsApplicationOctetStream() {
        val written = FormBody.write(listOf(MultipartPart.File("file", gpx, filename = "track.gpx")))

        assertTrue(
            written.bytes.decodeToString().contains(
                "Content-Disposition: form-data; name=\"file\"; filename=\"track.gpx\"\r\n" +
                    "Content-Type: application/octet-stream\r\n\r\n",
            ),
        )
    }

    /** A file name a visitor gave cannot close the header or write another. */
    @Test
    fun aQuoteAndALineBreakInAFileNameArePercentEncoded() {
        val written =
            FormBody.write(
                listOf(MultipartPart.File("file", gpx, filename = "the \"long\"\r\nContent-Type: text/html")),
            )

        assertTrue(
            written.bytes.decodeToString().contains(
                "filename=\"the %22long%22%0D%0AContent-Type: text/html\"\r\nContent-Type: application/octet-stream\r\n",
            ),
        )
    }

    @Test
    fun aMediaTypeNoHeaderCarriesIsRefusedBeforeTheBodyIsWritten() {
        for (type in listOf("text/html\r\nX-Evil: 1", "application/gpx+xml\u0000", "texte/é")) {
            val refusal =
                assertFailsWith<ConfigurationException>(type) {
                    FormBody.write(listOf(MultipartPart.File("file", gpx, contentType = type)))
                }
            assertTrue(refusal.message.orEmpty().contains("`file`"), type)
        }
    }

    /**
     * The boundary is lengthened one `x` past the longest run of `x` after the base that a part holds, the one the
     * base lengthened an `x` at a time until no part holds it reaches: a part holding the base at its very end, a
     * run of two and of three, a part holding the base but for its last letter, which holds none.
     */
    @Test
    fun aBoundaryIsLengthenedOnePastTheLongestRunOfXAfterTheBaseAnyPartHolds() {
        val base = FormBody.base

        fun boundary(vararg contents: String): String =
            FormBody
                .write(contents.mapIndexed { index, it -> MultipartPart.File("f$index", it.encodeToByteArray()) })
                .contentType
                .substringAfter("boundary=")

        assertEquals(base + "x", boundary("a $base"))
        assertEquals(base + "xxx", boundary(base + "xx and " + base + "x", "é" + base + "y"))
        assertEquals(base + "xxxx", boundary(base + "x", base + "xxx" + base))
        assertEquals(base, boundary(base.dropLast(1), "xxxx", ""))
        assertEquals(base + "x", boundary(base.dropLast(1) + base))
    }

    /**
     * The writer as it was first written, which appended each piece to the body written so far and lengthened the
     * boundary an `x` at a time while a part held it: the writer answers its body, byte for byte, and its type.
     */
    private fun writtenFirst(parts: List<MultipartPart>): Pair<String, ByteArray> {
        fun content(part: MultipartPart): ByteArray =
            when (part) {
                is MultipartPart.Text -> part.value.encodeToByteArray()
                is MultipartPart.File -> part.bytes
            }

        fun holds(
            bytes: ByteArray,
            marker: ByteArray,
        ): Boolean {
            if (marker.isEmpty() || bytes.size < marker.size) return false
            for (start in 0..bytes.size - marker.size) {
                if ((marker.indices).all { bytes[start + it] == marker[it] }) return true
            }
            return false
        }

        fun escaped(name: String): String = name.replace("\"", "%22").replace("\r", "%0D").replace("\n", "%0A")

        fun disposition(part: MultipartPart): String {
            val field = "form-data; name=\"${escaped(part.name)}\""
            val filename = (part as? MultipartPart.File)?.filename ?: return field
            return "$field; filename=\"${escaped(filename)}\""
        }
        var boundary = FormBody.base
        while (parts.any { holds(content(it), boundary.encodeToByteArray()) }) boundary += "x"
        var bytes = ByteArray(0)
        for (part in parts) {
            bytes += "--$boundary\r\n".encodeToByteArray()
            bytes += "Content-Disposition: ${disposition(part)}\r\n".encodeToByteArray()
            if (part is MultipartPart.File) {
                bytes += "Content-Type: ${part.contentType ?: FormBody.unknownFileType}\r\n".encodeToByteArray()
            }
            bytes += "\r\n".encodeToByteArray()
            bytes += content(part)
            bytes += "\r\n".encodeToByteArray()
        }
        bytes += "--$boundary--\r\n".encodeToByteArray()
        return "multipart/form-data; boundary=$boundary" to bytes
    }

    /** A part's content drawn by [random] from pieces holding the base, runs of `x` after it, and what nearly does. */
    private fun drawnContent(random: Random): String =
        buildString {
            repeat(random.nextInt(0, 6)) {
                when (random.nextInt(7)) {
                    0 -> append(FormBody.base)
                    1 -> append(FormBody.base).append("x".repeat(random.nextInt(0, 5)))
                    2 -> append(FormBody.base.take(random.nextInt(1, FormBody.base.length)))
                    3 -> append("x".repeat(random.nextInt(1, 4)))
                    4 -> append("Mountaya é \r\n\"")
                    5 -> append(FormBody.base.drop(1))
                    else -> repeat(random.nextInt(1, 40)) { append(('!'.code + random.nextInt(94)).toChar()) }
                }
            }
        }

    /**
     * Two thousand bodies drawn at random, each of up to four parts, texts and files, with names that need escaping,
     * file names or none, media types or none, and contents that hold the base and runs of `x` after it.
     */
    @Test
    fun aBodyIsTheOneTheFirstWriterWroteByteForByte() {
        val random = Random(20261123)
        val names = listOf("file", "name", "the \"long\"\r\nway", "é")
        val types = listOf(null, "application/gpx+xml", "text/plain")
        var lengthened = 0
        repeat(2_000) { case ->
            val parts =
                List(random.nextInt(1, 5)) {
                    val name = names[random.nextInt(names.size)]
                    if (random.nextBoolean()) {
                        MultipartPart.Text(name, drawnContent(random))
                    } else {
                        MultipartPart.File(
                            name,
                            drawnContent(random).encodeToByteArray(),
                            filename = names.getOrNull(random.nextInt(names.size + 1)),
                            contentType = types[random.nextInt(types.size)],
                        )
                    }
                }
            val (type, bytes) = writtenFirst(parts)
            val written = FormBody.write(parts)

            assertEquals(type, written.contentType, "case $case: $parts")
            assertContentEquals(bytes, written.bytes, "case $case: $parts")
            if (!type.endsWith(FormBody.base)) lengthened += 1
        }
        assertTrue(lengthened > 500, "the control: $lengthened bodies lengthened their boundary")
    }
}
