package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MultipartPartTest {
    @Test
    fun aTextPartIsItsNameAndValue() {
        assertEquals(MultipartPart.Text("provider", "gpx"), MultipartPart.Text("provider", "gpx"))
        assertNotEquals(MultipartPart.Text("provider", "gpx"), MultipartPart.Text("provider", "kml"))
        assertEquals("Text(provider=gpx)", MultipartPart.Text("provider", "gpx").toString())
    }

    @Test
    fun aFilePartIsComparedByItsBytes() {
        assertEquals(MultipartPart.File("file", byteArrayOf(1, 2)), MultipartPart.File("file", byteArrayOf(1, 2)))
        assertNotEquals(MultipartPart.File("file", byteArrayOf(1, 2)), MultipartPart.File("file", byteArrayOf(2, 1)))
        assertEquals(
            MultipartPart.File("file", byteArrayOf(1, 2)).hashCode(),
            MultipartPart.File("file", byteArrayOf(1, 2)).hashCode(),
        )
        assertEquals("File(file, 2 bytes)", MultipartPart.File("file", byteArrayOf(1, 2)).toString())
    }

    @Test
    fun aFilePartIsComparedByItsFileNameAndItsMediaTypeToo() {
        val named = MultipartPart.File("file", byteArrayOf(1), filename = "a.gpx", contentType = "application/gpx+xml")

        assertEquals(named, MultipartPart.File("file", byteArrayOf(1), "a.gpx", "application/gpx+xml"))
        assertEquals(
            named.hashCode(),
            MultipartPart.File("file", byteArrayOf(1), "a.gpx", "application/gpx+xml").hashCode(),
        )
        assertNotEquals(named, MultipartPart.File("file", byteArrayOf(1), "b.gpx", "application/gpx+xml"))
        assertNotEquals(named, MultipartPart.File("file", byteArrayOf(1), "a.gpx", null))
        assertNotEquals(named, MultipartPart.File("file", byteArrayOf(1)))
        assertEquals("File(file, 1 bytes, filename=a.gpx, contentType=application/gpx+xml)", named.toString())
    }
}
